import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { RefreshCw, CheckCircle2, CircleSlash } from 'lucide-react'
import { getSources, syncSources } from '../api/sources'
import { useToast } from '../contexts/ToastContext'
import { Skeleton } from '../components/ui/Skeleton'

export function SourcesPage() {
  const toast = useToast()
  const queryClient = useQueryClient()

  const { data: sources, isLoading } = useQuery({ queryKey: ['sources'], queryFn: getSources })

  const sync = useMutation({
    mutationFn: syncSources,
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ['sources'] })
      queryClient.invalidateQueries({ queryKey: ['jobs'] })
      queryClient.invalidateQueries({ queryKey: ['today'] })

      if (result.sourcesUsed.length === 0) {
        toast.error('No sources are configured yet')
      } else {
        toast.success(
          result.added > 0
            ? `${result.added} new ${result.added === 1 ? 'opportunity' : 'opportunities'} collected`
            : 'Nothing new since the last check'
        )
      }
      result.failures.forEach((failure) => toast.error(failure))
    },
    onError: () => toast.error('The sync could not be completed'),
  })

  const enabled = sources?.filter((source) => source.enabled) ?? []

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <header className="flex items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Where opportunities come from</h1>
          <p className="text-slate-500 text-sm mt-1">
            Each source is an official API used within its terms. Nothing is scraped.
          </p>
        </div>
        <button
          onClick={() => sync.mutate()}
          disabled={sync.isPending || enabled.length === 0}
          className="btn-primary gap-2 shrink-0"
        >
          <RefreshCw className={sync.isPending ? 'w-4 h-4 animate-spin' : 'w-4 h-4'} />
          {sync.isPending ? 'Checking…' : 'Check for new'}
        </button>
      </header>

      {isLoading ? (
        <Skeleton className="h-40 w-full" />
      ) : (
        <section className="card divide-y divide-slate-100">
          {sources?.map((source) => (
            <div key={source.name} className="flex items-center justify-between gap-4 px-5 py-4">
              <div className="flex items-center gap-3 min-w-0">
                {source.enabled ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                ) : (
                  <CircleSlash className="w-4 h-4 text-slate-300 shrink-0" />
                )}
                <div className="min-w-0">
                  <p className="text-sm font-medium text-slate-900">{source.name}</p>
                  <p className="text-xs text-slate-500">
                    {source.enabled
                      ? `${source.storedOpportunities} collected so far`
                      : 'Not configured — add its settings to switch it on'}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </section>
      )}

      {enabled.length === 0 && !isLoading && (
        <div className="card p-5 text-sm text-slate-600 space-y-2">
          <p className="font-medium text-slate-900">No source is switched on yet</p>
          <p>
            Greenhouse and Lever need only the name of a company's job board — no account, no key.
            Set <code className="text-xs bg-slate-100 px-1 py-0.5 rounded">GREENHOUSE_BOARDS</code> or{' '}
            <code className="text-xs bg-slate-100 px-1 py-0.5 rounded">LEVER_COMPANIES</code> to a
            comma-separated list and restart.
          </p>
          <p>
            France Travail needs a client id and secret from francetravail.io, set as{' '}
            <code className="text-xs bg-slate-100 px-1 py-0.5 rounded">FRANCE_TRAVAIL_CLIENT_ID</code>{' '}
            and <code className="text-xs bg-slate-100 px-1 py-0.5 rounded">FRANCE_TRAVAIL_CLIENT_SECRET</code>.
          </p>
        </div>
      )}
    </div>
  )
}
