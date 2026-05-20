import { Routes } from '@angular/router';
import { CvInputComponent } from './components/cv-input/cv-input.component';
import { SearchQueriesComponent } from './components/search-queries/search-queries.component';
import { JobSearchRankingComponent } from './components/job-search-ranking/job-search-ranking.component';

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
    path: '',
    redirectTo: '/cv',
    pathMatch: 'full'
  }
];
