import { useState, useRef, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { cn } from '@/lib/utils'

interface Props {
  label: string
  value: number
  min: number
  max: number
  step: number
  onChange: (value: number) => void
  unit?: string
}

export default function Slider({ label, value, min, max, step, onChange, unit = '' }: Props) {
  const [isDragging, setIsDragging] = useState(false)
  const sliderRef = useRef<HTMLDivElement>(null)

  const hasNegativeRange = min < 0
  const centerPercent = hasNegativeRange ? ((0 - min) / (max - min)) * 100 : 0

  const getPercent = (val: number) => ((val - min) / (max - min)) * 100

  const thumbPercent = getPercent(value)
  const filledPercent = hasNegativeRange
    ? value >= 0
      ? ((value - 0) / (max - 0)) * 100
      : ((0 - value) / (0 - min)) * 100
    : thumbPercent

  const handlePointerDown = useCallback((e: React.PointerEvent) => {
    e.preventDefault()
    setIsDragging(true)
    ;(e.target as HTMLElement).setPointerCapture(e.pointerId)
  }, [])

  const handlePointerMove = useCallback((e: React.PointerEvent) => {
    if (!sliderRef.current || !isDragging) return
    const rect = sliderRef.current.getBoundingClientRect()
    const percent = Math.max(0, Math.min(100, ((e.clientX - rect.left) / rect.width) * 100))
    const rawValue = (percent / 100) * (max - min) + min
    const steppedValue = Math.round(rawValue / step) * step
    const clampedValue = Math.max(min, Math.min(max, steppedValue))
    onChange(clampedValue)
  }, [isDragging, min, max, step, onChange])

  const handlePointerUp = useCallback(() => {
    setIsDragging(false)
  }, [])

  const handleDoubleClick = useCallback(() => {
    onChange(0)
  }, [onChange])

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-between">
        <label className="text-xs text-white/60">{label}</label>
        <div className="relative">
          <AnimatePresence>
            {isDragging && (
              <motion.div
                initial={{ opacity: 0, y: 4 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 4 }}
                className="absolute -top-8 left-1/2 -translate-x-1/2 px-2 py-0.5 rounded bg-hasselblad-500 text-white text-xs font-semibold whitespace-nowrap"
              >
                {value}{unit}
              </motion.div>
            )}
          </AnimatePresence>
          <span className={cn(
            'text-xs font-mono transition-shadow duration-200',
            value !== 0 && isDragging && 'text-hasselblad-400'
          )}>
            {value}{unit}
          </span>
        </div>
      </div>

      <div
        ref={sliderRef}
        className="relative h-6 flex items-center select-none"
        onPointerDown={handlePointerDown}
        onPointerMove={handlePointerMove}
        onPointerUp={handlePointerUp}
        onPointerCancel={handlePointerUp}
        onDoubleClick={handleDoubleClick}
        role="slider"
        aria-label={label}
        aria-valuemin={min}
        aria-valuemax={max}
        aria-valuenow={value}
      >
        {/* Track background */}
        <div className="absolute w-full h-1.5 rounded-full bg-white/10">
          {/* Center mark for negative range */}
          {hasNegativeRange && (
            <div
              className="absolute top-0 w-0.5 h-full bg-white/30"
              style={{ left: `${centerPercent}%` }}
            />
          )}
        </div>

        {/* Filled track */}
        <div
          className="absolute h-1.5 rounded-full bg-gradient-to-r from-hasselblad-500 to-hasselblad-400 transition-all"
          style={{
            left: hasNegativeRange ? `${centerPercent}%` : '0%',
            width: `${filledPercent}%`,
            boxShadow: value !== 0 ? '0 0 8px rgba(255,107,43,0.5)' : 'none',
          }}
        />

        {/* Thumb */}
        <div
          className={cn(
            'absolute w-4 h-4 rounded-full bg-white shadow-lg transition-all duration-150',
            'hover:scale-110 active:scale-95',
            isDragging && 'scale-110',
            value !== 0 && 'shadow-[0_0_12px_rgba(255,107,43,0.6)]'
          )}
          style={{ left: `calc(${thumbPercent}% - 8px)` }}
        />
      </div>

      <div className="flex justify-between text-[10px] text-white/30">
        <span>{min}{unit}</span>
        <span>{max}{unit}</span>
      </div>
    </div>
  )
}
