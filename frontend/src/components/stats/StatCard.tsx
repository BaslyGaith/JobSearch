import type { LucideIcon } from 'lucide-react'
import { clsx } from 'clsx'
import { Skeleton } from '../ui/Skeleton'

interface StatCardProps {
  label: string
  value: number | string
  icon: LucideIcon
  colorClass: string
  isLoading?: boolean
}

export function StatCard({ label, value, icon: Icon, colorClass, isLoading }: StatCardProps) {
  return (
    <div className="card p-5">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-slate-500 font-medium">{label}</p>
          {isLoading
            ? <Skeleton className="h-9 w-16 mt-1" />
            : <p className="text-3xl font-bold text-slate-900 mt-1">{value}</p>
          }
        </div>
        <div className={clsx('w-11 h-11 rounded-xl flex items-center justify-center', colorClass)}>
          <Icon className="w-5 h-5" />
        </div>
      </div>
    </div>
  )
}
