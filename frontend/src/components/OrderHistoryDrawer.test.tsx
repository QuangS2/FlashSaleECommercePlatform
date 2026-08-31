import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { OrderHistoryDrawer } from './OrderHistoryDrawer';
import { orderService } from '../services/orderService';

// Mock Keycloak
vi.mock('../auth/keycloak', () => ({
  default: {
    authenticated: true,
    tokenParsed: {
      sub: 'test_user_id',
      name: 'Test Customer',
    },
  },
}));

import { useAuthStore } from '../store/useAuthStore';

describe('OrderHistoryDrawer Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    useAuthStore.setState({
      isAuthenticated: true,
      user: {
        sub: 'test_user_id',
        name: 'Test Customer',
        username: 'customer',
        email: 'customer@ecommerce.vn',
        roles: ['ROLE_CUSTOMER'],
      },
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('không render khi isOpen là false', () => {
    const { container } = render(
      <OrderHistoryDrawer isOpen={false} onClose={vi.fn()} onSelectOrder={vi.fn()} />
    );
    expect(container.firstChild).toBeNull();
  });

  it('render danh sách đơn hàng lấy từ orderService', async () => {
    const mockOrders = [
      {
        orderId: 'ORD-1001',
        status: 'COMPLETED',
        userId: 'test_user_id',
        productId: 'prod-1',
        productTitle: 'Điện thoại iPhone 15',
        quantity: 1,
        unitPrice: 20000000,
        totalPrice: 20000000,
        createdAt: '2026-08-31T00:00:00Z',
      },
    ];

    vi.spyOn(orderService, 'getUserOrders').mockResolvedValue(mockOrders);

    render(<OrderHistoryDrawer isOpen={true} onClose={vi.fn()} onSelectOrder={vi.fn()} />);

    expect(screen.getByText(/ĐƠN HÀNG CỦA TÔI/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('ORD-1001')).toBeInTheDocument();
      expect(screen.getByText(/Điện thoại iPhone 15/i)).toBeInTheDocument();
    });
  });

  it('gọi onSelectOrder khi người dùng bấm vào một đơn hàng', async () => {
    const mockOrders = [
      {
        orderId: 'ORD-1001',
        status: 'COMPLETED',
        userId: 'test_user_id',
        productId: 'prod-1',
        productTitle: 'Điện thoại iPhone 15',
        quantity: 1,
        unitPrice: 20000000,
        totalPrice: 20000000,
        createdAt: '2026-08-31T00:00:00Z',
      },
    ];

    vi.spyOn(orderService, 'getUserOrders').mockResolvedValue(mockOrders);
    const onSelectSpy = vi.fn();

    render(<OrderHistoryDrawer isOpen={true} onClose={vi.fn()} onSelectOrder={onSelectSpy} />);

    await waitFor(() => {
      expect(screen.getByText('ORD-1001')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('ORD-1001'));
    expect(onSelectSpy).toHaveBeenCalledWith(mockOrders[0]);
  });

  it('lọc danh sách theo tab trạng thái', async () => {
    const mockOrders = [
      {
        orderId: 'ORD-COMPLETED',
        status: 'COMPLETED',
        userId: 'test_user_id',
        productId: 'prod-1',
        productTitle: 'SP Hoàn tất',
        quantity: 1,
        unitPrice: 1000,
        totalPrice: 1000,
        createdAt: '2026-08-31T00:00:00Z',
      },
      {
        orderId: 'ORD-PENDING',
        status: 'PENDING',
        userId: 'test_user_id',
        productId: 'prod-2',
        productTitle: 'SP Đang xử lý',
        quantity: 1,
        unitPrice: 2000,
        totalPrice: 2000,
        createdAt: '2026-08-31T00:00:00Z',
      },
    ];

    vi.spyOn(orderService, 'getUserOrders').mockResolvedValue(mockOrders);

    render(<OrderHistoryDrawer isOpen={true} onClose={vi.fn()} onSelectOrder={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('ORD-COMPLETED')).toBeInTheDocument();
      expect(screen.getByText('ORD-PENDING')).toBeInTheDocument();
    });

    // Chuyển sang tab "Hoàn tất"
    fireEvent.click(screen.getByText('Hoàn tất'));
    expect(screen.getByText('ORD-COMPLETED')).toBeInTheDocument();
    expect(screen.queryByText('ORD-PENDING')).not.toBeInTheDocument();
  });
});
