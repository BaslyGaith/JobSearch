import type { JobStatus } from '../../types'
import { Badge } from '../ui/Badge'

const statusConfig: Record<JobStatus, { label: string; variant: 'default' | 'success' | 'warning' | 'danger' | 'info' | 'purple' }> = {
  NEW: { label: 'New', variant: 'info' },
  REVIEWED: { label: 'Reviewed', variant: 'default' },
  INTERESTED: { label: 'Interested', variant: 'purple' },
  APPLIED: { label: 'Applied', variant: 'success' },
  REJECTED: { label: 'Rejected', variant: 'danger' },
  ARCHIVED: { label: 'Archived', variant: 'default' },
}

export function JobStatusBadge({ status }: { status: JobStatus }) {
  const config = statusConfig[status]
  return <Badge variant={config.variant}>{config.label}</Badge>
}
