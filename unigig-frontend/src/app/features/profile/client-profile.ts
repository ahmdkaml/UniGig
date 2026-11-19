import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-client-profile',
  template: `
    <div class="mx-auto mt-6 max-w-3xl rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-xl font-bold text-slate-900">Client Profile</h1>
          <p class="mt-1 text-sm text-slate-500">{{ authService.currentUser()?.email }}</p>
        </div>
        <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-800">
          CLIENT
        </span>
      </div>
      <p class="mt-4 text-sm text-slate-600">
        Client profile awaits implementation.
      </p>
    </div>
  `,
})
export class ClientProfile {
  protected readonly authService = inject(AuthService);
}