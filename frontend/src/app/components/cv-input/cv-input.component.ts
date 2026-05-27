import { Component, ElementRef, HostListener, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CvAnalysisService } from '../../services/cv-analysis.service';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { SearchProfileService } from '../../services/search-profile.service';
import { SearchQueryService } from '../../services/search-query.service';
import { SearchProfileCreateRequest, SearchProfileResponse } from '../../models/search-profile.model';
import { JobMatchService } from '../../services/job-match.service';
import { JobMatchAnalysisResponse } from '../../models/job-match.model';

@Component({
  selector: 'app-cv-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cv-input.component.html',
  styleUrl: './cv-input.component.css'
})
export class CvInputComponent {
  readonly collapsedListLimit = 5;

  cvText = signal('');
  isLoading = signal(false);
  error = signal<string | null>(null);
  analysisResult = signal<CvAnalysisResponse | null>(null);
  showAllSearchRoles = signal(false);
  showAllAlternativeRoles = signal(false);
  showAllKeywords = signal(false);
  // temporary inputs for adding new items
  newSearchRole = signal('');
  newAlternativeRole = signal('');
  newKeyword = signal('');
  // Search profile saving state
  isSaving = signal(false);
  saveSuccess = signal(false);
  saveError = signal<string | null>(null);
  savedProfileId = signal<number | null>(null);
  savedProfiles = signal<SearchProfileResponse[]>([]);
  selectedProfileId = signal<number | null>(null);
  isLoadingProfiles = signal(false);
  profileLoadError = signal<string | null>(null);
  isProfileDropdownOpen = signal(false);
  isDeletingProfile = signal(false);
  deleteProfileError = signal<string | null>(null);
  profilePendingDeletion = signal<SearchProfileResponse | null>(null);
  // Generate queries state
  isGenerating = signal(false);
  generateError = signal<string | null>(null);

  // Job match analysis state (support multiple job descriptions)
  jobInputs = signal<{ id: number; description: string }[]>([{ id: 1, description: '' }]);
  // results aligned with jobInputs indexes: same length, null when no result yet
  jobMatchResults = signal<(JobMatchAnalysisResponse | null)[]>([]);
  isAnalyzing = signal(false);
  jobMatchError = signal<string | null>(null);

  constructor(
    private cvAnalysisService: CvAnalysisService,
    private state: SearchQueryStateService,
    private searchProfileService: SearchProfileService,
    private searchQueryService: SearchQueryService,
    private jobMatchService: JobMatchService,
    private router: Router,
    private route: ActivatedRoute,
    private elementRef: ElementRef<HTMLElement>
  ) { }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;

    if (!target || !this.elementRef.nativeElement.contains(target)) {
      this.isProfileDropdownOpen.set(false);
      return;
    }

