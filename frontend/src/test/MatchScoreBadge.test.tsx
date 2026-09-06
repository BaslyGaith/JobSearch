import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { MatchScoreBadge } from '../components/jobs/MatchScoreBadge'

describe('MatchScoreBadge', () => {
  it('renders score value', () => {
    render(<MatchScoreBadge score={90} />)
    expect(screen.getByText('90% match')).toBeInTheDocument()
  })

  it('applies green ring for score >= 85', () => {
    const { container } = render(<MatchScoreBadge score={85} />)
    expect(container.firstChild).toHaveClass('ring-green-200')
  })

  it('applies amber ring for score 70-84', () => {
    const { container } = render(<MatchScoreBadge score={75} />)
    expect(container.firstChild).toHaveClass('ring-amber-200')
  })

  it('applies gray ring for score < 70', () => {
    const { container } = render(<MatchScoreBadge score={60} />)
    expect(container.firstChild).toHaveClass('ring-slate-200')
  })

  it('returns null when score is undefined', () => {
    const { container } = render(<MatchScoreBadge score={undefined} />)
    expect(container.firstChild).toBeNull()
  })
})
