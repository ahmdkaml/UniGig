import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  template: `
    <header class="border-b border-slate-200 bg-white">
      <nav class="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <a routerLink="/" class="text-lg font-bold tracking-tight text-slate-900">
          UniGig
        </a>

        <div class="flex items-center gap-4 text-sm font-medium">
          @if (authService.getToken()) {
            <button
              type="button"
              (click)="onLogout()"
              class="rounded-md bg-slate-900 px-3 py-1.5 text-white hover:bg-slate-800"
            >
              Logout
            </button>
          } @else {
            <a routerLink="/auth/login" class="text-slate-600 hover:text-slate-900">Login</a>
            <a
              routerLink="/auth/register"
              class="rounded-md bg-slate-900 px-3 py-1.5 text-white hover:bg-slate-800"
            >
              Register
            </a>
          }
        </div>
      </nav>
    </header>
  `,
})
export class Navbar {
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
