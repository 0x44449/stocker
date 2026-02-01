import { NewsMappingSummary, MappingStatus } from "./types";

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export interface NewsMappingListResponse {
  items: NewsMappingSummary[];
  totalCount: number;
  page: number;
  size: number;
}

interface ApiMappingItem {
  newsId: number;
  title: string;
  extractedNames: string[];
  matchedCount: number;
  totalCount: number;
  status: string;
}

interface ApiResponse {
  items: ApiMappingItem[];
  totalCount: number;
  page: number;
  size: number;
}

/** 뉴스 매핑 목록 조회 */
export async function fetchNewsMappings(params: {
  filter?: string;
  page?: number;
  size?: number;
  search?: string;
}): Promise<NewsMappingListResponse> {
  const query = new URLSearchParams();
  if (params.filter) query.set("filter", params.filter);
  if (params.page !== undefined) query.set("page", String(params.page));
  if (params.size !== undefined) query.set("size", String(params.size));
  if (params.search) query.set("search", params.search);

  const res = await fetch(`${API_BASE}/api/admin/news-mappings?${query}`);
  if (!res.ok) {
    throw new Error(`API error: ${res.status}`);
  }

  const data: ApiResponse = await res.json();
  return {
    items: data.items.map((item) => ({
      ...item,
      status: item.status as MappingStatus,
    })),
    totalCount: data.totalCount,
    page: data.page,
    size: data.size,
  };
}
