import { Product } from '../types';

const FALLBACK_PRODUCTS: Product[] = [
  {
    id: 'cat-1',
    name: 'Laptop Apple MacBook Air M2 13.6 inch 8GB/256GB',
    category: 'Laptop',
    originalPrice: 28990000,
    salePrice: 24490000,
    discountPercent: 15,
    imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 320,
    stockCount: 45,
    description: 'MacBook Air M2 siêu mỏng nhẹ, màn hình Liquid Retina sắc nét, thời lượng pin lên tới 18 giờ liên tục.',
    specs: {
      'CPU': 'Apple M2 8-core CPU',
      'RAM': '8GB Unified Memory',
      'SSD': '256GB NVMe',
      'Màn hình': '13.6" Liquid Retina 500 nits',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-2',
    name: 'Điện thoại Samsung Galaxy S24 Ultra 5G 12GB/256GB - AI Phone',
    category: 'Điện thoại',
    originalPrice: 33990000,
    salePrice: 29990000,
    discountPercent: 11,
    imageUrl: 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 210,
    stockCount: 28,
    description: 'Quyền năng Galaxy AI đỉnh cao, khung viền Titan bền bỉ, camera 200MP biến đêm thành ngày.',
    specs: {
      'Màn hình': 'Dynamic AMOLED 2X 6.8 inch 120Hz',
      'Chipset': 'Snapdragon 8 Gen 3 for Galaxy',
      'RAM / ROM': '12GB / 256GB',
      'Camera': '200MP + 50MP + 12MP + 10MP',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-3',
    name: 'Tai nghe Bluetooth True Wireless Apple AirPods Pro Gen 2 USB-C',
    category: 'Phụ kiện',
    originalPrice: 6190000,
    salePrice: 5390000,
    discountPercent: 12,
    imageUrl: 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 540,
    stockCount: 60,
    description: 'Chip H2 mạnh mẽ, tính năng Khử tiếng ồn chủ động gấp 2 lần, cổng sạc USB-C hiện đại.',
    specs: {
      'Chipset': 'Apple H2 headphone chip',
      'Cổng sạc': 'MagSafe USB-C',
      'Chống nước': 'IP54',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-4',
    name: 'Nồi chiên không dầu Philips HD9252/90 4.1 Lít - Công nghệ Rapid Air',
    category: 'Đồ gia dụng',
    originalPrice: 2990000,
    salePrice: 1890000,
    discountPercent: 36,
    imageUrl: 'https://images.unsplash.com/photo-1585515320310-259814833e62?w=500&auto=format&fit=crop&q=60',
    rating: 4.7,
    soldCount: 185,
    stockCount: 19,
    description: 'Giảm 90% lượng chất béo thừa, bảng điều khiển cảm ứng với 7 chương trình nấu cài đặt sẵn.',
    specs: {
      'Dung tích': '4.1 Lít (Rổ chiên 0.8kg)',
      'Công suất': '1400W',
      'Công nghệ': 'Rapid Air luồng khí xoáy',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-5',
    name: 'Bàn phím Cơ Không dây Keychron K2 Pro QMK/VIA Swappable',
    category: 'Phụ kiện',
    originalPrice: 2490000,
    salePrice: 1990000,
    discountPercent: 20,
    imageUrl: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=60',
    rating: 4.8,
    soldCount: 142,
    stockCount: 22,
    description: 'Bàn phím cơ layout 75% nâng cấp foam tiêu âm, hỗ trợ QMK/VIA tùy chỉnh phím tự do.',
    specs: {
      'Switch': 'Keychron K Pro Red / Brown',
      'Kết nối': 'Bluetooth 5.1 & Type-C Wired',
      'Pin': '4000 mAh',
    },
    isFlashSale: false,
  },
  {
    id: 'cat-6',
    name: 'Đồng hồ Garmin Fenix 7 Pro Sapphire Solar Edition',
    category: 'Đồng hồ',
    originalPrice: 23990000,
    salePrice: 19990000,
    discountPercent: 16,
    imageUrl: 'https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=500&auto=format&fit=crop&q=60',
    rating: 4.9,
    soldCount: 65,
    stockCount: 12,
    description: 'Đồng hồ thể thao chuyên nghiệp với kính sạc năng lượng mặt trời Power Sapphire, đèn LED chiếu sáng.',
    specs: {
      'Màn hình': '1.3" MIP chống chói',
      'Thời lượng pin': 'Up to 22 ngày (chế độ smartwatch)',
      'Độ bền': 'Chuẩn quân đội MIL-STD-810G',
    },
    isFlashSale: false,
  },
];

export const productService = {
  /**
   * Lấy danh sách sản phẩm từ Backend Product-Service (MongoDB qua API Gateway)
   */
  async fetchProducts(category?: string, search?: string): Promise<Product[]> {
    try {
      const params = new URLSearchParams();
      if (category && category !== 'Tất cả') {
        params.append('category', category);
      }
      if (search && search.trim() !== '') {
        params.append('search', search.trim());
      }

      const queryString = params.toString() ? `?${params.toString()}` : '';
      const response = await fetch(`/api/v1/products${queryString}`);

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: any[] = await response.json();
      if (Array.isArray(data) && data.length > 0) {
        return data.map((item) => ({
          id: item.id,
          name: item.name,
          category: item.category || 'Khác',
          description: item.description || '',
          originalPrice: Number(item.price) || 0,
          salePrice: item.discountPrice ? Number(item.price) - Number(item.discountPrice) : Number(item.price) || 0,
          discountPercent: item.discountPercent || (item.discountPrice ? Math.round((Number(item.discountPrice) / Number(item.price)) * 100) : 0),
          imageUrl: item.imageUrl || '',
          rating: item.rating || 4.8,
          soldCount: item.soldCount || 0,
          stockCount: item.stockCount || 50,
          specs: item.specs || {},
          isFlashSale: Boolean(item.isFlashSale),
        }));
      }

      // Nếu database trả về mảng rỗng, áp dụng lọc trên fallback
      return this.filterFallback(category, search);
    } catch (error) {
      console.warn('[ProductService] Backend chưa sẵn sàng, sử dụng dữ liệu cục bộ:', error);
      return this.filterFallback(category, search);
    }
  },

  /**
   * Lấy chi tiết 1 sản phẩm theo ID
   */
  async fetchProductById(id: string): Promise<Product | null> {
    try {
      const response = await fetch(`/api/v1/products/${id}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const item = await response.json();
      return {
        id: item.id,
        name: item.name,
        category: item.category || 'Khác',
        description: item.description || '',
        originalPrice: Number(item.price) || 0,
        salePrice: item.discountPrice ? Number(item.price) - Number(item.discountPrice) : Number(item.price) || 0,
        discountPercent: item.discountPercent || 0,
        imageUrl: item.imageUrl || '',
        rating: item.rating || 4.8,
        soldCount: item.soldCount || 0,
        stockCount: item.stockCount || 50,
        specs: item.specs || {},
        isFlashSale: Boolean(item.isFlashSale),
      };
    } catch {
      return FALLBACK_PRODUCTS.find((p) => p.id === id) || null;
    }
  },

  filterFallback(category?: string, search?: string): Product[] {
    return FALLBACK_PRODUCTS.filter((p) => {
      const matchesCat = !category || category === 'Tất cả' || p.category === category;
      const matchesSearch =
        !search ||
        p.name.toLowerCase().includes(search.toLowerCase()) ||
        p.category.toLowerCase().includes(search.toLowerCase());
      return matchesCat && matchesSearch;
    });
  },
};
