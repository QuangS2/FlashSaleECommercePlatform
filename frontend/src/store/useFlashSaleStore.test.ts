import { describe, it, expect, beforeEach } from 'vitest';
import { useFlashSaleStore } from './useFlashSaleStore';

describe('useFlashSaleStore', () => {
  beforeEach(() => {
    // Reset state before each test
    useFlashSaleStore.setState({
      activeSlotId: 'slot-1',
      slots: [
        { id: 'slot-1', startTime: '12:00', endTime: '14:00', label: '12:00 - 14:00', status: 'ACTIVE' },
        { id: 'slot-2', startTime: '14:00', endTime: '16:00', label: '14:00 - 16:00', status: 'UPCOMING' },
      ],
    });
  });

  it('thay đổi slot đang active (setActiveSlot)', () => {
    useFlashSaleStore.getState().setActiveSlot('slot-2');
    const state = useFlashSaleStore.getState();
    expect(state.activeSlotId).toBe('slot-2');
  });

  it('lấy số lượng sản phẩm flash sale hiện tại', () => {
    const products = useFlashSaleStore.getState().products;
    expect(products.length).toBeGreaterThan(0);
  });
});
