import apiClient from './client'
import type { User } from '../types'

export const getCurrentUser = async (): Promise<User> => {
  const { data } = await apiClient.get<User>('/api/auth/me')
  return data
}

export const logout = async (): Promise<void> => {
  await apiClient.post('/api/auth/logout')
}

export const getGoogleAuthUrl = (): string =>
  `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/oauth2/authorization/google`

export const getMicrosoftAuthUrl = (): string =>
  `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/oauth2/authorization/microsoft`
