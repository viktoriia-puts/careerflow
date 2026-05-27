import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';
import { SearchQueryGenerationResponse } from '../../models/search-query-generation.model';
import { JobMatchService } from '../../services/job-match.service';
import { JobMatchAnalysisResponse } from '../../models/job-match.model';
import { SearchQueryService } from '../../services/search-query.service';
import { SearchProfileService } from '../../services/search-profile.service';
import { SearchProfileResponse } from '../../models/search-profile.model';

@Component({
  selector: 'app-search-queries',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-queries.component.html',
  styleUrls: ['./search-queries.component.css']
})
export class SearchQueriesComponent {
  readonly collapsedListLimit = 5;

  cleanedAnalysis = signal<CvAnalysisResponse | null>(null);
  generatedQueries = signal<SearchQueryGenerationResponse | null>(null);
  showAllSearchRoles = signal(false);
  showAllAlternativeRoles = signal(false);
  showAllKeywords = signal(false);
  showAllRoleTitleQueries = signal(false);
  showAllRequirementBasedQueries = signal(false);
  showAllAlternativeDirectionQueries = signal(false);

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
  querySaveError = signal<string | null>(null);

  constructor(
    private state: SearchQueryStateService,
    private router: Router,
    private route: ActivatedRoute,
    private jobMatchService: JobMatchService,
    private searchQueryService: SearchQueryService,
    private searchProfileService: SearchProfileService
  ) {
    const routeProfileId = this.resolveRouteProfileId();
    const profileId = routeProfileId ?? this.state.getSavedProfileId();

    this.cleanedAnalysis.set(this.state.getCleanedAnalysis());
    this.generatedQueries.set(this.state.getGeneratedQueries());
    this.savedProfileId.set(profileId);

    if (profileId) {
      this.state.setSavedProfileId(profileId);
      this.loadSavedProfile(profileId);
      this.loadSavedGeneratedQueries(profileId);
    }
  }

  // Remove methods
  removeRoleTitleQuery(query: string) {
    const current = this.generatedQueries();
    if (!current) return;
    const updated = {
      ...current,
      roleTitleQueries: current.roleTitleQueries.filter(q => q !== query)
    };
    this.updateGeneratedQueries(updated);
  }

  removeRequirementBasedQuery(query: string) {
    const current = this.generatedQueries();
    if (!current) return;
    const updated = {
      ...current,
      requirementBasedQueries: current.requirementBasedQueries.filter(q => q !== query)
    };
    this.updateGeneratedQueries(updated);
  }

  removeAlternativeDirectionQuery(query: string) {
    const current = this.generatedQueries();
    if (!current) return;
    const updated = {
      ...current,
      alternativeDirectionQueries: current.alternativeDirectionQueries.filter(q => q !== query)
    };
    this.updateGeneratedQueries(updated);
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
    this.updateGeneratedQueries(updated);
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
    this.updateGeneratedQueries(updated);
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
    this.updateGeneratedQueries(updated);
    this.newAlternativeDirectionQuery.set('');
  }

  visibleSearchRoles(): string[] {
    return this.visibleItems(this.cleanedAnalysis()?.searchRoles ?? [], this.showAllSearchRoles());
  }

  visibleAlternativeCareerRoles(): string[] {
    return this.visibleItems(
      this.cleanedAnalysis()?.alternativeCareerRoles ?? [],
      this.showAllAlternativeRoles()
    );
  }

  visibleKeywords(): string[] {
    return this.visibleItems(this.cleanedAnalysis()?.keywords ?? [], this.showAllKeywords());
  }

  visibleRoleTitleQueries(): string[] {
    return this.visibleItems(
      this.generatedQueries()?.roleTitleQueries ?? [],
      this.showAllRoleTitleQueries()
    );
  }

  visibleRequirementBasedQueries(): string[] {
    return this.visibleItems(
      this.generatedQueries()?.requirementBasedQueries ?? [],
      this.showAllRequirementBasedQueries()
    );
  }

  visibleAlternativeDirectionQueries(): string[] {
    return this.visibleItems(
      this.generatedQueries()?.alternativeDirectionQueries ?? [],
      this.showAllAlternativeDirectionQueries()
    );
  }

  hiddenSearchRoleCount(): number {
    return this.hiddenItemCount(this.cleanedAnalysis()?.searchRoles ?? []);
  }

  hiddenAlternativeRoleCount(): number {
    return this.hiddenItemCount(this.cleanedAnalysis()?.alternativeCareerRoles ?? []);
  }

