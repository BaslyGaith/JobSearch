import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Send, Trash2, ExternalLink, Clock, Paperclip, Copy, Check } from 'lucide-react'
import { formatDistanceToNow, format } from 'date-fns'
import {
  changeApplicationStatus, deleteApplication, listApplications, markApplicationSent,
  updateApplication,
} from '../api/applications'
import { useToast } from '../contexts/ToastContext'
import { EmptyState } from '../components/ui/EmptyState'
import { Skeleton } from '../components/ui/Skeleton'
import type { Application, ApplicationStatus } from '../types'

const statusLabels: Record<ApplicationStatus, string> = {
  DRAFT: 'Draft',
  READY: 'Ready to send',
  SENT: 'Sent',
  FOLLOW_UP: 'Followed up',
  INTERVIEW: 'Interview',
  REJECTED: 'Not taken forward',
  CLOSED: 'Closed',
}

const statusStyles: Record<ApplicationStatus, string> = {
  DRAFT: 'bg-slate-100 text-slate-600',
  READY: 'bg-blue-100 text-blue-700',
  SENT: 'bg-emerald-100 text-emerald-700',
  FOLLOW_UP: 'bg-amber-100 text-amber-700',
  INTERVIEW: 'bg-violet-100 text-violet-700',
  REJECTED: 'bg-slate-100 text-slate-500',
  CLOSED: 'bg-slate-100 text-slate-500',
}

