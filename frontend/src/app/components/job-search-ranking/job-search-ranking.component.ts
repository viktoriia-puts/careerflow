import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { SearchQueryGenerationResponse } from '../../models/search-query-generation.model';
import { CvAnalysisResponse } from '../../models/cv-analysis.model';
import { JobMatchService } from '../../services/job-match.service';
import { RankedJobSearchResult } from '../../models/ranked-job.model';
import { TrackedJobService } from '../../services/tracked-job.service';
import { TrackedJobCreateRequest, TrackedJobResponse } from '../../models/tracked-job.model';
import { MatchHistoryService } from '../../services/match-history.service';
import { MatchHistoryRunDetail } from '../../models/match-history.model';

@Component({
  selector: 'app-job-search-ranking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './job-search-ranking.component.html',
  styleUrl: './job-search-ranking.component.css'
})
export class JobSearchRankingComponent {
  private readonly targetPerProvider = 25;
  readonly jobLevelOptions = [
    { value: 'JUNIOR', label: 'Junior' },
    { value: 'SENIOR_MIDDLE', label: 'Senior / Middle' }
  ];

  savedProfileId = signal<number | null>(null);
  cleanedAnalysis = signal<CvAnalysisResponse | null>(null);
  generatedQueries = signal<SearchQueryGenerationResponse | null>(null);

  jobSearchLocation = signal<string>('');
  jobLevel = signal<string>('JUNIOR');
  includeRemote = signal<boolean>(true);
  isSearchStarted = signal<boolean>(false);
  searchMessage = signal<string>('');

  isLoadingRanking = signal<boolean>(false);
  rankingError = signal<string | null>(null);
  trackerMessage = signal<string | null>(null);
  trackerError = signal<string | null>(null);
  rankedJobs = signal<RankedJobSearchResult[]>([]);
  trackedJobs = signal<TrackedJobResponse[]>([]);

