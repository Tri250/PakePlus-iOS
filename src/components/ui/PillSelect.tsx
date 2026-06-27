import { motion } from 'framer-motion'
import { cn } from '@/lib/utils'
interface Option { id: string; label: string }
interface Props { options: Option[]; selected: string; onChange: (id: string) => void }
export function PillSelect({ options, selected, onChange }: Props) {
  return (
    <div className="flex flex-wrap gap-1.5">
      {options.map((opt) => (
        <button key={opt.id} onClick={() => onChange(opt.id)}
          className={cn('px-3 py-1.5 rounded-full text-xs font-semibold transition-all duration-200',
            selected === opt.id ? 'bg-hasselblad-500 text-white shadow-[0_0_12px_rgba(255,107,43,0.3)]' : 'glass text-white/50 hover:text-white/80')}>
          {opt.label}
        </button>
      ))}
    </div>
  )
}
