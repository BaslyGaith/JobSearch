import { ChevronLeft, ChevronRight } from 'lucide-react'

interface PaginationProps {
  page: number
  totalPages: number
  totalElements: number
  size: number
  onPageChange: (page: number) => void
}

export function Pagination({ page, totalPages, totalElements, size, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null

  const start = page * size + 1
  const end = Math.min((page + 1) * size, totalElements)

  return (
    <div className="flex items-center justify-between px-1 py-3">
      <p className="text-sm text-slate-600">
        Showing <span className="font-medium">{start}</span> to{' '}
        <span className="font-medium">{end}</span> of{' '}
        <span className="font-medium">{totalElements}</span> results
      </p>
      <div className="flex items-center gap-1">
        <button
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          className="btn-ghost p-2 disabled:opacity-40"
          aria-label="Previous page"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>

        {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
          let pageNum: number
          if (totalPages <= 7) {
            pageNum = i
          } else if (page < 4) {
            pageNum = i < 5 ? i : i === 5 ? -1 : totalPages - 1
          } else if (page > totalPages - 5) {
            pageNum = i === 0 ? 0 : i === 1 ? -1 : totalPages - (6 - i)
          } else {
            pageNum = i === 0 ? 0 : i === 1 ? -1 : i === 5 ? -1 : i === 6 ? totalPages - 1 : page + (i - 3)
          }

          if (pageNum === -1) {
            return <span key={`ellipsis-${i}`} className="px-2 text-slate-400">…</span>
          }

          return (
            <button
              key={pageNum}
              onClick={() => onPageChange(pageNum)}
              className={`w-9 h-9 rounded-lg text-sm font-medium transition-colors ${
                pageNum === page
                  ? 'bg-primary-600 text-white'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {pageNum + 1}
            </button>
          )
        })}

        <button
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
          className="btn-ghost p-2 disabled:opacity-40"
          aria-label="Next page"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  )
}
