import React, { useState } from 'react';
import { Product } from '../types';
import { ProductCard } from './ProductCard';
import { Filter, SlidersHorizontal } from 'lucide-react';

interface ProductCatalogProps {
  activeCategory: string;
  searchTerm?: string;
  onQuickView?: (product: Product) => void;
  onBuyNow?: (product: Product) => void;
}

const MOCK_CATALOG_PRODUCTS: Product[] = [
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
  },
];

export const ProductCatalog: React.FC<ProductCatalogProps> = ({
  activeCategory,
  searchTerm = '',
  onQuickView,
  onBuyNow,
}) => {
  const [sortBy, setSortBy] = useState<'featured' | 'price-asc' | 'price-desc'>('featured');

  // Filter products by Category & Search term
  let filtered = MOCK_CATALOG_PRODUCTS.filter((p) => {
    const matchesCategory = activeCategory === 'Tất cả' || p.category === activeCategory;
    const matchesSearch =
      !searchTerm ||
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.category.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  // Sort products
  if (sortBy === 'price-asc') {
    filtered.sort((a, b) => (a.salePrice || a.originalPrice) - (b.salePrice || b.originalPrice));
  } else if (sortBy === 'price-desc') {
    filtered.sort((a, b) => (b.salePrice || b.originalPrice) - (a.salePrice || a.originalPrice));
  }

  return (
    <section className="bg-white border border-slate-200 rounded-md p-4 mb-8 shadow-sm">
      {/* Title & Filter Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-200 mb-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <Filter className="w-5 h-5 text-blue-600" />
            <span>DANH MỤC SẢN PHẨM {activeCategory !== 'Tất cả' && `- ${activeCategory.toUpperCase()}`}</span>
          </h2>
          <p className="text-xs text-slate-500">
            Hiển thị {filtered.length} sản phẩm phù hợp
          </p>
        </div>

        {/* Sort Controls */}
        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-500 font-medium flex items-center gap-1">
            <SlidersHorizontal className="w-3.5 h-3.5" /> Sắp xếp:
          </span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="text-xs bg-white border border-slate-300 text-slate-700 rounded-[4px] px-3 py-2 focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
          >
            <option value="featured">Nổi bật nhất</option>
            <option value="price-asc">Giá: Thấp đến Cao</option>
            <option value="price-desc">Giá: Cao đến Thấp</option>
          </select>
        </div>
      </div>

      {/* Grid of Catalog Products */}
      {filtered.length > 0 ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
          {filtered.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onQuickView={onQuickView}
              onBuyNow={onBuyNow}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-12 text-slate-500 bg-slate-50 rounded border border-dashed border-slate-300">
          <p className="font-semibold text-slate-700">Không tìm thấy sản phẩm nào phù hợp.</p>
          <p className="text-xs text-slate-400 mt-1">Thử thay đổi từ khóa tìm kiếm hoặc chọn danh mục khác.</p>
        </div>
      )}
    </section>
  );
};
