import { useState, useRef, useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import { LogOut, User as UserIcon, ChevronDown } from 'lucide-react'
import { useAuth } from '../../contexts/AuthContext'
import { Avatar } from '../ui/Avatar'

const pageTitles: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/jobs': 'Job Opportunities',
  '/preferences': 'Search Preferences',
  '/linkedin': 'LinkedIn Connection',
  '/profile': 'My Profile',
}

export function TopBar() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  const title = pageTitles[location.pathname] ?? 'JobFinder AI'

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const fullName = user ? `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() || user.email : ''

  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 shrink-0">
      <h1 className="text-xl font-semibold text-slate-900">{title}</h1>

      {user && (
        <div className="relative" ref={menuRef}>
          <button
            onClick={() => setMenuOpen(!menuOpen)}
            className="flex items-center gap-2.5 rounded-lg px-3 py-2 hover:bg-slate-50 transition-colors"
          >
            <Avatar src={user.profilePicture} name={fullName} size="sm" />
            <div className="text-left hidden sm:block">
              <p className="text-sm font-medium text-slate-900 leading-none">{fullName}</p>
              <p className="text-xs text-slate-500 mt-0.5">{user.email}</p>
            </div>
            <ChevronDown className="w-4 h-4 text-slate-400" />
          </button>

          {menuOpen && (
            <div className="absolute right-0 mt-1 w-52 card shadow-lg py-1 z-50 animate-slide-in">
              <a
                href="/profile"
                className="flex items-center gap-2.5 px-4 py-2.5 text-sm text-slate-700 hover:bg-slate-50 transition-colors"
                onClick={() => setMenuOpen(false)}
              >
                <UserIcon className="w-4 h-4 text-slate-400" />
                My Profile
              </a>
              <hr className="my-1 border-slate-100" />
              <button
                onClick={() => { setMenuOpen(false); logout() }}
                className="w-full flex items-center gap-2.5 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors"
              >
                <LogOut className="w-4 h-4" />
                Sign Out
              </button>
            </div>
          )}
        </div>
      )}
    </header>
  )
}
