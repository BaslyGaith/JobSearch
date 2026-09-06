import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Save, Trash2 } from 'lucide-react'
import { getPreferences, savePreferences, deletePreferences } from '../api/preferences'
import type { JobSearchPreference } from '../types'
import { TagInput } from '../components/ui/TagInput'
import { useToast } from '../contexts/ToastContext'

const employmentTypes = [
  { value: 'FULL_TIME', label: 'Full-time' },
  { value: 'PART_TIME', label: 'Part-time' },
  { value: 'CONTRACT', label: 'Contract' },
  { value: 'INTERNSHIP', label: 'Internship' },
]

const experienceLevels = [
  { value: 'INTERNSHIP', label: 'Internship' },
  { value: 'ENTRY_LEVEL', label: 'Entry level' },
  { value: 'JUNIOR', label: 'Junior' },
  { value: 'MID_LEVEL', label: 'Mid-level' },
  { value: 'SENIOR', label: 'Senior' },
]

const remoteOptions = [
  { value: 'ANY', label: 'Any' },
  { value: 'REMOTE', label: 'Remote' },
  { value: 'HYBRID', label: 'Hybrid' },
  { value: 'ON_SITE', label: 'On-site' },
]

const jobTitleSuggestions = [
  'Java Developer', 'Data Analyst', 'Data Engineer', 'Power BI Developer',
  'Business Intelligence Analyst', 'Software Engineer', 'Backend Developer',
  'Full Stack Developer', 'Python Developer',
]

const locationSuggestions = [
  'Germany', 'France', 'Remote', 'Tunisia', 'Europe',
  'Berlin', 'Paris', 'Munich', 'Amsterdam', 'London',
]

const keywordSuggestions = [
  'Java', 'Spring Boot', 'SQL', 'Power BI', 'Python', 'Data Analysis',
  'Kubernetes', 'Docker', 'AWS', 'Azure', 'PostgreSQL', 'Kafka',
]

const emptyPrefs: Omit<JobSearchPreference, 'id' | 'createdAt' | 'updatedAt'> = {
  jobTitles: [],
  locations: [],
  employmentTypes: [],
  experienceLevels: [],
  remotePreference: 'ANY',
  keywords: [],
}

