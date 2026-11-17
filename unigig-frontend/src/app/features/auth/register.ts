import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';
import { RegisterRequest, UserRole } from '../../core/models/auth.models';
import { ApiError } from '../../core/models/api-error.model';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly fieldErrorList = signal<string[]>([]);

  protected readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['STUDENT' as UserRole, [Validators.required]],
  });

  protected onSubmit(): void {
    if (this.form.invalid || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.fieldErrorList.set([]);

    const payload: RegisterRequest = this.form.getRawValue();

    this.authService.register(payload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigate(['/']);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const apiError = err.error as ApiError | undefined;
        this.errorMessage.set(apiError?.message || 'Registration failed.');

        if (apiError?.fieldErrors) {
          this.fieldErrorList.set(
            Object.entries(apiError.fieldErrors).map(([field, msg]) => `${field}: ${msg}`)
          );
        }
      },
    });
  }
}
