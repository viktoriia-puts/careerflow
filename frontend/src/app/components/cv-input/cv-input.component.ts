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

  constructor(
    private cvAnalysisService: CvAnalysisService,
    private state: SearchQueryStateService,
    private searchProfileService: SearchProfileService,
    private searchQueryService: SearchQueryService,
    private router: Router
  ) { }

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
}



