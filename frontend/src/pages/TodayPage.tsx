import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { ArrowRight, Linkedin } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { getToday } from '../api/today'
import { JobCard } from '../components/jobs/JobCard'
import { JobCardSkeleton, Skeleton } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'

/**
 * The landing page: what needs attention right now, in plain sentences rather
 * than a wall of counters.
 */
export function TodayPage() {
  const { user } = useAuth()
  const { data, isLoading } = useQuery({ queryKey: ['today'], queryFn: getToday })

  const firstName = user?.firstName || user?.email?.split('@')[0] || 'there'

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <header>
        <h1 className="text-2xl font-bold text-slate-900">{greeting()}, {firstName}</h1>
        {isLoading ? (
          <Skeleton className="h-5 w-72 mt-2" />
        ) : (
          <p className="text-slate-600 mt-1">{summarise(data)}</p>
        )}
      </header>

      {/* The numbers, as a sentence rather than a dashboard */}
      {!isLoading && data && (
        <section className="card divide-y divide-slate-100">
          <Row
            label="Waiting for you to review"
            value={data.awaitingReview}
            to="/jobs?status=NEW"
            emphasis={data.awaitingReview > 0}
          />
          <Row label="Strong matches" value={data.strongMatches} to="/jobs?minMatchScore=85" />
          <Row label="You marked interested" value={data.interested} to="/jobs?status=INTERESTED" />
          <Row label="Applied" value={data.applied} to="/jobs?status=APPLIED" />
          <Row label="CVs prepared" value={data.cvsPrepared} to="/cv" />
        </section>
      )}

      {/* LinkedIn */}
      {!isLoading && data && !data.linkedInConnected && (
        <section className="rounded-xl border border-slate-200 bg-white p-4 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-[#0A66C2] flex items-center justify-center shrink-0">
              <Linkedin className="w-5 h-5 text-white" />
            </div>
            <div>
              <p className="text-sm font-semibold text-slate-900">Connect LinkedIn</p>
              <p className="text-xs text-slate-500 mt-0.5">
                Adds your LinkedIn profile as a source of opportunities.
              </p>
            </div>
          </div>
          <Link to="/linkedin" className="btn-ghost text-xs shrink-0">Connect</Link>
        </section>
      )}

      {/* Worth reviewing */}
      <section>
        <div className="flex items-center justify-between mb-3">
          <h2 className="font-semibold text-slate-900">Worth a look</h2>
          <Link to="/jobs" className="text-sm text-primary-700 hover:text-primary-800 inline-flex items-center gap-1">
            All opportunities
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {isLoading ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {[0, 1, 2].map((i) => <JobCardSkeleton key={i} />)}
          </div>
        ) : !data || data.worthReviewing.length === 0 ? (
          <div className="card">
            <EmptyState
              title="Nothing new to review"
              description="You have been through everything currently on the list."
            />
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data.worthReviewing.map((job) => <JobCard key={job.id} job={job} />)}
          </div>
        )}
      </section>
    </div>
  )
}

function Row({ label, value, to, emphasis = false }: {
  label: string
  value: number
  to: string
  emphasis?: boolean
}) {
  return (
    <Link to={to} className="flex items-center justify-between px-5 py-3 hover:bg-slate-50 transition-colors">
      <span className="text-sm text-slate-600">{label}</span>
      <span className="flex items-center gap-2">
        <span className={emphasis ? 'text-lg font-bold text-slate-900' : 'text-lg font-semibold text-slate-500'}>
          {value}
        </span>
        <ArrowRight className="w-3.5 h-3.5 text-slate-300" />
      </span>
    </Link>
  )
}

function greeting(): string {
  const hour = new Date().getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 18) return 'Good afternoon'
  return 'Good evening'
}

/** One honest sentence about the state of the search. */
function summarise(data?: { publishedToday: number; awaitingReview: number; strongMatches: number }): string {
  if (!data) return ''
  if (data.awaitingReview === 0) {
    return 'Nothing is waiting on you. New opportunities will show up here.'
  }
  const parts: string[] = []
  parts.push(
    data.publishedToday > 0
      ? `${data.publishedToday} ${plural(data.publishedToday, 'opportunity', 'opportunities')} published today`
      : `${data.awaitingReview} ${plural(data.awaitingReview, 'opportunity', 'opportunities')} waiting`
  )
  if (data.strongMatches > 0) {
    parts.push(`${data.strongMatches} of them a strong match`)
  }
  return `${parts.join(', ')}.`
}

function plural(count: number, one: string, many: string): string {
  return count === 1 ? one : many
}
