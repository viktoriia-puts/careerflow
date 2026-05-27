import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { MatchHistoryService } from '../../services/match-history.service';
import {
  MatchHistoryResult,
  MatchHistoryRunDetail,
  MatchHistoryRunSummary
} from '../../models/match-history.model';
import { TrackedJobCreateRequest, TrackedJobResponse } from '../../models/tracked-job.model';
import { TrackedJobService } from '../../services/tracked-job.service';

@Component({
  selector: 'app-match-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './match-history.component.html',
  styleUrl: './match-history.component.css'
})
export class MatchHistoryComponent {
  savedProfileId = signal<number | null>(null);
  runs = signal<MatchHistoryRunSummary[]>([]);
  selectedRun = signal<MatchHistoryRunDetail | null>(null);
  trackedJobs = signal<TrackedJobResponse[]>([]);
  isLoadingRuns = signal(false);
  isLoadingRun = signal(false);
  error = signal<string | null>(null);
  trackerMessage = signal<string | null>(null);
  trackerError = signal<string | null>(null);

  constructor(
    private state: SearchQueryStateService,
    private matchHistoryService: MatchHistoryService,
    private trackedJobService: TrackedJobService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    const profileId = this.resolveRouteProfileId() ?? this.state.getSavedProfileId();
    this.savedProfileId.set(profileId);
    if (profileId) {
      this.state.setSavedProfileId(profileId);
    }

    if (!profileId) {
      this.error.set('Select or save a search profile before opening match history.');
      return;
    }

    this.loadRuns(profileId);
    this.loadTrackedJobs(profileId);
  }

  selectRun(runId: number): void {
    const profileId = this.savedProfileId();

    if (!profileId) {
      return;
    }

    this.isLoadingRun.set(true);
    this.error.set(null);

    this.matchHistoryService.getRun(profileId, runId).subscribe({
      next: (run) => {
        this.selectedRun.set(run);
        this.isLoadingRun.set(false);
      },
      error: (error) => {
        this.error.set('Failed to load match history details.');
        this.isLoadingRun.set(false);
        console.error('Match history detail load error:', error);
      }
    });
  }

  removeFromHistory(result: MatchHistoryResult): void {
    const profileId = this.savedProfileId();

    if (!profileId) {
      return;
    }

    this.matchHistoryService.deleteResult(profileId, result.id).subscribe({
      next: () => {
        const currentRun = this.selectedRun();

        if (currentRun) {
          const remainingResults = currentRun.results.filter(item => item.id !== result.id);
          this.selectedRun.set({ ...currentRun, results: remainingResults });
          this.updateSelectedRunCount(currentRun.id, remainingResults.length);
        }
      },
      error: (error) => {
        this.error.set('Failed to remove match from history.');
        console.error('Match history delete error:', error);
      }
    });
  }

  addToTracker(result: MatchHistoryResult): void {
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

  isTracked(result: MatchHistoryResult): boolean {
    return this.trackedJobs().some(trackedJob =>
      this.sameTrackedJob(trackedJob, result)
    );
  }

  openHome(): void {
    this.router.navigate(['/cv'], {
      queryParams: this.navigationQueryParams()
    });
  }

  openRanking(): void {
    this.router.navigate(['/job-search-ranking'], {
      queryParams: this.navigationQueryParams()
    });
  }

  openJobTracker(): void {
    this.router.navigate(['/job-tracker'], {
      queryParams: this.navigationQueryParams()
    });
  }

  formatRunDate(value: string): string {
    return new Date(value).toLocaleString();
  }

  formatProviderName(source: string): string {
    switch (source) {
      case 'ARBEITNOW':
        return 'Arbeitnow';
      case 'BUNDESAGENTUR':
        return 'Bundesagentur fuer Arbeit';
      case 'REMOTIVE':
        return 'Remotive';
      default:
        return source || 'Unknown provider';
    }
  }

  private loadRuns(profileId: number): void {
    this.isLoadingRuns.set(true);
    this.error.set(null);

    this.matchHistoryService.getRuns(profileId).subscribe({
      next: (runs) => {
        this.runs.set(runs);
        this.isLoadingRuns.set(false);

        if (runs.length > 0) {
          this.selectRun(runs[0].id);
        }
      },
      error: (error) => {
        this.error.set('Failed to load match history.');
        this.isLoadingRuns.set(false);
        console.error('Match history load error:', error);
      }
    });
  }

  private loadTrackedJobs(profileId: number): void {
    this.trackedJobService.getTrackedJobs(profileId).subscribe({
      next: (trackedJobs) => {
        this.trackedJobs.set(trackedJobs);
      },
      error: (error) => {
        console.error('Tracked jobs load error:', error);
      }
    });
  }

  private updateSelectedRunCount(runId: number, resultCount: number): void {
    this.runs.set(
      this.runs().map(run =>
        run.id === runId ? { ...run, resultCount } : run
      )
    );
  }

  private upsertTrackedJob(trackedJob: TrackedJobResponse): void {
    const existingJobs = this.trackedJobs()
      .filter(currentJob => currentJob.id !== trackedJob.id);

    this.trackedJobs.set([trackedJob, ...existingJobs]);
  }

  private sameTrackedJob(
    trackedJob: TrackedJobResponse,
    result: MatchHistoryResult
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