export function PreferencesPage() {
  const queryClient = useQueryClient()
  const toast = useToast()
  const [form, setForm] = useState(emptyPrefs)

  const { data: existing, isLoading } = useQuery({
    queryKey: ['preferences'],
    queryFn: getPreferences,
  })

  useEffect(() => {
    if (existing) {
      setForm({
        jobTitles: existing.jobTitles ?? [],
        locations: existing.locations ?? [],
        employmentTypes: existing.employmentTypes ?? [],
        experienceLevels: existing.experienceLevels ?? [],
        remotePreference: existing.remotePreference ?? 'ANY',
        keywords: existing.keywords ?? [],
      })
    }
  }, [existing])

  const saveMutation = useMutation({
    mutationFn: () => savePreferences(form),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['preferences'] })
      toast.success('Preferences saved successfully!')
    },
    onError: () => toast.error('Failed to save preferences. Please try again.'),
  })

  const deleteMutation = useMutation({
    mutationFn: deletePreferences,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['preferences'] })
      setForm(emptyPrefs)
      toast.info('Preferences cleared')
    },
    onError: () => toast.error('Failed to clear preferences'),
  })

  const toggleMulti = (field: 'employmentTypes' | 'experienceLevels', value: string) => {
    setForm(prev => ({
      ...prev,
      [field]: prev[field].includes(value)
        ? prev[field].filter(v => v !== value)
        : [...prev[field], value],
    }))
  }

  if (isLoading) {
    return (
      <div className="max-w-2xl mx-auto space-y-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="card p-6">
            <div className="h-5 w-1/3 bg-slate-200 rounded animate-pulse mb-4" />
            <div className="h-10 bg-slate-100 rounded animate-pulse" />
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <p className="text-slate-500 text-sm">Configure what the AI agent will search for</p>
        {existing && (
          <button
            onClick={() => deleteMutation.mutate()}
            disabled={deleteMutation.isPending}
            className="btn-ghost text-red-600 hover:bg-red-50 text-xs gap-1.5"
          >
            <Trash2 className="w-3.5 h-3.5" />
            Clear all
          </button>
        )}
      </div>

      {/* Job Titles */}
      <div className="card p-6">
        <h3 className="font-semibold text-slate-900 mb-4">Job Titles</h3>
        <TagInput
          label="What roles are you looking for?"
          placeholder="Type a job title and press Enter"
          tags={form.jobTitles}
          onChange={tags => setForm(prev => ({ ...prev, jobTitles: tags }))}
          suggestions={jobTitleSuggestions}
        />
      </div>

      {/* Locations */}
      <div className="card p-6">
        <h3 className="font-semibold text-slate-900 mb-4">Locations</h3>
        <TagInput
          label="Where are you looking?"
          placeholder="Type a location and press Enter"
          tags={form.locations}
          onChange={tags => setForm(prev => ({ ...prev, locations: tags }))}
          suggestions={locationSuggestions}
        />
      </div>

      {/* Employment Type */}
      <div className="card p-6">
        <h3 className="font-semibold text-slate-900 mb-4">Employment Type</h3>
        <div className="grid grid-cols-2 gap-2.5">
          {employmentTypes.map(({ value, label }) => (
            <label key={value} className={`flex items-center gap-2.5 p-3 rounded-lg border cursor-pointer transition-colors ${
              form.employmentTypes.includes(value)
                ? 'border-primary-500 bg-primary-50 text-primary-700'
                : 'border-slate-200 hover:border-slate-300'
            }`}>
              <input
                type="checkbox"
                checked={form.employmentTypes.includes(value)}
                onChange={() => toggleMulti('employmentTypes', value)}
                className="w-4 h-4 text-primary-600 rounded"
              />
              <span className="text-sm font-medium">{label}</span>
            </label>
          ))}
        </div>
      </div>

      {/* Experience Level */}
      <div className="card p-6">
        <h3 className="font-semibold text-slate-900 mb-4">Experience Level</h3>
        <div className="grid grid-cols-2 gap-2.5">
          {experienceLevels.map(({ value, label }) => (
            <label key={value} className={`flex items-center gap-2.5 p-3 rounded-lg border cursor-pointer transition-colors ${
              form.experienceLevels.includes(value)
                ? 'border-primary-500 bg-primary-50 text-primary-700'
                : 'border-slate-200 hover:border-slate-300'
            }`}>
              <input
                type="checkbox"
                checked={form.experienceLevels.includes(value)}
                onChange={() => toggleMulti('experienceLevels', value)}
                className="w-4 h-4 text-primary-600 rounded"
              />
              <span className="text-sm font-medium">{label}</span>
            </label>
          ))}
        </div>
      </div>

      {/* Remote Preference */}
      <div className="card p-6">
        <h3 className="font-semibold text-slate-900 mb-4">Work Mode</h3>
        <div className="flex gap-2 flex-wrap">
          {remoteOptions.map(({ value, label }) => (
            <button
              key={value}
              type="button"
              onClick={() => setForm(prev => ({ ...prev, remotePreference: value as JobSearchPreference['remotePreference'] }))}
              className={`px-4 py-2 rounded-lg text-sm font-medium border transition-colors ${
                form.remotePreference === value
                  ? 'bg-primary-600 text-white border-primary-600'
                  : 'border-slate-200 text-slate-600 hover:border-slate-300'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* Keywords */}
      <div className="card p-6">
        <h3 className="font-semibold text-slate-900 mb-4">Keywords & Skills</h3>
        <TagInput
          label="Technologies, skills, or keywords"
          placeholder="Java, Spring Boot, SQL…"
          tags={form.keywords}
          onChange={tags => setForm(prev => ({ ...prev, keywords: tags }))}
          suggestions={keywordSuggestions}
        />
      </div>

      {/* Save Button */}
      <div className="flex justify-end pb-4">
        <button
          onClick={() => saveMutation.mutate()}
          disabled={saveMutation.isPending}
          className="btn-primary gap-2 px-6"
        >
          <Save className="w-4 h-4" />
          {saveMutation.isPending ? 'Saving…' : 'Save Preferences'}
        </button>
      </div>
    </div>
  )
}
