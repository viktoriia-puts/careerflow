export type TrackedJobStatus =
  | 'SAVED'
  | 'APPLIED'
  | 'INTERVIEW'
  | 'TEST_TASK'
  | 'OFFER'
  | 'REJECTED'
  | 'ARCHIVED';

export interface TrackedJobResponse {
  id: number;
  searchProfileId: number | null;
  company: string;
  positionTitle: string;
  location: string | null;
  source: string | null;
  jobUrl: string | null;
  referenceId: string | null;
  matchScore: number | null;
  status: TrackedJobStatus;
  appliedDate: string | null;
  resultNote: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TrackedJobCreateRequest {
  searchProfileId: number | null;
  company: string;
  positionTitle: string;
  location: string | null;
  source: string | null;
  jobUrl: string | null;
  referenceId: string | null;
  matchScore: number | null;
  status: TrackedJobStatus;
  notes: string | null;
}

export interface TrackedJobUpdateRequest {
  company?: string;
  positionTitle?: string;
  location?: string | null;
  jobUrl?: string | null;
  matchScore?: number | null;
  status?: TrackedJobStatus;
  appliedDate?: string | null;
  resultNote?: string | null;
  notes?: string | null;
}
