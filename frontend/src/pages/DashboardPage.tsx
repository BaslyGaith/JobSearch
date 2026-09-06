import { useQuery } from '@tanstack/react-query'
import { Briefcase, TrendingUp, Heart, CheckSquare, Linkedin, ChevronRight } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getJobs, getJobStats } from '../api/jobs'
import { getLinkedInStatus } from '../api/linkedin'
import { StatCard } from '../components/stats/StatCard'
import { JobCard } from '../components/jobs/JobCard'
import { JobCardSkeleton } from '../components/ui/Skeleton'

export function DashboardPage() {
  const { user } = useAuth()

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['job-stats'],
    queryFn: getJobStats,
  })

  const { data: jobsData, isLoading: jobsLoading } = useQuery({
    queryKey: ['jobs', { page: 0, size: 6 }],
    queryFn: () => getJobs({ page: 0, size: 6, sortBy: 'newest' }),
  })

  const { data: linkedIn } = useQuery({
    queryKey: ['linkedin-status'],
    queryFn: getLinkedInStatus,
    retry: false,
  })

  const firstName = user?.firstName || user?.email?.split('@')[0] || 'there'

  return (
    <div className="max-w-7xl mx-auto space-y-8">
      {/* Welcome */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">Good day, {firstName} 👋</h2>
          <p className="text-slate-500 mt-0.5">Here's what's waiting for you today</p>
        </div>
        <Link to="/jobs" className="btn-primary gap-2 hidden sm:inline-flex">
          <Briefcase className="w-4 h-4" />
          Browse All Jobs
        </Link>
      </div>

      {/* LinkedIn banner */}
      {linkedIn && !linkedIn.connected && (
        <div className="rounded-xl border border-blue-200 bg-blue-50 p-4 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-blue-600 flex items-center justify-center">
              <Linkedin className="w-5 h-5 text-white" />
            </div>
            <div>
              <p className="text-sm font-semibold text-blue-900">Connect your LinkedIn account</p>
              <p className="text-xs text-blue-700 mt-0.5">Enable the AI agent to discover more opportunities</p>
            </div>
          </div>
          <Link to="/linkedin" className="btn-primary text-xs px-3 py-1.5 shrink-0">
            Connect
          </Link>
        </div>
      )}

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="New Opportunities"
          value={stats?.newOpportunities ?? 0}
          icon={Briefcase}
          colorClass="bg-blue-100 text-blue-600"
          isLoading={statsLoading}
        />
        <StatCard
          label="High Matches"
          value={stats?.highMatches ?? 0}
          icon={TrendingUp}
          colorClass="bg-green-100 text-green-600"
          isLoading={statsLoading}
        />
        <StatCard
          label="Interested"
          value={stats?.interested ?? 0}
          icon={Heart}
          colorClass="bg-purple-100 text-purple-600"
          isLoading={statsLoading}
        />
        <StatCard
          label="Applied"
          value={stats?.applied ?? 0}
          icon={CheckSquare}
          colorClass="bg-amber-100 text-amber-600"
          isLoading={statsLoading}
        />
      </div>

      {/* Recent Jobs */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-slate-900">Latest Opportunities</h3>
          <Link to="/jobs" className="text-sm text-primary-600 hover:text-primary-700 font-medium flex items-center gap-1">
            View all <ChevronRight className="w-4 h-4" />
          </Link>
        </div>

        {jobsLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {Array.from({ length: 6 }).map((_, i) => <JobCardSkeleton key={i} />)}
          </div>
        ) : jobsData?.content && jobsData.content.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {jobsData.content.map(job => (
              <JobCard key={job.id} job={job} />
            ))}
          </div>
        ) : (
          <div className="card p-12 text-center">
            <Briefcase className="w-10 h-10 text-slate-300 mx-auto mb-3" />
            <p className="text-slate-500 font-medium">No jobs yet</p>
            <p className="text-slate-400 text-sm mt-1">Set up your preferences to start discovering opportunities</p>
            <Link to="/preferences" className="btn-primary mt-4 inline-flex">
              Set Preferences
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}
