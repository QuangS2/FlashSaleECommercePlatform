import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Header } from './Header';
import { useCartStore } from '../store/useCartStore';
import { useAuthStore } from '../store/useAuthStore';

// Mock Keycloak default to false for clean test
vi.mock('../auth/keycloak', () => ({
  default: {
    authenticated: false,
    tokenParsed: null,
    login: vi.fn(),
    logout: vi.fn(),
  },
}));

describe('Header Component', () => {
  beforeEach(() => {
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

    useAuthStore.setState({
      user: {
        id: 'usr-1',
        username: 'khachhang',
        fullName: 'Nguyễn Văn Test',
        email: 'test@example.com',
        roles: ['ROLE_CUSTOMER'],
      },
      isAuthenticated: true,
    });
  });

  it('hiển thị user name khi đã đăng nhập qua useAuthStore', () => {
    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);
    expect(screen.getByText('Nguyễn Văn Test')).toBeInTheDocument();
  });

  it('hiển thị số lượng giỏ hàng đúng từ store', () => {
    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('mở Auth Modal khi chưa đăng nhập và click vào Đăng nhập', () => {
    useAuthStore.setState({ isAuthenticated: false, user: null });
    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);

    const loginBtn = screen.getByText('Đăng nhập / Đăng ký');
    fireEvent.click(loginBtn);

    expect(useAuthStore.getState().isAuthModalOpen).toBe(true);
  });

  it('gọi onOpenProfile khi đã đăng nhập và click vào thẻ tài khoản', () => {
    const onOpenProfileSpy = vi.fn();
    render(
      <Header
        activeCategory="Tất cả"
        onSelectCategory={vi.fn()}
        onOpenProfile={onOpenProfileSpy}
      />
    );

    const userCard = screen.getByTitle('Xem thông tin tài khoản');
    fireEvent.click(userCard);

    expect(onOpenProfileSpy).toHaveBeenCalled();
  });
});
