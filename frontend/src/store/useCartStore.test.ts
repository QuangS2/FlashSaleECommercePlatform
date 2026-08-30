import { describe, it, expect, beforeEach } from 'vitest';
import { useCartStore } from './useCartStore';
import { Product } from '../types';

const mockProduct: Product = {
  id: 'p1',
  name: 'Sản phẩm 1',
  category: 'Test',
  originalPrice: 150,
  salePrice: 100,
  discountPercent: 33,
  soldCount: 0,
  stockCount: 100,
  description: '',
  imageUrl: '',
};

describe('useCartStore', () => {
  beforeEach(() => {
    // Đặt lại state trước mỗi test case
    useCartStore.setState({
      items: [],
      isOpen: false,
    });
  });

  it('thêm sản phẩm vào giỏ hàng', () => {
    useCartStore.getState().addItem(mockProduct);

    const state = useCartStore.getState();
    expect(state.items.length).toBe(1);
    expect(state.items[0].product.id).toBe('p1');
    expect(state.items[0].quantity).toBe(1);
  });

  it('mở và đóng giỏ hàng', () => {
    useCartStore.getState().openCart();
    expect(useCartStore.getState().isOpen).toBe(true);

    useCartStore.getState().closeCart();
    expect(useCartStore.getState().isOpen).toBe(false);
  });

  it('tăng số lượng khi thêm sản phẩm đã có', () => {
    useCartStore.getState().addItem(mockProduct);
    useCartStore.getState().addItem(mockProduct); // Add again

    const state = useCartStore.getState();
    expect(state.items.length).toBe(1);
    expect(state.items[0].quantity).toBe(2);
  });

  it('xóa sản phẩm khỏi giỏ hàng', () => {
    useCartStore.getState().addItem(mockProduct);
    useCartStore.getState().removeItem('p1');

    const state = useCartStore.getState();
    expect(state.items.length).toBe(0);
  });

  it('xóa hoàn toàn giỏ hàng', () => {
    useCartStore.getState().addItem(mockProduct);
    useCartStore.getState().clearCart();

    const state = useCartStore.getState();
    expect(state.items.length).toBe(0);
  });
});
