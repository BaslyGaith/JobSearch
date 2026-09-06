import apiClient from './client'
import type { JobFilters, JobOpportunity, JobStats, JobStatus, PagedResponse } from '../types'

export const getJobs = async (filters: JobFilters = {}): Promise<PagedResponse<JobOpportunity>> => {
  const params = new URLSearchParams()
  if (filters.search) params.append('search', filters.search)
  if (filters.location) params.append('location', filters.location)
  if (filters.company) params.append('company', filters.company)
  if (filters.employmentType) params.append('employmentType', filters.employmentType)
  if (filters.status) params.append('status', filters.status)
  if (filters.minMatchScore !== undefined) params.append('minMatchScore', String(filters.minMatchScore))
  if (filters.remote !== undefined) params.append('remote', String(filters.remote))
  if (filters.sortBy) params.append('sortBy', filters.sortBy)
  params.append('page', String(filters.page ?? 0))
  params.append('size', String(filters.size ?? 12))

  const { data } = await apiClient.get<PagedResponse<JobOpportunity>>(`/api/jobs?${params}`)
  return data
}

export const getJobStats = async (): Promise<JobStats> => {
  const { data } = await apiClient.get<JobStats>('/api/jobs/stats')
  return data
}

export const getJobById = async (id: string): Promise<JobOpportunity> => {
  const { data } = await apiClient.get<JobOpportunity>(`/api/jobs/${id}`)
  return data
}

export const saveJob = async (id: string): Promise<JobOpportunity> => {
  const { data } = await apiClient.post<JobOpportunity>(`/api/jobs/${id}/save`)
  return data
}

export const unsaveJob = async (id: string): Promise<void> => {
  await apiClient.delete(`/api/jobs/${id}/save`)
}

export const updateJobStatus = async (id: string, status: JobStatus): Promise<JobOpportunity> => {
  const { data } = await apiClient.post<JobOpportunity>(`/api/jobs/${id}/status`, { status })
  return data
}
