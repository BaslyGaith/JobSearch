import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Search, SlidersHorizontal, X } from 'lucide-react'
import { getJobs } from '../api/jobs'
import type { JobFilters, JobStatus } from '../types'
import { JobCard } from '../components/jobs/JobCard'
import { JobCardSkeleton } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'
import { Pagination } from '../components/ui/Pagination'

const employmentTypes = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP']
const employmentTypeLabels: Record<string, string> = {
  FULL_TIME: 'Full-time',
  PART_TIME: 'Part-time',
  CONTRACT: 'Contract',
  INTERNSHIP: 'Internship',
}

const statuses: { value: JobStatus; label: string }[] = [
  { value: 'NEW', label: 'New' },
  { value: 'REVIEWED', label: 'Reviewed' },
  { value: 'INTERESTED', label: 'Interested' },
  { value: 'APPLIED', label: 'Applied' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'ARCHIVED', label: 'Archived' },
]

const sortOptions = [
  { value: 'newest', label: 'Newest first' },
  { value: 'highest_match', label: 'Best match' },
  { value: 'company', label: 'Company A-Z' },
  { value: 'publication_date', label: 'Publication date' },
]

export function JobsPage() {
  const [filters, setFilters] = useState<JobFilters>({
    page: 0,
    size: 12,
    sortBy: 'newest',
  })
  const [searchInput, setSearchInput] = useState('')
  const [filtersOpen, setFiltersOpen] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['jobs', filters],
    queryFn: () => getJobs(filters),
    placeholderData: (prev) => prev,
  })

  const updateFilter = <K extends keyof JobFilters>(key: K, value: JobFilters[K] | undefined) => {
    setFilters(prev => ({ ...prev, [key]: value, page: 0 }))
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    updateFilter('search', searchInput || undefined)
  }

  const clearFilters = () => {
    setFilters({ page: 0, size: 12, sortBy: 'newest' })
    setSearchInput('')
  }

  const hasActiveFilters = !!(filters.search || filters.location || filters.employmentType || filters.status || filters.minMatchScore || filters.remote)

  return (
    <div className="max-w-7xl mx-auto">
      <div className="flex flex-col lg:flex-row gap-6">
        {/* Sidebar Filters */}
        <aside className="lg:w-64 shrink-0">
          <div className="card p-4 space-y-5 lg:sticky lg:top-0">
            <div className="flex items-center justify-between">
              <h3 className="font-semibold text-slate-900 text-sm flex items-center gap-2">
                <SlidersHorizontal className="w-4 h-4" />
                Filters
              </h3>
              {hasActiveFilters && (
                <button onClick={clearFilters} className="text-xs text-primary-600 hover:text-primary-700 flex items-center gap-1">
                  <X className="w-3 h-3" /> Clear
                </button>
              )}
            </div>

            {/* Location */}
            <div>
              <label className="label">Location</label>
              <input
                type="text"
                className="input"
                placeholder="e.g. Berlin, Remote"
                value={filters.location ?? ''}
                onChange={e => updateFilter('location', e.target.value || undefined)}
              />
            </div>

            {/* Employment Type */}
            <div>
              <label className="label">Employment Type</label>
              <div className="space-y-1.5">
                {employmentTypes.map(type => (
                  <label key={type} className="flex items-center gap-2.5 cursor-pointer group">
                    <input
                      type="radio"
                      name="employmentType"
                      checked={filters.employmentType === type}
                      onChange={() => updateFilter('employmentType', filters.employmentType === type ? undefined : type)}
                      className="w-3.5 h-3.5 text-primary-600"
                    />
                    <span className="text-sm text-slate-600 group-hover:text-slate-900">{employmentTypeLabels[type]}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Status */}
            <div>
              <label className="label">Status</label>
              <select
                className="input"
                value={filters.status ?? ''}
                onChange={e => updateFilter('status', (e.target.value as JobStatus) || undefined)}
              >
                <option value="">All statuses</option>
                {statuses.map(s => (
                  <option key={s.value} value={s.value}>{s.label}</option>
                ))}
              </select>
            </div>

            {/* Min Match Score */}
            <div>
              <label className="label">
                Min Match Score: <span className="text-primary-600 font-semibold">{filters.minMatchScore ?? 0}%</span>
              </label>
              <input
                type="range"
                min="0"
                max="100"
                step="5"
                value={filters.minMatchScore ?? 0}
                onChange={e => updateFilter('minMatchScore', Number(e.target.value) || undefined)}
                className="w-full h-1.5 accent-primary-600"
              />
            </div>

            {/* Remote Toggle */}
            <div>
              <label className="flex items-center gap-2.5 cursor-pointer">
                <div
                  onClick={() => updateFilter('remote', filters.remote ? undefined : true)}
                  className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors cursor-pointer ${
                    filters.remote ? 'bg-primary-600' : 'bg-slate-200'
                  }`}
                >
                  <span className={`inline-block h-4 w-4 rounded-full bg-white shadow transform transition-transform ${
                    filters.remote ? 'translate-x-4' : 'translate-x-0.5'
                  }`} />
                </div>
                <span className="text-sm text-slate-600">Remote only</span>
              </label>
            </div>
          </div>
        </aside>

        {/* Main Content */}
        <div className="flex-1 min-w-0 space-y-4">
          {/* Search + Sort Bar */}
          <div className="flex gap-3">
            <form onSubmit={handleSearch} className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                type="text"
                className="input pl-9"
                placeholder="Search jobs, companies…"
                value={searchInput}
                onChange={e => setSearchInput(e.target.value)}
              />
            </form>
            <select
              className="input w-48 shrink-0"
              value={filters.sortBy ?? 'newest'}
              onChange={e => updateFilter('sortBy', e.target.value as JobFilters['sortBy'])}
            >
              {sortOptions.map(o => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>

          {/* Results count */}
          {data && (
            <p className="text-sm text-slate-500">
              <span className="font-medium text-slate-900">{data.totalElements}</span> opportunities found
            </p>
          )}

          {/* Job Grid */}
          {isLoading ? (
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
              {Array.from({ length: 6 }).map((_, i) => <JobCardSkeleton key={i} />)}
            </div>
          ) : data?.content && data.content.length > 0 ? (
            <>
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
                {data.content.map(job => <JobCard key={job.id} job={job} />)}
              </div>
              <Pagination
                page={data.page}
                totalPages={data.totalPages}
                totalElements={data.totalElements}
                size={data.size}
                onPageChange={(p) => setFilters(prev => ({ ...prev, page: p }))}
              />
            </>
          ) : (
            <EmptyState
              title="No jobs found"
              description="Try adjusting your filters or search terms to find more opportunities."
              action={
                hasActiveFilters ? (
                  <button onClick={clearFilters} className="btn-secondary">Clear filters</button>
                ) : undefined
              }
            />
          )}
        </div>
      </div>
    </div>
  )
}
