import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { Home } from './features/home/home';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: Home,
  },
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
