import apiClient from './client'
import type { SourceStatus, SyncResult } from '../types'

export const getSources = async (): Promise<SourceStatus[]> => {
  const { data } = await apiClient.get<SourceStatus[]>('/api/sources')
  return data
}

export const syncSources = async (): Promise<SyncResult> => {
  const { data } = await apiClient.post<SyncResult>('/api/sources/sync')
  return data
}
