export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  profilePicture?: string;
  provider: 'GOOGLE' | 'MICROSOFT';
  createdAt: string;
}

export interface LinkedInStatus {
  connected: boolean;
  linkedinEmail?: string;
  connectedAt?: string;
  mode: 'MOCK' | 'LIVE';
}

export interface JobSearchPreference {
  id?: string;
  jobTitles: string[];
  locations: string[];
  employmentTypes: string[];
  experienceLevels: string[];
  remotePreference: 'REMOTE' | 'HYBRID' | 'ON_SITE' | 'ANY';
  keywords: string[];
  createdAt?: string;
  updatedAt?: string;
}

export type JobStatus = 'NEW' | 'REVIEWED' | 'INTERESTED' | 'APPLIED' | 'REJECTED' | 'ARCHIVED';

export type EmploymentType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP';

export interface JobOpportunity {
  id: string;
  title: string;
  companyName: string;
  location: string;
  employmentType: string;
  description: string;
  jobUrl: string;
  source: string;
  publicationDate: string;
  recruiterName?: string;
  recruiterEmail?: string;
  recruiterProfileUrl?: string;
  matchScore?: number;
  status: JobStatus;
  saved: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface JobStats {
  newOpportunities: number;
  highMatches: number;
  interested: number;
  applied: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface JobFilters {
  search?: string;
  location?: string;
  company?: string;
  minMatchScore?: number;
  remote?: boolean;
  employmentType?: string;
  status?: JobStatus;
  sortBy?: 'newest' | 'highest_match' | 'company' | 'publication_date';
  page?: number;
  size?: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
