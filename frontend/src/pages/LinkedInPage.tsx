import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { useEffect } from 'react'
import { Linkedin, CheckCircle, XCircle, Shield, AlertCircle, Unlink } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import { getLinkedInStatus, connectLinkedIn, disconnectLinkedIn } from '../api/linkedin'
import { useToast } from '../contexts/ToastContext'

export function LinkedInPage() {
  const queryClient = useQueryClient()
  const toast = useToast()
  const [searchParams] = useSearchParams()

  const { data: status, isLoading } = useQuery({
    queryKey: ['linkedin-status'],
    queryFn: getLinkedInStatus,
  })

  useEffect(() => {
    if (searchParams.get('connected') === 'true') {
      queryClient.invalidateQueries({ queryKey: ['linkedin-status'] })
      toast.success('LinkedIn account connected successfully!')
    } else if (searchParams.get('error') === 'true') {
      toast.error('LinkedIn connection failed. Please try again.')
    }
  }, [])

  const disconnectMutation = useMutation({
    mutationFn: disconnectLinkedIn,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['linkedin-status'] })
      toast.info('LinkedIn account disconnected')
    },
    onError: () => toast.error('Failed to disconnect. Please try again.'),
  })

  if (isLoading) {
    return (
      <div className="max-w-xl mx-auto">
        <div className="card p-8 animate-pulse space-y-4">
          <div className="h-16 w-16 rounded-xl bg-slate-200 mx-auto" />
          <div className="h-6 w-48 bg-slate-200 rounded mx-auto" />
          <div className="h-4 w-64 bg-slate-100 rounded mx-auto" />
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-xl mx-auto space-y-6">
      {/* Status Card */}
      <div className="card p-8 text-center">
        <div className={`inline-flex items-center justify-center w-16 h-16 rounded-2xl mb-4 ${
          status?.connected ? 'bg-blue-600' : 'bg-slate-100'
        }`}>
          <Linkedin className={`w-9 h-9 ${status?.connected ? 'text-white' : 'text-slate-400'}`} />
        </div>

        {status?.connected ? (
          <>
            <div className="flex items-center justify-center gap-2 mb-1">
              <h2 className="text-xl font-bold text-slate-900">LinkedIn Connected</h2>
              <CheckCircle className="w-5 h-5 text-green-500" />
            </div>
            <p className="text-slate-500 text-sm mb-1">Account: <span className="font-medium text-slate-700">{status.linkedinEmail}</span></p>
            {status.connectedAt && (
              <p className="text-xs text-slate-400 mb-6">
                Connected {formatDistanceToNow(new Date(status.connectedAt), { addSuffix: true })}
              </p>
            )}
            {status.mode === 'MOCK' && (
              <div className="inline-flex items-center gap-1.5 text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-full px-3 py-1.5 mb-6">
                <AlertCircle className="w-3.5 h-3.5" />
                Development mode — using mock connection
              </div>
            )}
            <button
              onClick={() => disconnectMutation.mutate()}
              disabled={disconnectMutation.isPending}
              className="btn-danger gap-2 mx-auto"
            >
              <Unlink className="w-4 h-4" />
              {disconnectMutation.isPending ? 'Disconnecting…' : 'Disconnect LinkedIn'}
            </button>
          </>
        ) : (
          <>
            <div className="flex items-center justify-center gap-2 mb-2">
              <h2 className="text-xl font-bold text-slate-900">LinkedIn Not Connected</h2>
              <XCircle className="w-5 h-5 text-slate-400" />
            </div>
            <p className="text-slate-500 text-sm mb-8">
              Connect your LinkedIn account to enable AI-powered job discovery in Sprint 2
            </p>
            <button
              onClick={() => connectLinkedIn()}
              className="btn-primary gap-2 mx-auto px-6 py-2.5"
            >
              <Linkedin className="w-4 h-4" />
              Connect LinkedIn
            </button>
          </>
        )}
      </div>

      {/* Info Cards */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-4">
          <Shield className="w-4 h-4 text-primary-600" />
          <h3 className="font-semibold text-slate-900">Privacy & Security</h3>
        </div>
        <ul className="space-y-2.5 text-sm text-slate-600">
          <li className="flex items-start gap-2">
            <CheckCircle className="w-4 h-4 text-green-500 shrink-0 mt-0.5" />
            We use OAuth 2.0 — your password is never shared with us
          </li>
          <li className="flex items-start gap-2">
            <CheckCircle className="w-4 h-4 text-green-500 shrink-0 mt-0.5" />
            Access tokens are stored securely and never exposed to the frontend
          </li>
          <li className="flex items-start gap-2">
            <CheckCircle className="w-4 h-4 text-green-500 shrink-0 mt-0.5" />
            We only access publicly available job/recruitment content
          </li>
          <li className="flex items-start gap-2">
            <CheckCircle className="w-4 h-4 text-green-500 shrink-0 mt-0.5" />
            You can disconnect at any time from this page
          </li>
        </ul>
      </div>

      {/* Permissions */}
      <div className="card p-6">
        <h3 className="font-semibold text-slate-900 mb-4">Required Permissions</h3>
        <div className="space-y-3 text-sm">
          {[
            { scope: 'openid, profile, email', desc: 'Basic account identification' },
            { scope: 'r_liteprofile', desc: 'Read your basic LinkedIn profile' },
            { scope: 'r_emailaddress', desc: 'Access your email address' },
          ].map(({ scope, desc }) => (
            <div key={scope} className="flex items-start gap-3 p-3 rounded-lg bg-slate-50">
              <div className="w-2 h-2 rounded-full bg-primary-500 mt-1.5 shrink-0" />
              <div>
                <code className="text-xs font-mono text-primary-700 bg-primary-50 px-1.5 py-0.5 rounded">{scope}</code>
                <p className="text-slate-500 mt-0.5 text-xs">{desc}</p>
              </div>
            </div>
          ))}
        </div>
        <p className="text-xs text-slate-400 mt-4">
          Note: Full LinkedIn API job search capabilities require LinkedIn Partner Program access.
          In Sprint 1, a mock provider is used for development and testing purposes.
        </p>
      </div>
    </div>
  )
}
