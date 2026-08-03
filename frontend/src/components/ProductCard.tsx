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
    <div className="bg-white border border-slate-200 rounded-md overflow-hidden hover:shadow-md transition-shadow flex flex-col justify-between group">
      {/* Top Image Section */}
      <div className="relative aspect-square overflow-hidden bg-slate-100 cursor-pointer" onClick={() => onQuickView && onQuickView(product)}>
        <img
          src={product.imageUrl}
          alt={product.name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          loading="lazy"
        />

        {/* Discount Badge */}
        {product.discountPercent > 0 && (
          <div className="absolute top-2 left-2 bg-rose-600 text-white font-bold text-xs px-2 py-0.5 rounded-md shadow-sm flex items-center gap-0.5">
            <Zap className="w-3 h-3 fill-current" />
            <span>-{product.discountPercent}%</span>
          </div>
        )}

        {/* Flash Sale Tag */}
        {isFlashSale && (
          <div className="absolute top-2 right-2 bg-amber-500 text-slate-900 font-extrabold text-[10px] uppercase px-1.5 py-0.5 rounded-md tracking-wider shadow-sm">
            FLASH SALE
          </div>
        )}
      </div>

      {/* Product Content Details */}
      <div className="p-3 flex-1 flex flex-col justify-between">
        <div>
          {/* Category & Rating */}
          <div className="flex items-center justify-between text-xs text-slate-500 mb-1">
            <span>{product.category}</span>
            <div className="flex items-center gap-1 text-amber-500 font-medium">
              <Star className="w-3.5 h-3.5 fill-current" />
              <span>{product.rating}</span>
            </div>
          </div>

          {/* Product Title */}
          <h3
            onClick={() => onQuickView && onQuickView(product)}
            className="text-sm font-semibold text-slate-800 line-clamp-2 hover:text-rose-600 transition-colors cursor-pointer leading-snug mb-2"
          >
            {product.name}
          </h3>
        </div>

        <div>
          {/* Price Tag */}
          <div className="mb-2">
            <div className="flex items-baseline gap-2">
              <span className="text-base font-extrabold text-rose-600">
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
            <div className="mb-3">
              <StockProgressBar
                soldStock={flashSaleProduct.soldStock}
                totalStock={flashSaleProduct.totalStock}
              />
            </div>
          ) : (
            <div className="text-xs text-slate-500 mb-3">
              {isOutOfStock ? (
                <span className="text-slate-400 font-semibold">Tạm hết hàng</span>
              ) : (
                <span>Còn lại: <strong className="text-slate-700">{product.stockCount}</strong> sản phẩm</span>
              )}
            </div>
          )}

          {/* Action Buttons */}
          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => addItem(product)}
              disabled={isOutOfStock}
              className="w-full bg-slate-100 hover:bg-slate-200 text-slate-700 disabled:opacity-50 text-xs font-semibold py-2 rounded-md transition-colors flex items-center justify-center gap-1 border border-slate-300"
            >
              <ShoppingCart className="w-3.5 h-3.5 text-rose-600" />
              <span>Thêm giỏ</span>
            </button>

            <button
              onClick={() => onBuyNow ? onBuyNow(product) : addItem(product)}
              disabled={isOutOfStock}
              className="w-full bg-rose-600 hover:bg-rose-700 disabled:bg-slate-400 text-white text-xs font-bold py-2 rounded-md shadow-sm transition-colors flex items-center justify-center gap-1"
            >
              <Zap className="w-3.5 h-3.5 fill-current" />
              <span>{isOutOfStock ? 'HẾT HÀNG' : 'MUA NGAY'}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
