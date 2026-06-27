import { AnimatePresence, motion } from 'framer-motion'
import { useUIStore } from '@/stores/useUIStore'
import { CheckCircle, XCircle, Info, X } from 'lucide-react'

export default function ToastContainer() {
  const { toastMessages, removeToast } = useUIStore()
  return (
    <div className="fixed top-4 left-1/2 -translate-x-1/2 z-[200] flex flex-col gap-2 w-full max-w-sm px-4">
      <AnimatePresence>
        {toastMessages.map((t) => (
          <motion.div
            key={t.id}
            initial={{ opacity: 0, y: -20, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -10, scale: 0.95 }}
            className={`glass-strong rounded-xl overflow-hidden flex items-center gap-3 px-4 py-3 border-l-4 ${
              t.type === 'success' ? 'border-l-green-500' :
              t.type === 'error' ? 'border-l-red-500' : 'border-l-blue-500'
            }`}
          >
            {t.type === 'success' && <CheckCircle className="w-4 h-4 text-green-500 flex-shrink-0" />}
            {t.type === 'error' && <XCircle className="w-4 h-4 text-red-500 flex-shrink-0" />}
            {t.type === 'info' && <Info className="w-4 h-4 text-blue-500 flex-shrink-0" />}
            <span className="text-sm text-white/80 flex-1">{t.message}</span>
            <button onClick={() => removeToast(t.id)} className="text-white/30 hover:text-white/60">
              <X className="w-3.5 h-3.5" />
            </button>
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  )
}
