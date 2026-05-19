export interface JobMatchAnalysisRequest {
  searchProfileId: number;
  jobDescription: string;
}

export interface JobMatchAnalysisResponse {
  matchScore: number;
  recommendation: string;
  summary: string;
  matchingSkills: string[];
  missingSkills: string[];
  concerns: string[];
  suggestedApplicationFocus: string[];
}

