import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { AuthCallbackPage } from './pages/AuthCallbackPage'
import { TodayPage } from './pages/TodayPage'
import { JobsPage } from './pages/JobsPage'
import { PreferencesPage } from './pages/PreferencesPage'
import { CvPage } from './pages/CvPage'
import { CareerProfilePage } from './pages/CareerProfilePage'
import { LinkedInPage } from './pages/LinkedInPage'
import { ProfilePage } from './pages/ProfilePage'
import { NotFoundPage } from './pages/NotFoundPage'

export function App() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/auth/callback" element={<AuthCallbackPage />} />

      {/* Protected routes */}
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/today" element={<TodayPage />} />
          <Route path="/dashboard" element={<Navigate to="/today" replace />} />
          <Route path="/jobs" element={<JobsPage />} />
          <Route path="/preferences" element={<PreferencesPage />} />
          <Route path="/cv" element={<CvPage />} />
          <Route path="/career" element={<CareerProfilePage />} />
          <Route path="/linkedin" element={<LinkedInPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
      </Route>

      {/* Redirects */}
      <Route path="/" element={<Navigate to="/today" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