    if (!target.closest('.profile-picker')) {
      this.isProfileDropdownOpen.set(false);
    }
  }

  ngOnInit(): void {
    // Load persisted state from state service
    const savedCvText = this.state.getCvText();
    if (savedCvText) {
      this.cvText.set(savedCvText);
    }

    const savedAnalysis = this.state.getAnalysisResult();
    if (savedAnalysis) {
      this.analysisResult.set(savedAnalysis);
    }

    const routeProfileId = this.resolveRouteProfileId();
    const savedProfileId = routeProfileId ?? this.state.getSavedProfileId();
    if (savedProfileId) {
      this.savedProfileId.set(savedProfileId);
      this.selectedProfileId.set(savedProfileId);
      this.state.setSavedProfileId(savedProfileId);
      // restore save success state briefly
      if (this.state.getSaveSuccess()) {
        this.saveSuccess.set(true);
        setTimeout(() => {
          this.saveSuccess.set(false);
        }, 3000);
      }
    }

    const savedJobInputs = this.state.getJobInputs();
    if (savedJobInputs && savedJobInputs.length > 0) {
      this.jobInputs.set(savedJobInputs);
    }

    const savedJobResults = this.state.getJobMatchResults();
    if (savedJobResults && savedJobResults.length > 0) {
      this.jobMatchResults.set(savedJobResults);
    }

    // ensure results array matches current inputs
    this.syncResultsLength();
    this.loadSavedProfiles();
  }

  get characterCount(): number {
    return this.cvText().length;
  }

  get isButtonDisabled(): boolean {
    return this.cvText().trim().length === 0 || this.isLoading();
  }

  onAnalyzeCv(): void {
    const text = this.cvText();

    if (!text.trim()) {
      this.error.set('Please enter CV text before analyzing');
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);
    this.analysisResult.set(null);

    this.cvAnalysisService.analyzeCv(text).subscribe({
      next: (response) => {
        this.analysisResult.set(response);
        this.savedProfileId.set(null);
        this.selectedProfileId.set(null);
        this.resetExpandedLists();
        this.isLoading.set(false);
        // persist to state service
        this.state.setCvText(text);
        this.state.setAnalysisResult(response);
        this.state.setSavedProfileId(null);
        this.state.clearGeneratedQueries();
        this.state.clearRankedJobs();
        console.log('CV Analysis successful:', response);
      },
      error: (error) => {
        this.error.set('Failed to analyze CV. Please try again.');
        this.isLoading.set(false);
        console.error('CV Analysis error:', error);
      }
    });
  }

  onClear(): void {
    this.cvText.set('');
    this.analysisResult.set(null);
    this.resetExpandedLists();
    this.error.set(null);
    // Also clear state service
    this.state.clear();
    this.savedProfileId.set(null);
    this.selectedProfileId.set(null);
    this.saveSuccess.set(false);
    this.saveError.set(null);
    this.jobInputs.set([{ id: 1, description: '' }]);
    this.jobMatchResults.set([]);
  }

  // Remove methods (do not mutate original object)
  removeSearchRole(role: string) {
    const current = this.analysisResult();
    if (!current) return;
    const updated = { ...current, searchRoles: current.searchRoles.filter(r => r !== role) };
    this.analysisResult.set(updated);
  }

  removeAlternativeCareerRole(role: string) {
    const current = this.analysisResult();
    if (!current) return;
    const updated = { ...current, alternativeCareerRoles: current.alternativeCareerRoles.filter(r => r !== role) };
    this.analysisResult.set(updated);
  }

  removeKeyword(keyword: string) {
    const current = this.analysisResult();
    if (!current) return;
    const updated = { ...current, keywords: current.keywords.filter(k => k !== keyword) };
    this.analysisResult.set(updated);
  }

  // Add methods
  addSearchRole() {
    const value = this.newSearchRole().trim();
    if (!value) return;
    const current = this.analysisResult();
    if (!current) return;
    if (current.searchRoles.includes(value)) { this.newSearchRole.set(''); return; }
    const updated = { ...current, searchRoles: [...current.searchRoles, value] };
    this.analysisResult.set(updated);
    this.newSearchRole.set('');
  }

  addAlternativeCareerRole() {
    const value = this.newAlternativeRole().trim();
    if (!value) return;
    const current = this.analysisResult();
    if (!current) return;
    if (current.alternativeCareerRoles.includes(value)) { this.newAlternativeRole.set(''); return; }
    const updated = { ...current, alternativeCareerRoles: [...current.alternativeCareerRoles, value] };
    this.analysisResult.set(updated);
    this.newAlternativeRole.set('');
  }

  addKeyword() {
    const value = this.newKeyword().trim();
    if (!value) return;
    const current = this.analysisResult();
    if (!current) return;
    if (current.keywords.includes(value)) { this.newKeyword.set(''); return; }
    const updated = { ...current, keywords: [...current.keywords, value] };
    this.analysisResult.set(updated);
    this.newKeyword.set('');
  }

  visibleSearchRoles(): string[] {
    return this.visibleItems(this.analysisResult()?.searchRoles ?? [], this.showAllSearchRoles());
  }

  visibleAlternativeCareerRoles(): string[] {
    return this.visibleItems(
      this.analysisResult()?.alternativeCareerRoles ?? [],
      this.showAllAlternativeRoles()
    );
  }

  visibleKeywords(): string[] {
    return this.visibleItems(this.analysisResult()?.keywords ?? [], this.showAllKeywords());
  }

  hiddenSearchRoleCount(): number {
    return this.hiddenItemCount(this.analysisResult()?.searchRoles ?? []);
  }

  hiddenAlternativeRoleCount(): number {
    return this.hiddenItemCount(this.analysisResult()?.alternativeCareerRoles ?? []);
  }

  hiddenKeywordCount(): number {
    return this.hiddenItemCount(this.analysisResult()?.keywords ?? []);
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

  // Save search profile to backend
  onSaveSearchProfile() {
    if (!this.canSaveSearchProfile()) {
      return;
    }

    const current = this.analysisResult();
    if (!current) return;

    this.isSaving.set(true);
    this.saveError.set(null);
    this.saveSuccess.set(false);

    const request: SearchProfileCreateRequest = {
      summary: current.summary,
      searchRoles: current.searchRoles,
      alternativeCareerRoles: current.alternativeCareerRoles,
      keywords: current.keywords
    };

    this.searchProfileService.createSearchProfile(request).subscribe({
      next: (response) => {
        this.isSaving.set(false);
        this.saveSuccess.set(true);
        this.savedProfileId.set(response.id);
        this.selectedProfileId.set(response.id);
        this.addOrReplaceSavedProfile(response);
        // store saved profile id in shared state so other pages can use it
        this.state.setSavedProfileId(response.id);
        this.state.setSaveSuccess(true);
        this.state.clearGeneratedQueries();
        this.state.clearRankedJobs();
        // Also persist current state
        this.state.setCvText(this.cvText());
        this.state.setAnalysisResult(current);
        console.log('Search profile saved successfully');
        // Clear after 3 seconds
        setTimeout(() => {
          this.saveSuccess.set(false);
          this.state.setSaveSuccess(false);
        }, 3000);
      },
      error: (error) => {
        this.isSaving.set(false);
        this.saveError.set('Failed to save search profile. Please try again.');
        console.error('Search profile save error:', error);
      }
    });
  }

  onSelectSavedProfile(profileId: number | null) {
    this.deleteProfileError.set(null);
    this.profilePendingDeletion.set(null);
    this.isProfileDropdownOpen.set(false);

    if (!profileId) {
      this.selectedProfileId.set(null);
      return;
    }

    const profile = this.savedProfiles()
      .find(savedProfile => savedProfile.id === profileId);

    if (!profile) {
      return;
    }

    this.applySavedProfile(profile);
  }

  toggleProfileDropdown(event?: MouseEvent): void {
    event?.stopPropagation();

    if (this.isLoadingProfiles() || this.savedProfiles().length === 0) {
      return;
    }

    this.deleteProfileError.set(null);
    this.isProfileDropdownOpen.update(isOpen => !isOpen);
  }

  openDeleteProfileDialog(profile: SearchProfileResponse, event: MouseEvent): void {
    event.stopPropagation();

    if (this.isDeletingProfile()) {
      return;
    }

    this.deleteProfileError.set(null);
    this.profilePendingDeletion.set(profile);
    this.isProfileDropdownOpen.set(false);
  }

  cancelDeleteProfile(): void {
    if (this.isDeletingProfile()) {
      return;
    }

    this.profilePendingDeletion.set(null);
    this.deleteProfileError.set(null);
  }

  confirmDeleteProfile(): void {
    const profile = this.profilePendingDeletion();

    if (!profile || this.isDeletingProfile()) {
      return;
    }

    const profileId = profile.id;

    this.isDeletingProfile.set(true);
    this.deleteProfileError.set(null);

    this.searchProfileService.deleteSearchProfile(profileId).subscribe({
      next: () => {
        this.isDeletingProfile.set(false);
        this.profilePendingDeletion.set(null);
        this.savedProfiles.set(
          this.savedProfiles().filter(profile => profile.id !== profileId)
        );

        if (this.savedProfileId() === profileId) {
          this.savedProfileId.set(null);
          this.selectedProfileId.set(null);
          this.analysisResult.set(null);
          this.resetExpandedLists();
          this.state.setSavedProfileId(null);
          this.state.setAnalysisResult(null);
          this.state.clearGeneratedQueries();
          this.state.clearRankedJobs();
        }
      },
      error: (error) => {
        this.isDeletingProfile.set(false);
        this.deleteProfileError.set('Failed to delete search profile.');
        console.error('Search profile delete error:', error);
      }
    });
  }

  selectedProfileLabel(): string {
    if (this.isLoadingProfiles()) {
      return 'Loading saved profiles...';
    }

    const selectedProfile = this.savedProfiles()
      .find(profile => profile.id === this.selectedProfileId());

    if (selectedProfile) {
      return this.savedProfileLabel(selectedProfile);
    }

    return this.savedProfiles().length === 0
      ? 'No saved profiles yet'
      : 'Select saved profile';
  }

  savedProfileLabel(profile: SearchProfileResponse): string {
    const createdAt = profile.createdAt
      ? new Date(profile.createdAt).toLocaleDateString()
      : 'Saved profile';
    const summary = profile.summary.length > 90
      ? `${profile.summary.slice(0, 90)}...`
      : profile.summary;

    return `#${profile.id} - ${createdAt} - ${summary}`;
  }

  // Save cleaned analysis and generate queries
  onGenerateSearchQueries() {
    this.loadQueriesOrGenerateAndNavigate('/search-queries', '/search-queries');
  }

  private generateQueriesAndNavigate(route: string) {
    const profileId = this.savedProfileId();

    if (!profileId) {
      this.generateError.set('Search profile not saved. Please save first.');
      return;
    }

    if (this.hasUnsavedProfileChanges()) {
      this.generateError.set('Save profile changes before generating search queries.');
      return;
    }

    const current = this.analysisResult();
    if (!current) return;

    // Save cleaned analysis to state
    const copy: CvAnalysisResponse = {
      summary: current.summary,
      searchRoles: [...current.searchRoles],
      alternativeCareerRoles: [...current.alternativeCareerRoles],
      keywords: [...current.keywords]
    };
    this.state.setCleanedAnalysis(copy);
    // Also persist full CV state before navigation
    this.state.setCvText(this.cvText());
    this.state.setAnalysisResult(current);
    this.state.setJobInputs(this.jobInputs());
    this.state.setJobMatchResults(this.jobMatchResults());

    // Generate queries from backend
    this.isGenerating.set(true);
    this.generateError.set(null);

    this.searchQueryService.generateQueries(profileId).subscribe({
      next: (response) => {
        this.isGenerating.set(false);
        // Store generated queries in state
        this.state.setGeneratedQueries(response);
        this.state.clearRankedJobs();
        console.log('Search queries generated successfully:', response);
        this.router.navigate([route], {
          queryParams: this.navigationQueryParams()
        });
      },
      error: (error) => {
        this.isGenerating.set(false);
        this.generateError.set('Failed to generate search queries. Please try again.');
        console.error('Generate queries error:', error);
      }
    });
  }

  // Update single job description by index
  updateJobDescription(index: number, value: string) {
    const copy = this.jobInputs().map(j => ({ ...j }));
    if (index < 0 || index >= copy.length) return;
    copy[index].description = value;
    this.jobInputs.set(copy);
    this.state.setJobInputs(copy);
    this.syncResultsLength();
  }

  addJobInput() {
    const current = this.jobInputs();
    const nextId = current.length > 0 ? Math.max(...current.map(j => j.id)) + 1 : 1;
    const updated = [...current, { id: nextId, description: '' }];
    this.jobInputs.set(updated);
    this.state.setJobInputs(updated);
    this.syncResultsLength();
  }

  canAnalyzeMatches(): boolean {
    return Boolean(this.savedProfileId()) && !this.isAnalyzing() && this.jobInputs().some(j => j.description.trim().length > 0);
  }

  removeJobInput(index: number) {
    const current = this.jobInputs();
    if (current.length <= 1) return; // keep at least one
    const updated = current.filter((_, i) => i !== index);
    this.jobInputs.set(updated);
    this.state.setJobInputs(updated);
    this.syncResultsLength();
  }

  private syncResultsLength() {
    const inputs = this.jobInputs();
    const results = this.jobMatchResults().slice();
    while (results.length < inputs.length) results.push(null);
    if (results.length > inputs.length) results.splice(inputs.length);
    this.jobMatchResults.set(results);
  }

  // Analyze multiple job descriptions (one request per non-empty description)
  onAnalyzeMatches() {
    const profileId = this.savedProfileId();
    if (!profileId) {
      this.jobMatchError.set('Please save a search profile first before analyzing jobs.');
      return;
    }

    const inputs = this.jobInputs();
    const nonEmpty = inputs
      .map((j, idx) => ({ idx, desc: j.description.trim() }))
      .filter(x => x.desc.length > 0);

    if (nonEmpty.length === 0) {
      this.jobMatchError.set('Please enter at least one job description.');
      return;
    }

    this.jobMatchError.set(null);
    this.isAnalyzing.set(true);

    this.syncResultsLength();

    let remaining = nonEmpty.length;

    nonEmpty.forEach(item => {
      this.jobMatchService.analyzeJobMatch({ searchProfileId: profileId, jobDescription: item.desc }).subscribe({
        next: (res) => {
          const resultsCopy = this.jobMatchResults().slice();
          resultsCopy[item.idx] = res;
          this.jobMatchResults.set(resultsCopy);
          // Persist job match results to state
          this.state.setJobMatchResults(resultsCopy);
          remaining -= 1;
          if (remaining <= 0) this.isAnalyzing.set(false);
        },
        error: (err) => {
          const resultsCopy = this.jobMatchResults().slice();
          resultsCopy[item.idx] = null;
          this.jobMatchResults.set(resultsCopy);
          this.state.setJobMatchResults(resultsCopy);
          this.jobMatchError.set('Some analyses failed. Please try again.');
          console.error('Job match error', err);
          remaining -= 1;
          if (remaining <= 0) this.isAnalyzing.set(false);
        }
      });
    });
  }

  canSearchAndRank(): boolean {
    return Boolean(
      this.savedProfileId()
      && this.analysisResult()
      && !this.hasUnsavedProfileChanges()
    );
  }

  canSaveSearchProfile(): boolean {
    return Boolean(
      this.analysisResult()
      && !this.isSaving()
      && this.hasUnsavedProfileChanges()
    );
  }

  searchAndRankDisabledReason(): string {
    if (!this.analysisResult()) {
      return 'Please analyze your CV first.';
    }

    if (!this.savedProfileId()) {
      return 'Please save the search profile first.';
    }

    if (this.hasUnsavedProfileChanges()) {
      return 'Please save profile changes first.';
    }

    return '';
  }

  saveDisabledReason(): string {
    if (!this.analysisResult()) {
      return 'Please analyze a CV or select a saved profile first.';
    }

    if (!this.hasUnsavedProfileChanges()) {
      return 'No changes to save.';
    }

    return '';
  }

  hasGeneratedQueries(): boolean {
    return Boolean(this.state.getGeneratedQueries());
  }

  onOpenJobMatch(): void {
    this.state.setCvText(this.cvText());
    this.state.setAnalysisResult(this.analysisResult());
    this.state.setJobInputs(this.jobInputs());
    this.state.setJobMatchResults(this.jobMatchResults());

    this.router.navigate(['/job-match'], {
      queryParams: this.navigationQueryParams()
    });
  }

  onOpenJobTracker(): void {
    this.router.navigate(['/job-tracker'], {
      queryParams: this.navigationQueryParams()
    });
  }

  onOpenMatchHistory(): void {
    this.router.navigate(['/match-history'], {
      queryParams: this.navigationQueryParams()
    });
  }

  onSearchAndRank() {
    // Persist current state before navigation
    this.state.setCvText(this.cvText());
    this.state.setAnalysisResult(this.analysisResult());
    this.state.setJobInputs(this.jobInputs());
    this.state.setJobMatchResults(this.jobMatchResults());

    if (!this.canSearchAndRank()) {
      return;
    }

    if (!this.state.getGeneratedQueries()) {
      this.loadQueriesOrGenerateAndNavigate('/job-search-ranking', '/search-queries');
      return;
    }

    this.router.navigate(['/job-search-ranking'], {
      queryParams: this.navigationQueryParams()
    });
  }

  private loadQueriesOrGenerateAndNavigate(
    savedQueriesRoute: string,
    generatedQueriesRoute: string
  ): void {
    const profileId = this.savedProfileId();

    if (!profileId) {
      this.generateError.set('Search profile not saved. Please save first.');
      return;
    }

    if (this.hasUnsavedProfileChanges()) {
      this.generateError.set('Save profile changes before using search queries.');
      return;
    }

    this.isGenerating.set(true);
    this.generateError.set(null);

    this.searchQueryService.getQueries(profileId).subscribe({
      next: (response) => {
        const hasSavedQueries = response.roleTitleQueries.length > 0
          || response.requirementBasedQueries.length > 0
          || response.alternativeDirectionQueries.length > 0;

        if (hasSavedQueries) {
          this.isGenerating.set(false);
          this.state.setGeneratedQueries(response);
          this.state.clearRankedJobs();
          this.router.navigate([savedQueriesRoute], {
            queryParams: this.navigationQueryParams()
          });
          return;
        }

        this.generateQueriesAndNavigate(generatedQueriesRoute);
      },
      error: (error) => {
        console.error('Saved queries load error:', error);
        this.generateQueriesAndNavigate(generatedQueriesRoute);
      }
    });
  }

  private hasUnsavedProfileChanges(): boolean {
    const current = this.analysisResult();

    if (!current) {
      return false;
    }

    const profileId = this.savedProfileId();

    if (!profileId) {
      return true;
    }

    const savedProfile = this.savedProfiles()
      .find(profile => profile.id === profileId);

    if (!savedProfile) {
      return true;
    }

    return savedProfile.summary !== current.summary
      || !this.arraysEqual(savedProfile.searchRoles, current.searchRoles)
      || !this.arraysEqual(savedProfile.alternativeCareerRoles, current.alternativeCareerRoles)
      || !this.arraysEqual(savedProfile.keywords, current.keywords);
  }

  private arraysEqual(left: string[], right: string[]): boolean {
    if (left.length !== right.length) {
      return false;
    }

    return left.every((value, index) => value === right[index]);
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

  private resetExpandedLists(): void {
    this.showAllSearchRoles.set(false);
    this.showAllAlternativeRoles.set(false);
    this.showAllKeywords.set(false);
  }

  private loadSavedProfiles(): void {
    this.isLoadingProfiles.set(true);
    this.profileLoadError.set(null);

    this.searchProfileService.getSearchProfiles().subscribe({
      next: (profiles) => {
        this.savedProfiles.set(profiles);
        this.isLoadingProfiles.set(false);

        const currentProfileId = this.savedProfileId();
        if (currentProfileId && profiles.some(profile => profile.id === currentProfileId)) {
          this.selectedProfileId.set(currentProfileId);
          if (!this.analysisResult()) {
            const selectedProfile = profiles.find(profile => profile.id === currentProfileId);
            if (selectedProfile) {
              this.applySavedProfile(selectedProfile);
            }
          }
        }
      },
      error: (error) => {
        this.isLoadingProfiles.set(false);
        this.profileLoadError.set('Failed to load saved profiles.');
        console.error('Saved profiles load error:', error);
      }
    });
  }

  private addOrReplaceSavedProfile(profile: SearchProfileResponse): void {
    const profilesWithoutSaved = this.savedProfiles()
      .filter(savedProfile => savedProfile.id !== profile.id);

    this.savedProfiles.set([profile, ...profilesWithoutSaved]);
  }

  private applySavedProfile(profile: SearchProfileResponse): void {
    const analysis: CvAnalysisResponse = {
      summary: profile.summary,
      searchRoles: [...profile.searchRoles],
      alternativeCareerRoles: [...profile.alternativeCareerRoles],
      keywords: [...profile.keywords]
    };

    this.selectedProfileId.set(profile.id);
    this.savedProfileId.set(profile.id);
    this.analysisResult.set(analysis);
    this.resetExpandedLists();
    this.saveSuccess.set(false);
    this.saveError.set(null);
    this.generateError.set(null);

    this.state.setSavedProfileId(profile.id);
    this.state.setAnalysisResult(analysis);
    this.state.setCleanedAnalysis(analysis);
    this.state.clearGeneratedQueries();
    this.state.clearRankedJobs();

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { profileId: profile.id },
      queryParamsHandling: 'merge',
      replaceUrl: true
    });
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



