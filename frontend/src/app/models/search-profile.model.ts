export interface SearchProfileCreateRequest {
  summary: string;
  searchRoles: string[];
  alternativeCareerRoles: string[];
  keywords: string[];
}

export interface SearchProfileResponse {
  id: number;
  summary: string;
  searchRoles: string[];
  alternativeCareerRoles: string[];
  keywords: string[];
  createdAt: string;
}

