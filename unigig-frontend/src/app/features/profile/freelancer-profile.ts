import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ProfileService } from '../../core/services/profile.service';
import {
  FreelancerProfileRequest,
  FreelancerProfileResponse,
} from '../../core/models/profile.models';
import { ApiError } from '../../core/models/api-error.model';

@Component({
  selector: 'app-freelancer-profile',
  imports: [ReactiveFormsModule],
  templateUrl: './freelancer-profile.html',
})
export class FreelancerProfile implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly route = inject(ActivatedRoute);

  protected readonly profile = signal<FreelancerProfileResponse | null>(null);
  protected readonly isOwnProfile = signal(true);
  protected readonly isLoading = signal(true);
  protected readonly isEditing = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly fieldErrors = signal<Record<string, string>>({});
  protected readonly skills = signal<string[]>([]);
  protected readonly skillInput = signal('');

  protected readonly canAddSkill = computed(
    () => this.skillInput().trim().length > 0 && this.skills().length < 25
  );

  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    bio: ['', [Validators.maxLength(2000)]],
    portfolioUrl: ['', [Validators.maxLength(255), Validators.pattern(/^https?:\/\/.+/)]],
  });

  ngOnInit(): void {
    const userIdParam = this.route.snapshot.paramMap.get('userId');
    if (userIdParam) {
      this.isOwnProfile.set(false);
      this.loadPublicProfile(Number(userIdParam));
    } else {
      this.isOwnProfile.set(true);
      this.loadOwnProfile();
    }
  }

  protected enableEdit(): void {
    const current = this.profile();
    if (current) {
      this.form.patchValue({
        title: current.title,
        bio: current.bio ?? '',
        portfolioUrl: current.portfolioUrl ?? '',
      });
      this.skills.set([...current.skills]);
    }
    this.successMessage.set(null);
    this.errorMessage.set(null);
    this.fieldErrors.set({});
    this.isEditing.set(true);
  }

  protected cancelEdit(): void {
    this.isEditing.set(false);
    this.errorMessage.set(null);
    this.fieldErrors.set({});
  }

  protected addSkill(event?: Event): void {
    event?.preventDefault();
    const trimmed = this.skillInput().trim();
    if (!trimmed || trimmed.length > 50 || this.skills().length >= 25) return;

    const exists = this.skills().some((s) => s.toLowerCase() === trimmed.toLowerCase());
    if (!exists) {
      this.skills.update((list) => [...list, trimmed]);
    }
    this.skillInput.set('');
  }

  protected removeSkill(skillToRemove: string): void {
    this.skills.update((list) => list.filter((s) => s !== skillToRemove));
  }

  protected onSubmit(): void {
    if (this.form.invalid || this.isSaving()) return;

    this.isSaving.set(true);
    this.successMessage.set(null);
    this.errorMessage.set(null);
    this.fieldErrors.set({});

    const raw = this.form.getRawValue();
    const payload: FreelancerProfileRequest = {
      title: raw.title.trim(),
      bio: raw.bio.trim() ? raw.bio.trim() : null,
      skills: this.skills(),
      portfolioUrl: raw.portfolioUrl.trim() ? raw.portfolioUrl.trim() : null,
    };

    this.profileService.upsertMyProfile(payload).subscribe({
      next: (saved) => {
        this.profile.set(saved);
        this.skills.set([...saved.skills]);
        this.isSaving.set(false);
        this.isEditing.set(false);
        this.successMessage.set('Profile saved successfully.');
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const apiError = err.error as ApiError | undefined;
        if (err.status === 403) {
          this.errorMessage.set('Only freelancer accounts can create or edit a freelancer profile.');
        } else {
          this.errorMessage.set(apiError?.message || 'Failed to save profile.');
        }
        if (apiError?.fieldErrors) {
          this.fieldErrors.set(apiError.fieldErrors);
        }
      },
    });
  }

  private loadOwnProfile(): void {
    this.profileService.getMyProfile().subscribe({
      next: (data) => {
        this.profile.set(data);
        this.skills.set([...data.skills]);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        if (err.status === 404) {
          this.isEditing.set(true);
        } else {
          const apiError = err.error as ApiError | undefined;
          this.errorMessage.set(apiError?.message || 'Could not load your profile.');
        }
      },
    });
  }

  private loadPublicProfile(userId: number): void {
    this.profileService.getProfileById(userId).subscribe({
      next: (data) => {
        this.profile.set(data);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        const apiError = err.error as ApiError | undefined;
        this.errorMessage.set(apiError?.message || 'Freelancer profile not found.');
      },
    });
  }
}