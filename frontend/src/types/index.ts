export interface Product {
  id: string;
  name: string;
  category: string;
  originalPrice: number;
  salePrice: number;
  discountPercent: number;
  imageUrl: string;
  rating: number;
  soldCount: number;
  stockCount: number;
  description: string;
  specs?: Record<string, string>;
  isFlashSale?: boolean;
}

export interface FlashSaleSlot {
  id: string;
  startTime: string; // e.g. "09:00"
  endTime: string;   // e.g. "12:00"
  label: string;     // e.g. "Khung giờ 09:00 - 12:00"
  status: 'UPCOMING' | 'ACTIVE' | 'ENDED';
}

export interface FlashSaleProduct extends Product {
  slotId: string;
  totalStock: number;
  soldStock: number;
  remainingStock: number;
  maxLimitPerUser: number;
}

export interface CartItem {
  product: Product;
  quantity: number;
  selectedPrice: number;
}

export interface Voucher {
  code: string;
  discountType: 'PERCENT' | 'FIXED';
  discountValue: number;
  minOrderValue: number;
  description: string;
}

export interface UserProfile {
  id: string;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
  avatarUrl?: string;
  phone?: string;
  address?: string;
}

export interface OrderRequest {
  items: CartItem[];
  shippingAddress: {
    fullName: string;
    phone: string;
    address: string;
    city: string;
    note?: string;
  };
  paymentMethod: 'COD' | 'VNPAY' | 'KEYCLOAK_WALLET';
  totalAmount: number;
  discountAmount: number;
  finalAmount: number;
}
