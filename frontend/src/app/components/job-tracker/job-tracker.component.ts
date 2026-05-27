import { Component, ElementRef, HostListener, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TrackedJobService } from '../../services/tracked-job.service';
import { SearchProfileService } from '../../services/search-profile.service';
import { SearchQueryStateService } from '../../services/search-query-state.service';
import { SearchProfileResponse } from '../../models/search-profile.model';
import {
  TrackedJobCreateRequest,
  TrackedJobResponse,
  TrackedJobStatus,
  TrackedJobUpdateRequest
} from '../../models/tracked-job.model';

@Component({
  selector: 'app-job-tracker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './job-tracker.component.html',
  styleUrl: './job-tracker.component.css'
})
export class JobTrackerComponent {
  readonly statuses: TrackedJobStatus[] = [
    'SAVED',
    'APPLIED',
    'INTERVIEW',
    'TEST_TASK',
    'OFFER',
    'REJECTED'
  ];

  trackedJobs = signal<TrackedJobResponse[]>([]);
  isLoading = signal(false);
  error = signal<string | null>(null);
  savedProfiles = signal<SearchProfileResponse[]>([]);
  profileLoadError = signal<string | null>(null);
  isProfileDropdownOpen = signal(false);
  isAllProfilesSelected = signal(true);
  isManualSelected = signal(false);
  selectedProfileIds = signal<number[]>([]);
  statusFilter = signal<TrackedJobStatus | 'ALL'>('ALL');
  searchText = signal('');
  isAddingJob = signal(false);
  newCompany = signal('');
  newPositionTitle = signal('');
  newLocation = signal('');
  newJobUrl = signal('');
  newNotes = signal('');

  constructor(
    private trackedJobService: TrackedJobService,
    private searchProfileService: SearchProfileService,
    private router: Router,
    private route: ActivatedRoute,
    private state: SearchQueryStateService,
    private elementRef: ElementRef<HTMLElement>
  ) {
    const profileId = this.resolveRouteProfileId() ?? this.state.getSavedProfileId();
    if (profileId) {
      this.isAllProfilesSelected.set(false);
      this.selectedProfileIds.set([profileId]);
      this.state.setSavedProfileId(profileId);
    }

    this.loadSavedProfiles();
    this.loadTrackedJobs();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;

    if (!target || !this.elementRef.nativeElement.contains(target)) {
      this.isProfileDropdownOpen.set(false);
      return;
    }

    if (!target.closest('.profile-filter')) {
      this.isProfileDropdownOpen.set(false);
    }
  }

  filteredJobs(): TrackedJobResponse[] {
    const status = this.statusFilter();
    const search = this.searchText().trim().toLowerCase();
    const selectedProfileIds = new Set(this.selectedProfileIds());
    const showAllProfiles = this.isAllProfilesSelected();
    const showManual = this.isManualSelected();

    return this.trackedJobs().filter(job => {
      const matchesProfile = showAllProfiles
        || (job.searchProfileId === null && showManual)
        || (job.searchProfileId !== null && selectedProfileIds.has(job.searchProfileId));
      const matchesStatus = status === 'ALL' || job.status === status;
      const searchable = `${job.company} ${job.positionTitle} ${job.location ?? ''}`.toLowerCase();
      const matchesSearch = !search || searchable.includes(search);

      return matchesProfile && matchesStatus && matchesSearch;
    });
  }

  showProfileColumn(): boolean {
    return this.isAllProfilesSelected()
      || this.isManualSelected()
      || this.selectedProfileIds().length > 1;
  }

  profileFilterLabel(): string {
    if (this.isAllProfilesSelected()) {
      return 'All jobs';
    }

    const selectedCount = this.selectedProfileIds().length;

    if (selectedCount === 0) {
      return this.isManualSelected() ? 'Manual' : 'Select profiles';
    }

    if (this.isManualSelected()) {
      return `${selectedCount + 1} sources selected`;
    }

    if (selectedCount === 1) {
      return `Profile #${this.selectedProfileIds()[0]}`;
    }

    return `${selectedCount} profiles selected`;
  }

  toggleProfileDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.isProfileDropdownOpen.update(value => !value);
  }

  toggleAllProfiles(checked: boolean): void {
    this.isAllProfilesSelected.set(checked);

    if (checked) {
      this.selectedProfileIds.set([]);
      this.isManualSelected.set(false);
    }
  }

  toggleManual(checked: boolean): void {
    this.isAllProfilesSelected.set(false);
    this.isManualSelected.set(checked);
  }

  toggleProfile(profileId: number, checked: boolean): void {
    const current = this.selectedProfileIds();

    this.isAllProfilesSelected.set(false);

    if (checked && !current.includes(profileId)) {
      this.selectedProfileIds.set([...current, profileId]);
      return;
    }

    if (!checked) {
      this.selectedProfileIds.set(current.filter(id => id !== profileId));
    }
  }

  isProfileSelected(profileId: number): boolean {
    return this.selectedProfileIds().includes(profileId);
  }

  profileColumnLabel(profileId: number | null): string {
    return profileId === null ? 'Manual' : `#${profileId}`;
  }

  savedProfileLabel(profile: SearchProfileResponse): string {
    const summary = profile.summary.length > 70
      ? `${profile.summary.slice(0, 70)}...`
      : profile.summary;

    return `#${profile.id} - ${summary}`;
  }

  updateStatus(job: TrackedJobResponse, status: TrackedJobStatus): void {
    const patch: TrackedJobUpdateRequest = { status };

    if (status === 'APPLIED' && !job.appliedDate) {
      patch.appliedDate = this.todayIsoDate();
    }

    this.updateJob(job, patch);
  }

  updateAppliedDate(job: TrackedJobResponse, appliedDate: string): void {
    this.updateJob(job, { appliedDate: appliedDate || null });
  }

  updateNotes(job: TrackedJobResponse, notes: string): void {
    this.updateJob(job, { notes });
  }

  updateJobUrl(job: TrackedJobResponse, jobUrl: string): void {
    this.updateJob(job, { jobUrl: this.normalizeUrl(jobUrl) });
  }

  addManualJob(): void {
    const company = this.newCompany().trim();
    const positionTitle = this.newPositionTitle().trim();

    if (!company || !positionTitle) {
      this.error.set('Company and position are required.');
      return;
    }

    const request: TrackedJobCreateRequest = {
      searchProfileId: null,
      company,
      positionTitle,
      location: this.emptyToNull(this.newLocation()),
      source: 'MANUAL',
      jobUrl: this.normalizeUrl(this.newJobUrl()),
      referenceId: null,
      matchScore: null,
      status: 'SAVED',
      notes: this.emptyToNull(this.newNotes())
    };

    this.isAddingJob.set(true);
    this.error.set(null);

    this.trackedJobService.createTrackedJob(request).subscribe({
      next: (createdJob) => {
        this.trackedJobs.set([createdJob, ...this.trackedJobs()]);
        this.resetManualForm();
        this.isAddingJob.set(false);
      },
      error: (error) => {
        this.error.set('Failed to add tracked job.');
        this.isAddingJob.set(false);
        console.error('Tracked job create error:', error);
      }
    });
  }

  deleteJob(job: TrackedJobResponse): void {
    this.trackedJobService.deleteTrackedJob(job.id).subscribe({
      next: () => {
        this.trackedJobs.set(
          this.trackedJobs().filter(currentJob => currentJob.id !== job.id)
        );
      },
      error: (error) => {
        this.error.set('Failed to delete tracked job.');
        console.error('Tracked job delete error:', error);
      }
    });
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

  statusLabel(status: TrackedJobStatus): string {
    return status
      .toLowerCase()
      .split('_')
      .map(part => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  statusClass(status: TrackedJobStatus): string {
    return `status-${status.toLowerCase().replace('_', '-')}`;
  }

  private loadTrackedJobs(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.trackedJobService.getTrackedJobs().subscribe({
      next: (jobs) => {
        this.trackedJobs.set(jobs);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.error.set('Failed to load tracked jobs.');
        this.isLoading.set(false);
        console.error('Tracked jobs load error:', error);
      }
    });
  }

  private loadSavedProfiles(): void {
    this.profileLoadError.set(null);

    this.searchProfileService.getSearchProfiles().subscribe({
      next: (profiles) => {
        this.savedProfiles.set(profiles);
      },
      error: (error) => {
        this.profileLoadError.set('Failed to load saved profiles.');
        console.error('Saved profiles load error:', error);
      }
    });
  }

  private updateJob(
    job: TrackedJobResponse,
    patch: TrackedJobUpdateRequest
  ): void {
    const request: TrackedJobUpdateRequest = {
      company: job.company,
      positionTitle: job.positionTitle,
      location: job.location,
      jobUrl: job.jobUrl,
      matchScore: job.matchScore,
      status: job.status,
      appliedDate: job.appliedDate,
      resultNote: job.resultNote,
      notes: job.notes,
      ...patch
    };

    this.trackedJobService.updateTrackedJob(job.id, request).subscribe({
      next: (updatedJob) => {
        this.trackedJobs.set(
          this.trackedJobs().map(currentJob =>
            currentJob.id === updatedJob.id ? updatedJob : currentJob
          )
        );
        this.error.set(null);
      },
      error: (error) => {
        this.error.set('Failed to update tracked job.');
        console.error('Tracked job update error:', error);
      }
    });
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private resetManualForm(): void {
    this.newCompany.set('');
    this.newPositionTitle.set('');
    this.newLocation.set('');
    this.newJobUrl.set('');
    this.newNotes.set('');
  }

  private emptyToNull(value: string): string | null {
    const trimmed = value.trim();
    return trimmed ? trimmed : null;
  }

  private normalizeUrl(value: string | null): string | null {
    if (!value) {
      return null;
    }

    const trimmed = value.trim();
    if (!trimmed) {
      return null;
    }

    if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
      return trimmed;
    }

    return `https://${trimmed}`;
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
    if (this.selectedProfileIds().length === 1 && !this.isManualSelected()) {
      return { profileId: this.selectedProfileIds()[0] };
    }

    const storedProfileId = this.state.getSavedProfileId();
    return storedProfileId ? { profileId: storedProfileId } : {};
  }
}
