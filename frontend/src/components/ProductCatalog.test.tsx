import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ProductCatalog } from './ProductCatalog';
import { productService } from '../services/productService';
import { Product } from '../types';

const mockProducts: Product[] = [
  {
    id: 'p1',
    name: 'Laptop Dell XPS 13',
    category: 'Laptop',
    originalPrice: 30000000,
    salePrice: 27000000,
    discountPercent: 10,
    imageUrl: 'http://dell.jpg',
    rating: 4.8,
    soldCount: 50,
    stockCount: 10,
    description: 'Laptop cao cấp',
    isFlashSale: false,
  },
  {
    id: 'p2',
    name: 'Điện thoại iPhone 14',
    category: 'Điện thoại',
    originalPrice: 20000000,
    salePrice: 18000000,
    discountPercent: 10,
    imageUrl: 'http://iphone.jpg',
    rating: 4.9,
    soldCount: 120,
    stockCount: 25,
    description: 'iPhone chính hãng',
    isFlashSale: false,
  },
];

describe('ProductCatalog Component', () => {
  beforeEach(() => {
    vi.spyOn(productService, 'fetchProducts').mockResolvedValue(mockProducts);
  });

  it('hiển thị danh sách sản phẩm sau khi tải xong', async () => {
    render(<ProductCatalog activeCategory="Tất cả" />);

    expect(screen.getByText('Đang đồng bộ dữ liệu...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Laptop Dell XPS 13')).toBeInTheDocument();
      expect(screen.getByText('Điện thoại iPhone 14')).toBeInTheDocument();
    });
  });

  it('lọc danh mục sản phẩm', async () => {
    render(<ProductCatalog activeCategory="Laptop" />);

    await waitFor(() => {
      expect(screen.getByText('DANH MỤC SẢN PHẨM - LAPTOP')).toBeInTheDocument();
    });
  });

  it('sắp xếp sản phẩm theo giá tăng dần', async () => {
    render(<ProductCatalog activeCategory="Tất cả" />);

    await waitFor(() => {
      expect(screen.getByText('Laptop Dell XPS 13')).toBeInTheDocument();
    });

    const sortSelect = screen.getByRole('combobox');
    fireEvent.change(sortSelect, { target: { value: 'price-asc' } });

    // 18.000.000 (iPhone 14) phải xuất hiện trước 27.000.000 (Dell XPS)
    const productTitles = screen.getAllByRole('heading', { level: 3 });
    expect(productTitles[0]).toHaveTextContent('Điện thoại iPhone 14');
  });
});
