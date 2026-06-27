import { create } from 'zustand';
import { Adjustments, MaskDefinition, INITIAL_ADJUSTMENTS } from '../utils/adjustments';

interface HistogramData {
  r: number[];
  g: number[];
  b: number[];
  luminance: number[];
}

interface EditHistoryEntry {
  adjustments: Adjustments;
  timestamp: number;
}

interface EditorState {
  adjustments: Adjustments;
  selectedPhoto: string | null;
  activePanel: 'light' | 'color' | 'effects' | 'crop' | 'masks' | 'curves' | 'hsl' | 'colorGrading' | 'export' | null;
  histogramData: HistogramData | null;
  isBeforeAfter: boolean;
  zoom: number;
  undoStack: Adjustments[];
  redoStack: Adjustments[];
  selectedPhotos: Set<string>;
  isMultiSelectMode: boolean;
  isExporting: boolean;
  exportProgress: number;
  masks: MaskDefinition[];
  activeMaskId: string | null;
  detectedScene: string | null;
  isAnalyzing: boolean;
  editHistory: EditHistoryEntry[];
  beforeAfterPosition: number;
  isShowingOriginal: boolean;
}

interface EditorActions {
  updateAdjustment: <K extends keyof Adjustments>(key: K, value: Adjustments[K]) => void;
  setAdjustments: (adjustments: Adjustments) => void;
  setActivePanel: (panel: EditorState['activePanel']) => void;
  setSelectedPhoto: (photoId: string | null) => void;
  undo: () => void;
  redo: () => void;
  resetAdjustments: () => void;
  toggleBeforeAfter: () => void;
  setZoom: (zoom: number) => void;
  toggleMultiSelect: () => void;
  togglePhotoSelection: (photoId: string) => void;
  selectAllPhotos: (photoIds: string[]) => void;
  clearSelection: () => void;
  setExporting: (isExporting: boolean) => void;
  setExportProgress: (progress: number) => void;
  addMask: (mask: MaskDefinition) => void;
  removeMask: (maskId: string) => void;
  toggleMaskVisibility: (maskId: string) => void;
  setActiveMaskId: (maskId: string | null) => void;
  setDetectedScene: (scene: string | null) => void;
  setIsAnalyzing: (isAnalyzing: boolean) => void;
  addEditHistory: (entry: EditHistoryEntry) => void;
  setBeforeAfterPosition: (position: number) => void;
  setIsShowingOriginal: (showing: boolean) => void;
}

type EditorStore = EditorState & EditorActions;

const MAX_UNDO_STACK = 50;

export const useEditorStore = create<EditorStore>((set, get) => ({
  adjustments: { ...INITIAL_ADJUSTMENTS },
  selectedPhoto: null,
  activePanel: null,
  histogramData: null,
  isBeforeAfter: false,
  zoom: 1,
  undoStack: [],
  redoStack: [],
  selectedPhotos: new Set(),
  isMultiSelectMode: false,
  isExporting: false,
  exportProgress: 0,
  masks: [],
  activeMaskId: null,
  detectedScene: null,
  isAnalyzing: false,
  editHistory: [],
  beforeAfterPosition: 50,
  isShowingOriginal: false,

  updateAdjustment: (key, value) => {
    const { adjustments, undoStack } = get();
    const newUndoStack = [...undoStack, { ...adjustments }].slice(-MAX_UNDO_STACK);
    set({
      adjustments: { ...adjustments, [key]: value },
      undoStack: newUndoStack,
      redoStack: [],
    });
  },

  setAdjustments: (adjustments) => {
    const { undoStack } = get();
    const newUndoStack = [...undoStack, { ...get().adjustments }].slice(-MAX_UNDO_STACK);
    set({
      adjustments,
      undoStack: newUndoStack,
      redoStack: [],
    });
  },

  setActivePanel: (panel) => set({ activePanel: panel }),

  setSelectedPhoto: (photoId) => set({ selectedPhoto: photoId }),

  undo: () => {
    const { undoStack, adjustments, redoStack } = get();
    if (undoStack.length === 0) return;
    const previous = undoStack[undoStack.length - 1];
    const newUndoStack = undoStack.slice(0, -1);
    const newRedoStack = [...redoStack, { ...adjustments }].slice(-MAX_UNDO_STACK);
    set({
      adjustments: previous,
      undoStack: newUndoStack,
      redoStack: newRedoStack,
    });
  },

  redo: () => {
    const { undoStack, adjustments, redoStack } = get();
    if (redoStack.length === 0) return;
    const next = redoStack[redoStack.length - 1];
    const newRedoStack = redoStack.slice(0, -1);
    const newUndoStack = [...undoStack, { ...adjustments }].slice(-MAX_UNDO_STACK);
    set({
      adjustments: next,
      undoStack: newUndoStack,
      redoStack: newRedoStack,
    });
  },

  resetAdjustments: () => {
    const { adjustments, undoStack } = get();
    const newUndoStack = [...undoStack, { ...adjustments }].slice(-MAX_UNDO_STACK);
    set({
      adjustments: { ...INITIAL_ADJUSTMENTS },
      undoStack: newUndoStack,
      redoStack: [],
    });
  },

  toggleBeforeAfter: () => set((state) => ({ isBeforeAfter: !state.isBeforeAfter })),

  setZoom: (zoom) => set({ zoom: Math.max(0.1, Math.min(10, zoom)) }),

  toggleMultiSelect: () => set((state) => ({ isMultiSelectMode: !state.isMultiSelectMode })),

  togglePhotoSelection: (photoId) => {
    const { selectedPhotos } = get();
    const newSelected = new Set(selectedPhotos);
    if (newSelected.has(photoId)) {
      newSelected.delete(photoId);
    } else {
      newSelected.add(photoId);
    }
    set({ selectedPhotos: newSelected });
  },

  selectAllPhotos: (photoIds) => set({ selectedPhotos: new Set(photoIds) }),

  clearSelection: () => set({ selectedPhotos: new Set(), isMultiSelectMode: false }),

  setExporting: (isExporting) => set({ isExporting }),

  setExportProgress: (progress) => set({ exportProgress: progress }),

  addMask: (mask) => {
    const { masks, adjustments } = get();
    const newMasks = [...masks, mask];
    set({
      masks: newMasks,
      adjustments: { ...adjustments, masks: newMasks },
    });
  },

  removeMask: (maskId) => {
    const { masks, adjustments, activeMaskId } = get();
    const newMasks = masks.filter((m) => m.id !== maskId);
    set({
      masks: newMasks,
      activeMaskId: activeMaskId === maskId ? null : activeMaskId,
      adjustments: { ...adjustments, masks: newMasks },
    });
  },

  toggleMaskVisibility: (maskId) => {
    const { masks, adjustments } = get();
    const newMasks = masks.map((m) =>
      m.id === maskId ? { ...m, visible: !m.visible } : m
    );
    set({
      masks: newMasks,
      adjustments: { ...adjustments, masks: newMasks },
    });
  },

  setActiveMaskId: (maskId) => set({ activeMaskId: maskId }),

  setDetectedScene: (scene) => set({ detectedScene: scene }),

  setIsAnalyzing: (analyzing) => set({ isAnalyzing: analyzing }),

  addEditHistory: (entry) => {
    const { editHistory } = get();
    const newHistory = [...editHistory, entry].slice(-100);
    set({ editHistory: newHistory });
  },

  setBeforeAfterPosition: (position) => set({ beforeAfterPosition: position }),

  setIsShowingOriginal: (showing) => set({ isShowingOriginal: showing }),
}));
