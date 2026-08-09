import { create } from 'zustand';

interface OrderQueueState {
  isQueueOpen: boolean;
  queueStatus: 'WAITING' | 'SUCCESS' | 'ERROR';
  orderId: string | null;
  setQueueOpen: (open: boolean) => void;
  setQueueStatus: (status: 'WAITING' | 'SUCCESS' | 'ERROR', orderId?: string) => void;
  resetQueue: () => void;
}

export const useOrderQueueStore = create<OrderQueueState>((set) => ({
  isQueueOpen: false,
  queueStatus: 'WAITING',
  orderId: null,
  
  setQueueOpen: (open) => set({ isQueueOpen: open }),
  
  setQueueStatus: (status, orderId) => set((state) => ({ 
    queueStatus: status,
    orderId: orderId || state.orderId 
  })),
  
  resetQueue: () => set({ 
    isQueueOpen: false, 
    queueStatus: 'WAITING', 
    orderId: null 
  }),
}));
