import { useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { PhoneSimulator } from '@/components/showcase/PhoneSimulator'
import { PhotoGrid } from '@/components/album/PhotoGrid'
import { EditorShell } from '@/components/editor/EditorShell'
import { AdjustPanel } from '@/components/panels/AdjustPanel'
import { CurvesPanel } from '@/components/panels/CurvesPanel'
import { HSLPanel } from '@/components/panels/HSLPanel'
import { GradingPanel } from '@/components/panels/GradingPanel'
import { DetailsPanel } from '@/components/panels/DetailsPanel'
import { EffectsPanel } from '@/components/panels/EffectsPanel'
import { GeometryPanel } from '@/components/panels/GeometryPanel'
import { AIPanel } from '@/components/panels/AIPanel'
import { PresetsPanel } from '@/components/panels/PresetsPanel'
import { ExportPanel } from '@/components/panels/ExportPanel'
import { useEditorStore } from '@/stores/useEditorStore'
import { cn } from '@/lib/utils'

const SCREENS = [
  { id: 'album', label: '相册' },
  { id: 'editor', label: '编辑' },
  { id: 'adjust', label: '调整' },
  { id: 'curves', label: '曲线' },
  { id: 'hsl', label: 'HSL' },
  { id: 'grading', label: '影调' },
  { id: 'details', label: '细节' },
  { id: 'effects', label: '效果' },
  { id: 'geometry', label: '几何' },
  { id: 'ai', label: 'AI' },
  { id: 'presets', label: '预设' },
  { id: 'export', label: '导出' },
] as const

type ScreenId = typeof SCREENS[number]['id']

function PanelScreen({ panelId }: { panelId: string }) {
  const setActivePanel = useEditorStore((s) => s.setActivePanel)

  const PanelComponents: Record<string, React.ComponentType> = {
    adjust: AdjustPanel,
    curves: CurvesPanel,
    hsl: HSLPanel,
    grading: GradingPanel,
    details: DetailsPanel,
    effects: EffectsPanel,
    geometry: GeometryPanel,
    ai: AIPanel,
    presets: PresetsPanel,
    export: ExportPanel,
  }

  const PanelComponent = PanelComponents[panelId]

  return (
    <div className="h-full flex flex-col bg-zinc-950">
      {/* Mini Topbar */}
      <div className="flex items-center gap-3 px-4 h-12 bg-zinc-900/80 backdrop-blur-md border-b border-zinc-800">
        <svg className="w-5 h-5 text-orange-500" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
        </svg>
        <span className="text-white text-sm font-medium">DSC_0001.ARW</span>
        <span className="text-zinc-500 text-xs">2026年6月</span>
      </div>

      {/* Preview Area */}
      <div className="flex-1 relative">
        <div className="absolute inset-4 rounded-xl overflow-hidden bg-gradient-to-br from-blue-900 via-green-800 to-orange-700">
          <img 
            src="https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&q=80" 
            alt="Preview"
            className="w-full h-full object-cover"
          />
        </div>
      </div>

      {/* Mini Histogram */}
      <div className="px-4 py-2 bg-zinc-900/60">
        <div className="flex items-end h-8 gap-px">
          {[40, 55, 45, 70, 85, 75, 60, 80, 65, 50, 35, 45, 55, 70, 60, 45, 30, 25, 40, 55, 65, 75, 85, 70].map((h, i) => (
            <div 
              key={i} 
              className="flex-1 bg-zinc-600 rounded-t-sm"
              style={{ height: `${h}%` }}
            />
          ))}
        </div>
      </div>

      {/* Panel Content */}
      <div className="h-64 bg-zinc-900">
        {PanelComponent && <PanelComponent />}
      </div>

      {/* Mini Bottom Toolbar */}
      <div className="flex items-center justify-around h-14 bg-zinc-900/90 border-t border-zinc-800">
        <button className="text-zinc-400 text-xs">返回</button>
        <button className="text-orange-500 text-xs font-medium">应用</button>
        <button className="text-zinc-400 text-xs">重置</button>
      </div>
    </div>
  )
}

export default function MobileShowcase() {
  const [viewMode, setViewMode] = useState<'single' | 'dual'>('single')
  const [currentScreen, setCurrentScreen] = useState<ScreenId>('album')
  const [direction, setDirection] = useState(0)

  const currentIndex = SCREENS.findIndex((s) => s.id === currentScreen)

  const handlePrev = () => {
    if (currentIndex > 0) {
      setDirection(-1)
      setCurrentScreen(SCREENS[currentIndex - 1].id)
    }
  }

  const handleNext = () => {
    if (currentIndex < SCREENS.length - 1) {
      setDirection(1)
      setCurrentScreen(SCREENS[currentIndex + 1].id)
    }
  }

  const renderScreenContent = (screenId: ScreenId) => {
    switch (screenId) {
      case 'album':
        return <PhotoGrid />
      case 'editor':
        return <EditorShell />
      default:
        return <PanelScreen panelId={screenId} />
    }
  }

  return (
    <div className="min-h-screen bg-zinc-950 flex flex-col">
      {/* Header */}
      <header className="flex items-center justify-between px-6 py-4 border-b border-zinc-800">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-gradient-to-br from-orange-500 to-orange-600 rounded-lg flex items-center justify-center">
            <span className="text-white text-sm font-bold">D</span>
          </div>
          <h1 className="text-white text-lg font-semibold">口袋暗房 · Android UI Preview</h1>
        </div>

        {/* View Mode Toggle */}
        <div className="flex items-center gap-1 bg-zinc-800 rounded-lg p-1">
          <button
            onClick={() => setViewMode('single')}
            className={cn(
              'px-3 py-1.5 rounded-md text-xs font-medium transition-colors',
              viewMode === 'single' ? 'bg-orange-500 text-white' : 'text-zinc-400'
            )}
          >
            单机
          </button>
          <button
            onClick={() => setViewMode('dual')}
            className={cn(
              'px-3 py-1.5 rounded-md text-xs font-medium transition-colors',
              viewMode === 'dual' ? 'bg-orange-500 text-white' : 'text-zinc-400'
            )}
          >
            双机
          </button>
        </div>
      </header>

      {/* Screen Navigation Pills */}
      <div className="relative px-6 py-3 border-b border-zinc-800">
        <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-hide">
          {SCREENS.map((screen) => (
            <button
              key={screen.id}
              onClick={() => {
                setDirection(screen.id > currentScreen ? 1 : -1)
                setCurrentScreen(screen.id)
              }}
              className={cn(
                'px-4 py-2 rounded-full text-xs font-medium whitespace-nowrap transition-colors',
                currentScreen === screen.id
                  ? 'bg-orange-500 text-white'
                  : 'bg-zinc-800 text-zinc-400 hover:bg-zinc-700'
              )}
            >
              {screen.label}
            </button>
          ))}
        </div>

        {/* Navigation Arrows */}
        <button
          onClick={handlePrev}
          disabled={currentIndex === 0}
          className="absolute left-1 top-1/2 -translate-y-1/2 w-8 h-8 bg-zinc-800 rounded-full flex items-center justify-center disabled:opacity-30"
        >
          <svg className="w-4 h-4 text-white" viewBox="0 0 24 24" fill="currentColor">
            <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
          </svg>
        </button>
        <button
          onClick={handleNext}
          disabled={currentIndex === SCREENS.length - 1}
          className="absolute right-1 top-1/2 -translate-y-1/2 w-8 h-8 bg-zinc-800 rounded-full flex items-center justify-center disabled:opacity-30"
        >
          <svg className="w-4 h-4 text-white" viewBox="0 0 24 24" fill="currentColor">
            <path d="M8.59 16.59L10 18l6-6-6-6-1.41 1.41L13.17 12z"/>
          </svg>
        </button>
      </div>

      {/* Phone Simulators */}
      <div className="flex-1 flex items-center justify-center p-8 overflow-hidden">
        <AnimatePresence mode="wait" initial={false}>
          <motion.div
            key={currentScreen}
            initial={{ opacity: 0, x: direction * 100 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: direction * -100 }}
            transition={{ duration: 0.3 }}
            className={viewMode === 'dual' ? 'flex gap-8' : ''}
          >
            <PhoneSimulator>
              {renderScreenContent(currentScreen)}
            </PhoneSimulator>
            {viewMode === 'dual' && (
              <PhoneSimulator model="Find X8 Pro">
                {renderScreenContent(currentScreen)}
              </PhoneSimulator>
            )}
          </motion.div>
        </AnimatePresence>
      </div>

      {/* Feature Badges */}
      <footer className="px-6 py-4 border-t border-zinc-800">
        <div className="flex items-center justify-center gap-3 flex-wrap">
          <span className="px-3 py-1 bg-orange-500/20 text-orange-500 text-xs font-medium rounded-full">
            哈苏橙 #FF6B2B
          </span>
          <span className="px-3 py-1 bg-zinc-800 text-zinc-300 text-xs rounded-full">
            液态玻璃
          </span>
          <span className="px-3 py-1 bg-zinc-800 text-zinc-300 text-xs rounded-full">
            DM Sans
          </span>
          <span className="px-3 py-1 bg-zinc-800 text-zinc-300 text-xs rounded-full">
            ColorOS 16
          </span>
          <span className="px-3 py-1 bg-zinc-800 text-zinc-300 text-xs rounded-full">
            90Hz
          </span>
          <span className="px-3 py-1 bg-zinc-800 text-zinc-300 text-xs rounded-full">
            Android 16
          </span>
        </div>
      </footer>
    </div>
  )
}
