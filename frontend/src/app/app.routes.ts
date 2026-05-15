import { Routes } from '@angular/router';
import { CvInputComponent } from './components/cv-input/cv-input.component';

export const routes: Routes = [
  {
    path: 'cv',
    component: CvInputComponent
  },
  {
    path: '',
    redirectTo: '/cv',
    pathMatch: 'full'
  }
];
