import { describe, it, expect, beforeEach } from 'vitest';
import { useOrderQueueStore } from './useOrderQueueStore';

describe('useOrderQueueStore', () => {
  beforeEach(() => {
    useOrderQueueStore.setState({
      isQueueOpen: false,
      queueStatus: 'IDLE',
      orderId: undefined,
    });
  });

  it('thay đổi trạng thái mở/đóng queue', () => {
    useOrderQueueStore.getState().setQueueOpen(true);
    expect(useOrderQueueStore.getState().isQueueOpen).toBe(true);
    
    useOrderQueueStore.getState().setQueueOpen(false);
    expect(useOrderQueueStore.getState().isQueueOpen).toBe(false);
  });

  it('cập nhật trạng thái queue và orderId', () => {
    useOrderQueueStore.getState().setQueueStatus('SUCCESS', 'ORD-123');
    
    const state = useOrderQueueStore.getState();
    expect(state.queueStatus).toBe('SUCCESS');
    expect(state.orderId).toBe('ORD-123');
  });
});
