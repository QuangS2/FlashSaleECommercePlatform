import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Header } from './Header';
import { useCartStore } from '../store/useCartStore';
import { useAuthStore } from '../store/useAuthStore';

describe('Header Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      isAuthenticated: false,
      user: null,
      isLoginModalOpen: false,
    });
    useCartStore.setState({
      items: [
        {
          product: {
            id: '1',
            name: 'SP1',
            originalPrice: 1,
            salePrice: 1,
            discountPercent: 0,
            stockCount: 1,
            soldCount: 0,
            rating: 5,
            category: 'C1',
            imageUrl: '',
            description: '',
            isFlashSale: false,
          },
          quantity: 2,
          selectedPrice: 1,
        },
      ],
    });
  });

  it('hiển thị số lượng giỏ hàng đúng từ store', () => {
    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('mở LoginModal khi chưa đăng nhập và click vào thẻ tài khoản', () => {
    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);

    const userCard = screen.getByText('Đăng nhập / Đăng ký');
    fireEvent.click(userCard);

    expect(useAuthStore.getState().isLoginModalOpen).toBe(true);
  });

  it('hiển thị tên người dùng khi đã đăng nhập', () => {
    useAuthStore.setState({
      isAuthenticated: true,
      user: {
        sub: '123',
        name: 'Lê Văn Khách',
        username: 'customer',
        email: 'customer@ecommerce.vn',
        roles: ['ROLE_CUSTOMER'],
      },
    });

    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);
    expect(screen.getByText('Lê Văn Khách')).toBeInTheDocument();
  });

  it('thực hiện logout khi click vào nút đăng xuất', () => {
    useAuthStore.setState({
      isAuthenticated: true,
      user: {
        sub: '123',
        name: 'Lê Văn Khách',
        username: 'customer',
        email: 'customer@ecommerce.vn',
        roles: ['ROLE_CUSTOMER'],
      },
    });

    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);

    const logoutBtn = screen.getByTitle('Đăng xuất khỏi Keycloak');
    fireEvent.click(logoutBtn);

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
  });

  it('gọi onOpenOrderHistory khi click vào nút Đơn mua', () => {
    const onOpenSpy = vi.fn();
    render(
      <Header
        activeCategory="Tất cả"
        onSelectCategory={vi.fn()}
        onOpenOrderHistory={onOpenSpy}
      />
    );

    const orderBtn = screen.getByTitle('Xem lịch sử đơn hàng của tôi');
    fireEvent.click(orderBtn);

    expect(onOpenSpy).toHaveBeenCalled();
  });
});
