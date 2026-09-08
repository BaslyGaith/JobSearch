import apiClient from './client'
import type { Application, ApplicationStatus } from '../types'

export const listApplications = async (): Promise<Application[]> => {
  const { data } = await apiClient.get<Application[]>('/api/applications')
  return data
}

export const getApplication = async (id: string): Promise<Application> => {
  const { data } = await apiClient.get<Application>(`/api/applications/${id}`)
  return data
}

/** Assembles the pack for an opportunity, or returns the existing one untouched. */
export const prepareApplication = async (jobId: string): Promise<Application> => {
  const { data } = await apiClient.post<Application>(`/api/applications/prepare/${jobId}`)
  return data
}

export const updateApplication = async (
  id: string,
  changes: {
    recipientName?: string
    recipientEmail?: string
    subject: string
    body: string
    cvDocumentId?: string
  }
): Promise<Application> => {
  const { data } = await apiClient.put<Application>(`/api/applications/${id}`, changes)
  return data
}

export const markApplicationSent = async (id: string): Promise<Application> => {
  const { data } = await apiClient.post<Application>(`/api/applications/${id}/sent`)
  return data
}

export const changeApplicationStatus = async (
  id: string,
  status: ApplicationStatus,
  note?: string
): Promise<Application> => {
  const { data } = await apiClient.post<Application>(`/api/applications/${id}/status`, { status, note })
  return data
}

export const deleteApplication = async (id: string): Promise<void> => {
  await apiClient.delete(`/api/applications/${id}`)
}
