import { describe, it, expect, beforeEach } from 'vitest';
import { useCartStore } from './useCartStore';

describe('useCartStore', () => {
  beforeEach(() => {
    // Đặt lại state trước mỗi test case
    useCartStore.setState({
      items: [],
      isOpen: false,
    });
  });

  it('thêm sản phẩm vào giỏ hàng', () => {
    const product = {
      id: 'p1',
      title: 'Sản phẩm 1',
      price: 100,
      originalPrice: 150,
      soldQuantity: 0,
      totalQuantity: 100,
      imageUrl: '',
    };

    useCartStore.getState().addItem(product);
    
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
    const product = {
      id: 'p1',
      title: 'Sản phẩm 1',
      price: 100,
      originalPrice: 150,
      soldQuantity: 0,
      totalQuantity: 100,
      imageUrl: '',
    };

    useCartStore.getState().addItem(product);
    useCartStore.getState().addItem(product); // Add again
    
    const state = useCartStore.getState();
    expect(state.items.length).toBe(1);
    expect(state.items[0].quantity).toBe(2);
  });

  it('xóa sản phẩm khỏi giỏ hàng', () => {
    const product = {
      id: 'p1',
      title: 'Sản phẩm 1',
      price: 100,
      originalPrice: 150,
      soldQuantity: 0,
      totalQuantity: 100,
      imageUrl: '',
    };

    useCartStore.getState().addItem(product);
    useCartStore.getState().removeItem('p1');
    
    const state = useCartStore.getState();
    expect(state.items.length).toBe(0);
  });

  it('xóa hoàn toàn giỏ hàng', () => {
    const product = {
      id: 'p1',
      title: 'Sản phẩm 1',
      price: 100,
      originalPrice: 150,
      soldQuantity: 0,
      totalQuantity: 100,
      imageUrl: '',
    };

    useCartStore.getState().addItem(product);
    useCartStore.getState().clearCart();
    
    const state = useCartStore.getState();
    expect(state.items.length).toBe(0);
  });
});