  constructor(
    private state: SearchQueryStateService,
    private router: Router,
    private route: ActivatedRoute,
    private jobMatchService: JobMatchService,
    private trackedJobService: TrackedJobService,
    private matchHistoryService: MatchHistoryService
  ) {
    const routeProfileId = this.resolveRouteProfileId();
    const routeLocation = this.route.snapshot.queryParamMap.get('location')?.trim();
    const routeJobLevel = this.route.snapshot.queryParamMap.get('jobLevel')?.trim();
    const profileId = routeProfileId ?? this.state.getSavedProfileId();

    this.savedProfileId.set(profileId);
    if (profileId) {
      this.state.setSavedProfileId(profileId);
    }

    this.cleanedAnalysis.set(this.state.getCleanedAnalysis());
    this.generatedQueries.set(this.state.getGeneratedQueries());

    const savedLocation = routeLocation || this.state.getJobSearchLocation();
    this.jobSearchLocation.set(savedLocation);
    this.state.setJobSearchLocation(savedLocation);
    this.jobLevel.set(this.normalizeJobLevel(routeJobLevel));
    this.includeRemote.set(this.state.getIncludeRemote());

    if (profileId) {
      this.loadLatestSavedRanking(profileId, savedLocation);
    }

    this.loadTrackedJobs();
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
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { profileId, location, jobLevel: this.jobLevel() },
      queryParamsHandling: 'merge',
      replaceUrl: true
    });

    this.isSearchStarted.set(true);
    this.isLoadingRanking.set(true);
    this.rankingError.set(null);
    this.trackerMessage.set(null);
    this.trackerError.set(null);
    this.rankedJobs.set([]);

    let message = `Searching and ranking jobs for location: ${location}.`;
    message += ' This may take a few moments because Gemini analyzes the matching jobs.';
    this.searchMessage.set(message);

    this.jobMatchService.getRankedJobs(
      profileId,
      location,
      this.targetPerProvider,
      this.jobLevel()
    ).subscribe({
      next: (results) => {
        this.rankedJobs.set(results);
        this.isLoadingRanking.set(false);

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

  goBack() {
    this.router.navigate(['/cv'], {
      queryParams: this.navigationQueryParams()
    });
  }

  openJobTracker(): void {
    this.router.navigate(['/job-tracker'], {
      queryParams: this.navigationQueryParams()
    });
  }

  openMatchHistory(): void {
    this.router.navigate(['/match-history'], {
      queryParams: this.navigationQueryParams()
    });
  }

  onLocationChange(location: string): void {
    this.jobSearchLocation.set(location);
    this.state.setJobSearchLocation(location);
    this.rankedJobs.set([]);
    this.isSearchStarted.set(false);
    this.searchMessage.set('');
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: this.navigationQueryParams(location),
      queryParamsHandling: 'merge',
      replaceUrl: true
    });
  }

  onJobLevelChange(jobLevel: string): void {
    this.jobLevel.set(this.normalizeJobLevel(jobLevel));
    this.rankedJobs.set([]);
    this.isSearchStarted.set(false);
    this.searchMessage.set('');
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: this.navigationQueryParams(),
      queryParamsHandling: 'merge',
      replaceUrl: true
    });
  }

  addToTracker(result: RankedJobSearchResult): void {
    const profileId = this.savedProfileId();

    if (!profileId) {
      this.trackerError.set('No saved search profile found.');
      return;
    }

    const request: TrackedJobCreateRequest = {
      searchProfileId: profileId,
      company: result.job.company || 'Unknown company',
      positionTitle: result.job.title || 'Untitled position',
      location: result.job.location || null,
      source: result.job.source || null,
      jobUrl: result.job.url || null,
      referenceId: result.job.referenceId || null,
      matchScore: result.matchAnalysis.matchScore ?? null,
      status: 'SAVED',
      notes: null
    };

    this.trackedJobService.createTrackedJob(request).subscribe({
      next: (trackedJob) => {
        this.trackerError.set(null);
        this.trackerMessage.set('Job added to tracker.');
        this.upsertTrackedJob(trackedJob);
      },
      error: (error) => {
        this.trackerError.set('Failed to add job to tracker.');
        console.error('Tracked job create error:', error);
      }
    });
  }

  isTracked(result: RankedJobSearchResult): boolean {
    return this.trackedJobs().some(trackedJob =>
      this.sameTrackedJob(trackedJob, result)
    );
  }

  formatProviderName(source: string): string {
    if (!source) {
      return 'Unknown provider';
    }

    switch (source) {
      case 'ARBEITNOW':
        return 'Arbeitnow';
      case 'BUNDESAGENTUR':
        return 'Bundesagentur für Arbeit';
      case 'REMOTIVE':
        return 'Remotive';
      default:
        return source;
    }
  }

  private loadTrackedJobs(): void {
    this.trackedJobService.getTrackedJobs(this.savedProfileId()).subscribe({
      next: (trackedJobs) => {
        this.trackedJobs.set(trackedJobs);
      },
      error: (error) => {
        console.error('Tracked jobs load error:', error);
      }
    });
  }

  private loadLatestSavedRanking(profileId: number, location: string): void {
    this.matchHistoryService.getRuns(profileId).subscribe({
      next: (runs) => {
        const matchingRun = runs.find(run =>
          this.normalizeLocation(run.location || '') === this.normalizeLocation(location)
        ) ?? runs[0];

        if (!matchingRun) {
          return;
        }

        this.matchHistoryService.getRun(profileId, matchingRun.id).subscribe({
          next: (run) => this.restoreRankingFromHistory(run),
          error: (error) => {
            console.error('Latest match history run load error:', error);
          }
        });
      },
      error: (error) => {
        console.error('Match history runs load error:', error);
      }
    });
  }

  private restoreRankingFromHistory(run: MatchHistoryRunDetail): void {
    const restoredResults: RankedJobSearchResult[] = run.results.map(result => ({
      job: result.job,
      matchAnalysis: result.matchAnalysis
    }));

    if (restoredResults.length === 0) {
      return;
    }

    this.rankedJobs.set(restoredResults);
    this.isSearchStarted.set(true);
    this.isLoadingRanking.set(false);
    this.searchMessage.set(`Loaded ${restoredResults.length} saved ranked job(s) from Match History.`);

    if (run.location?.trim()) {
      this.jobSearchLocation.set(run.location);
      this.state.setJobSearchLocation(run.location);
    }
  }

  private resolveRouteProfileId(): number | null {
    const value = this.route.snapshot.queryParamMap.get('profileId');
    if (!value) {
      return null;
    }

    const parsedValue = Number(value);
    return Number.isFinite(parsedValue) ? parsedValue : null;
  }

  private navigationQueryParams(location: string = this.jobSearchLocation()): Record<string, string | number> {
    const params: Record<string, string | number> = {};
    const profileId = this.savedProfileId();

    if (profileId) {
      params['profileId'] = profileId;
    }

    if (location.trim()) {
      params['location'] = location.trim();
    }

    params['jobLevel'] = this.jobLevel();

    return params;
  }

  private normalizeLocation(location: string): string {
    return (location ?? '').trim().toLowerCase();
  }

  private normalizeJobLevel(jobLevel: string | null | undefined): string {
    return jobLevel === 'SENIOR_MIDDLE' ? 'SENIOR_MIDDLE' : 'JUNIOR';
  }

  private upsertTrackedJob(trackedJob: TrackedJobResponse): void {
    const existingJobs = this.trackedJobs()
      .filter(currentJob => currentJob.id !== trackedJob.id);

    this.trackedJobs.set([trackedJob, ...existingJobs]);
  }

  private sameTrackedJob(
    trackedJob: TrackedJobResponse,
    result: RankedJobSearchResult
  ): boolean {
    if (
      trackedJob.referenceId
      && result.job.referenceId
      && trackedJob.source === result.job.source
    ) {
      return trackedJob.referenceId === result.job.referenceId;
    }

    if (trackedJob.jobUrl && result.job.url) {
      return trackedJob.jobUrl === result.job.url;
    }

    return trackedJob.company === result.job.company
      && trackedJob.positionTitle === result.job.title;
  }
}
