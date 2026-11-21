import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Category,
  GigRequest,
  GigResponse,
  GigSearchParams,
  PageResponse,
} from '../models/gig.models';

@Injectable({ providedIn: 'root' })
export class GigService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gigs';
  private readonly categoriesUrl = '/api/categories';

  searchGigs(params: GigSearchParams = {}): Observable<PageResponse<GigResponse>> {
    let httpParams = new HttpParams().set('page', String(params.page ?? 0));

    const trimmedKeyword = params.keyword?.trim();
    if (trimmedKeyword) {
      httpParams = httpParams.set('keyword', trimmedKeyword);
    }

    if (params.categoryId != null) {
      httpParams = httpParams.set('categoryId', String(params.categoryId));
    }

    return this.http.get<PageResponse<GigResponse>>(this.baseUrl, { params: httpParams });
  }

  postGig(payload: GigRequest): Observable<GigResponse> {
    return this.http.post<GigResponse>(this.baseUrl, payload);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.categoriesUrl);
  }
}
