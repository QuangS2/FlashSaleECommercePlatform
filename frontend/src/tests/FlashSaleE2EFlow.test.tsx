import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import App from '../App';
import { productService } from '../services/productService';
import { inventoryService } from '../services/inventoryService';
import { orderService } from '../services/orderService';
import { useCartStore } from '../store/useCartStore';
import { useOrderQueueStore } from '../store/useOrderQueueStore';

// Mock Keycloak
vi.mock('../auth/keycloak', () => ({
  default: {
    authenticated: true,
    tokenParsed: {
      sub: 'customer_1001',
      preferred_username: 'customer',
      name: 'Lê Văn Khách',
      email: 'customer@ecommerce.vn',
    },
    login: vi.fn(),
    logout: vi.fn(),
  },
  onAuthChange: vi.fn(() => () => {}),
  getKeycloakUrl: vi.fn(() => 'http://localhost:8180'),
}));

// Mock WebSocket
vi.mock('../hooks/useWebSocket', () => ({
  useWebSocket: () => ({
    isConnected: true,
    subscribe: vi.fn(),
  }),
}));

describe('E2E Full Flow: Khách hàng mua sắm Flash Sale và Đặt hàng Saga', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useCartStore.getState().clearCart();
    useOrderQueueStore.getState().resetQueue();

    vi.spyOn(productService, 'fetchProducts').mockResolvedValue([
      {
        id: 'fs-101',
        name: 'Điện thoại iPhone 15 Pro Max 256GB',
        category: 'Điện thoại',
        originalPrice: 34990000,
        salePrice: 28990000,
        discountPercent: 17,
        imageUrl: 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500',
        rating: 4.9,
        soldCount: 85,
        stockCount: 15,
        description: 'iPhone 15 Pro Max cao cấp',
        isFlashSale: true,
      },
      {
        id: 'cat-1',
        name: 'Laptop Apple MacBook Air M2',
        category: 'Laptop',
        originalPrice: 28990000,
        salePrice: 24490000,
        discountPercent: 15,
        imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500',
        rating: 4.9,
        soldCount: 320,
        stockCount: 45,
        description: 'MacBook Air M2 siêu mỏng nhẹ',
        isFlashSale: false,
      },
    ]);

    vi.spyOn(inventoryService, 'fetchStock').mockResolvedValue(15);

    vi.spyOn(orderService, 'getUserOrders').mockResolvedValue([
      {
        orderId: 'ORD-SAGA-999',
        status: 'COMPLETED',
        userId: 'customer_1001',
        productId: 'fs-101',
        productTitle: 'Điện thoại iPhone 15 Pro Max 256GB',
        quantity: 1,
        unitPrice: 28990000,
        totalPrice: 28990000,
        createdAt: new Date().toISOString(),
      },
    ]);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('USE CASE 1: Hiển thị giao diện, xác thực Keycloak và tải danh mục sản phẩm', async () => {
    render(<App />);

    // Kiểm tra tên người dùng hiển thị trên Header từ Keycloak Token
    expect(screen.getByText('Lê Văn Khách')).toBeInTheDocument();

    // Chờ tải sản phẩm hoàn tất từ Product Catalog
    await waitFor(() => {
      expect(screen.getByText('DANH MỤC SẢN PHẨM')).toBeInTheDocument();
    });
  });

  it('USE CASE 2 & 3: Thêm vào giỏ hàng, tính toán tổng tiền và áp dụng Voucher khuyến mãi', () => {
    const product = {
      id: 'cat-1',
      name: 'Laptop Apple MacBook Air M2',
      category: 'Laptop',
      originalPrice: 28990000,
      salePrice: 24490000,
      discountPercent: 15,
      imageUrl: '',
      rating: 4.9,
      soldCount: 320,
      stockCount: 45,
      description: '',
      isFlashSale: false,
    };

    // 1. Thêm 1 sản phẩm vào giỏ hàng
    useCartStore.getState().addItem(product, 1);
    expect(useCartStore.getState().getTotalCount()).toBe(1);
    expect(useCartStore.getState().getSubtotalPrice()).toBe(24490000);

    // 2. Cập nhật số lượng lên 2
    useCartStore.getState().updateQuantity('cat-1', 2);
    expect(useCartStore.getState().getTotalCount()).toBe(2);
    expect(useCartStore.getState().getSubtotalPrice()).toBe(48980000);

    // 3. Áp dụng mã Voucher 'FLASHSALE50' (Giảm 10% tối đa 100.000đ)
    const result = useCartStore.getState().applyVoucher('FLASHSALE50');
    expect(result.success).toBe(true);
    expect(useCartStore.getState().appliedVoucher?.code).toBe('FLASHSALE50');
    expect(useCartStore.getState().getDiscountAmount()).toBe(100000);
    expect(useCartStore.getState().getFinalPrice()).toBe(48880000);

    // 4. Gỡ bỏ Voucher
    useCartStore.getState().removeVoucher();
    expect(useCartStore.getState().appliedVoucher).toBeNull();
    expect(useCartStore.getState().getFinalPrice()).toBe(48980000);
  });

  it('USE CASE 4 & 5: Luồng Mua Ngay Flash Sale, Khởi tạo đơn hàng Saga và nhận thông báo hoàn tất', () => {
    const flashProduct = {
      id: 'fs-101',
      name: 'Điện thoại iPhone 15 Pro Max 256GB',
      category: 'Điện thoại',
      originalPrice: 34990000,
      salePrice: 28990000,
      discountPercent: 17,
      imageUrl: '',
      rating: 4.9,
      soldCount: 85,
      stockCount: 15,
      description: '',
      isFlashSale: true,
      slotId: 'slot-2',
      totalStock: 100,
      soldStock: 85,
      remainingStock: 15,
      maxLimitPerUser: 1,
    };

    // 1. Người dùng chọn mua Flash Sale
    useCartStore.getState().addItem(flashProduct, 1);
    expect(useCartStore.getState().items.length).toBe(1);
    expect(useCartStore.getState().items[0].product.id).toBe('fs-101');

    // 2. Kích hoạt hàng chờ phân tán Saga (QueueModal state)
    useOrderQueueStore.getState().setQueueOpen(true);
    useOrderQueueStore.getState().setQueueStatus('WAITING');

    expect(useOrderQueueStore.getState().isQueueOpen).toBe(true);
    expect(useOrderQueueStore.getState().queueStatus).toBe('WAITING');

    // 3. Nhận sự kiện Saga hoàn thành qua WebSocket Notification
    useOrderQueueStore.getState().setQueueStatus('SUCCESS', 'ORD-SAGA-999');
    useCartStore.getState().clearCart();

    expect(useOrderQueueStore.getState().queueStatus).toBe('SUCCESS');
    expect(useOrderQueueStore.getState().orderId).toBe('ORD-SAGA-999');
    expect(useCartStore.getState().items.length).toBe(0);
    expect(useCartStore.getState().getTotalCount()).toBe(0);
  });

  it('USE CASE 6: Quản lý Lịch sử Đơn hàng và Tra cứu Tiến trình Xử lý Đơn hàng', async () => {
    render(<App />);

    // Mở Order History
    const orderBtn = screen.getByTitle('Xem lịch sử đơn hàng của tôi');
    fireEvent.click(orderBtn);

    await waitFor(() => {
      expect(screen.getByText('ORD-SAGA-999')).toBeInTheDocument();
    });

    // Mở Modal chi tiết để xem Tiến trình xử lý đơn hàng
    fireEvent.click(screen.getByText('ORD-SAGA-999'));

    expect(screen.getByText('CHI TIẾT ĐƠN HÀNG')).toBeInTheDocument();
    expect(screen.getByText(/Tiến trình xử lý đơn hàng/i)).toBeInTheDocument();
    expect(screen.getByText(/1. Tiếp nhận đơn hàng/i)).toBeInTheDocument();
    expect(screen.getByText(/2. Kiểm tra & Giữ hàng trong kho/i)).toBeInTheDocument();
  });
});
