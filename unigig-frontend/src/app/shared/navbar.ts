import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
})
export class Navbar {
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
