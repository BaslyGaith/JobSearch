import apiClient from './client'
import type { Today } from '../types'

export const getToday = async (): Promise<Today> => {
  const { data } = await apiClient.get<Today>('/api/today')
  return data
}
