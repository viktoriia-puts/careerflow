import { Injectable } from '@angular/core';
import { CvAnalysisResponse } from '../models/cv-analysis.model';

@Injectable({ providedIn: 'root' })
export class SearchQueryStateService {
  private cleanedAnalysis: CvAnalysisResponse | null = null;

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

  clear() {
    this.cleanedAnalysis = null;
  }
}

