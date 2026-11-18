import { Routes } from '@angular/router';

export const PROFILE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./profile-shell').then((m) => m.ProfileShell),
  },
  {
    path: ':userId',
    loadComponent: () => import('./freelancer-profile').then((m) => m.FreelancerProfile),
  },
];