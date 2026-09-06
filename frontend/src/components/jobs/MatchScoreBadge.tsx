import { clsx } from 'clsx'

interface MatchScoreBadgeProps {
  score: number
  size?: 'sm' | 'md'
}

export function MatchScoreBadge({ score, size = 'md' }: MatchScoreBadgeProps) {
  const color = score >= 85
    ? 'bg-green-100 text-green-700 ring-green-200'
    : score >= 70
    ? 'bg-amber-100 text-amber-700 ring-amber-200'
    : 'bg-slate-100 text-slate-600 ring-slate-200'

  return (
    <span className={clsx(
      'inline-flex items-center font-semibold ring-1 rounded-full',
      size === 'md' ? 'text-sm px-3 py-1' : 'text-xs px-2 py-0.5',
      color
    )}>
      {score}% match
    </span>
  )
}
