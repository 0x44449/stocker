export interface NewsMappingSummary {
  newsId: number;
  title: string;
  extractedNames: string[];
  matchedCount: number;
  totalCount: number;
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
  mappings: CompanyMapping[];
}

export interface Stock {
  stockCode: string;
  nameKr: string;
  nameKrShort: string;
}
