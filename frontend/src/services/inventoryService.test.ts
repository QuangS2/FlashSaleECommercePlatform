import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { inventoryService } from './inventoryService';

describe('inventoryService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('tra cứu tồn kho khả dụng từ Inventory Service thành công', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve({ productId: 'p1', availableQuantity: 35 }),
        })
      )
    );

    const stock = await inventoryService.fetchStock('p1');
    expect(stock).toBe(35);
  });

  it('kiểm tra đủ hàng isInStock thành công', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(true),
        })
      )
    );

    const isAvailable = await inventoryService.checkStock('p1', 2);
    expect(isAvailable).toBe(true);
  });

  it('khấu trừ tồn kho Redisson Lock thành công', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          status: 200,
        })
      )
    );

    const result = await inventoryService.deductStock('p1', 1);
    expect(result).toBe(true);
  });
});
