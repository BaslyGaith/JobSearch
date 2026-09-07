import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus, Save, Trash2, ShieldCheck, RotateCcw } from 'lucide-react'
import { getCvProfile, updateFactBank } from '../api/cv'
import { useToast } from '../contexts/ToastContext'
import { TagInput } from '../components/ui/TagInput'
import { Skeleton } from '../components/ui/Skeleton'
import type { FactBank } from '../types'

/**
 * The single source of truth the CV agent writes from. Everything here is a
 * verified fact the user has confirmed - the agent may reorder and reframe it,
 * never add to it.
 */
export function CareerProfilePage() {
  const toast = useToast()
  const queryClient = useQueryClient()

  const { data: profile, isLoading } = useQuery({ queryKey: ['cv', 'profile'], queryFn: getCvProfile })
  const [facts, setFacts] = useState<FactBank | null>(null)
  const [dirty, setDirty] = useState(false)

  useEffect(() => {
    if (profile?.factBank && !dirty) {
      setFacts(JSON.parse(profile.factBank) as FactBank)
    }
  }, [profile, dirty])

  const save = useMutation({
    mutationFn: () => updateFactBank(JSON.stringify(facts, null, 2)),
    onSuccess: () => {
      setDirty(false)
      queryClient.invalidateQueries({ queryKey: ['cv', 'profile'] })
      toast.success('Career profile saved')
    },
    onError: (error: unknown) => {
      const message = (error as { response?: { data?: { message?: string } } }).response?.data?.message
      toast.error(message ?? 'Could not save the profile')
    },
  })

  const edit = (mutate: (draft: FactBank) => void) => {
    if (!facts) return
    const next = structuredClone(facts)
    mutate(next)
    setFacts(next)
    setDirty(true)
  }

  if (isLoading || !facts) {
    return <Skeleton className="h-96 w-full" />
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">My Career</h1>
          <p className="text-slate-500 text-sm mt-1">
            Everything the CV agent is allowed to say about you. Enter it once — every CV is built
            from here and nothing else.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {dirty && (
            <button
              onClick={() => { setDirty(false); setFacts(JSON.parse(profile!.factBank) as FactBank) }}
              className="btn-ghost gap-2 text-sm"
            >
              <RotateCcw className="w-4 h-4" />
              Discard
            </button>
          )}
          <button
            onClick={() => save.mutate()}
            disabled={!dirty || save.isPending}
            className="btn-primary gap-2"
          >
            <Save className="w-4 h-4" />
            {save.isPending ? 'Saving…' : 'Save changes'}
          </button>
        </div>
      </header>

      <div className="card p-4 flex items-start gap-3 bg-emerald-50/60 border-emerald-200">
        <ShieldCheck className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
        <p className="text-sm text-slate-700">
          Anything you remove here disappears from future CVs. Anything not listed here can never
          appear in one, however well it would fit a posting.
        </p>
      </div>

      {/* Identity */}
      <section className="card p-5">
        <h2 className="font-semibold text-slate-900 mb-4">Personal</h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Full name" value={facts.identity.fullName}
                 onChange={(v) => edit((d) => { d.identity.fullName = v })} />
          <Field label="Email" value={facts.identity.email}
                 onChange={(v) => edit((d) => { d.identity.email = v })} />
          <Field label="Phone" value={facts.identity.phone}
                 onChange={(v) => edit((d) => { d.identity.phone = v })} />
          <Field label="LinkedIn" value={facts.identity.linkedin}
                 onChange={(v) => edit((d) => { d.identity.linkedin = v })} />
          <Field label="Location" value={facts.identity.location}
                 onChange={(v) => edit((d) => { d.identity.location = v })} />
          <Field label="Years of experience" type="number"
                 value={String(facts.identity.yearsExperience ?? '')}
                 onChange={(v) => edit((d) => { d.identity.yearsExperience = Number(v) || 0 })} />
        </div>
        <div className="mt-4">
          <TagInput
            label="Languages"
            tags={facts.identity.languages}
            onChange={(tags) => edit((d) => { d.identity.languages = tags })}
            placeholder="French (fluent)"
          />
        </div>
      </section>

      {/* Experience */}
      <section className="card p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-semibold text-slate-900">Experience</h2>
          <button
            onClick={() => edit((d) => d.experiences.unshift({
              title: '', company: '', location: '', startDate: '', endDate: null, bullets: [],
            }))}
            className="btn-ghost gap-1.5 text-xs"
          >
            <Plus className="w-3.5 h-3.5" />
            Add role
          </button>
        </div>

        <div className="space-y-5">
          {facts.experiences.map((experience, index) => (
            <div key={index} className="rounded-lg border border-slate-200 p-4">
              <div className="flex items-start justify-between gap-3 mb-3">
                <div className="grid gap-3 sm:grid-cols-2 flex-1">
                  <Field label="Job title" value={experience.title}
                         onChange={(v) => edit((d) => { d.experiences[index].title = v })} />
                  <Field label="Company" value={experience.company}
                         onChange={(v) => edit((d) => { d.experiences[index].company = v })} />
                  <Field label="Start (YYYY-MM)" value={experience.startDate}
                         onChange={(v) => edit((d) => { d.experiences[index].startDate = v })} />
                  <Field label="End (YYYY-MM, blank if current)" value={experience.endDate ?? ''}
                         onChange={(v) => edit((d) => { d.experiences[index].endDate = v || null })} />
                </div>
                <button
                  onClick={() => edit((d) => { d.experiences.splice(index, 1) })}
                  className="btn-ghost p-1.5 rounded-lg text-slate-400 hover:text-red-600"
                  title="Remove this role"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>

              <label className="label">What you did</label>
              <div className="space-y-2">
                {experience.bullets.map((bullet, bulletIndex) => (
                  <div key={bulletIndex} className="flex gap-2">
                    <textarea
                      value={bullet.text}
                      rows={2}
                      onChange={(e) => edit((d) => {
                        d.experiences[index].bullets[bulletIndex].text = e.target.value
                      })}
                      className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
                    />
                    <button
                      onClick={() => edit((d) => { d.experiences[index].bullets.splice(bulletIndex, 1) })}
                      className="btn-ghost p-1.5 rounded-lg text-slate-400 hover:text-red-600 self-start"
                      title="Remove"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                ))}
                <button
                  onClick={() => edit((d) => d.experiences[index].bullets.push({ text: '', tags: [] }))}
                  className="btn-ghost gap-1.5 text-xs"
                >
                  <Plus className="w-3.5 h-3.5" />
                  Add a line
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Skills */}
      <section className="card p-5">
        <h2 className="font-semibold text-slate-900 mb-4">Skills</h2>
        <div className="space-y-4">
          {Object.entries(facts.skills).map(([group, values]) => (
            <TagInput
              key={group}
              label={group}
              tags={values}
              onChange={(tags) => edit((d) => { d.skills[group] = tags })}
            />
          ))}
        </div>
      </section>

      {/* Certifications */}
      <section className="card p-5">
        <h2 className="font-semibold text-slate-900 mb-4">Certifications</h2>
        <TagInput
          tags={facts.certifications}
          onChange={(tags) => edit((d) => { d.certifications = tags })}
          placeholder="Microsoft DP-600 Fabric Analytics Engineer"
        />
      </section>

      {/* Never claim */}
      <section className="card p-5">
        <h2 className="font-semibold text-slate-900 mb-1">Never claim</h2>
        <p className="text-xs text-slate-500 mb-3">
          The agent refuses to write a CV containing any of these, even when the posting asks for them.
        </p>
        <TagInput
          tags={facts.blocklist}
          onChange={(tags) => edit((d) => { d.blocklist = tags })}
          placeholder="Kubernetes"
        />
      </section>
    </div>
  )
}

function Field({ label, value, onChange, type = 'text' }: {
  label: string
  value: string
  onChange: (value: string) => void
  type?: string
}) {
  return (
    <div>
      <label className="label">{label}</label>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
      />
    </div>
  )
}
