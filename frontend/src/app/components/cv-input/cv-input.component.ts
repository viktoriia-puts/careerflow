import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CvAnalysisService } from '../../services/cv-analysis.service';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';

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

  constructor(private cvAnalysisService: CvAnalysisService) { }

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
}



