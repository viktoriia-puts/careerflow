import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { JobMatchService } from '../../services/job-match.service';
import { JobMatchAnalysisResponse } from '../../models/job-match.model';

@Component({
  selector: 'app-job-match',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './job-match.component.html',
  styleUrl: './job-match.component.css'
})
export class JobMatchComponent {
  savedProfileId = signal<number | null>(null);

  jobInputs = signal<{ id: number; description: string }[]>([
    { id: 1, description: '' }
  ]);

  jobMatchResults = signal<(JobMatchAnalysisResponse | null)[]>([]);
  isAnalyzing = signal(false);
  jobMatchError = signal<string | null>(null);

  constructor(
    private state: SearchQueryStateService,
    private jobMatchService: JobMatchService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    const profileId = this.resolveRouteProfileId() ?? this.state.getSavedProfileId();
    this.savedProfileId.set(profileId);
    if (profileId) {
      this.state.setSavedProfileId(profileId);
    }

    const savedJobInputs = this.state.getJobInputs();
    if (savedJobInputs && savedJobInputs.length > 0) {
      this.jobInputs.set(savedJobInputs);
    }

    const savedJobResults = this.state.getJobMatchResults();
    if (savedJobResults && savedJobResults.length > 0) {
      this.jobMatchResults.set(savedJobResults);
    }

    this.syncResultsLength();
  }

  updateJobDescription(index: number, value: string): void {
    const copy = this.jobInputs().map(job => ({ ...job }));

    if (index < 0 || index >= copy.length) {
      return;
    }

    copy[index].description = value;
    this.jobInputs.set(copy);
    this.state.setJobInputs(copy);
    this.syncResultsLength();
  }

  addJobInput(): void {
    const current = this.jobInputs();
    const nextId = current.length > 0
      ? Math.max(...current.map(job => job.id)) + 1
      : 1;

    const updated = [
      ...current,
      { id: nextId, description: '' }
    ];

    this.jobInputs.set(updated);
    this.state.setJobInputs(updated);
    this.syncResultsLength();
  }

  removeJobInput(index: number): void {
    const current = this.jobInputs();

    if (current.length <= 1) {
      return;
    }

    const updated = current.filter((_, i) => i !== index);

    this.jobInputs.set(updated);
    this.state.setJobInputs(updated);
    this.syncResultsLength();
  }

  canAnalyzeMatches(): boolean {
    return Boolean(this.savedProfileId())
      && !this.isAnalyzing()
      && this.jobInputs().some(job => job.description.trim().length > 0);
  }

  onAnalyzeMatches(): void {
    const profileId = this.savedProfileId();

    if (!profileId) {
      this.jobMatchError.set('Please save a search profile first before analyzing jobs.');
      return;
    }

    const nonEmpty = this.jobInputs()
      .map((job, index) => ({
        index,
        description: job.description.trim()
      }))
      .filter(item => item.description.length > 0);

    if (nonEmpty.length === 0) {
      this.jobMatchError.set('Please enter at least one job description.');
      return;
    }

    this.jobMatchError.set(null);
    this.isAnalyzing.set(true);
    this.syncResultsLength();

    let remaining = nonEmpty.length;

    nonEmpty.forEach(item => {
      this.jobMatchService.analyzeJobMatch({
        searchProfileId: profileId,
        jobDescription: item.description
      }).subscribe({
        next: (response) => {
          const resultsCopy = this.jobMatchResults().slice();
          resultsCopy[item.index] = response;

          this.jobMatchResults.set(resultsCopy);
          this.state.setJobMatchResults(resultsCopy);

          remaining -= 1;

          if (remaining <= 0) {
            this.isAnalyzing.set(false);
          }
        },
        error: (error) => {
          console.error('Job match error', error);

          const resultsCopy = this.jobMatchResults().slice();
          resultsCopy[item.index] = null;

          this.jobMatchResults.set(resultsCopy);
          this.state.setJobMatchResults(resultsCopy);

          this.jobMatchError.set('Some analyses failed. Please try again.');

          remaining -= 1;

          if (remaining <= 0) {
            this.isAnalyzing.set(false);
          }
        }
      });
    });
  }

  private syncResultsLength(): void {
    const inputs = this.jobInputs();
    const results = this.jobMatchResults().slice();

    while (results.length < inputs.length) {
      results.push(null);
    }

    if (results.length > inputs.length) {
      results.splice(inputs.length);
    }

    this.jobMatchResults.set(results);
  }

  goBack(): void {
    this.router.navigate(['/cv'], {
      queryParams: this.navigationQueryParams()
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
