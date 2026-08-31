import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { orderService } from './orderService';

describe('orderService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('gửi yêu cầu tạo đơn hàng thành công tới Order-Service', async () => {
    const mockOrderResponse = {
      orderId: 'ORD-TEST-123456',
      status: 'PENDING',
    };

    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockOrderResponse),
        })
      )
    );

    const result = await orderService.createOrder({
      productId: 'prod-1',
      productTitle: 'Sản phẩm Test',
      quantity: 1,
      unitPrice: 100000,
    });

    expect(result.orderId).toBe('ORD-TEST-123456');
    expect(result.status).toBe('PENDING');
  });

  it('fallback tạo mã đơn khi Order-Service offline', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.reject(new Error('Connection refused')))
    );

    const result = await orderService.createOrder({
      productId: 'prod-1',
      productTitle: 'Sản phẩm Test',
      quantity: 1,
      unitPrice: 100000,
    });

    expect(result.orderId).toMatch(/^ORD-\d+/);
    expect(result.status).toBe('PENDING');
  });

  it('tra cứu chi tiết đơn hàng theo orderId', async () => {
    const mockDetail = {
      orderId: 'ORD-123',
      status: 'COMPLETED',
      userId: 'user-1',
      productId: 'p1',
      productTitle: 'Title',
      quantity: 1,
      unitPrice: 50000,
      totalPrice: 50000,
      createdAt: '2026-08-31T00:00:00Z',
    };

    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockDetail),
        })
      )
    );

    const detail = await orderService.getOrderById('ORD-123');
    expect(detail?.orderId).toBe('ORD-123');
    expect(detail?.status).toBe('COMPLETED');
  });

  it('lấy danh sách đơn hàng của người dùng', async () => {
    const mockOrders = [
      { orderId: 'ORD-1', status: 'COMPLETED' },
      { orderId: 'ORD-2', status: 'PENDING' },
    ];

    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockOrders),
        })
      )
    );

    const orders = await orderService.getUserOrders('user-1');
    expect(orders.length).toBe(2);
    expect(orders[0].orderId).toBe('ORD-1');
  });
});
