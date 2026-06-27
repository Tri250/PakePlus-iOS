import { motion } from 'framer-motion'
import { cn } from '@/lib/utils'
interface Props { children: React.ReactNode; onClick?: () => void; variant?: 'primary'|'secondary'|'ghost'; size?: 'sm'|'md'|'lg'; icon?: React.ReactNode; disabled?: boolean; className?: string; fullWidth?: boolean }
export function Button({ children, onClick, variant = 'secondary', size = 'md', icon, disabled, className, fullWidth }: Props) {
  const variants = {
    primary: 'bg-hasselblad-500 hover:bg-hasselblad-600 text-white font-semibold',
    secondary: 'glass hover:border-hasselblad-500/30 text-white/80 hover:text-white',
    ghost: 'text-white/50 hover:text-white/80 hover:bg-white/5',
  }
  const sizes = { sm: 'px-3 py-1.5 text-xs', md: 'px-4 py-2 text-sm', lg: 'px-5 py-2.5 text-sm' }
  return (
    <motion.button whileTap={{ scale: 0.95 }} onClick={onClick} disabled={disabled}
      className={cn('rounded-xl flex items-center justify-center gap-2 transition-colors', variants[variant], sizes[size], fullWidth && 'w-full', disabled && 'opacity-50', className)}>
      {icon && <span className="flex-shrink-0">{icon}</span>}
      {children}
    </motion.button>
  )
}
