import type { CvDraft } from '../../types'

interface CvPreviewProps {
  draft: CvDraft
  accentColor: string
  fullName: string
  contactLine: string
}

/**
 * On-screen rendering of a generated CV, laid out like the .docx export so what
 * you approve here is what downloads.
 */
export function CvPreview({ draft, accentColor, fullName, contactLine }: CvPreviewProps) {
  const french = draft.language === 'fr'
  const t = (en: string, fr: string) => (french ? fr : en)

  return (
    <article className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
      {/* Header band */}
      <header className="px-8 py-6" style={{ backgroundColor: '#1B2A4A' }}>
        <h1 className="text-white font-bold text-2xl tracking-[0.12em] uppercase">{fullName}</h1>
        <p className="mt-1 text-sm font-medium" style={{ color: accentColor, filter: 'brightness(1.9)' }}>
          {draft.tagline}
        </p>
      </header>

      <div className="px-8 py-6 text-[13px] leading-relaxed text-slate-700">
        <p className="text-xs text-slate-500 mb-6">{contactLine}</p>

        <Section title={t('Profile', 'Profil')} accent={accentColor}>
          <p className="text-justify">{draft.profile}</p>
        </Section>

        <Section title={t('Professional experience', 'Expérience professionnelle')} accent={accentColor}>
          {draft.experiences.map((exp, i) => (
            <div key={`${exp.company}-${i}`} className="mb-4 last:mb-0">
              <div className="flex items-baseline justify-between gap-4">
                <h4 className="font-bold text-slate-900">{exp.title}</h4>
                <span className="text-xs text-slate-500 shrink-0">{exp.dates}</span>
              </div>
              <p className="text-xs font-semibold mb-1.5" style={{ color: accentColor }}>
                {exp.company}
                {exp.companyNote ? ` (${exp.companyNote})` : ''}
                {exp.location ? `, ${exp.location}` : ''}
              </p>
              <ul className="space-y-1">
                {exp.bullets.map((bullet, j) => (
                  <li key={j} className="flex gap-2">
                    <span style={{ color: accentColor }} aria-hidden="true">▪</span>
                    <span>{bullet}</span>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </Section>

        {draft.whatIBring.length > 0 && (
          <Section title={t('What I bring to this role', "Ce que j'apporte à ce poste")} accent={accentColor}>
            {draft.whatIBring.map((block, i) => (
              <div key={i} className="mb-3 last:mb-0">
                <h4 className="font-bold" style={{ color: accentColor }}>{block.heading}</h4>
                <p>{block.body}</p>
              </div>
            ))}
          </Section>
        )}

        <Section title={t('Core skills', 'Compétences clés')} accent={accentColor}>
          <dl className="space-y-1">
            {draft.coreSkills.map((skill) => (
              <div key={skill.label} className="sm:flex sm:gap-3">
                <dt className="font-semibold text-slate-900 sm:w-52 sm:shrink-0">{skill.label}</dt>
                <dd className="text-slate-600">{skill.values}</dd>
              </div>
            ))}
          </dl>
        </Section>

        {draft.selectedProject && (
          <Section title={t('Selected project', 'Projet sélectionné')} accent={accentColor}>
            <h4 className="font-bold" style={{ color: accentColor }}>{draft.selectedProject.name}</h4>
            <p className="text-xs text-slate-500 mb-1.5">
              {draft.selectedProject.organisation} · {draft.selectedProject.dates}
            </p>
            <ul className="space-y-1">
              {draft.selectedProject.bullets.map((bullet, i) => (
                <li key={i} className="flex gap-2">
                  <span style={{ color: accentColor }} aria-hidden="true">▪</span>
                  <span>{bullet}</span>
                </li>
              ))}
            </ul>
          </Section>
        )}

        {draft.certifications.length > 0 && (
          <Section title="Certifications" accent={accentColor}>
            <ul className="space-y-1">
              {draft.certifications.map((cert) => (
                <li key={cert} className="flex gap-2">
                  <span style={{ color: accentColor }} aria-hidden="true">▪</span>
                  <span>{cert}</span>
                </li>
              ))}
            </ul>
          </Section>
        )}

        <Section title={t('Education', 'Formation')} accent={accentColor}>
          {draft.education.map((line) => (
            <div key={line.qualification} className="flex flex-wrap items-baseline gap-x-2">
              <span className="font-semibold text-slate-900">{line.qualification}</span>
              <span className="text-xs text-slate-500">{line.institution} · {line.dates}</span>
            </div>
          ))}
        </Section>

        {draft.footer && (
          <p className="mt-6 text-xs italic text-slate-500">{draft.footer}</p>
        )}
      </div>
    </article>
  )
}

function Section({ title, accent, children }: {
  title: string
  accent: string
  children: React.ReactNode
}) {
  return (
    <section className="mb-5 last:mb-0">
      <h3
        className="text-[10px] font-bold uppercase tracking-[0.14em] text-slate-800 bg-slate-100 px-3 py-1.5 mb-3 border-l-[3px]"
        style={{ borderColor: accent }}
      >
        {title}
      </h3>
      {children}
    </section>
  )
}
