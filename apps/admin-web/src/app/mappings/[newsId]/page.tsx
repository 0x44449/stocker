"use client";

import { useState, useMemo } from "react";
import { useParams, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { mockMappingDetails, mockStocks } from "@/lib/mock-data";
import { Stock } from "@/lib/types";

function formatDate(value: string): string {
  const d = new Date(value);
  return `${d.getFullYear()}년 ${String(d.getMonth() + 1).padStart(2, "0")}월 ${String(d.getDate()).padStart(2, "0")}일`;
}

function formatDatetime(value: string): string {
  const d = new Date(value);
  return `${d.getFullYear()}년 ${String(d.getMonth() + 1).padStart(2, "0")}월 ${String(d.getDate()).padStart(2, "0")}일 ${String(d.getHours()).padStart(2, "0")}시 ${String(d.getMinutes()).padStart(2, "0")}분`;
}

interface StockSelection {
  stockCode: string;
  checked: boolean;
  feedback: string;
}

interface NoMatchState {
  active: boolean;
  feedback: string;
}

interface CompanyCard {
  extractedName: string;
  originalMappingId: number | null;
  selections: StockSelection[];
  noMatch: NoMatchState;
  // 해당없음 전환 시 이전 종목 선택 상태 백업
  savedSelections: StockSelection[] | null;
  stockSearch: string;
}

export default function MappingDetailPage() {
  const params = useParams();
  const router = useRouter();
  const newsId = Number(params.newsId);
  const detail = mockMappingDetails[newsId];

  const [cards, setCards] = useState<CompanyCard[]>(() => {
    if (!detail) return [];

    // 기업명별로 그룹핑
    const grouped = new Map<string, typeof detail.mappings>();
    for (const m of detail.mappings) {
      const name = m.extractedName ?? "";
      if (!grouped.has(name)) grouped.set(name, []);
      grouped.get(name)!.push(m);
    }

    return Array.from(grouped.entries()).map(([name, mappings]) => ({
      extractedName: name,
      originalMappingId: mappings[0].id,
      selections: mappings
        .filter((m) => m.matchedStockCode !== null)
        .map((m) => ({
          stockCode: m.matchedStockCode!,
          checked: true,
          feedback: m.feedback ?? "",
        })),
      noMatch: { active: false, feedback: "" },
      savedSelections: null,
      stockSearch: "",
    }));
  });

  if (!detail) {
    return (
      <div className="space-y-4">
        <p className="text-muted-foreground">뉴스를 찾을 수 없습니다.</p>
        <Button variant="outline" onClick={() => router.push("/mappings")}>
          목록으로
        </Button>
      </div>
    );
  }

  function updateCard(index: number, updater: (card: CompanyCard) => CompanyCard) {
    setCards((prev) => prev.map((c, i) => (i === index ? updater(c) : c)));
  }

  function toggleStock(cardIndex: number, stock: Stock) {
    updateCard(cardIndex, (card) => {
      const existing = card.selections.find((s) => s.stockCode === stock.stockCode);
      if (existing) {
        // 체크 토글 (항목은 유지)
        return {
          ...card,
          selections: card.selections.map((s) =>
            s.stockCode === stock.stockCode ? { ...s, checked: !s.checked } : s
          ),
        };
      }
      // 신규 추가
      return {
        ...card,
        selections: [...card.selections, { stockCode: stock.stockCode, checked: true, feedback: "" }],
      };
    });
  }

  function toggleNoMatch(cardIndex: number) {
    updateCard(cardIndex, (card) => {
      if (card.noMatch.active) {
        // 해당없음 해제 → 이전 상태 복원
        return {
          ...card,
          noMatch: { active: false, feedback: card.noMatch.feedback },
          selections: card.savedSelections ?? card.selections,
          savedSelections: null,
        };
      }
      // 해당없음 활성화 → 현재 종목 선택 백업 후 모두 해제
      return {
        ...card,
        noMatch: { active: true, feedback: card.noMatch.feedback },
        savedSelections: card.selections,
        selections: card.selections.map((s) => ({ ...s, checked: false })),
      };
    });
  }

  function updateFeedback(cardIndex: number, stockCode: string | null, feedback: string) {
    updateCard(cardIndex, (card) => {
      if (stockCode === null) {
        return { ...card, noMatch: { ...card.noMatch, feedback } };
      }
      return {
        ...card,
        selections: card.selections.map((s) =>
          s.stockCode === stockCode ? { ...s, feedback } : s
        ),
      };
    });
  }

  function handleSave() {
    // Phase 2에서 실제 API 호출로 교체
    const payload = {
      newsId,
      mappings: cards.flatMap((card) => {
        const items: { extractedName: string; stockCode: string | null; feedback: string | null }[] = [];

        for (const s of card.selections) {
          if (s.checked) {
            items.push({
              extractedName: card.extractedName,
              stockCode: s.stockCode,
              feedback: s.feedback || null,
            });
          }
        }

        if (card.noMatch.active) {
          items.push({
            extractedName: card.extractedName,
            stockCode: null,
            feedback: card.noMatch.feedback || null,
          });
        }

        return items;
      }),
    };
    console.log("저장 데이터:", payload);
    alert("저장되었습니다. (mock)");
    router.push("/mappings");
  }

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">매핑 상세</h2>
        <Button variant="outline" size="sm" onClick={() => router.push("/mappings")}>
          목록으로
        </Button>
      </div>

      {/* 뉴스 정보 */}
      <div className="rounded-lg border p-4 bg-muted/30 space-y-2">
        <p className="font-medium text-base">{detail.title}</p>
        <p className="text-xs text-muted-foreground whitespace-pre-wrap">{detail.content}</p>
        <div className="flex flex-col gap-1 text-[11px] text-muted-foreground pt-1">
          {detail.publishedAt && <span>발행일: {formatDate(detail.publishedAt)}</span>}
          <span>수집일: {formatDatetime(detail.collectedAt)}</span>
          {detail.url && (
            <a
              href={detail.url}
              target="_blank"
              rel="noopener noreferrer"
              className="text-blue-600 hover:underline"
            >
              원문 보기
            </a>
          )}
        </div>
      </div>

      {cards.map((card, cardIndex) => (
        <CompanyMappingCard
          key={card.extractedName}
          card={card}
          onSearchChange={(value) =>
            updateCard(cardIndex, (c) => ({ ...c, stockSearch: value }))
          }
          onToggleStock={(stock) => toggleStock(cardIndex, stock)}
          onToggleNoMatch={() => toggleNoMatch(cardIndex)}
          onFeedbackChange={(stockCode, feedback) =>
            updateFeedback(cardIndex, stockCode, feedback)
          }
        />
      ))}

      <div className="flex justify-end gap-2">
        <Button variant="outline" onClick={() => router.push("/mappings")}>
          취소
        </Button>
        <Button onClick={handleSave}>저장</Button>
      </div>
    </div>
  );
}

