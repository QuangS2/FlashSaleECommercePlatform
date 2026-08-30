import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import { ProductCard } from './ProductCard';
import { useCartStore } from '../store/useCartStore';

// Mock useCartStore
vi.mock('../store/useCartStore', () => ({
  useCartStore: () => ({
    addItem: vi.fn(),
  }),
}));

const mockProduct = {
  id: 'p1',
  name: 'Sản phẩm test',
  description: 'Test',
  categoryId: 'c1',
  category: 'Test',
  originalPrice: 100000,
  salePrice: 90000,
  discountPercent: 10,
  stockCount: 10,
  soldCount: 0,
  rating: 4.5,
  reviewCount: 10,
  imageUrl: 'img.jpg',
  brand: 'Brand',
  isFlashSale: false
};

describe('ProductCard', () => {
  it('render sản phẩm đầy đủ thông tin', () => {
    render(<ProductCard product={mockProduct} />);
    
    expect(screen.getByText('Sản phẩm test')).toBeInTheDocument();
    // 90000 -> 90.000
    expect(screen.getByText(/90.000/)).toBeInTheDocument();
    expect(screen.getByText('-10%')).toBeInTheDocument();
  });

  it('vô hiệu hóa nút bấm khi hết hàng', () => {
    const outOfStockProduct = { ...mockProduct, stockCount: 0 };
    render(<ProductCard product={outOfStockProduct} />);
    
    expect(screen.getByText('Tạm hết hàng')).toBeInTheDocument();
    
    const buyButton = screen.getByText('HẾT HÀNG');
    expect(buyButton.closest('button')).toBeDisabled();
  });
});
