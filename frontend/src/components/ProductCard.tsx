import React from 'react';
import { ShoppingCart, Zap, Star } from 'lucide-react';
import { Product, FlashSaleProduct } from '../types';
import { StockProgressBar } from './StockProgressBar';
import { useCartStore } from '../store/useCartStore';

interface ProductCardProps {
  product: Product | FlashSaleProduct;
  onQuickView?: (product: Product) => void;
  onBuyNow?: (product: Product) => void;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product, onQuickView, onBuyNow }) => {
  const { addItem } = useCartStore();

  const isFlashSale = 'isFlashSale' in product && product.isFlashSale;
  const flashSaleProduct = isFlashSale ? (product as FlashSaleProduct) : null;

  const isOutOfStock = isFlashSale
    ? flashSaleProduct?.remainingStock === 0
    : product.stockCount === 0;

  const formatVND = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  return (
    <div className="bg-white border border-slate-200 hover:border-blue-400 rounded-md overflow-hidden hover:shadow-[0_4px_12px_rgba(0,0,0,0.05)] transition-all flex flex-col justify-between group">
      {/* Top Image Section */}
      <div className="relative aspect-square overflow-hidden bg-slate-50 cursor-pointer" onClick={() => onQuickView && onQuickView(product)}>
        <img
          src={product.imageUrl}
          alt={product.name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          loading="lazy"
        />

        {/* Discount Badge */}
        {product.discountPercent > 0 && (
          <div className="absolute top-2 left-2 bg-[#FF424E] text-white font-bold text-[11px] px-1.5 py-0.5 rounded shadow-sm flex items-center gap-0.5 z-10">
            <span>-{product.discountPercent}%</span>
          </div>
        )}

        {/* Flash Sale Tag */}
        {isFlashSale && (
          <div className="absolute top-2 right-2 bg-yellow-400 text-slate-900 font-extrabold text-[10px] uppercase px-1.5 py-0.5 rounded tracking-wider shadow-sm z-10">
            FLASH SALE
          </div>
        )}
      </div>

      {/* Product Content Details */}
      <div className="p-3 flex-1 flex flex-col justify-between">
        <div>
          {/* Category & Rating */}
          <div className="flex items-center justify-between text-[11px] text-slate-500 mb-1.5">
            <span className="uppercase tracking-wide">{product.category}</span>
            <div className="flex items-center gap-0.5 text-yellow-400 font-medium">
              <Star className="w-3.5 h-3.5 fill-current" />
              <span className="text-slate-600">{product.rating}</span>
            </div>
          </div>

          {/* Product Title */}
          <h3
            onClick={() => onQuickView && onQuickView(product)}
            className="text-sm font-medium text-slate-800 line-clamp-2 hover:text-blue-600 transition-colors cursor-pointer leading-tight mb-2 min-h-[40px]"
          >
            {product.name}
          </h3>
        </div>

        <div>
          {/* Price Tag */}
          <div className="mb-2">
            <div className="flex items-baseline gap-2 flex-wrap">
              <span className="text-base font-bold text-[#FF424E]">
                {formatVND(product.salePrice > 0 ? product.salePrice : product.originalPrice)}
              </span>
              {product.discountPercent > 0 && (
                <span className="text-xs text-slate-400 line-through">
                  {formatVND(product.originalPrice)}
                </span>
              )}
            </div>
          </div>

          {/* Stock Bar (for Flash Sale items) */}
          {isFlashSale && flashSaleProduct ? (
            <div className="mb-3 mt-1">
              <StockProgressBar
                soldStock={flashSaleProduct.soldStock}
                totalStock={flashSaleProduct.totalStock}
              />
            </div>
          ) : (
            <div className="text-xs text-slate-500 mb-3 flex items-center justify-between h-[18px]">
              <span>Đã bán: <strong className="text-slate-700 font-semibold">{product.soldCount || 0}</strong></span>
              {isOutOfStock ? (
                <span className="text-red-500 font-semibold">Tạm hết hàng</span>
              ) : (
                <span>Còn lại: <strong className="text-slate-700">{product.stockCount}</strong></span>
              )}
            </div>
          )}

          {/* Action Buttons */}
          <div className="grid grid-cols-2 gap-2 mt-auto">
            <button
              onClick={() => addItem(product)}
              disabled={isOutOfStock}
              className="w-full bg-blue-50 hover:bg-blue-100 text-blue-600 disabled:opacity-50 text-xs font-semibold py-2 rounded transition-colors flex items-center justify-center gap-1 border border-blue-200"
            >
              <ShoppingCart className="w-3.5 h-3.5" />
              <span>Thêm giỏ</span>
            </button>

            <button
              onClick={() => onBuyNow ? onBuyNow(product) : addItem(product)}
              disabled={isOutOfStock}
              className="w-full bg-[#1A94FF] hover:bg-[#0074DA] disabled:bg-slate-300 text-white text-xs font-bold py-2 rounded shadow-sm transition-colors flex items-center justify-center gap-1"
            >
              <span>{isOutOfStock ? 'HẾT HÀNG' : 'MUA NGAY'}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
