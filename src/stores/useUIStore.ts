import { create } from 'zustand';

interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

interface UIState {
  currentPage: 'gallery' | 'editor' | 'export' | 'settings';
  showExportModal: boolean;
  showWatermarkEditor: boolean;
  isPanelCollapsed: boolean;
  showExifPanel: boolean;
  showEditTimeline: boolean;
  toastMessages: Toast[];
}

interface UIActions {
  setCurrentPage: (page: UIState['currentPage']) => void;
  setShowExportModal: (show: boolean) => void;
  setShowWatermarkEditor: (show: boolean) => void;
  togglePanel: () => void;
  toggleExifPanel: () => void;
  toggleEditTimeline: () => void;
  addToast: (message: string, type?: Toast['type']) => void;
  removeToast: (id: string) => void;
}

type UIStore = UIState & UIActions;

let toastIdCounter = 0;

export const useUIStore = create<UIStore>((set, get) => ({
  currentPage: 'gallery',
  showExportModal: false,
  showWatermarkEditor: false,
  isPanelCollapsed: false,
  showExifPanel: false,
  showEditTimeline: false,
  toastMessages: [],

  setCurrentPage: (page) => set({ currentPage: page }),

  setShowExportModal: (show) => set({ showExportModal: show }),

  setShowWatermarkEditor: (show) => set({ showWatermarkEditor: show }),

  togglePanel: () => set((state) => ({ isPanelCollapsed: !state.isPanelCollapsed })),

  toggleExifPanel: () => set((state) => ({ showExifPanel: !state.showExifPanel })),

  toggleEditTimeline: () => set((state) => ({ showEditTimeline: !state.showEditTimeline })),

  addToast: (message, type = 'info') => {
    const id = `toast-${++toastIdCounter}`;
    const { toastMessages } = get();
    const newToast: Toast = { id, message, type };
    set({ toastMessages: [...toastMessages, newToast] });

    setTimeout(() => {
      get().removeToast(id);
    }, 3000);
  },

  removeToast: (id) => {
    const { toastMessages } = get();
    set({ toastMessages: toastMessages.filter((t) => t.id !== id) });
  },
}));
