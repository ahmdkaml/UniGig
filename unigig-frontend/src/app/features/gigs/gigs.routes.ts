import { Routes } from '@angular/router';

export const GIG_ROUTES: Routes = [
  { path: 'new', loadComponent: () => import('./post-gig').then((m) => m.PostGig) },
];
