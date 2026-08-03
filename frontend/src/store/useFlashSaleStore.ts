import { create } from 'zustand';
import { FlashSaleProduct, FlashSaleSlot } from '../types';

interface FlashSaleState {
  slots: FlashSaleSlot[];
  activeSlotId: string;
  products: FlashSaleProduct[];
  
  // Actions
  setActiveSlot: (slotId: string) => void;
  updateProductStock: (productId: string, remainingStock: number) => void;
  setProducts: (products: FlashSaleProduct[]) => void;
}

const MOCK_SLOTS: FlashSaleSlot[] = [
  {
    id: 'slot-1',
    startTime: '09:00',
    endTime: '12:00',
    label: '09:00 - 12:00',
    status: 'ENDED',
  },
  {
    id: 'slot-2',
    startTime: '12:00',
    endTime: '15:00',
    label: '12:00 - 15:00',
    status: 'ACTIVE',
  },
  {
    id: 'slot-3',
    startTime: '15:00',
    endTime: '18:00',
    label: '15:00 - 18:00',
    status: 'UPCOMING',
  },
  {
    id: 'slot-4',
    startTime: '20:00',
    endTime: '23:59',
    label: '20:00 - 23:59',
    status: 'UPCOMING',
  },
];

const MOCK_FLASH_SALE_PRODUCTS: FlashSaleProduct[] = [
  {
    id: 'fs-101',
    name: 'Điện thoại iPhone 15 Pro Max 256GB - Chính hãng VN/A',
    category: 'Điện thoại',
    originalPrice: 34990000,
    salePrice: 28990000,
    discountPercent: 17,
    imageUrl: 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 85,
    stockCount: 15,
    description: 'iPhone 15 Pro Max với khung Titan cao cấp, chip A17 Pro siêu mạnh mẽ và camera Zoom quang học 5x sắc nét.',
    specs: {
      'Màn hình': 'Super Retina XDR OLED 6.7 inch 120Hz',
      'Chipset': 'Apple A17 Pro (3nm)',
      'RAM / Bộ nhớ': '8GB / 256GB',
      'Pin': '4,422 mAh, Sạc nhanh 20W',
    },
    isFlashSale: true,
    slotId: 'slot-2',
    totalStock: 100,
    soldStock: 85,
    remainingStock: 15,
    maxLimitPerUser: 1,
  },
  {
    id: 'fs-102',
    name: 'Laptop Gaming ASUS ROG Strix G16 i7-13650HX / RTX 4060',
    category: 'Laptop',
    originalPrice: 42990000,
    salePrice: 33490000,
    discountPercent: 22,
    imageUrl: 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 42,
    stockCount: 8,
    description: 'Sức mạnh vượt trội với Intel Core i7 thế hệ 13 và card đồ họa RTX 4060, màn hình 240Hz siêu mượt.',
    specs: {
      'CPU': 'Intel Core i7-13650HX',
      'VGA': 'NVIDIA GeForce RTX 4060 8GB',
      'RAM / SSD': '16GB DDR5 / 512GB NVMe PCIe 4.0',
      'Màn hình': '16" QHD+ 240Hz 100% DCI-P3',
    },
    isFlashSale: true,
    slotId: 'slot-2',
    totalStock: 50,
    soldStock: 42,
    remainingStock: 8,
    maxLimitPerUser: 1,
  },
  {
    id: 'fs-103',
    name: 'Tai nghe Chống ồn Sony WH-1000XM5 Hi-Res Audio',
    category: 'Phụ kiện',
    originalPrice: 8490000,
    salePrice: 6290000,
    discountPercent: 26,
    imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 95,
    stockCount: 5,
    description: 'Công nghệ chống ồn chủ động ANC đỉnh cao thế giới với 8 micro và bộ xử lý Integrated Processor V1.',
    specs: {
      'Thời lượng pin': 'Up to 30 giờ (bật ANC)',
      'Kết nối': 'Bluetooth 5.2, LDAC, Multi-point',
      'Trọng lượng': '250g',
    },
    isFlashSale: true,
    slotId: 'slot-2',
    totalStock: 100,
    soldStock: 95,
    remainingStock: 5,
    maxLimitPerUser: 2,
  },
  {
    id: 'fs-104',
    name: 'Đồng hồ Thông minh Apple Watch Series 9 GPS 45mm',
    category: 'Đồng hồ',
    originalPrice: 11990000,
    salePrice: 9490000,
    discountPercent: 21,
    imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=60',
    rating: 4.7,
    soldCount: 18,
    stockCount: 32,
    description: 'Chip S9 SiP mạnh mẽ hơn, thao tác Double Tap chạm hai lần tiện lợi, màn hình sáng gấp đôi 2000 nits.',
    specs: {
      'Kích thước': '45mm Viền nhôm',
      'Tính năng': 'Đo nhịp tim, SpO2, ECG, Phát hiện té ngã',
      'Chống nước': 'WR50 (50 mét)',
    },
    isFlashSale: true,
    slotId: 'slot-2',
    totalStock: 50,
    soldStock: 18,
    remainingStock: 32,
    maxLimitPerUser: 1,
  },
];

export const useFlashSaleStore = create<FlashSaleState>((set) => ({
  slots: MOCK_SLOTS,
  activeSlotId: 'slot-2',
  products: MOCK_FLASH_SALE_PRODUCTS,

  setActiveSlot: (slotId: string) => set({ activeSlotId: slotId }),

  updateProductStock: (productId: string, remainingStock: number) => {
    set((state) => ({
      products: state.products.map((p) => {
        if (p.id === productId) {
          const newSold = Math.max(0, p.totalStock - remainingStock);
          return {
            ...p,
            remainingStock: remainingStock,
            stockCount: remainingStock,
            soldStock: newSold,
            soldCount: newSold,
          };
        }
        return p;
      }),
    }));
  },

  setProducts: (products: FlashSaleProduct[]) => set({ products }),
}));
