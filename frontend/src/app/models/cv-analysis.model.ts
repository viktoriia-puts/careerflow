export interface CvAnalysisRequest {
  cvText: string;
}

export interface CvAnalysisResponse {
  summary: string;
  searchRoles: string[];
  alternativeCareerRoles: string[];
  keywords: string[];
}
