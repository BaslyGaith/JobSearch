import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Save, Mail, Calendar, Shield } from 'lucide-react'
import { format } from 'date-fns'
import { useAuth } from '../contexts/AuthContext'
import { Avatar } from '../components/ui/Avatar'
import apiClient from '../api/client'
import type { User } from '../types'
import { useToast } from '../contexts/ToastContext'

export function ProfilePage() {
  const { user, refetch } = useAuth()
  const toast = useToast()
  const queryClient = useQueryClient()

  const [form, setForm] = useState({
    firstName: user?.firstName ?? '',
    lastName: user?.lastName ?? '',
  })

  const updateMutation = useMutation({
    mutationFn: async (data: { firstName: string; lastName: string }) => {
      const { data: updated } = await apiClient.put<User>('/api/users/me', data)
      return updated
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['auth', 'me'] })
      refetch()
      toast.success('Profile updated successfully!')
    },
    onError: () => toast.error('Failed to update profile'),
  })

  if (!user) return null

  const fullName = `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() || user.email

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Profile Header */}
      <div className="card p-8">
        <div className="flex items-start gap-6">
          <Avatar src={user.profilePicture} name={fullName} size="lg" />
          <div className="flex-1 min-w-0">
            <h2 className="text-xl font-bold text-slate-900">{fullName}</h2>
            <p className="text-slate-500 text-sm mt-0.5">{user.email}</p>
            <div className="flex items-center gap-2 mt-3">
              <span className="badge bg-slate-100 text-slate-600 gap-1">
                <Shield className="w-3 h-3" />
                {user.provider === 'GOOGLE' ? 'Google Account' : 'Microsoft Account'}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Edit Profile */}
      <div className="card p-6">
        <h3 className="font-semibold text-slate-900 mb-5">Edit Profile</h3>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">First Name</label>
              <input
                type="text"
                className="input"
                value={form.firstName}
                onChange={e => setForm(prev => ({ ...prev, firstName: e.target.value }))}
                placeholder="First name"
              />
            </div>
            <div>
              <label className="label">Last Name</label>
              <input
                type="text"
                className="input"
                value={form.lastName}
                onChange={e => setForm(prev => ({ ...prev, lastName: e.target.value }))}
                placeholder="Last name"
              />
            </div>
          </div>
          <div>
            <label className="label">Email</label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                type="email"
                className="input pl-9 bg-slate-50 cursor-not-allowed"
                value={user.email}
                disabled
              />
            </div>
            <p className="text-xs text-slate-400 mt-1">Email is managed by your {user.provider.toLowerCase()} account</p>
          </div>
          <div className="flex justify-end">
            <button
              onClick={() => updateMutation.mutate(form)}
              disabled={updateMutation.isPending}
              className="btn-primary gap-2"
            >
              <Save className="w-4 h-4" />
              {updateMutation.isPending ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        </div>
      </div>

      {/* Account Info */}
      <div className="card p-6">
        <h3 className="font-semibold text-slate-900 mb-5">Account Information</h3>
        <div className="space-y-3 text-sm">
          <div className="flex items-center justify-between py-2 border-b border-slate-100">
            <span className="text-slate-500 flex items-center gap-2">
              <Calendar className="w-4 h-4" />
              Member since
            </span>
            <span className="font-medium text-slate-900">
              {user.createdAt ? format(new Date(user.createdAt), 'MMMM d, yyyy') : 'Unknown'}
            </span>
          </div>
          <div className="flex items-center justify-between py-2 border-b border-slate-100">
            <span className="text-slate-500">Sign-in method</span>
            <span className="font-medium text-slate-900">{user.provider === 'GOOGLE' ? 'Google SSO' : 'Microsoft SSO'}</span>
          </div>
          <div className="flex items-center justify-between py-2">
            <span className="text-slate-500">User ID</span>
            <code className="text-xs font-mono bg-slate-100 px-2 py-1 rounded text-slate-600">{user.id}</code>
          </div>
        </div>
      </div>
    </div>
  )
}
