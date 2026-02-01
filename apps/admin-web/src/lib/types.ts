export type MappingStatus = "no_mapping" | "unmapped" | "auto_pending" | "partial" | "done";

export interface NewsMappingSummary {
  newsId: number;
  title: string;
  extractedNames: string[];
  matchedCount: number;
  totalCount: number;
  status: MappingStatus;
}

export interface CompanyMapping {
  id: number | null;
  extractedName: string | null;
  matchedStockCode: string | null;
  matchType: string;
  verified: boolean;
  feedback: string | null;
}

export interface NewsMappingDetail {
  newsId: number;
  title: string;
  content: string;
  publishedAt: string | null;
  collectedAt: string;
  url: string | null;
  mappings: CompanyMapping[];
}

export interface Stock {
  stockCode: string;
  nameKr: string;
  nameKrShort: string;
}
