import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { GigService } from '../../core/gigs/gig.service';
import { Category, GigRequest } from '../../core/models/gig.models';
import { ApiError } from '../../core/models/api-error.model';

@Component({
  selector: 'app-post-gig',
  imports: [ReactiveFormsModule],
  templateUrl: './post-gig.html',
})
export class PostGig implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly gigService = inject(GigService);

  protected readonly categories = signal<Category[]>([]);
  protected readonly categoriesError = signal<string | null>(null);
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly fieldErrorList = signal<string[]>([]);
  protected readonly successMessage = signal<string | null>(null);

  protected readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.required, Validators.maxLength(5000)]],
    categoryId: [null as number | null, [Validators.required]],
  });

  ngOnInit(): void {
    this.gigService.getCategories().subscribe({
      next: (list) => this.categories.set(list),
      error: () => this.categoriesError.set('Could not load categories. Please try again later.'),
    });
  }

  protected onSubmit(): void {
    if (this.form.invalid || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.fieldErrorList.set([]);
    this.successMessage.set(null);

    const value = this.form.getRawValue();
    const payload: GigRequest = {
      title: value.title!.trim(),
      description: value.description!.trim(),
      categoryId: value.categoryId!,
    };

    this.gigService.postGig(payload).subscribe({
      next: (gig) => {
        this.isSubmitting.set(false);
        this.successMessage.set(`Your gig "${gig.title}" is now open for bids.`);
        this.form.reset();
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const apiError = err.error as ApiError | undefined;

        if (err.status === 403) {
          this.errorMessage.set('Only client accounts can post gigs.');
        } else {
          this.errorMessage.set(apiError?.message || 'Could not post the gig.');
        }

        if (apiError?.fieldErrors) {
          this.fieldErrorList.set(
            Object.entries(apiError.fieldErrors).map(([field, msg]) => `${field}: ${msg}`)
          );
        }
      },
    });
  }
}
