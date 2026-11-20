import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GigService } from '../../core/services/gig.service';
import { CategoryItem, GigResponse } from '../../core/models/gig.models';

@Component({
  selector: 'app-marketplace-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './marketplace-page.component.html',
})
export class MarketplacePageComponent implements OnInit {
  private readonly gigService = inject(GigService);

  keyword = '';
  selectedCategoryId: number | null = null;
  categories: CategoryItem[] = [];

  gigs: GigResponse[] = [];
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  isFirstPage = true;
  isLastPage = true;
  loading = false;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.loadCategories();
    this.loadGigs(0);
  }

  loadCategories(): void {
    this.gigService.getCategories().subscribe({
      next: (items) => {
        this.categories = items;
      },
      error: () => {
        this.categories = [];
      },
    });
  }

  loadGigs(page = 0): void {
    this.loading = true;
    this.errorMessage = null;
    this.currentPage = page;

    this.gigService
      .searchGigs({
        keyword: this.keyword,
        categoryId: this.selectedCategoryId,
        page: this.currentPage,
      })
      .subscribe({
        next: (res) => {
          this.gigs = (res.content ?? []).filter((g) => g.status === 'OPEN');
          this.totalPages = res.totalPages ?? 0;
          this.totalElements = res.totalElements ?? 0;
          this.currentPage = res.number ?? page;
          this.isFirstPage = res.first ?? this.currentPage === 0;
          this.isLastPage = res.last ?? true;
          this.loading = false;
        },
        error: () => {
          this.gigs = [];
          this.errorMessage = 'Failed to load open gigs. Please try again.';
          this.loading = false;
        },
      });
  }

  onSearchSubmit(): void {
    this.loadGigs(0);
  }

  onCategoryChange(): void {
    this.loadGigs(0);
  }

  clearFilters(): void {
    this.keyword = '';
    this.selectedCategoryId = null;
    this.loadGigs(0);
  }

  goToPreviousPage(): void {
    if (!this.isFirstPage && this.currentPage > 0) {
      this.loadGigs(this.currentPage - 1);
    }
  }

  goToNextPage(): void {
    if (!this.isLastPage) {
      this.loadGigs(this.currentPage + 1);
    }
  }
}
