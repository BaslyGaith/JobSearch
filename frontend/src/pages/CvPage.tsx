import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { FileText, Download, Sparkles, AlertTriangle, Trash2, Clock } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import { deleteCv, downloadCv, generateCv, getCvProfile, listCvs } from '../api/cv'
import { useToast } from '../contexts/ToastContext'
import { CvPreview } from '../components/cv/CvPreview'
import { EmptyState } from '../components/ui/EmptyState'
import { Skeleton } from '../components/ui/Skeleton'
import type { CvDocument, PostingRequirement } from '../types'

export function CvPage() {
  const toast = useToast()
  const queryClient = useQueryClient()

  const [posting, setPosting] = useState('')
  const [language, setLanguage] = useState<'en' | 'fr'>('en')
  const [current, setCurrent] = useState<CvDocument[] | null>(null)

  const { data: profile } = useQuery({ queryKey: ['cv', 'profile'], queryFn: getCvProfile })
  const { data: history, isLoading: historyLoading } = useQuery({
    queryKey: ['cv', 'list'],
    queryFn: listCvs,
  })

  const generation = useMutation({
    mutationFn: () => generateCv(posting),
    onSuccess: (response) => {
      setCurrent(response.documents)
      queryClient.invalidateQueries({ queryKey: ['cv', 'list'] })
      toast.success(`CVs generated for ${response.analysis.targetRole}`)
    },
    onError: (error: unknown) => {
      const message = (error as { response?: { data?: { message?: string } } })
        .response?.data?.message
      toast.error(message ?? 'Could not generate the CV')
    },
  })

  const removal = useMutation({
    mutationFn: (id: string) => deleteCv(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cv', 'list'] })
      toast.success('CV deleted')
    },
    onError: () => toast.error('Could not delete that CV'),
  })

  const shown = current?.find((doc) => doc.language === language) ?? null
  const gaps: PostingRequirement[] = shown?.gaps ?? []

  const contactLine = [profile?.email, profile?.phone, profile?.linkedinUrl, profile?.location]
    .filter(Boolean)
    .join('  ·  ')

  const handleDownload = async (doc: CvDocument) => {
    try {
      const name = `${profile?.fullName ?? 'CV'} - ${doc.targetRole} - ${doc.language.toUpperCase()}.docx`
      await downloadCv(doc.id, name.replace(/_/g, ' '))
    } catch {
      toast.error('Download failed')
    }
  }

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold text-slate-900">CV Agent</h1>
        <p className="text-slate-500 text-sm mt-1">
          Paste a job posting. The agent rewrites your verified experience to fit it — in English and
          French — and never invents anything that is not in your fact bank.
        </p>
      </header>

      {/* Posting input */}
      <section className="card p-5">
        <label htmlFor="posting" className="label">Job posting</label>
        <textarea
          id="posting"
          value={posting}
          onChange={(e) => setPosting(e.target.value)}
          rows={10}
          placeholder="Paste the full job posting here — title, responsibilities, requirements…"
          className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-primary-500 focus:ring-1 focus:ring-primary-500 outline-none transition-colors font-mono"
        />
        <div className="flex items-center justify-between mt-3">
          <p className="text-xs text-slate-400">
            {posting.trim().length > 0 ? `${posting.trim().length} characters` : 'Both language versions are produced every time.'}
          </p>
          <button
            onClick={() => generation.mutate()}
            disabled={generation.isPending || posting.trim().length === 0}
            className="btn-primary gap-2"
          >
            <Sparkles className="w-4 h-4" />
            {generation.isPending ? 'Writing…' : 'Generate CV'}
          </button>
        </div>
      </section>

      {/* Result */}
      {shown && (
        <section className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="inline-flex rounded-lg border border-slate-200 bg-white p-1">
              {(['en', 'fr'] as const).map((code) => (
                <button
                  key={code}
                  onClick={() => setLanguage(code)}
                  className={
                    language === code
                      ? 'px-3 py-1.5 text-xs font-semibold rounded-md bg-primary-600 text-white'
                      : 'px-3 py-1.5 text-xs font-semibold rounded-md text-slate-600 hover:bg-slate-50'
                  }
                >
                  {code === 'en' ? 'English' : 'Français'}
                </button>
              ))}
            </div>
            <div className="flex items-center gap-2">
              <span className="badge bg-slate-100 text-slate-600">
                {shown.jobFamily} · written by {shown.generatedBy}
              </span>
              <button onClick={() => handleDownload(shown)} className="btn-primary gap-2 text-xs">
                <Download className="w-3.5 h-3.5" />
                Download .docx
              </button>
            </div>
          </div>

          <CvPreview
            draft={shown.draft}
            accentColor={shown.accentColor}
            fullName={profile?.fullName ?? ''}
            contactLine={contactLine}
          />

          {/* Gaps to know about */}
          <div className="card p-5 border-amber-200 bg-amber-50/50">
            <h3 className="flex items-center gap-2 font-semibold text-slate-900 mb-2">
              <AlertTriangle className="w-4 h-4 text-amber-600" />
              Gaps to know about
            </h3>
            {gaps.length === 0 ? (
              <p className="text-sm text-slate-600">
                Nothing in this posting falls outside your verified experience.
              </p>
            ) : (
              <ul className="space-y-3">
                {gaps.map((gap) => (
                  <li key={gap.text} className="text-sm">
                    <span className="font-semibold text-slate-900">{gap.text}</span>
                    {gap.essential && (
                      <span className="badge bg-amber-100 text-amber-700 ml-2">essential</span>
                    )}
                    {gap.interviewAdvice && (
                      <p className="text-slate-600 mt-0.5">{gap.interviewAdvice}</p>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      )}

      {/* History */}
      <section className="card p-5">
        <h2 className="font-semibold text-slate-900 mb-3">Previously generated</h2>
        {historyLoading ? (
          <Skeleton className="h-20 w-full" />
        ) : !history || history.length === 0 ? (
          <EmptyState
            icon={FileText}
            title="No CVs yet"
            description="Paste a posting above and the agent will write the first pair."
          />
        ) : (
          <ul className="divide-y divide-slate-100">
            {history.map((doc) => (
              <li key={doc.id} className="py-2.5 flex items-center justify-between gap-3">
                <div className="min-w-0">
                  <p className="text-sm font-medium text-slate-900 truncate">
                    {doc.targetRole}
                    <span className="badge bg-slate-100 text-slate-600 ml-2">
                      {doc.language.toUpperCase()}
                    </span>
                  </p>
                  <p className="text-xs text-slate-400 flex items-center gap-1 mt-0.5">
                    <Clock className="w-3 h-3" />
                    {formatDistanceToNow(new Date(doc.createdAt), { addSuffix: true })}
                    {doc.targetCompany ? ` · ${doc.targetCompany}` : ''}
                  </p>
                </div>
                <div className="flex items-center gap-1 shrink-0">
                  <button
                    onClick={() => { setCurrent([doc]); setLanguage(doc.language as 'en' | 'fr') }}
                    className="btn-ghost text-xs px-2 py-1"
                  >
                    View
                  </button>
                  <button
                    onClick={() => handleDownload(doc)}
                    className="btn-ghost p-1.5 rounded-lg"
                    title="Download .docx"
                  >
                    <Download className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => removal.mutate(doc.id)}
                    className="btn-ghost p-1.5 rounded-lg text-slate-400 hover:text-red-600"
                    title="Delete"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
