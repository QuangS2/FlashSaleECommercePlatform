import { create } from 'zustand';
import { CartItem, Product, Voucher } from '../types';

interface CartState {
  items: CartItem[];
  isOpen: boolean;
  appliedVoucher: Voucher | null;
  
  // Actions
  toggleCart: () => void;
  openCart: () => void;
  closeCart: () => void;
  addItem: (product: Product, quantity?: number) => void;
  removeItem: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  applyVoucher: (code: string) => { success: boolean; message: string };
  removeVoucher: () => void;
  clearCart: () => void;

  // Computed values
  getTotalCount: () => number;
  getSubtotalPrice: () => number;
  getDiscountAmount: () => number;
  getFinalPrice: () => number;
}

const AVAILABLE_VOUCHERS: Voucher[] = [
  {
    code: 'FLASHSALE50',
    discountType: 'PERCENT',
    discountValue: 10, // 10%
    minOrderValue: 200000,
    description: 'Giảm 10% tối đa 100k cho đơn từ 200k',
  },
  {
    code: 'FREESHIP',
    discountType: 'FIXED',
    discountValue: 30000,
    minOrderValue: 150000,
    description: 'Miễn phí vận chuyển 30.000đ',
  },
];

export const useCartStore = create<CartState>((set, get) => ({
  items: [],
  isOpen: false,
  appliedVoucher: null,

  toggleCart: () => set((state) => ({ isOpen: !state.isOpen })),
  openCart: () => set({ isOpen: true }),
  closeCart: () => set({ isOpen: false }),

  addItem: (product: Product, quantity = 1) => {
    set((state) => {
      const existingIndex = state.items.findIndex(
        (item) => item.product.id === product.id
      );

      const priceToUse = product.salePrice > 0 ? product.salePrice : product.originalPrice;

      if (existingIndex > -1) {
        const updated = [...state.items];
        updated[existingIndex].quantity += quantity;
        return { items: updated, isOpen: true };
      }

      return {
        items: [...state.items, { product, quantity, selectedPrice: priceToUse }],
        isOpen: true,
      };
    });
  },

  removeItem: (productId: string) => {
    set((state) => ({
      items: state.items.filter((item) => item.product.id !== productId),
    }));
  },

  updateQuantity: (productId: string, quantity: number) => {
    if (quantity <= 0) {
      get().removeItem(productId);
      return;
    }
    set((state) => ({
      items: state.items.map((item) =>
        item.product.id === productId ? { ...item, quantity } : item
      ),
    }));
  },

  applyVoucher: (code: string) => {
    const cleanCode = code.trim().toUpperCase();
    const voucher = AVAILABLE_VOUCHERS.find((v) => v.code === cleanCode);

    if (!voucher) {
      return { success: false, message: 'Mã giảm giá không hợp lệ!' };
    }

    const subtotal = get().getSubtotalPrice();
    if (subtotal < voucher.minOrderValue) {
      return {
        success: false,
        message: `Đơn hàng tối thiểu ${voucher.minOrderValue.toLocaleString('vi-VN')}đ để áp dụng mã này.`,
      };
    }

    set({ appliedVoucher: voucher });
    return { success: true, message: `Đã áp dụng mã ${voucher.code} thành công!` };
  },

  removeVoucher: () => set({ appliedVoucher: null }),

  clearCart: () => set({ items: [], appliedVoucher: null }),

  getTotalCount: () => {
    return get().items.reduce((total, item) => total + item.quantity, 0);
  },

  getSubtotalPrice: () => {
    return get().items.reduce(
      (total, item) => total + item.selectedPrice * item.quantity,
      0
    );
  },

  getDiscountAmount: () => {
    const subtotal = get().getSubtotalPrice();
    const voucher = get().appliedVoucher;
    if (!voucher) return 0;

    if (voucher.discountType === 'PERCENT') {
      const discount = (subtotal * voucher.discountValue) / 100;
      return Math.min(discount, 100000); // max 100k
    } else {
      return Math.min(voucher.discountValue, subtotal);
    }
  },

  getFinalPrice: () => {
    const subtotal = get().getSubtotalPrice();
    const discount = get().getDiscountAmount();
    return Math.max(0, subtotal - discount);
  },
}));
