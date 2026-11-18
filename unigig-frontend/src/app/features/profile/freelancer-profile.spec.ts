import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { FreelancerProfile } from './freelancer-profile';
import { ProfileService } from '../../core/services/profile.service';
import { FreelancerProfileResponse } from '../../core/models/profile.models';

describe('FreelancerProfileComponent', () => {
  let fixture: ComponentFixture<FreelancerProfile>;
  let profileServiceSpy: jasmine.SpyObj<ProfileService>;
  let routeParams: Record<string, string>;

  const existingProfile: FreelancerProfileResponse = {
    userId: 1,
    fullName: 'Ahmed Yousry',
    title: 'Software Engineering Student',
    bio: 'Experienced in Spring Boot and Angular.',
    skills: ['Spring Boot', 'Angular'],
    portfolioUrl: 'https://github.com/example',
    updatedAt: '2026-11-18T12:00:00Z',
  };

  beforeEach(async () => {
    routeParams = {};
    profileServiceSpy = jasmine.createSpyObj<ProfileService>('ProfileService', [
      'getMyProfile',
      'upsertMyProfile',
      'getProfileById',
    ]);

    await TestBed.configureTestingModule({
      imports: [FreelancerProfile],
      providers: [
        provideZonelessChangeDetection(),
        { provide: ProfileService, useValue: profileServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              get paramMap() {
                return convertToParamMap(routeParams);
              },
            },
          },
        },
      ],
    }).compileComponents();
  });

  function setInputValue(selector: string, value: string): void {
    const input = fixture.nativeElement.querySelector(selector) as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function findButtonByText(label: string): HTMLButtonElement | undefined {
    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('button')
    ) as HTMLButtonElement[];
    return buttons.find((btn) => btn.textContent?.trim() === label);
  }

  describe('Initialization & Route Modes', () => {
    it('ngOnInit_ownProfileExists_rendersReadViewWithEditButton', async () => {
      // Arrange
      profileServiceSpy.getMyProfile.and.returnValue(of(existingProfile));

      // Act
      fixture = TestBed.createComponent(FreelancerProfile);
      await fixture.whenStable();

      // Assert
      const host = fixture.nativeElement as HTMLElement;
      expect(profileServiceSpy.getMyProfile).toHaveBeenCalledTimes(1);
      expect(host.textContent).toContain('Ahmed Yousry');
      expect(host.textContent).toContain('Software Engineering Student');
      expect(host.textContent).toContain('Spring Boot');
      expect(findButtonByText('Edit Profile')).toBeDefined();
    });

    it('ngOnInit_ownProfileReturns404_opensCreateFormWithoutErrorAlert', async () => {
      // Arrange
      profileServiceSpy.getMyProfile.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 404 }))
      );

      // Act
      fixture = TestBed.createComponent(FreelancerProfile);
      await fixture.whenStable();

      // Assert
      const host = fixture.nativeElement as HTMLElement;
      expect(host.querySelector('form')).not.toBeNull();
      expect(host.textContent).toContain('Create Freelancer Profile');
      expect(host.querySelector('.bg-red-50')).toBeNull();
    });

    it('ngOnInit_ownProfileServerFailure_displaysErrorBanner', async () => {
      // Arrange
      profileServiceSpy.getMyProfile.and.returnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 500,
              error: { message: 'Internal server error' },
            })
        )
      );

      // Act
      fixture = TestBed.createComponent(FreelancerProfile);
      await fixture.whenStable();

      // Assert
      const host = fixture.nativeElement as HTMLElement;
      expect(host.textContent).toContain('Internal server error');
    });

    it('ngOnInit_publicUserIdParamPresent_rendersReadOnlyProfileWithoutEditButton', async () => {
      // Arrange
      routeParams = { userId: '5' };
      profileServiceSpy.getProfileById.and.returnValue(
        of({ ...existingProfile, userId: 5 })
      );

      // Act
      fixture = TestBed.createComponent(FreelancerProfile);
      await fixture.whenStable();

      // Assert
      const host = fixture.nativeElement as HTMLElement;
      expect(profileServiceSpy.getProfileById).toHaveBeenCalledWith(5);
      expect(profileServiceSpy.getMyProfile).not.toHaveBeenCalled();
      expect(host.textContent).toContain('Ahmed Yousry');
      expect(findButtonByText('Edit Profile')).toBeUndefined();
    });
  });

  describe('Edit Mode & Form Submission', () => {
    it('enableEditAndCancel_existingProfile_prepopulatesFieldsAndRevertsOnCancel', async () => {
      // Arrange
      profileServiceSpy.getMyProfile.and.returnValue(of(existingProfile));
      fixture = TestBed.createComponent(FreelancerProfile);
      await fixture.whenStable();

      // Act: Enter edit mode
      findButtonByText('Edit Profile')?.click();
      await fixture.whenStable();

      const titleInput = fixture.nativeElement.querySelector('#title') as HTMLInputElement;
      expect(titleInput.value).toBe('Software Engineering Student');

      // Act: Cancel edit mode
      findButtonByText('Cancel')?.click();
      await fixture.whenStable();

      // Assert
      expect(fixture.nativeElement.querySelector('form')).toBeNull();
      expect(findButtonByText('Edit Profile')).toBeDefined();
    });

    it('onSubmit_validInput_normalizesBlankFieldsToNullAndUpdatesView', async () => {
      // Arrange
      profileServiceSpy.getMyProfile.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 404 }))
      );
      const savedResponse: FreelancerProfileResponse = {
        userId: 1,
        fullName: 'Ahmed Yousry',
        title: 'Full-Stack Developer',
        bio: null,
        skills: [],
        portfolioUrl: null,
        updatedAt: '2026-11-18T13:00:00Z',
      };
      profileServiceSpy.upsertMyProfile.and.returnValue(of(savedResponse));

      fixture = TestBed.createComponent(FreelancerProfile);
      await fixture.whenStable();

      // Act
      setInputValue('#title', '  Full-Stack Developer  ');
      setInputValue('#bio', '   ');
      setInputValue('#portfolioUrl', '   ');
      await fixture.whenStable();

      const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
      form.dispatchEvent(new Event('submit'));
      await fixture.whenStable();

      // Assert
      expect(profileServiceSpy.upsertMyProfile).toHaveBeenCalledWith({
        title: 'Full-Stack Developer',
        bio: null,
        skills: [],
        portfolioUrl: null,
      });
      const host = fixture.nativeElement as HTMLElement;
      expect(host.querySelector('form')).toBeNull();
      expect(host.textContent).toContain('Profile saved successfully.');
      expect(host.textContent).toContain('Full-Stack Developer');
    });

    it('onSubmit_backendReturns400ValidationError_rendersFieldErrorInline', async () => {
      // Arrange
      profileServiceSpy.getMyProfile.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 404 }))
      );
      profileServiceSpy.upsertMyProfile.and.returnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 400,
              error: {
                message: 'Validation failed',
                fieldErrors: { portfolioUrl: 'Portfolio URL must be a valid URL' },
              },
            })
        )
      );

      fixture = TestBed.createComponent(FreelancerProfile);
      await fixture.whenStable();

      // Act
      setInputValue('#title', 'Backend Developer');
      await fixture.whenStable();
      const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
      form.dispatchEvent(new Event('submit'));
      await fixture.whenStable();

      // Assert
      const host = fixture.nativeElement as HTMLElement;
      expect(host.textContent).toContain('Validation failed');
      expect(host.textContent).toContain('Portfolio URL must be a valid URL');
    });

    it('onSubmit_clientAccountReturns403_displaysRoleRestrictionError', async () => {
      // Arrange
      profileServiceSpy.getMyProfile.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 404 }))
      );
      profileServiceSpy.upsertMyProfile.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 403 }))
      );

      fixture = TestBed.createComponent(FreelancerProfile);
      await fixture.whenStable();

      // Act
      setInputValue('#title', 'Backend Developer');
      await fixture.whenStable();
      const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
      form.dispatchEvent(new Event('submit'));
      await fixture.whenStable();

      // Assert
      const host = fixture.nativeElement as HTMLElement;
      expect(host.textContent).toContain(
        'Only freelancer accounts can create or edit a freelancer profile.'
      );
    });
  });

  describe('Technical Skills Management', () => {
    it('addAndRemoveSkill_preventsCaseInsensitiveDuplicatesAndRemovesChip', async () => {
      // Arrange
      profileServiceSpy.getMyProfile.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 404 }))
      );
      fixture = TestBed.createComponent(FreelancerProfile);
      await fixture.whenStable();

      // Act: Add "TypeScript" twice with different casing
      setInputValue('#skillInput', 'TypeScript');
      await fixture.whenStable();
      findButtonByText('Add')?.click();
      await fixture.whenStable();

      setInputValue('#skillInput', 'typescript');
      await fixture.whenStable();
      findButtonByText('Add')?.click();
      await fixture.whenStable();

      // Assert: Only 1 chip rendered
      let removeButtons = fixture.nativeElement.querySelectorAll(
        'button[aria-label="Remove skill"]'
      );
      expect(removeButtons.length).toBe(1);
      expect(fixture.nativeElement.textContent).toContain('Technical Skills (1/25)');

      // Act: Remove the chip
      (removeButtons[0] as HTMLButtonElement).click();
      await fixture.whenStable();

      // Assert: 0 chips remaining
      removeButtons = fixture.nativeElement.querySelectorAll(
        'button[aria-label="Remove skill"]'
      );
      expect(removeButtons.length).toBe(0);
    });
  });
});