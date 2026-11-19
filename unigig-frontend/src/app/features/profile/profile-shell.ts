import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { FreelancerProfile } from './freelancer-profile';
import { ClientProfile } from './client-profile';

@Component({
  selector: 'app-profile-shell',
  imports: [FreelancerProfile, ClientProfile],
  template: `
    @if (authService.currentUser()?.role === 'CLIENT') {
      <app-client-profile />
    } @else {
      <app-freelancer-profile />
    }
  `,
})
export class ProfileShell {
  protected readonly authService = inject(AuthService);
}