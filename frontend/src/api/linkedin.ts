import apiClient from './client'
import type { LinkedInStatus } from '../types'

export const getLinkedInStatus = async (): Promise<LinkedInStatus> => {
  const { data } = await apiClient.get<LinkedInStatus>('/api/linkedin/status')
  return data
}

export const connectLinkedIn = (): void => {
  window.location.href = `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/linkedin/connect`
}

export const disconnectLinkedIn = async (): Promise<void> => {
  await apiClient.post('/api/linkedin/disconnect')
}
