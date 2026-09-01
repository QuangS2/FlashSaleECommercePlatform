import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { productService } from './productService';

describe('productService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('lấy danh sách sản phẩm từ API Backend thành công', async () => {
    const mockApiResponse = [
      {
        id: 'p1',
        name: 'Sản phẩm Test',
        category: 'Laptop',
        description: 'Mô tả test',
        price: 20000000,
        discountPrice: 2000000,
        discountPercent: 10,
        imageUrl: 'http://img.png',
        rating: 4.9,
        soldCount: 10,
        stockCount: 50,
        specs: { CPU: 'Intel i7' },
        isFlashSale: false,
      },
    ];

    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockApiResponse),
        })
      )
    );

    const products = await productService.fetchProducts();

    expect(products.length).toBe(1);
    expect(products[0].id).toBe('p1');
    expect(products[0].salePrice).toBe(18000000);
    expect(products[0].discountPercent).toBe(10);
  });

  it('lọc fallback khi API Backend offline', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.reject(new Error('Network error')))
    );

    const products = await productService.fetchProducts('Laptop');
    expect(products.length).toBeGreaterThan(0);
    expect(products.every((p) => p.category === 'Laptop')).toBe(true);
  });

  it('tìm kiếm theo từ khóa trong fallback', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.reject(new Error('Network error')))
    );

    const products = await productService.fetchProducts('Tất cả', 'MacBook');
    expect(products.length).toBe(1);
    expect(products[0].name).toContain('MacBook');
  });

  it('lấy chi tiết sản phẩm theo ID thành công', async () => {
    const mockProduct = {
      id: 'cat-1',
      name: 'MacBook Air M2',
      category: 'Laptop',
      price: 28990000,
    };

    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockProduct),
        })
      )
    );

    const product = await productService.fetchProductById('cat-1');
    expect(product).not.toBeNull();
    expect(product?.id).toBe('cat-1');
    expect(product?.name).toBe('MacBook Air M2');
  });

  it('cập nhật tăng số lượng đã bán (incrementSoldCount) thành công', async () => {
    const mockUpdated = {
      id: 'cat-1',
      name: 'MacBook Air M2',
      category: 'Laptop',
      price: 28990000,
      soldCount: 325,
      stockCount: 45,
    };

    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockUpdated),
        })
      )
    );

    const result = await productService.incrementSoldCount('cat-1', 5);
    expect(result).not.toBeNull();
    expect(result?.soldCount).toBe(325);
  });
});