  hiddenKeywordCount(): number {
    return this.hiddenItemCount(this.cleanedAnalysis()?.keywords ?? []);
  }

  hiddenRoleTitleQueryCount(): number {
    return this.hiddenItemCount(this.generatedQueries()?.roleTitleQueries ?? []);
  }

  hiddenRequirementBasedQueryCount(): number {
    return this.hiddenItemCount(this.generatedQueries()?.requirementBasedQueries ?? []);
  }

  hiddenAlternativeDirectionQueryCount(): number {
    return this.hiddenItemCount(this.generatedQueries()?.alternativeDirectionQueries ?? []);
  }

  toggleSearchRoles(): void {
    this.showAllSearchRoles.update(value => !value);
  }

  toggleAlternativeRoles(): void {
    this.showAllAlternativeRoles.update(value => !value);
  }

  toggleKeywords(): void {
    this.showAllKeywords.update(value => !value);
  }

  toggleRoleTitleQueries(): void {
    this.showAllRoleTitleQueries.update(value => !value);
  }

  toggleRequirementBasedQueries(): void {
    this.showAllRequirementBasedQueries.update(value => !value);
  }

  toggleAlternativeDirectionQueries(): void {
    this.showAllAlternativeDirectionQueries.update(value => !value);
  }

  goBack() {
    this.router.navigate(['/cv'], {
      queryParams: this.navigationQueryParams()
    });
  }

  canSearchAndRank(): boolean {
    return Boolean(this.savedProfileId() && this.generatedQueries());
  }

  onSearchAndRank(): void {
    const currentQueries = this.generatedQueries();

    if (!this.savedProfileId() || !currentQueries) {
      return;
    }

    this.state.setGeneratedQueries(currentQueries);
    this.router.navigate(['/job-search-ranking'], {
      queryParams: this.navigationQueryParams()
    });
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

  private visibleItems(items: string[], isExpanded: boolean): string[] {
    if (isExpanded || items.length <= this.collapsedListLimit) {
      return items;
    }

    return items.slice(0, this.collapsedListLimit);
  }

  private hiddenItemCount(items: string[]): number {
    return Math.max(items.length - this.collapsedListLimit, 0);
  }

  private updateGeneratedQueries(updated: SearchQueryGenerationResponse): void {
    const profileId = this.savedProfileId();

    this.generatedQueries.set(updated);
    this.state.setGeneratedQueries(updated);
    this.state.clearRankedJobs();

    if (!profileId) {
      return;
    }

    this.searchQueryService.updateQueries(profileId, updated).subscribe({
      next: (savedQueries) => {
        this.querySaveError.set(null);
        this.generatedQueries.set(savedQueries);
        this.state.setGeneratedQueries(savedQueries);
        this.state.clearRankedJobs();
      },
      error: (error) => {
        this.querySaveError.set('Failed to save query changes.');
        console.error('Generated queries save error:', error);
      }
    });
  }

  private loadSavedGeneratedQueries(profileId: number): void {
    this.searchQueryService.getQueries(profileId).subscribe({
      next: (savedQueries) => {
        this.generatedQueries.set(savedQueries);
        this.state.setGeneratedQueries(savedQueries);
      },
      error: (error) => {
        this.querySaveError.set('Failed to load saved queries.');
        console.error('Saved generated queries load error:', error);
      }
    });
  }

  private loadSavedProfile(profileId: number): void {
    this.searchProfileService.getSearchProfile(profileId).subscribe({
      next: (profile) => {
        const analysis = this.toAnalysis(profile);
        this.cleanedAnalysis.set(analysis);
        this.state.setCleanedAnalysis(analysis);
        this.state.setAnalysisResult(analysis);
      },
      error: (error) => {
        this.querySaveError.set('Failed to load saved profile.');
        console.error('Saved profile load error:', error);
      }
    });
  }

  private toAnalysis(profile: SearchProfileResponse): CvAnalysisResponse {
    return {
      summary: profile.summary,
      searchRoles: [...profile.searchRoles],
      alternativeCareerRoles: [...profile.alternativeCareerRoles],
      keywords: [...profile.keywords]
    };
  }

  private resolveRouteProfileId(): number | null {
    const value = this.route.snapshot.queryParamMap.get('profileId');
    if (!value) {
      return null;
    }

    const parsedValue = Number(value);
    return Number.isFinite(parsedValue) ? parsedValue : null;
  }

  private navigationQueryParams(): Record<string, number> {
    const profileId = this.savedProfileId();
    return profileId ? { profileId } : {};
  }
}




