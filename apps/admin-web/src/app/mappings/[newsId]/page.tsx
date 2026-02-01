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

interface StockSelection {
  stockCode: string | null; // null = 해당 없음
  feedback: string;
}

interface CompanyCard {
  extractedName: string;
  originalMappingId: number | null;
  selections: StockSelection[];
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
          stockCode: m.matchedStockCode,
          feedback: m.feedback ?? "",
        })),
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
        return {
          ...card,
          selections: card.selections.filter((s) => s.stockCode !== stock.stockCode),
        };
      }
      return {
        ...card,
        selections: [...card.selections, { stockCode: stock.stockCode, feedback: "" }],
      };
    });
  }

  function toggleNoMatch(cardIndex: number) {
    updateCard(cardIndex, (card) => {
      const hasNoMatch = card.selections.some((s) => s.stockCode === null);
      if (hasNoMatch) {
        return {
          ...card,
          selections: card.selections.filter((s) => s.stockCode !== null),
        };
      }
      return {
        ...card,
        selections: [...card.selections, { stockCode: null, feedback: "" }],
      };
    });
  }

  function updateFeedback(cardIndex: number, stockCode: string | null, feedback: string) {
    updateCard(cardIndex, (card) => ({
      ...card,
      selections: card.selections.map((s) =>
        s.stockCode === stockCode ? { ...s, feedback } : s
      ),
    }));
  }

  function handleSave() {
    // Phase 2에서 실제 API 호출로 교체
    const payload = {
      newsId,
      mappings: cards.flatMap((card) =>
        card.selections.map((s) => ({
          extractedName: card.extractedName,
          stockCode: s.stockCode,
          feedback: s.feedback || null,
        }))
      ),
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

      <div className="rounded-lg border p-4 bg-muted/30">
        <p className="text-sm text-muted-foreground">뉴스</p>
        <p className="font-medium">{detail.title}</p>
      </div>

      {cards.map((card, cardIndex) => (
        <CompanyMappingCard
          key={card.extractedName}
          card={card}
          cardIndex={cardIndex}
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
  cardIndex,
  onSearchChange,
  onToggleStock,
  onToggleNoMatch,
  onFeedbackChange,
}: {
  card: CompanyCard;
  cardIndex: number;
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
  const hasNoMatch = selectedCodes.has(null);

  // 이미 선택된 종목 정보
  const selectedStocks = card.selections
    .filter((s) => s.stockCode !== null)
    .map((s) => {
      const stock = mockStocks.find((st) => st.stockCode === s.stockCode);
      return { ...s, stock };
    });

  return (
    <div className="rounded-lg border p-4 space-y-3">
      <div className="flex items-center gap-2">
        <span className="text-sm text-muted-foreground">기업명:</span>
        <span className="font-medium">{card.extractedName}</span>
        {card.selections.length > 0 && (
          <Badge variant="secondary">{card.selections.length}건 선택</Badge>
        )}
      </div>

      <Input
        placeholder="종목 검색 (이름, 코드)"
        value={card.stockSearch}
        onChange={(e) => onSearchChange(e.target.value)}
      />

      {/* 검색 결과 */}
      {searchResults.length > 0 && (
        <div className="border rounded-md divide-y max-h-40 overflow-y-auto">
          {searchResults.map((stock) => (
            <label
              key={stock.stockCode}
              className="flex items-center gap-2 px-3 py-2 hover:bg-muted/50 cursor-pointer"
            >
              <Checkbox
                checked={selectedCodes.has(stock.stockCode)}
                onCheckedChange={() => onToggleStock(stock)}
              />
              <span className="text-sm">
                {stock.nameKrShort} ({stock.stockCode})
              </span>
            </label>
          ))}
        </div>
      )}

      {/* 선택된 종목들 */}
      {selectedStocks.map(({ stockCode, feedback, stock }) => (
        <div key={stockCode} className="ml-2 space-y-1">
          <div className="flex items-center gap-2">
            <Checkbox
              checked={true}
              onCheckedChange={() => {
                if (stock) onToggleStock(stock);
              }}
            />
            <span className="text-sm font-medium">
              {stock?.nameKrShort ?? stockCode} ({stockCode})
            </span>
          </div>
          <Textarea
            placeholder="사유 입력"
            value={feedback}
            onChange={(e) => onFeedbackChange(stockCode, e.target.value)}
            className="ml-6 text-sm"
            rows={1}
          />
        </div>
      ))}

      {/* 해당 없음 */}
      <div className="space-y-1">
        <label className="flex items-center gap-2 cursor-pointer">
          <Checkbox checked={hasNoMatch} onCheckedChange={() => onToggleNoMatch()} />
          <span className="text-sm">해당 없음</span>
        </label>
        {hasNoMatch && (
          <Textarea
            placeholder="사유 (비상장, 외국기업 등)"
            value={card.selections.find((s) => s.stockCode === null)?.feedback ?? ""}
            onChange={(e) => onFeedbackChange(null, e.target.value)}
            className="ml-6 text-sm"
            rows={1}
          />
        )}
      </div>
    </div>
  );
}
