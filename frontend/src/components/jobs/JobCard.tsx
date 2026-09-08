import { MapPin, Building2, Calendar, User, ExternalLink, Bookmark, BookmarkCheck, CheckCircle, SendHorizontal } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { JobOpportunity, JobStatus } from '../../types'
import { MatchScoreBadge } from './MatchScoreBadge'
import { JobStatusBadge } from './JobStatusBadge'
import { saveJob, unsaveJob, updateJobStatus } from '../../api/jobs'
import { prepareApplication } from '../../api/applications'
import { useNavigate } from 'react-router-dom'
import { useToast } from '../../contexts/ToastContext'

interface JobCardProps {
  job: JobOpportunity
}

const employmentTypeLabel: Record<string, string> = {
  FULL_TIME: 'Full-time',
  PART_TIME: 'Part-time',
  CONTRACT: 'Contract',
  INTERNSHIP: 'Internship',
}

export function JobCard({ job }: JobCardProps) {
  const toast = useToast()
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['jobs'] })
    queryClient.invalidateQueries({ queryKey: ['job-stats'] })
  }

  const saveMutation = useMutation({
    mutationFn: async () => {
      if (job.saved) {
        await unsaveJob(job.id)
      } else {
        await saveJob(job.id)
      }
    },
    onSuccess: () => {
      invalidate()
      toast.success(job.saved ? 'Job removed from saved' : 'Job saved successfully')
    },
    onError: () => toast.error('Failed to update saved status'),
  })

  const statusMutation = useMutation({
    mutationFn: (status: JobStatus) => updateJobStatus(job.id, status),
    onSuccess: () => {
      invalidate()
      toast.success('Status updated')
    },
    onError: () => toast.error('Failed to update status'),
  })

  /** Assembles the pack and takes the user to it - nothing is sent. */
  const prepare = useMutation({
    mutationFn: () => prepareApplication(job.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] })
      toast.success('Application prepared for you to review')
      navigate('/applications')
    },
    onError: () => toast.error('Could not prepare that application'),
  })

  const publicationDate = job.publicationDate
    ? formatDistanceToNow(new Date(job.publicationDate), { addSuffix: true })
    : 'Unknown'

  return (
    <div className="card p-5 hover:shadow-md transition-shadow group">
      {/* Header */}
      <div className="flex items-start justify-between gap-3 mb-3">
        <div className="flex-1 min-w-0">
          <h3 className="font-semibold text-slate-900 text-base leading-tight line-clamp-2 group-hover:text-primary-700 transition-colors">
            {job.title}
          </h3>
          <div className="flex items-center gap-1.5 mt-1.5 text-slate-500 text-sm">
            <Building2 className="w-3.5 h-3.5 shrink-0" />
            <span className="font-medium text-slate-700">{job.companyName}</span>
          </div>
        </div>
        <MatchScoreBadge score={job.matchScore} />
      </div>

      {/* Meta */}
      <div className="flex flex-wrap gap-x-4 gap-y-1.5 text-xs text-slate-500 mb-3">
        <span className="flex items-center gap-1">
          <MapPin className="w-3.5 h-3.5" />
          {job.location}
        </span>
        <span className="flex items-center gap-1">
          <Calendar className="w-3.5 h-3.5" />
          {publicationDate}
        </span>
        {job.employmentType && (
          <span className="badge bg-slate-100 text-slate-600">
            {employmentTypeLabel[job.employmentType] ?? job.employmentType}
          </span>
        )}
      </div>

      {/* Description */}
      {job.description && (
        <p className="text-xs text-slate-500 line-clamp-2 mb-3 leading-relaxed">
          {job.description}
        </p>
      )}

      {/* Recruiter */}
      {job.recruiterName && (
        <div className="flex items-center gap-1.5 text-xs text-slate-500 mb-3">
          <User className="w-3.5 h-3.5" />
          <span>{job.recruiterName}</span>
        </div>
      )}

      {/* Status + Actions */}
      <div className="flex items-center justify-between pt-3 border-t border-slate-100">
        <JobStatusBadge status={job.status} />

        <div className="flex items-center gap-1.5">
          {/* Save button */}
          <button
            onClick={() => saveMutation.mutate()}
            disabled={saveMutation.isPending}
            className="btn-ghost p-2 rounded-lg"
            title={job.saved ? 'Remove from saved' : 'Save job'}
          >
            {job.saved
              ? <BookmarkCheck className="w-4 h-4 text-primary-600" />
              : <Bookmark className="w-4 h-4" />
            }
          </button>

          {/* Mark as Applied */}
          {job.status !== 'APPLIED' && (
            <button
              onClick={() => statusMutation.mutate('APPLIED')}
              disabled={statusMutation.isPending}
              className="btn-ghost p-2 rounded-lg"
              title="Mark as Applied"
            >
              <CheckCircle className="w-4 h-4" />
            </button>
          )}

          {/* Prepare application */}
          <button
            onClick={() => prepare.mutate()}
            disabled={prepare.isPending}
            className="btn-ghost p-2 rounded-lg"
            title="Prepare application"
          >
            <SendHorizontal className="w-4 h-4" />
          </button>

          {/* View Job */}
          <a
            href={job.jobUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="btn-primary text-xs px-3 py-1.5 gap-1"
          >
            View Job
            <ExternalLink className="w-3 h-3" />
          </a>
        </div>
      </div>
    </div>
  )
}
