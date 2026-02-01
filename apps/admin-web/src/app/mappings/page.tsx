"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { fetchNewsMappings } from "@/lib/api";
import { MappingStatus, NewsMappingSummary } from "@/lib/types";

type Filter = "all" | "unmatched" | "done";

const PAGE_SIZE = 10;

const STATUS_CONFIG: Record<MappingStatus, { color: string; label: string }> = {
  no_mapping: { color: "bg-gray-400", label: "매핑 없음" },
  unmapped: { color: "bg-red-500", label: "미매핑" },
  auto_pending: { color: "bg-yellow-500", label: "자동매칭 대기" },
  partial: { color: "bg-orange-500", label: "부분 완료" },
  done: { color: "bg-blue-500", label: "완료" },
};

export default function MappingsPage() {
  const router = useRouter();
  const [filter, setFilter] = useState<Filter>("all");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [items, setItems] = useState<NewsMappingSummary[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetchNewsMappings({
        filter,
        page,
        size: PAGE_SIZE,
        search: search.trim() || undefined,
      });
      setItems(res.items);
      setTotalCount(res.totalCount);
    } catch (e) {
      console.error("목록 조회 실패:", e);
      setItems([]);
      setTotalCount(0);
    } finally {
      setLoading(false);
    }
  }, [filter, page, search]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const totalPages = Math.max(1, Math.ceil(totalCount / PAGE_SIZE));

  function formatNames(names: string[]) {
    if (names.length <= 1) return names[0] ?? "-";
    return `${names[0]} 외 ${names.length - 1}`;
  }

  function matchStatusBadge(matched: number, total: number) {
    if (matched === total) {
      return <Badge variant="default">{matched}/{total} 완료</Badge>;
    }
    if (matched === 0) {
      return <Badge variant="destructive">0/{total} 미매칭</Badge>;
    }
    return <Badge variant="secondary">{matched}/{total} 매칭</Badge>;
  }

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold">뉴스 매핑</h2>

      <div className="flex gap-2 items-center">
        <div className="flex gap-1">
          {([
            ["all", "전체"],
            ["unmatched", "미매칭있음"],
            ["done", "완료"],
          ] as const).map(([value, label]) => (
            <Button
              key={value}
              variant={filter === value ? "default" : "outline"}
              size="sm"
              onClick={() => { setFilter(value); setPage(0); }}
            >
              {label}
            </Button>
          ))}
        </div>
        <Input
          placeholder="뉴스 제목, 기업명 검색"
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          className="max-w-xs"
        />
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-10">상태</TableHead>
            <TableHead className="w-[50%]">뉴스 제목</TableHead>
            <TableHead>추출 기업명</TableHead>
            <TableHead>매칭상태</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading ? (
            <TableRow>
              <TableCell colSpan={4} className="text-center text-muted-foreground py-8">
                로딩 중...
              </TableCell>
            </TableRow>
          ) : items.length === 0 ? (
            <TableRow>
              <TableCell colSpan={4} className="text-center text-muted-foreground py-8">
                결과가 없습니다.
              </TableCell>
            </TableRow>
          ) : (
            items.map((item) => {
              const statusCfg = STATUS_CONFIG[item.status];
              return (
                <TableRow
                  key={item.newsId}
                  className="cursor-pointer hover:bg-muted/50"
                  onClick={() => router.push(`/mappings/${item.newsId}`)}
                >
                  <TableCell>
                    <span
                      className={`inline-block w-3 h-3 rounded-full ${statusCfg.color}`}
                      title={statusCfg.label}
                    />
                  </TableCell>
                  <TableCell className="font-medium truncate max-w-md">
                    {item.title}
                  </TableCell>
                  <TableCell>{formatNames(item.extractedNames)}</TableCell>
                  <TableCell>
                    {matchStatusBadge(item.matchedCount, item.totalCount)}
                  </TableCell>
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>

      {totalPages > 1 && (() => {
        const GROUP_SIZE = 10;
        const currentGroup = Math.floor(page / GROUP_SIZE);
        const groupStart = currentGroup * GROUP_SIZE;
        const groupEnd = Math.min(groupStart + GROUP_SIZE, totalPages);
        const last = totalPages - 1;

        return (
          <div className="flex justify-center items-center gap-1">
            <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(0)}>
              ◀◀
            </Button>
            <Button variant="outline" size="sm" disabled={currentGroup === 0} onClick={() => setPage(groupStart - GROUP_SIZE)}>
              ◀
            </Button>
            {Array.from({ length: groupEnd - groupStart }, (_, i) => groupStart + i).map((p) => (
              <Button
                key={p}
                variant={page === p ? "default" : "outline"}
                size="sm"
                onClick={() => setPage(p)}
              >
                {p + 1}
              </Button>
            ))}
            <Button variant="outline" size="sm" disabled={groupEnd >= totalPages} onClick={() => setPage(groupEnd)}>
              ▶
            </Button>
            <Button variant="outline" size="sm" disabled={page === last} onClick={() => setPage(last)}>
              ▶▶
            </Button>
          </div>
        );
      })()}
    </div>
  );
}
