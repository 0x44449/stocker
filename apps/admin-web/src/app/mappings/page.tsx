"use client";

import { useState, useMemo } from "react";
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
import { mockMappingList } from "@/lib/mock-data";
import { MappingStatus } from "@/lib/types";

type Filter = "all" | "unmatched" | "done";

const PAGE_SIZE = 10;

const STATUS_CONFIG: Record<MappingStatus, { color: string; label: string }> = {
  unmapped: { color: "bg-red-500", label: "미매핑" },
  auto_pending: { color: "bg-yellow-500", label: "자동매칭 대기" },
  partial: { color: "bg-orange-500", label: "부분 완료" },
  done: { color: "bg-blue-500", label: "완료" },
};

export default function MappingsPage() {
  const router = useRouter();
  const [filter, setFilter] = useState<Filter>("all");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);

  const filtered = useMemo(() => {
    let items = mockMappingList;

    if (filter === "unmatched") {
      items = items.filter((item) => item.matchedCount < item.totalCount);
    } else if (filter === "done") {
      items = items.filter((item) => item.matchedCount === item.totalCount);
    }

    if (search.trim()) {
      const q = search.trim().toLowerCase();
      items = items.filter(
        (item) =>
          item.title.toLowerCase().includes(q) ||
          item.extractedNames.some((name) => name.toLowerCase().includes(q))
      );
    }

    return items;
  }, [filter, search]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

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
              onClick={() => { setFilter(value); setPage(1); }}
            >
              {label}
            </Button>
          ))}
        </div>
        <Input
          placeholder="뉴스 제목, 기업명 검색"
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(1); }}
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
          {paginated.map((item) => {
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
          })}
          {paginated.length === 0 && (
            <TableRow>
              <TableCell colSpan={4} className="text-center text-muted-foreground py-8">
                결과가 없습니다.
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {totalPages > 1 && (
        <div className="flex justify-center gap-1">
          {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
            <Button
              key={p}
              variant={page === p ? "default" : "outline"}
              size="sm"
              onClick={() => setPage(p)}
            >
              {p}
            </Button>
          ))}
        </div>
      )}
    </div>
  );
}
