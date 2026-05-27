import { Routes } from '@angular/router';
import { CvInputComponent } from './components/cv-input/cv-input.component';
import { SearchQueriesComponent } from './components/search-queries/search-queries.component';
import { JobSearchRankingComponent } from './components/job-search-ranking/job-search-ranking.component';
import { JobMatchComponent } from './components/job-match/job-match.component';
import { JobTrackerComponent } from './components/job-tracker/job-tracker.component';
import { MatchHistoryComponent } from './components/match-history/match-history.component';

export const routes: Routes = [
  {
    path: 'cv',
    component: CvInputComponent
  },
  {
    path: 'search-queries',
    component: SearchQueriesComponent
  },
  {
    path: 'job-search-ranking',
    component: JobSearchRankingComponent
  },
  {
    path: 'job-tracker',
    component: JobTrackerComponent
  },
  {
    path: 'match-history',
    component: MatchHistoryComponent
  },
  {
    path: '',
    redirectTo: '/cv',
    pathMatch: 'full'
  },
  {
    path: 'job-match',
    component: JobMatchComponent
  }
];
