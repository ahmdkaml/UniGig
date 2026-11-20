/**
 * ============================================================================
 * STRICT CONTRACT: FRONTEND MODELS <-> BACKEND DTOs
 * ============================================================================
 * Every interface and type in this file MUST strictly mirror the corresponding
 * Java DTO record / enum in `unigig-backend` (`com.unigig.backend.dto` &
 * `com.unigig.backend.entity`).
 * ============================================================================
 */

export type GigStatus = 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface CategoryItem {
  id: number;
  name: string;
}

export interface GigResponse {
  id: number;
  title: string;
  description: string;
  budget: number | null;
  status: GigStatus;
  categoryId: number | null;
  categoryName: string | null;
  clientId: number | null;
  clientName: string | null;
  createdAt: string | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface GigSearchParams {
  keyword?: string;
  categoryId?: number | null;
  page?: number;
}