import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  FreelancerProfileRequest,
  FreelancerProfileResponse,
} from '../models/profile.models';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/profiles';

  getMyProfile(): Observable<FreelancerProfileResponse> {
    return this.http.get<FreelancerProfileResponse>(`${this.baseUrl}/me`);
  }

  upsertMyProfile(payload: FreelancerProfileRequest): Observable<FreelancerProfileResponse> {
    return this.http.put<FreelancerProfileResponse>(`${this.baseUrl}/me`, payload);
  }

  getProfileById(userId: number): Observable<FreelancerProfileResponse> {
    return this.http.get<FreelancerProfileResponse>(`${this.baseUrl}/${userId}`);
  }
}