import React, { useState } from 'react';
import { X, Trash2, Plus, Minus, ShoppingBag, ArrowRight, Tag, ShieldCheck } from 'lucide-react';
import { useCartStore } from '../store/useCartStore';

interface CartDrawerProps {
  onCheckout: () => void;
}

export const CartDrawer: React.FC<CartDrawerProps> = ({ onCheckout }) => {
  const {
    items,
    isOpen,
    closeCart,
    removeItem,
    updateQuantity,
    appliedVoucher,
    applyVoucher,
    removeVoucher,
    getSubtotalPrice,
    getDiscountAmount,
    getFinalPrice,
  } = useCartStore();

  const [voucherCodeInput, setVoucherCodeInput] = useState('');
  const [voucherFeedback, setVoucherFeedback] = useState<{ success: boolean; message: string } | null>(null);

  if (!isOpen) return null;

  const handleApplyVoucherSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!voucherCodeInput.trim()) return;
    const res = applyVoucher(voucherCodeInput);
    setVoucherFeedback(res);
  };

  const formatVND = (num: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      {/* Dark Overlay Background */}
      <div
        className="absolute inset-0 bg-slate-900/60 backdrop-blur-xs transition-opacity"
        onClick={closeCart}
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white shadow-2xl flex flex-col justify-between">
          
          {/* Header */}
          <div className="p-4 bg-slate-900 text-white flex items-center justify-between border-b border-slate-800">
            <div className="flex items-center gap-2">
              <ShoppingBag className="w-5 h-5 text-rose-500" />
              <h2 className="text-base font-bold">GIỎ HÀNG CỦA BẠN</h2>
              <span className="bg-rose-600 text-white text-xs px-2 py-0.5 rounded-full font-bold">
                {items.reduce((acc, item) => acc + item.quantity, 0)}
              </span>
            </div>
            <button
              onClick={closeCart}
              className="text-slate-400 hover:text-white p-1 rounded-md transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Cart Item List */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {items.length > 0 ? (
              items.map((item) => (
                <div
                  key={item.product.id}
                  className="flex gap-3 p-3 bg-slate-50 border border-slate-200 rounded-md relative group"
                >
                  <img
                    src={item.product.imageUrl}
                    alt={item.product.name}
                    className="w-16 h-16 object-cover rounded-md border border-slate-200 bg-white"
                  />
                  <div className="flex-1 flex flex-col justify-between">
                    <div>
                      <h4 className="text-xs font-semibold text-slate-800 line-clamp-2 pr-6">
                        {item.product.name}
                      </h4>
                      <p className="text-xs font-extrabold text-rose-600 mt-1">
                        {formatVND(item.selectedPrice)}
                      </p>
                    </div>

                    {/* Quantity Controls */}
                    <div className="flex items-center justify-between mt-2">
                      <div className="flex items-center border border-slate-300 rounded-md bg-white">
                        <button
                          onClick={() => updateQuantity(item.product.id, item.quantity - 1)}
                          className="px-2 py-1 text-slate-600 hover:bg-slate-100 transition-colors"
                        >
                          <Minus className="w-3 h-3" />
                        </button>
                        <span className="px-3 text-xs font-bold text-slate-800">
                          {item.quantity}
                        </span>
                        <button
                          onClick={() => updateQuantity(item.product.id, item.quantity + 1)}
                          className="px-2 py-1 text-slate-600 hover:bg-slate-100 transition-colors"
                        >
                          <Plus className="w-3 h-3" />
                        </button>
                      </div>

                      <button
                        onClick={() => removeItem(item.product.id)}
                        className="text-slate-400 hover:text-rose-600 p-1 transition-colors"
                        title="Xóa mặt hàng"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-16 text-slate-500">
                <ShoppingBag className="w-12 h-12 mx-auto text-slate-300 mb-3" />
                <p className="font-semibold text-slate-700">Giỏ hàng của bạn đang trống</p>
                <p className="text-xs text-slate-400 mt-1">Hãy chọn các sản phẩm Flash Sale giá sốc để thêm vào giỏ nhé!</p>
              </div>
            )}
          </div>

          {/* Footer Summary & Checkout */}
          {items.length > 0 && (
            <div className="p-4 bg-slate-50 border-t border-slate-200 space-y-3">
              {/* Voucher Section */}
              <div className="bg-white p-2.5 rounded-md border border-slate-200">
                <form onSubmit={handleApplyVoucherSubmit} className="flex gap-2">
                  <div className="relative flex-1">
                    <Tag className="w-4 h-4 text-slate-400 absolute left-2.5 top-2.5" />
                    <input
                      type="text"
                      placeholder="Nhập mã ưu đãi (FLASHSALE50)"
                      value={voucherCodeInput}
                      onChange={(e) => {
                        setVoucherCodeInput(e.target.value);
                        setVoucherFeedback(null);
                      }}
                      className="w-full text-xs pl-8 pr-2 py-2 border border-slate-300 rounded-md uppercase font-semibold focus:outline-none focus:border-rose-500"
                    />
                  </div>
                  <button
                    type="submit"
                    className="bg-slate-800 hover:bg-slate-900 text-white text-xs font-bold px-3 py-2 rounded-md transition-colors"
                  >
                    Áp dụng
                  </button>
                </form>

                {voucherFeedback && (
                  <p className={`text-[11px] font-medium mt-1.5 ${voucherFeedback.success ? 'text-emerald-600' : 'text-rose-600'}`}>
                    {voucherFeedback.message}
                  </p>
                )}

                {appliedVoucher && (
                  <div className="mt-2 flex items-center justify-between text-xs bg-emerald-50 text-emerald-800 p-2 rounded border border-emerald-200">
                    <span>Mã đã dùng: <strong>{appliedVoucher.code}</strong></span>
                    <button
                      onClick={removeVoucher}
                      className="text-rose-600 font-bold hover:underline"
                    >
                      Bỏ chọn
                    </button>
                  </div>
                )}
              </div>

              {/* Pricing Breakdown */}
              <div className="space-y-1.5 text-xs text-slate-600 pt-1">
                <div className="flex justify-between">
                  <span>Tạm tính:</span>
                  <span className="font-semibold text-slate-800">{formatVND(getSubtotalPrice())}</span>
                </div>
                {getDiscountAmount() > 0 && (
                  <div className="flex justify-between text-emerald-600 font-semibold">
                    <span>Giảm giá Voucher:</span>
                    <span>-{formatVND(getDiscountAmount())}</span>
                  </div>
                )}
                <div className="flex justify-between text-sm font-extrabold text-slate-900 pt-2 border-t border-slate-200">
                  <span>Tổng tiền thanh toán:</span>
                  <span className="text-rose-600 text-base">{formatVND(getFinalPrice())}</span>
                </div>
              </div>

              {/* Checkout Button */}
              <button
                onClick={onCheckout}
                className="w-full bg-rose-600 hover:bg-rose-700 text-white font-bold text-sm py-3 rounded-md shadow-md transition-colors flex items-center justify-center gap-2"
              >
                <span>TIẾN HÀNH THANH TOÁN</span>
                <ArrowRight className="w-4 h-4" />
              </button>

              <div className="flex items-center justify-center gap-1 text-[11px] text-slate-400">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
                <span>Bảo mật thanh toán & Bảo vệ thông tin cá nhân</span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
