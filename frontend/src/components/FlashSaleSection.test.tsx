import React from 'react';
import { render, screen } from '@testing-library/react';
import { vi } from 'vitest';
import { FlashSaleSection } from './FlashSaleSection';
import { useFlashSaleStore } from '../store/useFlashSaleStore';

// Mock Component con để tập trung test logic
vi.mock('./ProductCard', () => ({
  ProductCard: ({ product }: any) => <div data-testid="product-card">{product.name}</div>
}));

describe('FlashSaleSection', () => {
  beforeEach(() => {
    useFlashSaleStore.setState({
      activeSlotId: 'slot-1',
      slots: [
        { id: 'slot-1', startTime: '12:00', endTime: '14:00', label: '12:00 - 14:00', status: 'ACTIVE' },
        { id: 'slot-2', startTime: '14:00', endTime: '16:00', label: '14:00 - 16:00', status: 'UPCOMING' },
      ],
      products: [
        { id: 'p1', name: 'Sản phẩm 1', categoryId: '1', category: 'C1', originalPrice: 100, salePrice: 90, discountPercent: 10, rating: 5, reviewCount: 1, imageUrl: '', brand: '', isFlashSale: true, totalStock: 100, soldStock: 10, remainingStock: 90, slotId: 'slot-1' } as any,
        { id: 'p2', name: 'Sản phẩm 2', categoryId: '1', category: 'C1', originalPrice: 100, salePrice: 90, discountPercent: 10, rating: 5, reviewCount: 1, imageUrl: '', brand: '', isFlashSale: true, totalStock: 100, soldStock: 10, remainingStock: 90, slotId: 'slot-2' } as any,
      ],
    });
  });

  it('render danh sách tabs', () => {
    render(<FlashSaleSection />);
    expect(screen.getByText('12:00 - 14:00')).toBeInTheDocument();
    expect(screen.getByText('ĐANG MỞ BÁN')).toBeInTheDocument();
    
    expect(screen.getByText('14:00 - 16:00')).toBeInTheDocument();
    expect(screen.getByText('SẮP DIỄN RA')).toBeInTheDocument();
  });

  it('render đúng sản phẩm của tab đang active', () => {
    render(<FlashSaleSection />);
    
    // Tab đang active là slot-1, có 1 sản phẩm
    expect(screen.getByText('Sản phẩm 1')).toBeInTheDocument();
    expect(screen.queryByText('Sản phẩm 2')).not.toBeInTheDocument();
  });
});
