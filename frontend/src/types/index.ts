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
  generationId: string;
  title: string;
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

export interface FactBankBullet {
  text: string;
  tags: string[];
}

export interface FactBankExperience {
  title: string;
  company: string;
  companyNote?: string | null;
  location?: string;
  startDate: string;
  endDate?: string | null;
  bullets: FactBankBullet[];
}

export interface FactBankProject {
  name: string;
  organisation?: string;
  dates?: string;
  bullets: string[];
  tags: string[];
}

export interface FactBankEducation {
  qualification: string;
  institution: string;
  dates: string;
}

/** The verified facts a CV may be built from - nothing else can reach a CV. */
export interface FactBank {
  identity: {
    fullName: string;
    email: string;
    alternateEmail?: string;
    phone: string;
    linkedin: string;
    location: string;
    yearsExperience: number;
    languages: string[];
  };
  constraints: {
    forbiddenTitleWords: string[];
    allowedFigures: string[];
    springBootAngularOnlyIn?: string;
  };
  experiences: FactBankExperience[];
  projects: FactBankProject[];
  education: FactBankEducation[];
  academicProjects: string[];
  certifications: string[];
  skills: Record<string, string[]>;
  skillTags: Record<string, string[]>;
  blocklist: string[];
}

/** What needs the user's attention right now. */
export interface Today {
  publishedToday: number;
  awaitingReview: number;
  strongMatches: number;
  interested: number;
  applied: number;
  cvsPrepared: number;
  linkedInConnected: boolean;
  worthReviewing: JobOpportunity[];
}

export interface SourceStatus {
  name: string;
  enabled: boolean;
  storedOpportunities: number;
}

export interface SyncResult {
  added: number;
  duplicates: number;
  sourcesUsed: string[];
  failures: string[];
}
