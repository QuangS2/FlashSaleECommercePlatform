import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { OrderDetailModal } from './OrderDetailModal';
import { OrderDetailResponse } from '../services/orderService';

describe('OrderDetailModal Component', () => {
  const mockOrder: OrderDetailResponse = {
    orderId: 'ORD-999-TEST',
    status: 'COMPLETED',
    userId: 'user-123',
    productId: 'p1',
    productTitle: 'Điện thoại iPhone 15 Pro Max',
    quantity: 1,
    unitPrice: 28990000,
    totalPrice: 28990000,
    createdAt: '2026-08-31T00:00:00Z',
  };

  it('không render khi order là null', () => {
    const { container } = render(<OrderDetailModal order={null} onClose={vi.fn()} />);
    expect(container.firstChild).toBeNull();
  });

  it('render thông tin chi tiết đơn hàng và tiến trình xử lý đơn hàng', () => {
    render(<OrderDetailModal order={mockOrder} onClose={vi.fn()} />);

    expect(screen.getByText('CHI TIẾT ĐƠN HÀNG')).toBeInTheDocument();
    expect(screen.getByText('ORD-999-TEST')).toBeInTheDocument();
    expect(screen.getByText(/Điện thoại iPhone 15 Pro Max/i)).toBeInTheDocument();
    expect(screen.getByText(/Tiến trình xử lý đơn hàng/i)).toBeInTheDocument();
    expect(screen.getByText(/1. Tiếp nhận đơn hàng/i)).toBeInTheDocument();
    expect(screen.getByText(/2. Kiểm tra & Giữ hàng trong kho/i)).toBeInTheDocument();
    expect(screen.getByText(/3. Xác nhận thanh toán/i)).toBeInTheDocument();
    expect(screen.getByText(/4. Đơn hàng hoàn tất & Chuẩn bị giao/i)).toBeInTheDocument();
  });

  it('gọi hàm onClose khi bấm nút đóng', () => {
    const onCloseSpy = vi.fn();
    render(<OrderDetailModal order={mockOrder} onClose={onCloseSpy} />);

    const closeBtn = screen.getByText('ĐÓNG');
    fireEvent.click(closeBtn);

    expect(onCloseSpy).toHaveBeenCalled();
  });

  it('hiển thị badge trạng thái ĐÃ HỦY khi status = CANCELLED', () => {
    const cancelledOrder = { ...mockOrder, status: 'CANCELLED' };
    render(<OrderDetailModal order={cancelledOrder} onClose={vi.fn()} />);

    expect(screen.getByText('ĐÃ HỦY')).toBeInTheDocument();
  });

  it('hiển thị badge trạng thái ĐÃ HOÀN TẤT khi status = CONFIRMED từ Saga', () => {
    const confirmedOrder = { ...mockOrder, status: 'CONFIRMED' };
    render(<OrderDetailModal order={confirmedOrder} onClose={vi.fn()} />);

    expect(screen.getByText('ĐÃ HOÀN TẤT')).toBeInTheDocument();
  });
});
