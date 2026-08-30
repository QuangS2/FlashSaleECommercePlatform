import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import { CartDrawer } from './CartDrawer';
import { useCartStore } from '../store/useCartStore';

describe('CartDrawer', () => {
  beforeEach(() => {
    useCartStore.setState({
      isOpen: true,
      items: [
        { product: { id: 'p1', name: 'SP1', originalPrice: 100, salePrice: 100, discountPercent: 0, stockCount: 10, soldCount: 0, rating: 5, reviewCount: 1, categoryId: '1', category: 'C1', imageUrl: '', brand: '', isFlashSale: false }, quantity: 1, selectedPrice: 100 }
      ],
      appliedVoucher: null
    });
  });

  it('render giỏ hàng trống', () => {
    useCartStore.setState({ items: [] });
    render(<CartDrawer />);
    
    expect(screen.getByText('Giỏ hàng của bạn đang trống')).toBeInTheDocument();
  });

  it('render sản phẩm trong giỏ hàng', () => {
    render(<CartDrawer />);
    expect(screen.getByText('SP1')).toBeInTheDocument();
    expect(screen.getAllByText('100 ₫')[0]).toBeInTheDocument(); // Format VND cơ bản
  });

  it('gọi đóng giỏ hàng khi nhấn overlay hoặc nút X', () => {
    const closeSpy = vi.spyOn(useCartStore.getState(), 'closeCart');
    render(<CartDrawer />);
    
    const closeButton = screen.getAllByRole('button')[0];
    fireEvent.click(closeButton);
    
    expect(closeSpy).toHaveBeenCalled();
  });
});
