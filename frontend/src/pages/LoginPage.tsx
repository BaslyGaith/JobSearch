import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Zap, Shield, Search, TrendingUp } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { getGoogleAuthUrl, getMicrosoftAuthUrl } from '../api/auth'
import { useToast } from '../contexts/ToastContext'

export function LoginPage() {
  const { isAuthenticated, isLoading } = useAuth()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const toast = useToast()

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      navigate('/dashboard', { replace: true })
    }
  }, [isAuthenticated, isLoading, navigate])

  useEffect(() => {
    if (searchParams.get('error')) {
      toast.error('Authentication failed. Please try again.')
    }
  }, [])

  const features = [
    { icon: Search, text: 'AI-powered job discovery' },
    { icon: TrendingUp, text: 'Smart match scoring' },
    { icon: Shield, text: 'Secure OAuth authentication' },
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-primary-950 to-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary-600 mb-4 shadow-lg">
            <Zap className="w-9 h-9 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-white">
            JobFinder <span className="text-primary-400">AI</span>
          </h1>
          <p className="text-slate-400 mt-2 text-sm">
            Discover your next career opportunity, powered by AI
          </p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-xl font-semibold text-slate-900 text-center mb-2">
            Welcome back
          </h2>
          <p className="text-slate-500 text-sm text-center mb-8">
            Sign in to access your personalized job search dashboard
          </p>

          <div className="space-y-3">
            {/* Google */}
            <button
              onClick={() => window.location.href = getGoogleAuthUrl()}
              className="w-full flex items-center justify-center gap-3 px-4 py-3 border border-slate-300 rounded-xl text-slate-700 font-medium text-sm hover:bg-slate-50 hover:border-slate-400 transition-all"
            >
              <svg width="18" height="18" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Continue with Google
            </button>

            {/* Microsoft */}
            <button
              onClick={() => window.location.href = getMicrosoftAuthUrl()}
              className="w-full flex items-center justify-center gap-3 px-4 py-3 bg-slate-900 rounded-xl text-white font-medium text-sm hover:bg-slate-800 transition-all"
            >
              <svg width="18" height="18" viewBox="0 0 21 21">
                <rect x="1" y="1" width="9" height="9" fill="#f25022"/>
                <rect x="11" y="1" width="9" height="9" fill="#7fba00"/>
                <rect x="1" y="11" width="9" height="9" fill="#00a4ef"/>
                <rect x="11" y="11" width="9" height="9" fill="#ffb900"/>
              </svg>
              Continue with Microsoft
            </button>
          </div>

          {/* Features */}
          <div className="mt-8 pt-6 border-t border-slate-100">
            <p className="text-xs text-slate-400 text-center mb-4">Why JobFinder AI?</p>
            <div className="space-y-2.5">
              {features.map(({ icon: Icon, text }) => (
                <div key={text} className="flex items-center gap-2.5 text-sm text-slate-600">
                  <div className="w-6 h-6 rounded-full bg-primary-100 flex items-center justify-center shrink-0">
                    <Icon className="w-3.5 h-3.5 text-primary-600" />
                  </div>
                  {text}
                </div>
              ))}
            </div>
          </div>
        </div>

        <p className="text-center text-xs text-slate-500 mt-6">
          By signing in, you agree to our Terms of Service and Privacy Policy.
          <br />We never store your OAuth credentials.
        </p>
      </div>
    </div>
  )
}
