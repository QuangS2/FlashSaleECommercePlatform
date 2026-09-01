import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { CheckoutModal } from './CheckoutModal';
import { useCartStore } from '../store/useCartStore';

// Mock Auth
vi.mock('../auth/keycloak', () => ({
  default: {
    authenticated: true,
    tokenParsed: { name: 'Test User' },
  },
  onAuthChange: vi.fn(() => () => {}),
  getKeycloakUrl: vi.fn(() => 'http://localhost:8180'),
}));

import { useAuthStore } from '../store/useAuthStore';

describe('CheckoutModal', () => {
  beforeEach(() => {
    useAuthStore.setState({
      isAuthenticated: true,
      user: {
        sub: 'user_1',
        name: 'Test User',
        username: 'testuser',
        email: 'test@ecommerce.vn',
        roles: ['ROLE_CUSTOMER'],
      },
    });
    useCartStore.setState({
      items: [
        {
          product: {
            id: 'p1',
            name: 'SP1',
            originalPrice: 100000,
            salePrice: 100000,
            discountPercent: 0,
            stockCount: 10,
            soldCount: 0,
            rating: 5,
            category: 'C1',
            imageUrl: '',
            description: '',
            isFlashSale: false,
          },
          quantity: 1,
          selectedPrice: 100000,
        },
      ],
      appliedVoucher: null,
    });
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve({ orderId: 'ORD-TEST-123', status: 'PENDING' }),
        })
      )
    );
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('không render nếu isOpen = false', () => {
    render(<CheckoutModal isOpen={false} onClose={vi.fn()} onOrderSuccess={vi.fn()} />);
    expect(screen.queryByText('XÁC NHẬN VÀ THANH TOÁN ĐƠN HÀNG')).not.toBeInTheDocument();
  });

  it('render form checkout', () => {
    render(<CheckoutModal isOpen={true} onClose={vi.fn()} onOrderSuccess={vi.fn()} />);
    expect(screen.getByText('XÁC NHẬN VÀ THANH TOÁN ĐƠN HÀNG')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Test User')).toBeInTheDocument();
  });

  it('submit form thành công và gọi onOrderSuccess', async () => {
    const onSuccessSpy = vi.fn();
    const onCloseSpy = vi.fn();

    render(<CheckoutModal isOpen={true} onClose={onCloseSpy} onOrderSuccess={onSuccessSpy} />);

    const submitBtn = screen.getByText('XÁC NHẬN ĐẶT HÀNG NGAY');
    fireEvent.click(submitBtn);

    expect(fetch).toHaveBeenCalledWith('/api/v1/orders', expect.any(Object));

    await waitFor(() => {
      expect(onCloseSpy).toHaveBeenCalled();
      expect(onSuccessSpy).toHaveBeenCalledWith('ORD-TEST-123');
    });
  });
});
