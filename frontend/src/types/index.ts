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

// ---------------------------------------------------------------------------
// CV agent
// ---------------------------------------------------------------------------

export interface CvProfile {
  id: string;
  fullName: string;
  headline?: string;
  email?: string;
  phone?: string;
  linkedinUrl?: string;
  location?: string;
  yearsExperience?: number;
  /** The verified fact bank, as a JSON string. */
  factBank: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CvExperienceBlock {
  title: string;
  company: string;
  companyNote?: string;
  location?: string;
  dates: string;
  bullets: string[];
}

export interface CvValueBlock {
  heading: string;
  body: string;
}

export interface CvSkillLine {
  label: string;
  values: string;
}

export interface CvProjectBlock {
  name: string;
  organisation?: string;
  dates?: string;
  bullets: string[];
}

export interface CvEducationLine {
  qualification: string;
  institution: string;
  dates: string;
}

export interface CvDraft {
  language: string;
  tagline: string;
  profile: string;
  experiences: CvExperienceBlock[];
  whatIBring: CvValueBlock[];
  coreSkills: CvSkillLine[];
  selectedProject?: CvProjectBlock | null;
  certifications: string[];
  education: CvEducationLine[];
  footer?: string;
}

export type Coverage = 'STRONG' | 'PARTIAL' | 'ABSENT';

export interface PostingRequirement {
  text: string;
  essential: boolean;
  coverage: Coverage;
  interviewAdvice?: string;
}

export interface PostingAnalysis {
  targetRole: string;
  targetCompany?: string;
  jobFamily: string;
  accentColor: string;
  requirements: PostingRequirement[];
  tags: string[];
}

export interface CvDocument {
  id: string;
  language: string;
  targetRole: string;
  targetCompany?: string;
  jobFamily: string;
  accentColor: string;
  generatedBy: string;
  createdAt: string;
  draft: CvDraft;
  gaps: PostingRequirement[];
}

export interface CvGenerationResponse {
  analysis: PostingAnalysis;
  documents: CvDocument[];
}
