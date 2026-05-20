import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { SearchQueryGenerationResponse } from '../../models/search-query-generation.model';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';

interface ProgressStep {
  name: string;
  status: 'pending' | 'in-progress' | 'completed' | 'placeholder';
}

@Component({
  selector: 'app-job-search-ranking',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './job-search-ranking.component.html',
  styleUrl: './job-search-ranking.component.css'
})
export class JobSearchRankingComponent {
  savedProfileId = signal<number | null>(null);
  cleanedAnalysis = signal<CvAnalysisResponse | null>(null);
  generatedQueries = signal<SearchQueryGenerationResponse | null>(null);

  progressSteps = signal<ProgressStep[]>([
    { name: 'Loading saved search profile', status: 'pending' },
    { name: 'Preparing search queries', status: 'pending' },
    { name: 'Searching job providers', status: 'pending' },
    { name: 'Normalizing job results', status: 'pending' },
    { name: 'Filtering by location', status: 'pending' },
    { name: 'Analyzing matches', status: 'pending' },
    { name: 'Ranking jobs', status: 'placeholder' }
  ]);

  constructor(
    private state: SearchQueryStateService,
    private router: Router
  ) {
    // Load persisted state
    this.savedProfileId.set(this.state.getSavedProfileId());
    this.cleanedAnalysis.set(this.state.getCleanedAnalysis());
    this.generatedQueries.set(this.state.getGeneratedQueries());
  }

  goBack() {
    this.router.navigate(['/cv']);
  }
}

