import { cn } from '@/lib/utils'
interface Props { children: React.ReactNode; className?: string; variant?: 'default' | 'strong' | 'orange'; onClick?: () => void }
export function GlassCard({ children, className, variant = 'default', onClick }: Props) {
  const base = variant === 'strong' ? 'glass-strong' : variant === 'orange' ? 'glass-orange' : 'glass'
  return (
    <div onClick={onClick} className={cn(base, 'rounded-2xl p-4 transition-all duration-200 hover:scale-[1.01] hover:border-hasselblad-500/30', onClick && 'cursor-pointer', className)}>
      {children}
    </div>
  )
}
