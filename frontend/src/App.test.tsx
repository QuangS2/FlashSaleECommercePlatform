import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import App from './App';
import { productService } from './services/productService';
import { orderService } from './services/orderService';
import { useCartStore } from './store/useCartStore';
import { useOrderQueueStore } from './store/useOrderQueueStore';

// Mock Keycloak
vi.mock('./auth/keycloak', () => ({
  default: {
    authenticated: true,
    tokenParsed: {
      sub: 'customer_1001',
      name: 'Lê Văn Khách',
      email: 'customer@ecommerce.vn',
    },
    login: vi.fn(),
    logout: vi.fn(),
  },
}));

// Mock WebSocket
vi.mock('./hooks/useWebSocket', () => ({
  useWebSocket: () => ({
    isConnected: true,
    subscribe: vi.fn(),
  }),
}));

describe('App Main Component Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useCartStore.getState().clearCart();
    useOrderQueueStore.getState().resetQueue();

    vi.spyOn(productService, 'fetchProducts').mockResolvedValue([
      {
        id: 'p-app-1',
        name: 'Tai nghe Sony WH-1000XM5',
        category: 'Phụ kiện',
        originalPrice: 8990000,
        salePrice: 6990000,
        discountPercent: 22,
        imageUrl: 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500',
        rating: 4.8,
        soldCount: 45,
        stockCount: 15,
        description: 'Tai nghe chống ồn đỉnh cao',
        isFlashSale: false,
      },
    ]);

    vi.spyOn(orderService, 'getUserOrders').mockResolvedValue([
      {
        orderId: 'ORD-APP-TEST-001',
        status: 'COMPLETED',
        userId: 'customer_1001',
        productId: 'p-app-1',
        productTitle: 'Tai nghe Sony WH-1000XM5',
        quantity: 1,
        unitPrice: 6990000,
        totalPrice: 6990000,
        createdAt: new Date().toISOString(),
      },
    ]);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('render giao diện trang chủ đầy đủ các thành phần chính', async () => {
    render(<App />);

    expect(screen.getAllByText('FLSALE').length).toBeGreaterThan(0);
    expect(screen.getByText('Lê Văn Khách')).toBeInTheDocument();
    expect(screen.getByText('Giao Hàng Siêu Tốc 2H')).toBeInTheDocument();
    expect(screen.getAllByText('100% Hàng Chính Hãng').length).toBeGreaterThan(0);
    expect(screen.getByText('30 Ngày Đổi Trả')).toBeInTheDocument();
    expect(screen.getByText('Hỗ Trợ Tận Tâm 24/7')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('DANH MỤC SẢN PHẨM')).toBeInTheDocument();
    });
  });

  it('mở OrderHistoryDrawer khi người dùng nhấn nút Đơn mua trên Header', async () => {
    render(<App />);

    const orderBtn = screen.getByTitle('Xem lịch sử đơn hàng của tôi');
    fireEvent.click(orderBtn);

    expect(screen.getByText(/ĐƠN HÀNG CỦA TÔI/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('ORD-APP-TEST-001')).toBeInTheDocument();
    });
  });

  it('mở CartDrawer khi nhấn vào nút Giỏ hàng trên Header', () => {
    render(<App />);

    const cartBtn = screen.getByRole('button', { name: /giỏ hàng/i });
    fireEvent.click(cartBtn);

    expect(screen.getByRole('heading', { name: /giỏ hàng của bạn/i })).toBeInTheDocument();
  });

  it('mở OrderDetailModal khi người dùng chọn một đơn hàng từ OrderHistoryDrawer', async () => {
    render(<App />);

    // Mở Order History
    const orderBtn = screen.getByTitle('Xem lịch sử đơn hàng của tôi');
    fireEvent.click(orderBtn);

    await waitFor(() => {
      expect(screen.getByText('ORD-APP-TEST-001')).toBeInTheDocument();
    });

    // Bấm vào đơn hàng
    fireEvent.click(screen.getByText('ORD-APP-TEST-001'));

    // Kiểm tra OrderDetailModal xuất hiện
    expect(screen.getByText('CHI TIẾT ĐƠN HÀNG')).toBeInTheDocument();
    expect(screen.getByText(/Tiến trình chuỗi giao dịch phân tán/i)).toBeInTheDocument();
    expect(screen.getAllByText('ORD-APP-TEST-001').length).toBeGreaterThanOrEqual(1);
  });
});
