import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { GigService } from '../../core/services/gig.service';
import { Category, GigResponse } from '../../core/models/gig.models';

@Component({
  selector: 'app-marketplace',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './marketplace.html',
})
export class Marketplace implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly gigService = inject(GigService);

  protected readonly categories = signal<Category[]>([]);
  protected readonly gigs = signal<GigResponse[]>([]);
  protected readonly currentPage = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly totalElements = signal(0);
  protected readonly isFirstPage = signal(true);
  protected readonly isLastPage = signal(true);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly filterForm = this.fb.group({
    keyword: [''],
    categoryId: [null as number | null],
  });

  ngOnInit(): void {
    this.loadCategories();
    this.loadGigs(0);
  }

  protected loadCategories(): void {
    this.gigService.getCategories().subscribe({
      next: (items) => this.categories.set(items),
      error: () => this.categories.set([]),
    });
  }

  protected loadGigs(page = 0): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.currentPage.set(page);

    const { keyword, categoryId } = this.filterForm.getRawValue();

    this.gigService
      .searchGigs({
        keyword: keyword ?? '',
        categoryId,
        page,
      })
      .subscribe({
        next: (res) => {
          this.gigs.set((res.content ?? []).filter((g) => g.status === 'OPEN'));
          this.totalPages.set(res.totalPages ?? 0);
          this.totalElements.set(res.totalElements ?? 0);
          this.currentPage.set(res.number ?? page);
          this.isFirstPage.set(res.first ?? this.currentPage() === 0);
          this.isLastPage.set(res.last ?? true);
          this.isLoading.set(false);
        },
        error: () => {
          this.gigs.set([]);
          this.errorMessage.set('Failed to load open gigs. Please try again.');
          this.isLoading.set(false);
        },
      });
  }

  protected onSearchSubmit(): void {
    this.loadGigs(0);
  }

  protected onCategoryChange(): void {
    this.loadGigs(0);
  }

  protected clearFilters(): void {
    this.filterForm.reset({ keyword: '', categoryId: null });
    this.loadGigs(0);
  }

  protected goToPreviousPage(): void {
    if (!this.isFirstPage() && this.currentPage() > 0) {
      this.loadGigs(this.currentPage() - 1);
    }
  }

  protected goToNextPage(): void {
    if (!this.isLastPage()) {
      this.loadGigs(this.currentPage() + 1);
    }
  }
}
