import React, { useState } from 'react';
import { X, ShoppingCart, Zap, Star, ShieldCheck, Truck, RotateCcw } from 'lucide-react';
import { Product, FlashSaleProduct } from '../types';
import { useCartStore } from '../store/useCartStore';

interface ProductDetailModalProps {
  product: Product | null;
  onClose: () => void;
  onBuyNow: (product: Product) => void;
}

export const ProductDetailModal: React.FC<ProductDetailModalProps> = ({
  product,
  onClose,
  onBuyNow,
}) => {
  const { addItem } = useCartStore();
  const [quantity, setQuantity] = useState(1);

  if (!product) return null;

  const isFlashSale = 'isFlashSale' in product && product.isFlashSale;
  const flashSaleProduct = isFlashSale ? (product as FlashSaleProduct) : null;

  const isOutOfStock = isFlashSale
    ? flashSaleProduct?.remainingStock === 0
    : product.stockCount === 0;

  const formatVND = (num: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  const handleAddToCart = () => {
    addItem(product, quantity);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/70 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white w-full max-w-3xl rounded-md shadow-2xl overflow-hidden border border-slate-200 relative">
        
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-3 right-3 z-10 bg-slate-100 hover:bg-slate-200 text-slate-600 p-1.5 rounded-full transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
          
          {/* Left: Image & Badge */}
          <div className="space-y-3">
            <div className="relative aspect-square bg-slate-100 rounded-md overflow-hidden border border-slate-200">
              <img
                src={product.imageUrl}
                alt={product.name}
                className="w-full h-full object-cover"
              />
              {product.discountPercent > 0 && (
                <span className="absolute top-3 left-3 bg-rose-600 text-white font-bold text-xs px-2.5 py-1 rounded-md shadow-sm">
                  -{product.discountPercent}%
                </span>
              )}
            </div>

            {/* Guarantees */}
            <div className="grid grid-cols-3 gap-2 text-[11px] text-slate-600 text-center pt-2">
              <div className="p-2 bg-slate-50 rounded border border-slate-200 flex flex-col items-center gap-1">
                <ShieldCheck className="w-4 h-4 text-emerald-600" />
                <span>100% Chính hãng</span>
              </div>
              <div className="p-2 bg-slate-50 rounded border border-slate-200 flex flex-col items-center gap-1">
                <Truck className="w-4 h-4 text-rose-600" />
                <span>Giao hàng 2H</span>
              </div>
              <div className="p-2 bg-slate-50 rounded border border-slate-200 flex flex-col items-center gap-1">
                <RotateCcw className="w-4 h-4 text-amber-600" />
                <span>Lỗi 1 đổi 1 30 ngày</span>
              </div>
            </div>
          </div>

          {/* Right: Details & Specs */}
          <div className="flex flex-col justify-between space-y-4">
            <div>
              <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
                <span className="bg-slate-100 px-2 py-0.5 rounded font-medium">{product.category}</span>
                <div className="flex items-center gap-1 text-amber-500 font-bold">
                  <Star className="w-3.5 h-3.5 fill-current" />
                  <span>{product.rating}</span>
                </div>
              </div>

              <h2 className="text-lg font-bold text-slate-900 leading-snug mb-2">
                {product.name}
              </h2>

              {/* Price Display */}
              <div className="bg-slate-50 p-3 rounded-md border border-slate-200 mb-3">
                <div className="flex items-baseline gap-3">
                  <span className="text-2xl font-extrabold text-rose-600">
                    {formatVND(product.salePrice > 0 ? product.salePrice : product.originalPrice)}
                  </span>
                  {product.discountPercent > 0 && (
                    <span className="text-sm text-slate-400 line-through">
                      {formatVND(product.originalPrice)}
                    </span>
                  )}
                </div>
                {isFlashSale && flashSaleProduct && (
                  <p className="text-xs text-rose-700 font-medium mt-1">
                    ⚡ Giá khuyến mãi Flash Sale áp dụng trong khung giờ mở bán
                  </p>
                )}
              </div>

              {/* Description */}
              <p className="text-xs text-slate-600 leading-relaxed mb-3">
                {product.description}
              </p>

              {/* Specs Table */}
              {product.specs && (
                <div className="mb-4">
                  <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
                    Thông số kỹ thuật chính:
                  </h4>
                  <div className="bg-slate-50 rounded-md border border-slate-200 text-xs overflow-hidden divide-y divide-slate-200">
                    {Object.entries(product.specs).map(([key, val]) => (
                      <div key={key} className="flex px-3 py-1.5">
                        <span className="w-1/3 text-slate-500 font-medium">{key}:</span>
                        <span className="w-2/3 text-slate-800 font-semibold">{val}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Quantity & Action Buttons */}
            <div className="pt-2 border-t border-slate-200 space-y-3">
              <div className="flex items-center gap-3">
                <span className="text-xs font-semibold text-slate-700">Số lượng:</span>
                <div className="flex items-center border border-slate-300 rounded-md bg-white">
                  <button
                    onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                    className="px-2.5 py-1 text-slate-600 hover:bg-slate-100 transition-colors"
                  >
                    -
                  </button>
                  <span className="px-3 text-xs font-bold text-slate-800">{quantity}</span>
                  <button
                    onClick={() => setQuantity((q) => q + 1)}
                    className="px-2.5 py-1 text-slate-600 hover:bg-slate-100 transition-colors"
                  >
                    +
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <button
                  onClick={handleAddToCart}
                  disabled={isOutOfStock}
                  className="bg-slate-100 hover:bg-slate-200 text-slate-800 text-xs font-bold py-2.5 rounded-md border border-slate-300 transition-colors flex items-center justify-center gap-1.5"
                >
                  <ShoppingCart className="w-4 h-4 text-rose-600" />
                  <span>THÊM GIỎ HÀNG</span>
                </button>

                <button
                  onClick={() => {
                    onBuyNow(product);
                    onClose();
                  }}
                  disabled={isOutOfStock}
                  className="bg-rose-600 hover:bg-rose-700 text-white text-xs font-bold py-2.5 rounded-md shadow transition-colors flex items-center justify-center gap-1.5 disabled:bg-slate-400"
                >
                  <Zap className="w-4 h-4 fill-current" />
                  <span>MUA NGAY KHUNG GIỜ</span>
                </button>
              </div>
            </div>

          </div>

        </div>
      </div>
    </div>
  );
};
