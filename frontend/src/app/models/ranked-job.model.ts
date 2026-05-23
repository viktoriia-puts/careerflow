import { JobMatchAnalysisResponse } from './job-match.model';

export interface JobSearchResult {
  source: string;
  title: string;
  company: string;
  location: string;
  description: string;
  url: string;
  referenceId: string;
  publishedAt: string;
  fullDescriptionAvailable: boolean;
}

export interface RankedJobSearchResult {
  job: JobSearchResult;
  matchAnalysis: JobMatchAnalysisResponse;
}
