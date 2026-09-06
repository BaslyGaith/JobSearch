import apiClient from './client'
import type { JobSearchPreference } from '../types'

export const getPreferences = async (): Promise<JobSearchPreference | null> => {
  try {
    const { data } = await apiClient.get<JobSearchPreference>('/api/preferences')
    return data
  } catch (err: unknown) {
    if ((err as { response?: { status: number } }).response?.status === 204) return null
    throw err
  }
}

export const savePreferences = async (preferences: Omit<JobSearchPreference, 'id' | 'createdAt' | 'updatedAt'>): Promise<JobSearchPreference> => {
  const { data } = await apiClient.put<JobSearchPreference>('/api/preferences', preferences)
  return data
}

export const deletePreferences = async (): Promise<void> => {
  await apiClient.delete('/api/preferences')
}
