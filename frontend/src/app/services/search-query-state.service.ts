import { Injectable } from '@angular/core';
import { CvAnalysisResponse } from '../models/cv-analysis.model';
import { SearchQueryGenerationResponse } from '../models/search-query-generation.model';

@Injectable({ providedIn: 'root' })
export class SearchQueryStateService {
  private cleanedAnalysis: CvAnalysisResponse | null = null;
  private generatedQueries: SearchQueryGenerationResponse | null = null;
  private savedProfileId: number | null = null;

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

  clear() {
    this.cleanedAnalysis = null;
    this.generatedQueries = null;
    this.savedProfileId = null;
  }
}

