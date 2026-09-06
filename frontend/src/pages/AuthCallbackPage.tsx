import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { Zap } from 'lucide-react'

export function AuthCallbackPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [searchParams] = useSearchParams()

  useEffect(() => {
    const success = searchParams.get('success')
    if (success === 'true') {
      // Invalidate and refetch auth state, then redirect to dashboard
      queryClient.invalidateQueries({ queryKey: ['auth', 'me'] })
      setTimeout(() => navigate('/dashboard', { replace: true }), 300)
    } else {
      navigate('/login?error=true', { replace: true })
    }
  }, [])

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center">
      <div className="text-center">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary-600 mb-4 animate-pulse">
          <Zap className="w-9 h-9 text-white" />
        </div>
        <p className="text-slate-600 font-medium">Signing you in…</p>
        <p className="text-slate-400 text-sm mt-1">Just a moment</p>
      </div>
    </div>
  )
}