export function ApplicationsPage() {
  const toast = useToast()
  const queryClient = useQueryClient()
  const [openId, setOpenId] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  const { data: applications, isLoading } = useQuery({
    queryKey: ['applications'],
    queryFn: listApplications,
  })

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['applications'] })
    queryClient.invalidateQueries({ queryKey: ['today'] })
    queryClient.invalidateQueries({ queryKey: ['jobs'] })
  }

  const save = useMutation({
    mutationFn: ({ id, application }: { id: string; application: Application }) =>
      updateApplication(id, {
        recipientName: application.recipientName,
        recipientEmail: application.recipientEmail,
        subject: application.subject,
        body: application.body,
        cvDocumentId: application.cvDocumentId,
      }),
    onSuccess: () => { invalidate(); toast.success('Saved') },
    onError: () => toast.error('Could not save your changes'),
  })

  const sent = useMutation({
    mutationFn: (id: string) => markApplicationSent(id),
    onSuccess: () => { invalidate(); toast.success('Recorded as sent. Good luck with this one.') },
    onError: () => toast.error('Could not record that'),
  })

  const status = useMutation({
    mutationFn: ({ id, next }: { id: string; next: ApplicationStatus }) =>
      changeApplicationStatus(id, next),
    onSuccess: () => { invalidate(); toast.success('Updated') },
    onError: () => toast.error('Could not update the status'),
  })

  const remove = useMutation({
    mutationFn: (id: string) => deleteApplication(id),
    onSuccess: () => { invalidate(); setOpenId(null); toast.success('Application deleted') },
    onError: () => toast.error('Could not delete that application'),
  })

  const open = applications?.find((a) => a.id === openId) ?? null

  const copyEmail = async (application: Application) => {
    await navigator.clipboard.writeText(
      `To: ${application.recipientEmail ?? ''}\nSubject: ${application.subject}\n\n${application.body}`
    )
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <header>
        <h1 className="text-2xl font-bold text-slate-900">Applications</h1>
        <p className="text-slate-500 text-sm mt-1">
          Everything is prepared for you to review. Nothing is sent until you send it.
        </p>
      </header>

      {isLoading ? (
        <Skeleton className="h-40 w-full" />
      ) : !applications || applications.length === 0 ? (
        <div className="card">
          <EmptyState
            title="No applications yet"
            description="Open an opportunity and prepare an application to see it here."
          />
        </div>
      ) : (
        <section className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="text-left font-semibold px-4 py-2.5">Role</th>
                <th className="text-left font-semibold px-4 py-2.5 hidden sm:table-cell">CV</th>
                <th className="text-left font-semibold px-4 py-2.5">Status</th>
                <th className="text-left font-semibold px-4 py-2.5 hidden md:table-cell">When</th>
                <th className="px-4 py-2.5"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {applications.map((application) => (
                <tr key={application.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3">
                    <p className="font-medium text-slate-900">{application.jobTitle}</p>
                    <p className="text-xs text-slate-500">{application.companyName}</p>
                  </td>
                  <td className="px-4 py-3 hidden sm:table-cell text-xs text-slate-600">
                    {application.cvTitle
                      ? `${application.cvTitle} · ${application.cvLanguage?.toUpperCase()}`
                      : <span className="text-amber-700">No CV attached</span>}
                  </td>
                  <td className="px-4 py-3">
                    <span className={`badge ${statusStyles[application.status]}`}>
                      {statusLabels[application.status]}
                    </span>
                  </td>
                  <td className="px-4 py-3 hidden md:table-cell text-xs text-slate-500">
                    {application.sentAt
                      ? format(new Date(application.sentAt), 'd MMM')
                      : formatDistanceToNow(new Date(application.createdAt), { addSuffix: true })}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button
                      onClick={() => setOpenId(application.id === openId ? null : application.id)}
                      className="btn-ghost text-xs px-2 py-1"
                    >
                      {application.id === openId ? 'Close' : 'Review'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {/* Review panel */}
      {open && (
        <section className="card p-5 space-y-4">
          <div className="flex items-start justify-between gap-3">
            <div>
              <h2 className="font-semibold text-slate-900">{open.jobTitle}</h2>
              <p className="text-sm text-slate-500">{open.companyName}</p>
            </div>
            <div className="flex items-center gap-2">
              {open.jobUrl && (
                <a href={open.jobUrl} target="_blank" rel="noopener noreferrer" className="btn-ghost text-xs gap-1">
                  Posting <ExternalLink className="w-3 h-3" />
                </a>
              )}
              <button
                onClick={() => remove.mutate(open.id)}
                className="btn-ghost p-1.5 rounded text-slate-400 hover:text-red-600"
                title="Delete"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>

          {!open.cvDocumentId && (
            <p className="text-sm text-amber-800 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2">
              No CV is attached — generate one in CV Studio for this role first.
            </p>
          )}

          <div className="grid gap-3 sm:grid-cols-2">
            <Field label="To (name)" value={open.recipientName ?? ''}
                   onChange={(v) => patch(open, { recipientName: v })} />
            <Field label="To (email)" value={open.recipientEmail ?? ''}
                   onChange={(v) => patch(open, { recipientEmail: v })} />
          </div>
          <Field label="Subject" value={open.subject} onChange={(v) => patch(open, { subject: v })} />

          <div>
            <label className="label">Message</label>
            <textarea
              value={open.body}
              rows={14}
              onChange={(e) => patch(open, { body: e.target.value })}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
            />
          </div>

          {open.cvTitle && (
            <p className="text-xs text-slate-500 flex items-center gap-1.5">
              <Paperclip className="w-3.5 h-3.5" />
              {open.cvTitle} ({open.cvLanguage?.toUpperCase()}) — download it from CV Studio and attach it
              when you send.
            </p>
          )}

          <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-slate-100">
            <button
              onClick={() => save.mutate({ id: open.id, application: open })}
              disabled={save.isPending}
              className="btn-ghost text-sm"
            >
              Save changes
            </button>
            <button onClick={() => copyEmail(open)} className="btn-ghost text-sm gap-1.5">
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
              {copied ? 'Copied' : 'Copy email'}
            </button>
            <a
              href={`mailto:${open.recipientEmail ?? ''}?subject=${encodeURIComponent(open.subject)}&body=${encodeURIComponent(open.body)}`}
              className="btn-ghost text-sm"
            >
              Open in mail app
            </a>
            <div className="flex-1" />
            {open.status !== 'SENT' && (
              <button
                onClick={() => sent.mutate(open.id)}
                disabled={sent.isPending}
                className="btn-primary text-sm gap-2"
              >
                <Send className="w-3.5 h-3.5" />
                I sent this
              </button>
            )}
            {open.status === 'SENT' && (
              <select
                value={open.status}
                onChange={(e) => status.mutate({ id: open.id, next: e.target.value as ApplicationStatus })}
                className="rounded-lg border border-slate-300 px-2 py-1.5 text-sm outline-none"
              >
                {(['SENT', 'FOLLOW_UP', 'INTERVIEW', 'REJECTED', 'CLOSED'] as ApplicationStatus[])
                  .map((value) => (
                    <option key={value} value={value}>{statusLabels[value]}</option>
                  ))}
              </select>
            )}
          </div>

          {/* Timeline */}
          <div className="pt-3 border-t border-slate-100">
            <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">Timeline</h3>
            <ol className="space-y-2">
              {open.events.map((event, i) => (
                <li key={i} className="flex gap-3 text-sm">
                  <span className="text-xs text-slate-400 w-24 shrink-0 flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    {format(new Date(event.occurredAt), 'd MMM HH:mm')}
                  </span>
                  <span>
                    <span className="text-slate-900">{event.label}</span>
                    {event.detail && <span className="text-slate-500"> — {event.detail}</span>}
                  </span>
                </li>
              ))}
            </ol>
          </div>
        </section>
      )}
    </div>
  )

  /** Local edit; persisted when the user presses Save changes. */
  function patch(application: Application, changes: Partial<Application>) {
    queryClient.setQueryData<Application[]>(['applications'], (current) =>
      (current ?? []).map((a) => (a.id === application.id ? { ...a, ...changes } : a))
    )
  }
}

function Field({ label, value, onChange }: {
  label: string
  value: string
  onChange: (value: string) => void
}) {
  return (
    <div>
      <label className="label">{label}</label>
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
      />
    </div>
  )
}
