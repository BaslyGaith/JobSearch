import { Link } from 'react-router-dom'
import { Home, Search } from 'lucide-react'

export function NotFoundPage() {
  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center px-4">
      <div className="text-center max-w-md">
        <div className="text-8xl font-black text-slate-200 mb-4 select-none">404</div>
        <h1 className="text-2xl font-bold text-slate-900 mb-2">Page not found</h1>
        <p className="text-slate-500 mb-8">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <div className="flex items-center justify-center gap-3">
          <Link to="/dashboard" className="btn-primary gap-2">
            <Home className="w-4 h-4" />
            Go to Dashboard
          </Link>
          <Link to="/jobs" className="btn-secondary gap-2">
            <Search className="w-4 h-4" />
            Browse Jobs
          </Link>
        </div>
      </div>
    </div>
  )
}
