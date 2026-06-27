import { cn } from '@/lib/utils'

interface PhoneSimulatorProps {
  width?: number
  height?: number
  model?: string
  children: React.ReactNode
}

export function PhoneSimulator({ width = 393, height = 852, model = 'Find X9 Ultra', children }: PhoneSimulatorProps) {
  return (
    <div className="inline-block">
      {/* Phone Frame */}
      <div
        className="relative bg-zinc-900 rounded-[3rem] p-2 shadow-2xl"
        style={{ width: width + 16, height: height + 16 }}
      >
        {/* Side Buttons - Right */}
        <div className="absolute right-0 top-24 w-1 h-12 bg-zinc-700 rounded-l-sm" />
        <div className="absolute right-0 top-36 w-1 h-8 bg-zinc-700 rounded-l-sm" />
        <div className="absolute right-0 top-48 w-1 h-16 bg-zinc-700 rounded-l-sm" />
        
        {/* Side Buttons - Left */}
        <div className="absolute left-0 top-28 w-1 h-10 bg-zinc-700 rounded-r-sm" />
        <div className="absolute left-0 top-44 w-1 h-14 bg-zinc-700 rounded-r-sm" />

        {/* Screen Container */}
        <div
          className="relative bg-black rounded-[2.2rem] overflow-hidden"
          style={{ width, height }}
        >
          {/* Dynamic Island */}
          <div className="absolute top-3 left-1/2 -translate-x-1/2 w-28 h-7 bg-black rounded-full z-50" />

          {/* Status Bar */}
          <div className="absolute top-0 left-0 right-0 h-12 px-6 flex items-center justify-between bg-zinc-900/80 backdrop-blur-sm z-40">
            <span className="text-white text-xs font-medium">9:41</span>
            <div className="flex items-center gap-1.5">
              <span className="text-white text-xs">5G</span>
              <svg className="w-4 h-4 text-white" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 3C7.46 3 3.34 4.78.29 7.67c-.18.18-.29.43-.29.71s.11.53.29.71l2.48 2.48c.18.18.43.29.71.29s.53-.11.71-.29l2.22-2.22C7.38 11.43 9.56 10.5 12 10.5s4.62.93 6.34 2.34l2.22 2.22c.18.18.43.29.71.29s.53-.11.71-.29l2.48-2.48c.18-.18.29-.43.29-.71s-.11-.53-.29-.71C20.66 4.78 16.54 3 12 3z"/>
                <path d="M12 18c-3.54 0-6.62-1.77-8.49-4.43l2.48-2.48c1.34 1.94 3.63 3.41 6.01 3.41s4.67-1.47 6.01-3.41l2.48 2.48C18.62 16.23 15.54 18 12 18z"/>
              </svg>
              <div className="w-6 h-3 flex items-end gap-px">
                <div className="w-0.5 h-1.5 bg-white rounded-sm" />
                <div className="w-0.5 h-2 bg-white rounded-sm" />
                <div className="w-0.5 h-2.5 bg-white rounded-sm" />
                <div className="w-0.5 h-3 bg-white rounded-sm" />
              </div>
              <div className="relative w-6 h-3">
                <div className="absolute inset-0 border border-white rounded-sm" />
                <div className="absolute right-0 top-0.5 bottom-0.5 w-4 bg-white rounded-sm" />
                <div className="absolute right-0.5 top-0.5 bottom-0.5 w-[2px] bg-zinc-900" />
              </div>
            </div>
          </div>

          {/* App Content */}
          <div className="pt-7 pb-14 h-full overflow-hidden">
            {children}
          </div>

          {/* Navigation Bar */}
          <div className="absolute bottom-1 left-1/2 -translate-x-1/2 w-32 h-7 bg-white/90 rounded-full shadow-lg z-40" />
        </div>
      </div>

      {/* Model Label */}
      <div className="text-center mt-4 text-xs text-zinc-500">
        {model}
      </div>
    </div>
  )
}
