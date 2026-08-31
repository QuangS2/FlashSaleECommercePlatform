import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useFlashSaleStore } from './useFlashSaleStore';
import { productService } from '../services/productService';
import { inventoryService } from '../services/inventoryService';

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

  it('cập nhật tồn kho sản phẩm (updateProductStock)', () => {
    useFlashSaleStore.getState().updateProductStock('fs-101', 5);
    const updated = useFlashSaleStore.getState().products.find((p) => p.id === 'fs-101');
    expect(updated?.remainingStock).toBe(5);
  });

  it('tải dữ liệu sản phẩm động (loadLiveProducts)', async () => {
    vi.spyOn(productService, 'fetchProducts').mockResolvedValue([
      {
        id: 'fs-101',
        name: 'Flash Sale Test',
        category: 'Điện thoại',
        originalPrice: 100,
        salePrice: 90,
        discountPercent: 10,
        soldCount: 10,
        stockCount: 20,
        imageUrl: '',
        rating: 5,
        description: '',
        isFlashSale: true,
      },
    ]);
    vi.spyOn(inventoryService, 'fetchStock').mockResolvedValue(15);

    await useFlashSaleStore.getState().loadLiveProducts();

    const state = useFlashSaleStore.getState();
    expect(state.products.length).toBe(1);
    expect(state.products[0].name).toBe('Flash Sale Test');
    expect(state.products[0].remainingStock).toBe(15);
  });
});
