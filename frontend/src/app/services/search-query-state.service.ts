import { Injectable } from '@angular/core';
import { CvAnalysisResponse } from '../models/cv-analysis.model';
import { SearchQueryGenerationResponse } from '../models/search-query-generation.model';
import { JobMatchAnalysisResponse } from '../models/job-match.model';

@Injectable({ providedIn: 'root' })
export class SearchQueryStateService {
  // CV Analysis page state
  private cvText: string = '';
  private analysisResult: CvAnalysisResponse | null = null;
  private cleanedAnalysis: CvAnalysisResponse | null = null;
  private generatedQueries: SearchQueryGenerationResponse | null = null;
  private savedProfileId: number | null = null;
  private saveSuccess: boolean = false;

  // Job match state
  private jobInputs: { id: number; description: string }[] = [{ id: 1, description: '' }];
  private jobMatchResults: (JobMatchAnalysisResponse | null)[] = [];

  // CV text persistence
  setCvText(text: string) {
    this.cvText = text;
  }

  getCvText(): string {
    return this.cvText;
  }

  // Analysis result persistence
  setAnalysisResult(data: CvAnalysisResponse | null) {
    if (!data) {
      this.analysisResult = null;
      return;
    }
    this.analysisResult = {
      summary: data.summary,
      searchRoles: [...data.searchRoles],
      alternativeCareerRoles: [...data.alternativeCareerRoles],
      keywords: [...data.keywords]
    };
  }

  getAnalysisResult(): CvAnalysisResponse | null {
    if (!this.analysisResult) return null;
    return {
      summary: this.analysisResult.summary,
      searchRoles: [...this.analysisResult.searchRoles],
      alternativeCareerRoles: [...this.analysisResult.alternativeCareerRoles],
      keywords: [...this.analysisResult.keywords]
    };
  }

  setCleanedAnalysis(data: CvAnalysisResponse) {
    // store a copy
    this.cleanedAnalysis = {
      summary: data.summary,
      searchRoles: [...data.searchRoles],
      alternativeCareerRoles: [...data.alternativeCareerRoles],
      keywords: [...data.keywords]
    };
  }

  getCleanedAnalysis(): CvAnalysisResponse | null {
    if (!this.cleanedAnalysis) return null;
    return {
      summary: this.cleanedAnalysis.summary,
      searchRoles: [...this.cleanedAnalysis.searchRoles],
      alternativeCareerRoles: [...this.cleanedAnalysis.alternativeCareerRoles],
      keywords: [...this.cleanedAnalysis.keywords]
    };
  }

  setGeneratedQueries(data: SearchQueryGenerationResponse) {
    // store a copy
    this.generatedQueries = {
      roleTitleQueries: [...data.roleTitleQueries],
      requirementBasedQueries: [...data.requirementBasedQueries],
      alternativeDirectionQueries: [...data.alternativeDirectionQueries]
    };
  }

  setSavedProfileId(id: number | null) {
    this.savedProfileId = id;
  }

  getSavedProfileId(): number | null {
    return this.savedProfileId;
  }

  getGeneratedQueries(): SearchQueryGenerationResponse | null {
    if (!this.generatedQueries) return null;
    return {
      roleTitleQueries: [...this.generatedQueries.roleTitleQueries],
      requirementBasedQueries: [...this.generatedQueries.requirementBasedQueries],
      alternativeDirectionQueries: [...this.generatedQueries.alternativeDirectionQueries]
    };
  }

  // Save success state
  setSaveSuccess(success: boolean) {
    this.saveSuccess = success;
  }

  getSaveSuccess(): boolean {
    return this.saveSuccess;
  }

  // Job match state
  setJobInputs(inputs: { id: number; description: string }[]) {
    this.jobInputs = inputs.map(j => ({ ...j }));
  }

  getJobInputs(): { id: number; description: string }[] {
    return this.jobInputs.map(j => ({ ...j }));
  }

  setJobMatchResults(results: (JobMatchAnalysisResponse | null)[]) {
    this.jobMatchResults = results.map(r => r ? { ...r } : null);
  }

  getJobMatchResults(): (JobMatchAnalysisResponse | null)[] {
    return this.jobMatchResults.map(r => r ? { ...r } : null);
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
  }
}

