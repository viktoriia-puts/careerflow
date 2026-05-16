import { Routes } from '@angular/router';
import { CvInputComponent } from './components/cv-input/cv-input.component';
import { SearchQueriesComponent } from './components/search-queries/search-queries.component';

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
    path: '',
    redirectTo: '/cv',
    pathMatch: 'full'
  }
];
