import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CvAnalysisService } from '../../services/cv-analysis.service';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';
import { Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { SearchProfileService } from '../../services/search-profile.service';
import { SearchQueryService } from '../../services/search-query.service';
import { SearchProfileCreateRequest } from '../../models/search-profile.model';
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
  cvText = signal('');
  isLoading = signal(false);
  error = signal<string | null>(null);
  analysisResult = signal<CvAnalysisResponse | null>(null);
  // temporary inputs for adding new items
  newSearchRole = signal('');
  newAlternativeRole = signal('');
  newKeyword = signal('');
  // Search profile saving state
  isSaving = signal(false);
  saveSuccess = signal(false);
  saveError = signal<string | null>(null);
  savedProfileId = signal<number | null>(null);
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
    private router: Router
  ) { }

  ngOnInit(): void {
    // ensure results array matches initial inputs
    this.syncResultsLength();
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
        this.isLoading.set(false);
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
    this.error.set(null);
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

  // Save search profile to backend
  onSaveSearchProfile() {
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
        // store saved profile id in shared state so other pages can use it
        this.state.setSavedProfileId(response.id);
        console.log('Search profile saved successfully');
        // Clear after 3 seconds
        setTimeout(() => {
          this.saveSuccess.set(false);
        }, 3000);
      },
      error: (error) => {
        this.isSaving.set(false);
        this.saveError.set('Failed to save search profile. Please try again.');
        console.error('Search profile save error:', error);
      }
    });
  }

  // Save cleaned analysis and generate queries
  onGenerateSearchQueries() {
    const profileId = this.savedProfileId();

    if (!profileId) {
      this.generateError.set('Search profile not saved. Please save first.');
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

    // Generate queries from backend
    this.isGenerating.set(true);
    this.generateError.set(null);

    this.searchQueryService.generateQueries(profileId).subscribe({
      next: (response) => {
        this.isGenerating.set(false);
        // Store generated queries in state
        this.state.setGeneratedQueries(response);
        console.log('Search queries generated successfully:', response);
        // Navigate to search queries page
        this.router.navigate(['/search-queries']);
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
    this.syncResultsLength();
  }

  addJobInput() {
    const current = this.jobInputs();
    const nextId = current.length > 0 ? Math.max(...current.map(j => j.id)) + 1 : 1;
    this.jobInputs.set([...current, { id: nextId, description: '' }]);
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
          remaining -= 1;
          if (remaining <= 0) this.isAnalyzing.set(false);
        },
        error: (err) => {
          const resultsCopy = this.jobMatchResults().slice();
          resultsCopy[item.idx] = null;
          this.jobMatchResults.set(resultsCopy);
          this.jobMatchError.set('Some analyses failed. Please try again.');
          console.error('Job match error', err);
          remaining -= 1;
          if (remaining <= 0) this.isAnalyzing.set(false);
        }
      });
    });
  }
}



