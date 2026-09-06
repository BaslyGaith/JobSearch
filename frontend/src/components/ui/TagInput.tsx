import { useState, useRef, KeyboardEvent } from 'react'
import { X } from 'lucide-react'
import { clsx } from 'clsx'

interface TagInputProps {
  label?: string
  placeholder?: string
  tags: string[]
  onChange: (tags: string[]) => void
  suggestions?: string[]
  className?: string
}

export function TagInput({ label, placeholder, tags, onChange, suggestions = [], className }: TagInputProps) {
  const [inputValue, setInputValue] = useState('')
  const [showSuggestions, setShowSuggestions] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const filteredSuggestions = suggestions.filter(
    s => s.toLowerCase().includes(inputValue.toLowerCase()) && !tags.includes(s)
  )

  const addTag = (tag: string) => {
    const trimmed = tag.trim()
    if (trimmed && !tags.includes(trimmed)) {
      onChange([...tags, trimmed])
    }
    setInputValue('')
    setShowSuggestions(false)
    inputRef.current?.focus()
  }

  const removeTag = (tag: string) => {
    onChange(tags.filter(t => t !== tag))
  }

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault()
      addTag(inputValue)
    } else if (e.key === 'Backspace' && !inputValue && tags.length > 0) {
      removeTag(tags[tags.length - 1])
    }
  }

  return (
    <div className={className}>
      {label && <label className="label">{label}</label>}
      <div
        className="min-h-[42px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2 flex flex-wrap gap-1.5 cursor-text focus-within:border-primary-500 focus-within:ring-1 focus-within:ring-primary-500 transition-colors relative"
        onClick={() => inputRef.current?.focus()}
      >
        {tags.map(tag => (
          <span
            key={tag}
            className="inline-flex items-center gap-1 bg-primary-100 text-primary-800 text-xs font-medium px-2 py-1 rounded-md"
          >
            {tag}
            <button
              type="button"
              onClick={(e) => { e.stopPropagation(); removeTag(tag) }}
              className="hover:text-primary-600 transition-colors"
            >
              <X className="w-3 h-3" />
            </button>
          </span>
        ))}
        <input
          ref={inputRef}
          type="text"
          value={inputValue}
          onChange={e => { setInputValue(e.target.value); setShowSuggestions(true) }}
          onKeyDown={handleKeyDown}
          onFocus={() => setShowSuggestions(true)}
          onBlur={() => setTimeout(() => setShowSuggestions(false), 150)}
          placeholder={tags.length === 0 ? placeholder : ''}
          className="flex-1 min-w-[120px] outline-none text-sm text-slate-900 placeholder-slate-400 bg-transparent"
        />
        {showSuggestions && filteredSuggestions.length > 0 && inputValue.length > 0 && (
          <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-slate-200 rounded-lg shadow-lg z-10 max-h-48 overflow-y-auto">
            {filteredSuggestions.slice(0, 8).map(s => (
              <button
                key={s}
                type="button"
                className="w-full text-left px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 transition-colors"
                onMouseDown={() => addTag(s)}
              >
                {s}
              </button>
            ))}
          </div>
        )}
      </div>
      <p className="mt-1 text-xs text-slate-400">Press Enter or comma to add</p>
    </div>
  )
}
