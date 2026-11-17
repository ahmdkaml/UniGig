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
  template: `
    <div class="mx-auto mt-10 max-w-md rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <h1 class="text-xl font-bold text-slate-900">Create a UniGig account</h1>
      <p class="mt-1 text-sm text-slate-500">Join with your university credentials.</p>

      @if (errorMessage()) {
        <div class="mt-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          <p class="font-medium">{{ errorMessage() }}</p>
          @if (fieldErrorList().length > 0) {
            <ul class="mt-1 list-inside list-disc space-y-0.5 text-xs">
              @for (item of fieldErrorList(); track item) {
                <li>{{ item }}</li>
              }
            </ul>
          }
        </div>
      }

      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="mt-5 space-y-4">
        <div>
          <label for="fullName" class="block text-sm font-medium text-slate-700">Full Name</label>
          <input
            id="fullName"
            type="text"
            formControlName="fullName"
            placeholder="Ahmed Kamal"
            class="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-900 focus:outline-none"
          />
        </div>

        <div>
          <label for="email" class="block text-sm font-medium text-slate-700">Email</label>
          <input
            id="email"
            type="email"
            formControlName="email"
            placeholder="student@alexu.edu.eg"
            class="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-900 focus:outline-none"
          />
        </div>

        <div>
          <label for="password" class="block text-sm font-medium text-slate-700">Password</label>
          <input
            id="password"
            type="password"
            formControlName="password"
            class="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-900 focus:outline-none"
          />
        </div>

        <div>
          <label for="role" class="block text-sm font-medium text-slate-700">Role</label>
          <select
            id="role"
            formControlName="role"
            class="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm focus:border-slate-900 focus:outline-none"
          >
            <option value="STUDENT">Student (Freelancer)</option>
            <option value="CLIENT">Client (Post Gigs)</option>
          </select>
        </div>

        <button
          type="submit"
          [disabled]="form.invalid || isSubmitting()"
          class="w-full rounded-md bg-slate-900 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {{ isSubmitting() ? 'Creating account...' : 'Register' }}
        </button>
      </form>

      <p class="mt-4 text-center text-sm text-slate-600">
        Already have an account?
        <a routerLink="/auth/login" class="font-medium text-slate-900 underline">Sign in</a>
      </p>
    </div>
  `,
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
        this.router.navigate(['/auth/login']);
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
