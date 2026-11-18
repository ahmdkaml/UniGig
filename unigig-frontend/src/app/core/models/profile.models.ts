export interface FreelancerProfileRequest {
  title: string;
  bio: string | null;
  skills: string[];
  portfolioUrl: string | null;
}

export interface FreelancerProfileResponse {
  userId: number;
  fullName: string;
  title: string;
  bio: string | null;
  skills: string[];
  portfolioUrl: string | null;
  updatedAt: string;
}