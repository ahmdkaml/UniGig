import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
  path: 'profile',
  canActivate: [authGuard],
  loadChildren: () => import('./features/profile/profile.routes').then((m) => m.PROFILE_ROUTES),
  },
  {
    path: 'gigs',
    canActivate: [authGuard],
    loadChildren: () => import('./features/gigs/gigs.routes').then((m) => m.GIG_ROUTES),
  },
];
