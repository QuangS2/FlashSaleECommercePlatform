import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import { CheckoutModal } from './CheckoutModal';
import { useCartStore } from '../store/useCartStore';

// Mock Auth
vi.mock('../auth/keycloak', () => ({
  default: {
    authenticated: true,
    tokenParsed: { name: 'Test User' },
  },
}));

describe('CheckoutModal', () => {
  beforeEach(() => {
    useCartStore.setState({
      items: [
        { product: { id: 'p1', name: 'SP1', originalPrice: 100000, salePrice: 100000, discountPercent: 0, stockCount: 10, soldCount: 0, rating: 5, reviewCount: 1, categoryId: '1', category: 'C1', imageUrl: '', brand: '', isFlashSale: false }, quantity: 1, selectedPrice: 100000 }
      ],
      appliedVoucher: null
    });
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({}),
      })
    ) as any;
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
    expect(screen.getByDisplayValue('Test User')).toBeInTheDocument(); // Giá trị mặc định fullName từ token
  });

  it('submit form thành công và gọi onOrderSuccess', async () => {
    const onSuccessSpy = vi.fn();
    const onCloseSpy = vi.fn();
    
    render(<CheckoutModal isOpen={true} onClose={onCloseSpy} onOrderSuccess={onSuccessSpy} />);
    
    const submitBtn = screen.getByText('XÁC NHẬN ĐẶT HÀNG NGAY');
    fireEvent.click(submitBtn); // Click type="submit" in form
    
    // Gọi fetch 1 lần cho deduct API
    expect(global.fetch).toHaveBeenCalledWith('/api/v1/inventory/deduct?productId=p1&quantity=1', expect.any(Object));
    
    // Sau khi submit, onClose và onOrderSuccess sẽ được gọi
    await waitFor(() => {
      expect(onCloseSpy).toHaveBeenCalled();
      expect(onSuccessSpy).toHaveBeenCalled();
    });
  });
});
