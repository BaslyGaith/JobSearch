import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { TagInput } from '../components/ui/TagInput'

describe('TagInput', () => {
  it('renders existing tags', () => {
    render(<TagInput tags={['Java', 'Python']} onChange={vi.fn()} />)
    expect(screen.getByText('Java')).toBeInTheDocument()
    expect(screen.getByText('Python')).toBeInTheDocument()
  })

  it('adds a tag on Enter', () => {
    const onChange = vi.fn()
    render(<TagInput tags={[]} onChange={onChange} />)
    const input = screen.getByRole('textbox')
    fireEvent.change(input, { target: { value: 'Spring Boot' } })
    fireEvent.keyDown(input, { key: 'Enter' })
    expect(onChange).toHaveBeenCalledWith(['Spring Boot'])
  })

  it('adds a tag on comma', () => {
    const onChange = vi.fn()
    render(<TagInput tags={[]} onChange={onChange} />)
    const input = screen.getByRole('textbox')
    fireEvent.change(input, { target: { value: 'Docker,' } })
    fireEvent.keyDown(input, { key: ',' })
    expect(onChange).toHaveBeenCalledWith(['Docker'])
  })

  it('removes a tag when X is clicked', () => {
    const onChange = vi.fn()
    render(<TagInput tags={['Java']} onChange={onChange} />)
    const removeBtn = screen.getByTitle(/remove java/i)
    fireEvent.click(removeBtn)
    expect(onChange).toHaveBeenCalledWith([])
  })

  it('removes last tag on Backspace when input is empty', () => {
    const onChange = vi.fn()
    render(<TagInput tags={['Java', 'Python']} onChange={onChange} />)
    const input = screen.getByRole('textbox')
    fireEvent.keyDown(input, { key: 'Backspace' })
    expect(onChange).toHaveBeenCalledWith(['Java'])
  })

  it('does not add duplicate tags', () => {
    const onChange = vi.fn()
    render(<TagInput tags={['Java']} onChange={onChange} />)
    const input = screen.getByRole('textbox')
    fireEvent.change(input, { target: { value: 'Java' } })
    fireEvent.keyDown(input, { key: 'Enter' })
    expect(onChange).not.toHaveBeenCalled()
  })
})