function CompanyMappingCard({
  card,
  onSearchChange,
  onToggleStock,
  onToggleNoMatch,
  onFeedbackChange,
}: {
  card: CompanyCard;
  onSearchChange: (value: string) => void;
  onToggleStock: (stock: Stock) => void;
  onToggleNoMatch: () => void;
  onFeedbackChange: (stockCode: string | null, feedback: string) => void;
}) {
  const searchResults = useMemo(() => {
    if (!card.stockSearch.trim()) return [];
    const q = card.stockSearch.trim().toLowerCase();
    return mockStocks.filter(
      (s) =>
        s.nameKr.toLowerCase().includes(q) ||
        s.nameKrShort.toLowerCase().includes(q) ||
        s.stockCode.includes(q)
    );
  }, [card.stockSearch]);

  const selectedCodes = new Set(card.selections.map((s) => s.stockCode));
  const checkedCount = card.selections.filter((s) => s.checked).length + (card.noMatch.active ? 1 : 0);

  return (
    <div className="rounded-lg border p-4 space-y-3">
      <div className="flex items-center gap-2">
        <span className="text-sm text-muted-foreground">기업명:</span>
        <span className="font-medium">{card.extractedName}</span>
        {checkedCount > 0 && (
          <Badge variant="secondary">{checkedCount}건 선택</Badge>
        )}
      </div>

      {/* 종목 검색 (해당없음 활성화 시 비활성) */}
      <Input
        placeholder="종목 검색 (이름, 코드)"
        value={card.stockSearch}
        onChange={(e) => onSearchChange(e.target.value)}
        disabled={card.noMatch.active}
      />

      {/* 검색 결과 */}
      {!card.noMatch.active && searchResults.length > 0 && (
        <div className="border rounded-md divide-y max-h-40 overflow-y-auto">
          {searchResults.map((stock) => (
            <label
              key={stock.stockCode}
              className="flex items-center gap-2 px-3 py-2 hover:bg-muted/50 cursor-pointer"
            >
              <Checkbox
                checked={selectedCodes.has(stock.stockCode) && card.selections.find((s) => s.stockCode === stock.stockCode)?.checked}
                onCheckedChange={() => onToggleStock(stock)}
              />
              <span className="text-sm">
                {stock.nameKrShort} ({stock.stockCode})
              </span>
            </label>
          ))}
        </div>
      )}

      {/* 선택된 종목들 (체크 해제해도 유지) */}
      {card.selections.map((sel) => {
        const stock = mockStocks.find((st) => st.stockCode === sel.stockCode);
        return (
          <div key={sel.stockCode} className="space-y-1">
            <label className="flex items-center gap-2 cursor-pointer">
              <Checkbox
                checked={sel.checked}
                disabled={card.noMatch.active}
                onCheckedChange={() => {
                  if (stock) onToggleStock(stock);
                }}
              />
              <span className={`text-sm ${sel.checked ? "font-medium" : "text-muted-foreground"}`}>
                {stock?.nameKrShort ?? sel.stockCode} ({sel.stockCode})
              </span>
            </label>
            <div className="pl-6">
              <Textarea
                placeholder="사유 입력"
                value={sel.feedback}
                onChange={(e) => onFeedbackChange(sel.stockCode, e.target.value)}
                className="text-sm"
                rows={1}
              />
            </div>
          </div>
        );
      })}

      {/* 구분선 */}
      {card.selections.length > 0 && <hr className="border-border" />}

      {/* 해당 없음 */}
      <div className="space-y-1">
        <label className="flex items-center gap-2 cursor-pointer">
          <Checkbox
            checked={card.noMatch.active}
            onCheckedChange={() => onToggleNoMatch()}
          />
          <span className={`text-sm ${card.noMatch.active ? "font-medium" : ""}`}>
            해당 없음
          </span>
        </label>
        {card.noMatch.active && (
          <div className="pl-6">
            <Textarea
              placeholder="사유 (비상장, 외국기업 등)"
              value={card.noMatch.feedback}
              onChange={(e) => onFeedbackChange(null, e.target.value)}
              className="text-sm"
              rows={1}
            />
          </div>
        )}
      </div>
    </div>
  );
}
