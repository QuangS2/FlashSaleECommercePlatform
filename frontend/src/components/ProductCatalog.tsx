import React, { useState, useEffect } from 'react';
import { Product } from '../types';
import { ProductCard } from './ProductCard';
import { Filter, SlidersHorizontal, Loader2 } from 'lucide-react';
import { productService } from '../services/productService';

interface ProductCatalogProps {
  activeCategory: string;
  searchTerm?: string;
  onQuickView?: (product: Product) => void;
  onBuyNow?: (product: Product) => void;
}

export const ProductCatalog: React.FC<ProductCatalogProps> = ({
  activeCategory,
  searchTerm = '',
  onQuickView,
  onBuyNow,
}) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [sortBy, setSortBy] = useState<'featured' | 'price-asc' | 'price-desc'>('featured');

  useEffect(() => {
    let isMounted = true;
    setIsLoading(true);

    const loadProducts = async () => {
      const data = await productService.fetchProducts(activeCategory, searchTerm);
      if (isMounted) {
        setProducts(data);
        setIsLoading(false);
      }
    };

    const timer = setTimeout(loadProducts, 150); // Debounce ngắn để mượt mà
    const unsubscribe = productService.onProductsChange(() => {
      loadProducts();
    });

    return () => {
      isMounted = false;
      clearTimeout(timer);
      unsubscribe();
    };
  }, [activeCategory, searchTerm]);

  // Sort products
  const sortedProducts = [...products].sort((a, b) => {
    const priceA = a.salePrice || a.originalPrice;
    const priceB = b.salePrice || b.originalPrice;
    if (sortBy === 'price-asc') return priceA - priceB;
    if (sortBy === 'price-desc') return priceB - priceA;
    return (b.soldCount || 0) - (a.soldCount || 0);
  });

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
            {isLoading ? 'Đang đồng bộ dữ liệu...' : `Hiển thị ${sortedProducts.length} sản phẩm`}
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

      {/* Loading Spinner */}
      {isLoading ? (
        <div className="flex flex-col items-center justify-center py-16 text-slate-400 space-y-2">
          <Loader2 className="w-8 h-8 animate-spin text-[#1A94FF]" />
          <span className="text-xs font-medium">Đang tải danh sách sản phẩm từ cơ sở dữ liệu...</span>
        </div>
      ) : sortedProducts.length > 0 ? (
        /* Grid of Catalog Products */
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3 animate-in fade-in">
          {sortedProducts.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onQuickView={onQuickView}
              onBuyNow={onBuyNow}
            />
          ))}
        </div>
      ) : (
        /* Empty State */
        <div className="text-center py-12 text-slate-500 bg-slate-50 rounded border border-dashed border-slate-300">
          <p className="font-semibold text-slate-700">Không tìm thấy sản phẩm nào phù hợp.</p>
          <p className="text-xs text-slate-400 mt-1">Thử thay đổi từ khóa tìm kiếm hoặc chọn danh mục khác.</p>
        </div>
      )}
    </section>
  );
};
