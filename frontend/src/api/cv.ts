import apiClient from './client'
import type { CvDocument, CvGenerationResponse, CvProfile } from '../types'

export const getCvProfile = async (): Promise<CvProfile> => {
  const { data } = await apiClient.get<CvProfile>('/api/cv/profile')
  return data
}

export const updateFactBank = async (factBank: string): Promise<CvProfile> => {
  const { data } = await apiClient.put<CvProfile>('/api/cv/profile', { factBank })
  return data
}

export const generateCv = async (
  posting: string,
  jobOpportunityId?: string
): Promise<CvGenerationResponse> => {
  const { data } = await apiClient.post<CvGenerationResponse>('/api/cv/generate', {
    posting,
    jobOpportunityId,
  })
  return data
}

export const listCvs = async (): Promise<CvDocument[]> => {
  const { data } = await apiClient.get<CvDocument[]>('/api/cv')
  return data
}

export const deleteCv = async (id: string): Promise<void> => {
  await apiClient.delete(`/api/cv/${id}`)
}

/**
 * Downloads the .docx through the API client so the session cookie travels with
 * the request, then hands the blob to the browser.
 */
export const downloadCv = async (id: string, filename: string): Promise<void> => {
  const response = await apiClient.get(`/api/cv/${id}/download`, { responseType: 'blob' })
  const url = window.URL.createObjectURL(response.data as Blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
