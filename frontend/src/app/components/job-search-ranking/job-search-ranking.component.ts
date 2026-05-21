import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
  imports: [CommonModule, FormsModule],
  templateUrl: './job-search-ranking.component.html',
  styleUrl: './job-search-ranking.component.css'
})
export class JobSearchRankingComponent {
  savedProfileId = signal<number | null>(null);
  cleanedAnalysis = signal<CvAnalysisResponse | null>(null);
  generatedQueries = signal<SearchQueryGenerationResponse | null>(null);

  // Search constraints state
  jobSearchLocation = signal<string>('');
  includeRemote = signal<boolean>(true);
  isSearchStarted = signal<boolean>(false);
  searchMessage = signal<string>('');

  // Progress steps - computed to show location in filtering step
  progressSteps = signal<ProgressStep[]>([
    { name: 'Loading saved search profile', status: 'pending' },
    { name: 'Preparing search queries', status: 'pending' },
    { name: 'Searching job providers', status: 'pending' },
    { name: 'Normalizing job results', status: 'pending' },
    { name: 'Filtering by location', status: 'pending' },
    { name: 'Analyzing matches', status: 'pending' },
    { name: 'Ranking jobs', status: 'placeholder' }
  ]);

  // Computed property to get the filtering step name with location
  filteringStepName = computed(() => {
    const location = this.jobSearchLocation();
    return location ? `Filtering by location: ${location}` : 'Filtering by location';
  });

  constructor(
    private state: SearchQueryStateService,
    private router: Router
  ) {
    // Load persisted state
    this.savedProfileId.set(this.state.getSavedProfileId());
    this.cleanedAnalysis.set(this.state.getCleanedAnalysis());
    this.generatedQueries.set(this.state.getGeneratedQueries());

    // Load persisted location settings
    const savedLocation = this.state.getJobSearchLocation();
    this.jobSearchLocation.set(savedLocation);
    this.includeRemote.set(this.state.getIncludeRemote());
  }

  canStartSearch(): boolean {
    return Boolean(this.savedProfileId()) && this.jobSearchLocation().trim().length > 0 && !this.isSearchStarted();
  }

  onStartJobSearch(): void {
    const location = this.jobSearchLocation().trim();

    if (!this.savedProfileId()) {
      return;
    }

    if (!location) {
      return;
    }

    // Persist location settings to state service
    this.state.setJobSearchLocation(location);
    this.state.setIncludeRemote(this.includeRemote());

    // Start search
    this.isSearchStarted.set(true);
    let message = `Automatic job search will use location: ${location}.`;
    message += ' Remote jobs will also be included.';
    this.searchMessage.set(message);

    // Update progress steps - simulate progress (for now)
    const steps = this.progressSteps();
    steps[0].status = 'completed';
    steps[1].status = 'in-progress';
    this.progressSteps.set([...steps]);

    // Update filtering step name with location
    const updatedSteps = this.progressSteps();
    updatedSteps[4].name = `Filtering by location: ${location}`;
    this.progressSteps.set([...updatedSteps]);
  }

  goBack() {
    this.router.navigate(['/cv']);
  }
}

