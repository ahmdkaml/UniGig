import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category, GigRequest, GigResponse } from '../models/gig.models';

@Injectable({ providedIn: 'root' })
export class GigService {
  private readonly http = inject(HttpClient);

  private readonly gigsUrl = '/api/gigs';
  private readonly categoriesUrl = '/api/categories';

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.categoriesUrl);
  }

  postGig(payload: GigRequest): Observable<GigResponse> {
    return this.http.post<GigResponse>(this.gigsUrl, payload);
  }
}
