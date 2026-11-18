import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProfileService } from './profile.service';
import {
  FreelancerProfileRequest,
  FreelancerProfileResponse,
} from '../models/profile.models';

describe('ProfileService', () => {
  let service: ProfileService;
  let httpMock: HttpTestingController;

  const mockProfileResponse: FreelancerProfileResponse = {
    userId: 1,
    fullName: 'Ahmed Yousry',
    title: 'Backend & Angular Developer',
    bio: 'Building campus apps.',
    skills: ['Java', 'Angular'],
    portfolioUrl: 'https://github.com/example',
    updatedAt: '2026-11-18T12:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideZonelessChangeDetection(), provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ProfileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getMyProfile()', () => {
    it('getMyProfile_existingProfile_sendsGetAndReturnsProfile', () => {
      // Arrange
      let actualResponse: FreelancerProfileResponse | undefined;

      // Act
      service.getMyProfile().subscribe((res) => (actualResponse = res));
      const req = httpMock.expectOne('/api/profiles/me');
      req.flush(mockProfileResponse);

      // Assert
      expect(req.request.method).toBe('GET');
      expect(actualResponse).toEqual(mockProfileResponse);
    });

    it('getMyProfile_profileNotCreatedYet_propagates404Error', () => {
      // Arrange
      let actualError: HttpErrorResponse | undefined;

      // Act
      service.getMyProfile().subscribe({
        next: () => fail('Expected 404 error, but request succeeded'),
        error: (err: HttpErrorResponse) => (actualError = err),
      });
      const req = httpMock.expectOne('/api/profiles/me');
      req.flush(
        { status: 404, message: 'Freelancer profile not found' },
        { status: 404, statusText: 'Not Found' }
      );

      // Assert
      expect(req.request.method).toBe('GET');
      expect(actualError?.status).toBe(404);
    });
  });

  describe('upsertMyProfile()', () => {
    it('upsertMyProfile_validPayload_sendsPutWithBodyAndReturnsSavedProfile', () => {
      // Arrange
      const payload: FreelancerProfileRequest = {
        title: 'Backend & Angular Developer',
        bio: 'Building campus apps.',
        skills: ['Java', 'Angular'],
        portfolioUrl: 'https://github.com/example',
      };
      let actualResponse: FreelancerProfileResponse | undefined;

      // Act
      service.upsertMyProfile(payload).subscribe((res) => (actualResponse = res));
      const req = httpMock.expectOne('/api/profiles/me');
      req.flush(mockProfileResponse);

      // Assert
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(payload);
      expect(actualResponse).toEqual(mockProfileResponse);
    });

    it('upsertMyProfile_clientRoleForbidden_propagates403Error', () => {
      // Arrange
      const payload: FreelancerProfileRequest = {
        title: 'Developer',
        bio: null,
        skills: [],
        portfolioUrl: null,
      };
      let actualError: HttpErrorResponse | undefined;

      // Act
      service.upsertMyProfile(payload).subscribe({
        next: () => fail('Expected 403 error, but request succeeded'),
        error: (err: HttpErrorResponse) => (actualError = err),
      });
      const req = httpMock.expectOne('/api/profiles/me');
      req.flush(
        { status: 403, message: 'Only freelancers can create or update a freelancer profile' },
        { status: 403, statusText: 'Forbidden' }
      );

      // Assert
      expect(req.request.method).toBe('PUT');
      expect(actualError?.status).toBe(403);
    });
  });

  describe('getProfileById()', () => {
    it('getProfileById_validUserId_sendsGetToUserEndpointAndReturnsProfile', () => {
      // Arrange
      const targetUserId = 7;
      let actualResponse: FreelancerProfileResponse | undefined;

      // Act
      service.getProfileById(targetUserId).subscribe((res) => (actualResponse = res));
      const req = httpMock.expectOne(`/api/profiles/${targetUserId}`);
      req.flush({ ...mockProfileResponse, userId: targetUserId });

      // Assert
      expect(req.request.method).toBe('GET');
      expect(actualResponse?.userId).toBe(targetUserId);
    });
  });
});