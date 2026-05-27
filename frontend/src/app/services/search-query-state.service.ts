import { Injectable } from '@angular/core';
import { CvAnalysisResponse } from '../models/cv-analysis.model';
import { SearchQueryGenerationResponse } from '../models/search-query-generation.model';
import { JobMatchAnalysisResponse } from '../models/job-match.model';

@Injectable({ providedIn: 'root' })
export class SearchQueryStateService {
  private readonly selectedProfileIdStorageKey = 'careerflow.selectedProfileId';
  private readonly jobSearchLocationStorageKey = 'careerflow.jobSearchLocation';

  private cvText: string = '';
  private analysisResult: CvAnalysisResponse | null = null;
  private cleanedAnalysis: CvAnalysisResponse | null = null;
  private generatedQueries: SearchQueryGenerationResponse | null = null;
  private savedProfileId: number | null = null;
  private saveSuccess: boolean = false;

  private jobInputs: { id: number; description: string }[] = [{ id: 1, description: '' }];
  private jobMatchResults: (JobMatchAnalysisResponse | null)[] = [];

  private jobSearchLocation: string = 'N\u00fcrnberg';
  private includeRemote: boolean = true;

  clearRankedJobs(): void {
    // Ranked jobs are persisted in Match History and reloaded from the backend.
  }

  setCvText(text: string) {
    this.cvText = text;
  }

  getCvText(): string {
    return this.cvText;
  }

  setAnalysisResult(data: CvAnalysisResponse | null) {
    if (!data) {
      this.analysisResult = null;
      return;
    }

    this.analysisResult = this.copyAnalysis(data);
  }

  getAnalysisResult(): CvAnalysisResponse | null {
    return this.analysisResult ? this.copyAnalysis(this.analysisResult) : null;
  }

  setCleanedAnalysis(data: CvAnalysisResponse) {
    this.cleanedAnalysis = this.copyAnalysis(data);
  }

  getCleanedAnalysis(): CvAnalysisResponse | null {
    return this.cleanedAnalysis ? this.copyAnalysis(this.cleanedAnalysis) : null;
  }

  setGeneratedQueries(data: SearchQueryGenerationResponse) {
    this.generatedQueries = this.copyQueries(data);
  }

  clearGeneratedQueries(): void {
    this.generatedQueries = null;
  }

  getGeneratedQueries(): SearchQueryGenerationResponse | null {
    return this.generatedQueries ? this.copyQueries(this.generatedQueries) : null;
  }

  setSavedProfileId(id: number | null) {
    this.savedProfileId = id;

    if (id === null) {
      this.removeStoredValue(this.selectedProfileIdStorageKey);
      return;
    }

    this.setStoredValue(this.selectedProfileIdStorageKey, id.toString());
  }

  getSavedProfileId(): number | null {
    if (this.savedProfileId !== null) {
      return this.savedProfileId;
    }

    const storedValue = this.getStoredValue(this.selectedProfileIdStorageKey);
    if (!storedValue) {
      return null;
    }

    const parsedValue = Number(storedValue);
    if (!Number.isFinite(parsedValue)) {
      this.removeStoredValue(this.selectedProfileIdStorageKey);
      return null;
    }

    this.savedProfileId = parsedValue;
    return parsedValue;
  }

  setSaveSuccess(success: boolean) {
    this.saveSuccess = success;
  }

  getSaveSuccess(): boolean {
    return this.saveSuccess;
  }

  setJobInputs(inputs: { id: number; description: string }[]) {
    this.jobInputs = inputs.map(jobInput => ({ ...jobInput }));
  }

  getJobInputs(): { id: number; description: string }[] {
    return this.jobInputs.map(jobInput => ({ ...jobInput }));
  }

  setJobMatchResults(results: (JobMatchAnalysisResponse | null)[]) {
    this.jobMatchResults = results.map(result => result ? { ...result } : null);
  }

  getJobMatchResults(): (JobMatchAnalysisResponse | null)[] {
    return this.jobMatchResults.map(result => result ? { ...result } : null);
  }

  setJobSearchLocation(location: string) {
    const normalizedLocation = location?.trim() || 'N\u00fcrnberg';
    this.jobSearchLocation = normalizedLocation;
    this.setStoredValue(this.jobSearchLocationStorageKey, normalizedLocation);
  }

  getJobSearchLocation(): string {
    const storedLocation = this.getStoredValue(this.jobSearchLocationStorageKey);
    if (storedLocation?.trim()) {
      this.jobSearchLocation = storedLocation.trim();
    }

    return this.jobSearchLocation;
  }

  setIncludeRemote(includeRemote: boolean) {
    this.includeRemote = includeRemote;
  }

  getIncludeRemote(): boolean {
    return this.includeRemote;
  }

  clear() {
    this.cvText = '';
    this.analysisResult = null;
    this.cleanedAnalysis = null;
    this.generatedQueries = null;
    this.savedProfileId = null;
    this.saveSuccess = false;
    this.jobInputs = [{ id: 1, description: '' }];
    this.jobMatchResults = [];
    this.jobSearchLocation = 'N\u00fcrnberg';
    this.includeRemote = true;
    this.removeStoredValue(this.selectedProfileIdStorageKey);
    this.removeStoredValue(this.jobSearchLocationStorageKey);
  }

  private copyAnalysis(data: CvAnalysisResponse): CvAnalysisResponse {
    return {
      summary: data.summary,
      searchRoles: [...data.searchRoles],
      alternativeCareerRoles: [...data.alternativeCareerRoles],
      keywords: [...data.keywords]
    };
  }

  private copyQueries(data: SearchQueryGenerationResponse): SearchQueryGenerationResponse {
    return {
      roleTitleQueries: [...data.roleTitleQueries],
      requirementBasedQueries: [...data.requirementBasedQueries],
      alternativeDirectionQueries: [...data.alternativeDirectionQueries]
    };
  }

  private getStoredValue(key: string): string | null {
    try {
      return window.localStorage.getItem(key);
    } catch {
      return null;
    }
  }

  private setStoredValue(key: string, value: string): void {
    try {
      window.localStorage.setItem(key, value);
    } catch {
      // URL params and backend state still keep the app usable.
    }
  }

  private removeStoredValue(key: string): void {
    try {
      window.localStorage.removeItem(key);
    } catch {
      // Ignore storage failures.
    }
  }
}
