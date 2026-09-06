import { clsx } from 'clsx'

interface AvatarProps {
  src?: string | null
  name: string
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

const sizeClasses = {
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-12 h-12 text-base',
}

export function Avatar({ src, name, size = 'md', className }: AvatarProps) {
  const initials = name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map(n => n[0])
    .join('')
    .toUpperCase()

  if (src) {
    return (
      <img
        src={src}
        alt={name}
        className={clsx('rounded-full object-cover', sizeClasses[size], className)}
      />
    )
  }

  return (
    <div
      className={clsx(
        'rounded-full bg-primary-600 text-white font-semibold flex items-center justify-center shrink-0',
        sizeClasses[size],
        className
      )}
    >
      {initials || '?'}
    </div>
  )
}
