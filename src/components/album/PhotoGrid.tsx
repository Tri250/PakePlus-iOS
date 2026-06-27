import { useState } from 'react'
import { cn } from '@/lib/utils'

interface Photo {
  id: number
  filename: string
  date: string
  type: 'RAW' | 'JPG'
  rating: number
  focal: string
}

const MOCK_PHOTOS: Photo[] = Array.from({ length: 24 }, (_, i) => ({
  id: i + 1,
  filename: `DSC_${String(i + 1).padStart(4, '0')}.ARW`,
  date: '2026年6月',
  type: i % 3 === 0 ? 'JPG' : 'RAW',
  rating: Math.floor(Math.random() * 5) + 1,
  focal: `${[24, 35, 50, 85, 135][i % 5]}mm`,
}))

const FILTERS = ['全部', 'RAW', 'JPG', '收藏'] as const
type FilterType = typeof FILTERS[number]

export function PhotoGrid() {
  const [activeFilter, setActiveFilter] = useState<FilterType>('全部')
  const [selectedPhoto, setSelectedPhoto] = useState<number | null>(null)

  const filteredPhotos = MOCK_PHOTOS.filter((photo) => {
    if (activeFilter === '全部') return true
    if (activeFilter === 'RAW') return photo.type === 'RAW'
    if (activeFilter === 'JPG') return photo.type === 'JPG'
    if (activeFilter === '收藏') return photo.rating >= 4
    return true
  })

  const handlePhotoClick = (photo: Photo) => {
    setSelectedPhoto(photo.id)
  }

  return (
    <div className="h-full flex flex-col bg-zinc-950">
      {/* Glass Topbar */}
      <div className="flex items-center justify-between px-4 h-14 bg-zinc-900/60 backdrop-blur-lg border-b border-zinc-800">
        <h2 className="text-white text-base font-semibold">相册</h2>
        <select className="bg-zinc-800 text-zinc-300 text-xs px-3 py-1.5 rounded-lg border border-zinc-700">
          <option>日期排序</option>
          <option>名称排序</option>
          <option>评分排序</option>
        </select>
      </div>

      {/* Filter Pills */}
      <div className="flex items-center gap-2 px-4 py-3 bg-zinc-900/40">
        {FILTERS.map((filter) => (
          <button
            key={filter}
            onClick={() => setActiveFilter(filter)}
            className={cn(
              'px-4 py-1.5 rounded-full text-xs font-medium transition-colors',
              activeFilter === filter
                ? 'bg-orange-500 text-white'
                : 'bg-zinc-800 text-zinc-400'
            )}
          >
            {filter}
          </button>
        ))}
      </div>

      {/* Photo Grid */}
      <div className="flex-1 overflow-y-auto p-3">
        {/* Date Section Header */}
        <div className="text-zinc-500 text-xs px-1 py-2 mb-2">2026年6月</div>
        
        <div className="grid grid-cols-3 gap-2">
          {filteredPhotos.map((photo) => (
            <button
              key={photo.id}
              onClick={() => handlePhotoClick(photo)}
              className={cn(
                'relative aspect-square rounded-lg overflow-hidden bg-zinc-800 group',
                selectedPhoto === photo.id && 'ring-2 ring-orange-500'
              )}
            >
              {/* Placeholder Image */}
              <div className="absolute inset-0 bg-gradient-to-br from-zinc-700 to-zinc-800">
                <img
                  src={`https://picsum.photos/seed/${photo.id}/200/200`}
                  alt={photo.filename}
                  className="w-full h-full object-cover opacity-80"
                />
              </div>

              {/* RAW Badge */}
              {photo.type === 'RAW' && (
                <div className="absolute top-1 left-1 px-1.5 py-0.5 bg-orange-500/90 text-white text-[10px] font-medium rounded">
                  RAW
                </div>
              )}

              {/* Bottom Info */}
              <div className="absolute bottom-0 left-0 right-0 p-1.5 bg-gradient-to-t from-black/60 to-transparent">
                {/* Stars */}
                <div className="flex items-center gap-0.5 mb-0.5">
                  {Array.from({ length: 5 }, (_, i) => (
                    <svg
                      key={i}
                      className={cn('w-2.5 h-2.5', i < photo.rating ? 'text-orange-400' : 'text-zinc-600')}
                      viewBox="0 0 24 24"
                      fill="currentColor"
                    >
                      <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                    </svg>
                  ))}
                </div>
                {/* Focal Length */}
                <span className="text-zinc-400 text-[10px]">{photo.focal}</span>
              </div>
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}
