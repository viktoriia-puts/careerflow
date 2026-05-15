/**
 * Request model for CV analysis
 */
export interface CvAnalysisRequest {
  cvText: string;
}

/**
 * Response model for CV analysis
 */
export interface CvAnalysisResponse {
  summary: string;
  suggestedRoles: string[];
  keywords: string[];
}

