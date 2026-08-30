import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Header } from './Header';
import { useCartStore } from '../store/useCartStore';

// Mock Auth
vi.mock('../auth/keycloak', () => ({
  default: {
    authenticated: true,
    tokenParsed: { name: 'Test User' },
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
  });

  it('hiển thị user name khi đã đăng nhập', () => {
    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);
    expect(screen.getByText('Test User')).toBeInTheDocument();
  });

  it('hiển thị số lượng giỏ hàng đúng từ store', () => {
    render(<Header activeCategory="Tất cả" onSelectCategory={vi.fn()} />);
    expect(screen.getByText('2')).toBeInTheDocument(); // quantity = 2
  });
});
