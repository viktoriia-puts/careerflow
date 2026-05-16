import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CvAnalysisService } from '../../services/cv-analysis.service';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';
import { Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';

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

  constructor(
    private cvAnalysisService: CvAnalysisService,
    private state: SearchQueryStateService,
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

  // Save cleaned analysis to frontend state and navigate to search queries page
  onGenerateSearchQueries() {
    const current = this.analysisResult();
    if (!current) return;
    // Save a deep copy to avoid later mutation issues
    const copy: CvAnalysisResponse = {
      summary: current.summary,
      searchRoles: [...current.searchRoles],
      alternativeCareerRoles: [...current.alternativeCareerRoles],
      keywords: [...current.keywords]
    };
    this.state.setCleanedAnalysis(copy);
    this.router.navigate(['/search-queries']);
  }
}



