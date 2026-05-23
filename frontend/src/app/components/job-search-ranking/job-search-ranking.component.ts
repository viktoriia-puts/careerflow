import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { SearchQueryGenerationResponse } from '../../models/search-query-generation.model';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';
import { JobMatchService } from '../../services/job-match.service';
import { RankedJobSearchResult } from '../../models/ranked-job.model';

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

  jobSearchLocation = signal<string>('');
  includeRemote = signal<boolean>(true);
  isSearchStarted = signal<boolean>(false);
  searchMessage = signal<string>('');

  isLoadingRanking = signal<boolean>(false);
  rankingError = signal<string | null>(null);
  rankedJobs = signal<RankedJobSearchResult[]>([]);

  progressSteps = signal<ProgressStep[]>([
    { name: 'Loading saved search profile', status: 'pending' },
    { name: 'Preparing search queries', status: 'pending' },
    { name: 'Searching job providers', status: 'pending' },
    { name: 'Normalizing job results', status: 'pending' },
    { name: 'Filtering by location', status: 'pending' },
    { name: 'Analyzing matches', status: 'pending' },
    { name: 'Ranking jobs', status: 'placeholder' }
  ]);

  filteringStepName = computed(() => {
    const location = this.jobSearchLocation();
    return location ? `Filtering by location: ${location}` : 'Filtering by location';
  });

  constructor(
    private state: SearchQueryStateService,
    private router: Router,
    private jobMatchService: JobMatchService
  ) {
    this.savedProfileId.set(this.state.getSavedProfileId());
    this.cleanedAnalysis.set(this.state.getCleanedAnalysis());
    this.generatedQueries.set(this.state.getGeneratedQueries());

    const savedLocation = this.state.getJobSearchLocation();
    this.jobSearchLocation.set(savedLocation);
    this.includeRemote.set(this.state.getIncludeRemote());
  }

  canStartSearch(): boolean {
    return Boolean(this.savedProfileId())
      && this.jobSearchLocation().trim().length > 0
      && !this.isLoadingRanking();
  }

  onStartJobSearch(): void {
    const profileId = this.savedProfileId();
    const location = this.jobSearchLocation().trim();

    if (!profileId) {
      this.rankingError.set('No saved search profile found. Please save a search profile first.');
      return;
    }

    if (!location) {
      this.rankingError.set('Please enter a location.');
      return;
    }

    this.state.setJobSearchLocation(location);
    this.state.setIncludeRemote(this.includeRemote());

    this.isSearchStarted.set(true);
    this.isLoadingRanking.set(true);
    this.rankingError.set(null);
    this.rankedJobs.set([]);

    let message = `Searching and ranking jobs for location: ${location}.`;
    message += ' This may take a few moments because Gemini analyzes the matching jobs.';
    this.searchMessage.set(message);

    this.markStepCompleted(0);
    this.markStepCompleted(1);
    this.markStepInProgress(2);

    const updatedSteps = this.progressSteps();
    updatedSteps[4].name = `Filtering by location: ${location}`;
    this.progressSteps.set([...updatedSteps]);

    this.jobMatchService.getRankedArbeitnowJobs(profileId, location, 10).subscribe({
      next: (results) => {
        this.rankedJobs.set(results);
        this.isLoadingRanking.set(false);

        this.markStepCompleted(2);
        this.markStepCompleted(3);
        this.markStepCompleted(4);
        this.markStepCompleted(5);
        this.markStepCompleted(6);

        this.searchMessage.set(`Found and ranked ${results.length} job(s).`);
      },
      error: (err) => {
        console.error('Ranked job search failed', err);
        this.isLoadingRanking.set(false);
        this.rankingError.set('Failed to search and rank jobs. Please try again.');
        this.searchMessage.set('');
      }
    });
  }

  private markStepCompleted(index: number): void {
    const steps = this.progressSteps();
    if (!steps[index]) return;

    steps[index].status = 'completed';
    this.progressSteps.set([...steps]);
  }

  private markStepInProgress(index: number): void {
    const steps = this.progressSteps();
    if (!steps[index]) return;

    steps[index].status = 'in-progress';
    this.progressSteps.set([...steps]);
  }

  goBack() {
    this.router.navigate(['/cv']);
  }
}
