import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, it, expect, vi } from 'vitest'
import { JobCard } from '../components/jobs/JobCard'
import { ToastProvider } from '../contexts/ToastContext'
import type { JobOpportunity } from '../types'

vi.mock('../api/jobs', () => ({
  saveJob: vi.fn().mockResolvedValue({}),
  unsaveJob: vi.fn().mockResolvedValue({}),
  updateJobStatus: vi.fn().mockResolvedValue({}),
}))

const mockJob: JobOpportunity = {
  id: 'abc-123',
  title: 'Senior Java Developer',
  companyName: 'Acme Corp',
  location: 'Berlin, Germany',
  employmentType: 'FULL_TIME',
  description: 'We are looking for a Java developer to join our team.',
  jobUrl: 'https://example.com/job/123',
  source: 'Mock',
  publicationDate: '2024-01-15',
  matchScore: 90,
  status: 'NEW',
  isSaved: false,
  createdAt: '2024-01-15T10:00:00Z',
  updatedAt: '2024-01-15T10:00:00Z',
}

function renderJobCard(job = mockJob) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <ToastProvider>
          <JobCard job={job} />
        </ToastProvider>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

describe('JobCard', () => {
  it('renders job title', () => {
    renderJobCard()
    expect(screen.getByText('Senior Java Developer')).toBeInTheDocument()
  })

  it('renders company name', () => {
    renderJobCard()
    expect(screen.getByText('Acme Corp')).toBeInTheDocument()
  })

  it('renders location', () => {
    renderJobCard()
    expect(screen.getByText('Berlin, Germany')).toBeInTheDocument()
  })

  it('renders match score badge', () => {
    renderJobCard()
    expect(screen.getByText('90%')).toBeInTheDocument()
  })

  it('renders View Job link', () => {
    renderJobCard()
    const link = screen.getByRole('link', { name: /view job/i })
    expect(link).toHaveAttribute('href', 'https://example.com/job/123')
    expect(link).toHaveAttribute('target', '_blank')
  })

  it('renders save button when not saved', () => {
    renderJobCard()
    expect(screen.getByTitle('Save job')).toBeInTheDocument()
  })

  it('renders unsave button when saved', () => {
    renderJobCard({ ...mockJob, isSaved: true })
    expect(screen.getByTitle('Unsave job')).toBeInTheDocument()
  })
})
