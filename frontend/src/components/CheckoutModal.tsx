import React, { useState } from 'react';
import { X, CheckCircle2, ShieldAlert, CreditCard, Truck, Wallet } from 'lucide-react';
import { useCartStore } from '../store/useCartStore';
import { useOrderQueueStore } from '../store/useOrderQueueStore';
import keycloak from '../auth/keycloak';

interface CheckoutModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOrderSuccess: (orderId: string) => void;
}

export const CheckoutModal: React.FC<CheckoutModalProps> = ({ isOpen, onClose, onOrderSuccess }) => {
  const { items, getSubtotalPrice, getDiscountAmount, getFinalPrice, clearCart } = useCartStore();
  const setQueueOpen = useOrderQueueStore(state => state.setQueueOpen);
  const setQueueStatus = useOrderQueueStore(state => state.setQueueStatus);

  const [formData, setFormData] = useState({
    fullName: keycloak.tokenParsed?.name || '',
    phone: '0987654321',
    address: '123 Đường Nguyễn Văn Cừ, Quận 5',
    city: 'TP. Hồ Chí Minh',
    paymentMethod: 'COD' as 'COD' | 'VNPAY' | 'KEYCLOAK_WALLET',
    note: '',
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  const formatVND = (num: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  const handleSubmitOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    const hasFlashSale = items.some(item => item.product.isFlashSale);

    try {
      // Gọi API trừ tồn kho cho từng sản phẩm (Tuần tự hoặc Promise.all)
      // Trong thực tế sẽ có 1 API Checkout tổng, nhưng ở đây ta test Redisson Lock qua API deduct
      for (const item of items) {
        // Sử dụng Vite Proxy: /api/v1/... sẽ tự động route tới API Gateway (8080) -> Inventory Service (8082)
        const res = await fetch(`/api/v1/inventory/deduct?productId=${item.product.id}&quantity=${item.quantity}`, {
          method: 'POST'
        });
        if (!res.ok) {
          throw new Error(`Sản phẩm ${item.product.name} đã hết hàng hoặc không đủ số lượng!`);
        }
      }

      setIsSubmitting(false);
      onClose(); // Đóng form checkout
      
      const generatedOrderId = `ORD-${Math.floor(100000 + Math.random() * 900000)}`;
      clearCart();

      if (hasFlashSale) {
        // Bật Modal Hàng chờ
        setQueueOpen(true);
        setQueueStatus('WAITING');
        
        // MÔ PHỎNG: Nhận tín hiệu WebSocket 'SUCCESS' từ backend sau 3.5 giây
        setTimeout(() => {
          setQueueStatus('SUCCESS', generatedOrderId);
        }, 3500);
      } else {
        onOrderSuccess(generatedOrderId);
      }
    } catch (error: any) {
      setIsSubmitting(false);
      alert(error.message); // Hiển thị lỗi hết hàng
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/70 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white w-full max-w-2xl rounded-md shadow-2xl overflow-hidden border border-slate-200">
        
        {/* Header */}
        <div className="bg-slate-900 text-white p-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 text-emerald-500" />
            <h2 className="text-base font-bold">XÁC NHẬN VÀ THANH TOÁN ĐƠN HÀNG</h2>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white p-1">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Form */}
        <form onSubmit={handleSubmitOrder} className="p-6 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            {/* Left Column: Shipping Address */}
            <div className="space-y-4">
              <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider flex items-center gap-1.5 pb-2 border-b border-slate-200">
                <Truck className="w-4 h-4 text-[#1A94FF]" />
                <span>Thông tin giao hàng</span>
              </h3>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Họ và tên người nhận *
                </label>
                <input
                  type="text"
                  required
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  className="w-full text-xs p-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Số điện thoại *
                </label>
                <input
                  type="tel"
                  required
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  className="w-full text-xs p-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Địa chỉ giao hàng *
                </label>
                <input
                  type="text"
                  required
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  className="w-full text-xs p-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Tỉnh / Thành phố *
                </label>
                <input
                  type="text"
                  required
                  value={formData.city}
                  onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                  className="w-full text-xs p-2.5 border border-slate-300 rounded-md focus:outline-none focus:border-blue-500"
                />
              </div>
            </div>

            {/* Right Column: Payment Method & Summary */}
            <div className="space-y-4">
              <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider flex items-center gap-1.5 pb-2 border-b border-slate-200">
                <CreditCard className="w-4 h-4 text-[#1A94FF]" />
                <span>Phương thức thanh toán</span>
              </h3>

              <div className="space-y-2">
                <label
                  onClick={() => setFormData({ ...formData, paymentMethod: 'COD' })}
                  className={`flex items-center gap-3 p-3 rounded-md border cursor-pointer transition-colors ${
                    formData.paymentMethod === 'COD'
                      ? 'border-[#1A94FF] bg-blue-50/50 text-blue-900 font-semibold'
                      : 'border-slate-200 hover:bg-slate-50 text-slate-700'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    checked={formData.paymentMethod === 'COD'}
                    onChange={() => {}}
                    className="text-[#1A94FF] focus:ring-blue-500"
                  />
                  <Truck className="w-4 h-4 text-slate-600" />
                  <span className="text-xs">Thanh toán khi nhận hàng (COD)</span>
                </label>

                <label
                  onClick={() => setFormData({ ...formData, paymentMethod: 'VNPAY' })}
                  className={`flex items-center gap-3 p-3 rounded-md border cursor-pointer transition-colors ${
                    formData.paymentMethod === 'VNPAY'
                      ? 'border-[#1A94FF] bg-blue-50/50 text-blue-900 font-semibold'
                      : 'border-slate-200 hover:bg-slate-50 text-slate-700'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    checked={formData.paymentMethod === 'VNPAY'}
                    onChange={() => {}}
                    className="text-[#1A94FF] focus:ring-blue-500"
                  />
                  <CreditCard className="w-4 h-4 text-slate-600" />
                  <span className="text-xs">Cổng VNPAY / Thẻ ATM Nội địa & QR Code</span>
                </label>

                <label
                  onClick={() => setFormData({ ...formData, paymentMethod: 'KEYCLOAK_WALLET' })}
                  className={`flex items-center gap-3 p-3 rounded-md border cursor-pointer transition-colors ${
                    formData.paymentMethod === 'KEYCLOAK_WALLET'
                      ? 'border-[#1A94FF] bg-blue-50/50 text-blue-900 font-semibold'
                      : 'border-slate-200 hover:bg-slate-50 text-slate-700'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    checked={formData.paymentMethod === 'KEYCLOAK_WALLET'}
                    onChange={() => {}}
                    className="text-[#1A94FF] focus:ring-blue-500"
                  />
                  <Wallet className="w-4 h-4 text-slate-600" />
                  <span className="text-xs">Ví điện tử Nội bộ (Ví Keycloak OAuth2)</span>
                </label>
              </div>

              {/* Order Summary Box */}
              <div className="bg-slate-50 p-3 rounded-md border border-slate-200 space-y-1.5 text-xs">
                <div className="flex justify-between text-slate-600">
                  <span>Tổng tiền hàng ({items.reduce((a, b) => a + b.quantity, 0)} sp):</span>
                  <span className="font-semibold text-slate-800">{formatVND(getSubtotalPrice())}</span>
                </div>
                {getDiscountAmount() > 0 && (
                  <div className="flex justify-between text-emerald-600 font-semibold">
                    <span>Giảm giá Voucher:</span>
                    <span>-{formatVND(getDiscountAmount())}</span>
                  </div>
                )}
                <div className="flex justify-between text-slate-600">
                  <span>Phí vận chuyển:</span>
                  <span className="font-semibold text-emerald-600">MIỄN PHÍ</span>
                </div>
                <div className="flex justify-between text-sm font-extrabold text-slate-900 pt-2 border-t border-slate-200">
                  <span>Tổng cộng cần trả:</span>
                  <span className="text-[#FF424E] text-base">{formatVND(getFinalPrice())}</span>
                </div>
              </div>

              <div className="flex items-center gap-1.5 text-[11px] text-amber-700 bg-amber-50 p-2.5 rounded border border-amber-200">
                <ShieldAlert className="w-4 h-4 flex-shrink-0 text-amber-600" />
                <span>Đơn hàng Flash Sale sẽ được giữ kho trong 15 phút.</span>
              </div>
            </div>

          </div>

          {/* Action Buttons */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-200">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-md transition-colors border border-slate-300"
            >
              Hủy bỏ
            </button>

            <button
              type="submit"
              disabled={isSubmitting}
              className="bg-[#1A94FF] hover:bg-[#0074DA] text-white text-xs font-bold px-6 py-2.5 rounded-md shadow transition-colors flex items-center gap-2 disabled:opacity-50"
            >
              {isSubmitting ? (
                <>
                  <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>ĐANG ĐẶT HÀNG...</span>
                </>
              ) : (
                <span>XÁC NHẬN ĐẶT HÀNG NGAY</span>
              )}
            </button>
          </div>
        </form>

      </div>
    </div>
  );
};
