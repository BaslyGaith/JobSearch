import { NavLink } from 'react-router-dom'
import { LayoutDashboard, Briefcase, SlidersHorizontal, Linkedin, User, Zap } from 'lucide-react'
import { clsx } from 'clsx'

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/jobs', icon: Briefcase, label: 'Jobs' },
  { to: '/preferences', icon: SlidersHorizontal, label: 'Search Preferences' },
  { to: '/linkedin', icon: Linkedin, label: 'LinkedIn' },
  { to: '/profile', icon: User, label: 'Profile' },
]

export function Sidebar() {
  return (
    <aside className="w-64 shrink-0 bg-white border-r border-slate-200 flex flex-col h-full">
      {/* Logo */}
      <div className="px-6 py-5 border-b border-slate-200">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-primary-600 flex items-center justify-center">
            <Zap className="w-5 h-5 text-white" />
          </div>
          <div>
            <span className="font-bold text-slate-900 text-sm">JobFinder</span>
            <span className="font-bold text-primary-600 text-sm"> AI</span>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              clsx('nav-link', isActive && 'nav-link-active')
            }
          >
            <Icon className="w-4 h-4 shrink-0" />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="px-4 py-4 border-t border-slate-200">
        <div className="rounded-lg bg-primary-50 p-3">
          <p className="text-xs font-semibold text-primary-700 mb-1">Sprint 1 — Foundation</p>
          <p className="text-xs text-primary-600">AI agent coming in Sprint 2</p>
        </div>
      </div>
    </aside>
  )
}
