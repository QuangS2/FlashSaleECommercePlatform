import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Header } from './Header';
import { useCartStore } from '../store/useCartStore';
import keycloak from '../auth/keycloak';

// Mock Keycloak default for clean test
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
    vi.clearAllMocks();
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

  it('gọi keycloak.login() khi chưa đăng nhập và click vào thẻ tài khoản', () => {
    (keycloak as any).authenticated = false;
    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);

    const userCard = screen.getByTitle('Đăng nhập với Keycloak IAM');
    fireEvent.click(userCard);

    expect(keycloak.login).toHaveBeenCalled();
  });

  it('hiển thị tên người dùng khi đã đăng nhập qua Keycloak', () => {
    (keycloak as any).authenticated = true;
    (keycloak as any).tokenParsed = { name: 'Lê Văn Khách' };

    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);
    expect(screen.getByText('Lê Văn Khách')).toBeInTheDocument();
  });

  it('gọi keycloak.logout() khi click vào nút đăng xuất', () => {
    (keycloak as any).authenticated = true;
    (keycloak as any).tokenParsed = { name: 'Lê Văn Khách' };

    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);

    const logoutBtn = screen.getByTitle('Đăng xuất khỏi Keycloak');
    fireEvent.click(logoutBtn);

    expect(keycloak.logout).toHaveBeenCalled();
  });
});
