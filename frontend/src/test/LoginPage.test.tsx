import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, it, expect, vi } from 'vitest'
import { LoginPage } from '../pages/LoginPage'
import { AuthProvider } from '../contexts/AuthContext'
import { ToastProvider } from '../contexts/ToastContext'

vi.mock('../api/auth', () => ({
  getCurrentUser: vi.fn().mockResolvedValue(null),
  logout: vi.fn(),
  getGoogleAuthUrl: vi.fn().mockReturnValue('/oauth2/authorization/google'),
  getMicrosoftAuthUrl: vi.fn().mockReturnValue('/oauth2/authorization/microsoft'),
}))

function renderLoginPage(search = '') {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[`/login${search}`]}>
        <AuthProvider>
          <ToastProvider>
            <LoginPage />
          </ToastProvider>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

describe('LoginPage', () => {
  it('renders login heading', () => {
    renderLoginPage()
    expect(screen.getByText(/Welcome back/i)).toBeInTheDocument()
  })

  it('renders Google sign-in button', () => {
    renderLoginPage()
    expect(screen.getByText(/Continue with Google/i)).toBeInTheDocument()
  })

  it('renders Microsoft sign-in button', () => {
    renderLoginPage()
    expect(screen.getByText(/Continue with Microsoft/i)).toBeInTheDocument()
  })

  it('renders security note', () => {
    renderLoginPage()
    expect(screen.getByText(/never store your OAuth credentials/i)).toBeInTheDocument()
  })
})
