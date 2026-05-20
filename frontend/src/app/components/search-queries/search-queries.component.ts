import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';
import { SearchQueryGenerationResponse } from '../../models/search-query-generation.model';
import { JobMatchService } from '../../services/job-match.service';
import { JobMatchAnalysisResponse } from '../../models/job-match.model';

@Component({
  selector: 'app-search-queries',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-queries.component.html',
  styleUrls: ['./search-queries.component.css']
})
export class SearchQueriesComponent {
  cleanedAnalysis = signal<CvAnalysisResponse | null>(null);
  generatedQueries = signal<SearchQueryGenerationResponse | null>(null);

  // Temporary input signals for adding new queries
  newRoleTitleQuery = signal('');
  newRequirementBasedQuery = signal('');
  newAlternativeDirectionQuery = signal('');
  // Job match analysis state
  jobDescription = signal('');
  isAnalyzing = signal(false);
  analysisError = signal<string | null>(null);
  analysisResult = signal<JobMatchAnalysisResponse | null>(null);
  savedProfileId = signal<number | null>(null);

  constructor(
    private state: SearchQueryStateService,
    private router: Router,
    private jobMatchService: JobMatchService
  ) {
    this.cleanedAnalysis.set(this.state.getCleanedAnalysis());
    this.generatedQueries.set(this.state.getGeneratedQueries());
    this.savedProfileId.set(this.state.getSavedProfileId());
  }

  // Remove methods
  removeRoleTitleQuery(query: string) {
    const current = this.generatedQueries();
    if (!current) return;
    const updated = {
      ...current,
      roleTitleQueries: current.roleTitleQueries.filter(q => q !== query)
    };
    this.generatedQueries.set(updated);
  }

  removeRequirementBasedQuery(query: string) {
    const current = this.generatedQueries();
    if (!current) return;
    const updated = {
      ...current,
      requirementBasedQueries: current.requirementBasedQueries.filter(q => q !== query)
    };
    this.generatedQueries.set(updated);
  }

  removeAlternativeDirectionQuery(query: string) {
    const current = this.generatedQueries();
    if (!current) return;
    const updated = {
      ...current,
      alternativeDirectionQueries: current.alternativeDirectionQueries.filter(q => q !== query)
    };
    this.generatedQueries.set(updated);
  }

  // Add methods
  addRoleTitleQuery() {
    const value = this.newRoleTitleQuery().trim();
    if (!value) return;

    const current = this.generatedQueries();
    if (!current) return;

    // Avoid duplicates
    if (current.roleTitleQueries.includes(value)) {
      this.newRoleTitleQuery.set('');
      return;
    }

    const updated = {
      ...current,
      roleTitleQueries: [...current.roleTitleQueries, value]
    };
    this.generatedQueries.set(updated);
    this.newRoleTitleQuery.set('');
  }

  addRequirementBasedQuery() {
    const value = this.newRequirementBasedQuery().trim();
    if (!value) return;

    const current = this.generatedQueries();
    if (!current) return;

    // Avoid duplicates
    if (current.requirementBasedQueries.includes(value)) {
      this.newRequirementBasedQuery.set('');
      return;
    }

    const updated = {
      ...current,
      requirementBasedQueries: [...current.requirementBasedQueries, value]
    };
    this.generatedQueries.set(updated);
    this.newRequirementBasedQuery.set('');
  }

  addAlternativeDirectionQuery() {
    const value = this.newAlternativeDirectionQuery().trim();
    if (!value) return;

    const current = this.generatedQueries();
    if (!current) return;

    // Avoid duplicates
    if (current.alternativeDirectionQueries.includes(value)) {
      this.newAlternativeDirectionQuery.set('');
      return;
    }

    const updated = {
      ...current,
      alternativeDirectionQueries: [...current.alternativeDirectionQueries, value]
    };
    this.generatedQueries.set(updated);
    this.newAlternativeDirectionQuery.set('');
  }

  goBack() {
    this.router.navigate(['/cv']);
  }

  onAnalyzeMatch() {
    const profileId = this.savedProfileId();
    const jd = this.jobDescription().trim();

    if (!profileId) {
      this.analysisError.set('No saved profile selected. Please save a search profile first.');
      return;
    }

    if (!jd) return;

    this.isAnalyzing.set(true);
    this.analysisError.set(null);
    this.analysisResult.set(null);

    this.jobMatchService.analyzeJobMatch({ searchProfileId: profileId, jobDescription: jd }).subscribe({
      next: (res) => {
        this.analysisResult.set(res);
        this.isAnalyzing.set(false);
      },
      error: (err) => {
        this.analysisError.set('Failed to analyze job match. Please try again.');
        this.isAnalyzing.set(false);
        console.error('Job match error', err);
      }
    });
  }
}




