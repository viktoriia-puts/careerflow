import { JobSearchResult } from './ranked-job.model';
import { JobMatchAnalysisResponse } from './job-match.model';

export interface MatchHistoryRunSummary {
  id: number;
  searchProfileId: number;
  location: string | null;
  createdAt: string;
  resultCount: number;
  topMatchScore: number | null;
}

export interface MatchHistoryResult {
  id: number;
  positionIndex: number;
  job: JobSearchResult;
  matchAnalysis: JobMatchAnalysisResponse;
}

export interface MatchHistoryRunDetail {
  id: number;
  searchProfileId: number;
  location: string | null;
  createdAt: string;
  results: MatchHistoryResult[];
}
