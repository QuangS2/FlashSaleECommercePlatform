import React, { useState } from 'react';
import { X, CheckCircle2, ShieldAlert, CreditCard, Truck, Wallet, Mail } from 'lucide-react';
import { useCartStore } from '../store/useCartStore';
import { useOrderQueueStore } from '../store/useOrderQueueStore';
import { orderService } from '../services/orderService';
import { productService } from '../services/productService';
import { useFlashSaleStore } from '../store/useFlashSaleStore';
import { useAuthStore } from '../store/useAuthStore';

interface CheckoutModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOrderSuccess: (orderId: string) => void;
}

export const CheckoutModal: React.FC<CheckoutModalProps> = ({ isOpen, onClose, onOrderSuccess }) => {
  const { items, getSubtotalPrice, getDiscountAmount, getFinalPrice, clearCart, closeCart } = useCartStore();
  const setQueueOpen = useOrderQueueStore((state) => state.setQueueOpen);
  const setQueueStatus = useOrderQueueStore((state) => state.setQueueStatus);
  const { isAuthenticated, user } = useAuthStore();

  const [formData, setFormData] = useState({
    fullName: user?.name || 'Nguyễn Văn Khách',
    phone: '0987654321',
    email: user?.email || 'khachhang@ecommerce.vn',
    address: '123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1',
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
    if (items.length === 0) return;

    setIsSubmitting(true);
    const hasFlashSale = items.some((item) => item.product.isFlashSale);
    const primaryItem = items[0];

    const getGuestUserId = () => {
      try {
        if (typeof window !== 'undefined' && window.localStorage) {
          let gid = window.localStorage.getItem('flsale_guest_user_id');
          if (!gid) {
            gid = `guest_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 7)}`;
            window.localStorage.setItem('flsale_guest_user_id', gid);
          }
          return gid;
        }
      } catch {
        // fallback
      }
      return 'guest_demo_user';
    };

    const currentUserId = isAuthenticated && (user?.sub || user?.username) ? (user?.sub || user?.username) : getGuestUserId();
    const currentUserEmail = user?.email || formData.email || 'customer@ecommerce.vn';

    try {
      // 1. Tạo đơn hàng thực tế vào Order-Service (MySQL) kích hoạt Saga qua Kafka
      const result = await orderService.createOrder({
        productId: primaryItem.product.id,
        productTitle: primaryItem.product.name,
        quantity: primaryItem.quantity,
        unitPrice: primaryItem.selectedPrice || primaryItem.product.salePrice,
        userId: currentUserId,
        userEmail: currentUserEmail,
        shippingAddress: {
          fullName: formData.fullName,
          phone: formData.phone,
          address: formData.address,
          city: formData.city,
          note: formData.note,
        },
        paymentMethod: formData.paymentMethod,
      });

      const finalOrderId = result.orderId;

      // Lưu email khách vào localStorage để khách có thể tra cứu lại đơn hàng
      try {
        if (typeof window !== 'undefined' && window.localStorage && formData.email) {
          window.localStorage.setItem('flsale_last_guest_email', formData.email.trim().toLowerCase());
        }
      } catch {
        // Bỏ qua lỗi storage
      }

      // 2. Cập nhật tăng số lượng Đã bán và giảm tồn kho thời gian thực
      try {
        await productService.incrementSoldCount(primaryItem.product.id, primaryItem.quantity);
      } catch (e) {
        // Bỏ qua lỗi increment nếu offline
      }
      useFlashSaleStore.getState().recordPurchase(primaryItem.product.id, primaryItem.quantity);
      productService.notifyChange();

      setIsSubmitting(false);
      onClose(); // Đóng form checkout
      clearCart();
      closeCart();

      if (hasFlashSale) {
        // Bật Modal Hàng chờ thời gian thực và lắng nghe kết quả Saga
        setQueueOpen(true);
        setQueueStatus('WAITING', finalOrderId);
      } else {
        onOrderSuccess(finalOrderId);
      }
    } catch (error: any) {
      setIsSubmitting(false);
      alert(error.message || 'Có lỗi xảy ra trong quá trình đặt hàng!');
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
                  Họ và tên người nhận <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  placeholder="Ví dụ: Nguyễn Văn A"
                  className="w-full text-xs border border-slate-300 rounded-[4px] px-3 py-2 focus:outline-none focus:border-[#1A94FF]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Số điện thoại <span className="text-red-500">*</span>
                </label>
                <input
                  type="tel"
                  required
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  placeholder="Ví dụ: 0987654321"
                  className="w-full text-xs border border-slate-300 rounded-[4px] px-3 py-2 focus:outline-none focus:border-[#1A94FF]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Email nhận thông tin đơn hàng <span className="text-red-500">*</span>
                </label>
                <input
                  type="email"
                  required
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  placeholder="Ví dụ: email@example.com"
                  className="w-full text-xs border border-slate-300 rounded-[4px] px-3 py-2 focus:outline-none focus:border-[#1A94FF]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Địa chỉ chi tiết <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  placeholder="Số nhà, tên đường, phường/xã..."
                  className="w-full text-xs border border-slate-300 rounded-[4px] px-3 py-2 focus:outline-none focus:border-[#1A94FF]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Tỉnh / Thành phố <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={formData.city}
                  onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                  placeholder="Ví dụ: TP. Hồ Chí Minh"
                  className="w-full text-xs border border-slate-300 rounded-[4px] px-3 py-2 focus:outline-none focus:border-[#1A94FF]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Ghi chú giao hàng
                </label>
                <textarea
                  rows={2}
                  value={formData.note}
                  onChange={(e) => setFormData({ ...formData, note: e.target.value })}
                  placeholder="Ghi chú thêm cho shipper (nếu có)..."
                  className="w-full text-xs border border-slate-300 rounded-[4px] px-3 py-2 focus:outline-none focus:border-[#1A94FF]"
                />
              </div>
            </div>

            {/* Right Column: Order Summary & Payment Method */}
            <div className="space-y-4">
              <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider flex items-center gap-1.5 pb-2 border-b border-slate-200">
                <CreditCard className="w-4 h-4 text-[#1A94FF]" />
                <span>Phương thức thanh toán</span>
              </h3>

              <div className="space-y-2">
                <label
                  onClick={() => setFormData({ ...formData, paymentMethod: 'COD' })}
                  className={`flex items-center gap-3 p-3 border rounded-[4px] cursor-pointer transition-colors ${
                    formData.paymentMethod === 'COD'
                      ? 'border-[#1A94FF] bg-blue-50/50 text-[#1A94FF] font-semibold'
                      : 'border-slate-200 hover:bg-slate-50 text-slate-700'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    value="COD"
                    checked={formData.paymentMethod === 'COD'}
                    onChange={() => {}}
                    className="accent-[#1A94FF]"
                  />
                  <span className="text-xs">Thanh toán khi nhận hàng (COD)</span>
                </label>

                <label
                  onClick={() => setFormData({ ...formData, paymentMethod: 'VNPAY' })}
                  className={`flex items-center gap-3 p-3 border rounded-[4px] cursor-pointer transition-colors ${
                    formData.paymentMethod === 'VNPAY'
                      ? 'border-[#1A94FF] bg-blue-50/50 text-[#1A94FF] font-semibold'
                      : 'border-slate-200 hover:bg-slate-50 text-slate-700'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    value="VNPAY"
                    checked={formData.paymentMethod === 'VNPAY'}
                    onChange={() => {}}
                    className="accent-[#1A94FF]"
                  />
                  <span className="text-xs">Cổng thanh toán VNPAY QR</span>
                </label>

                <label
                  onClick={() => setFormData({ ...formData, paymentMethod: 'KEYCLOAK_WALLET' })}
                  className={`flex items-center gap-3 p-3 border rounded-[4px] cursor-pointer transition-colors ${
                    formData.paymentMethod === 'KEYCLOAK_WALLET'
                      ? 'border-[#1A94FF] bg-blue-50/50 text-[#1A94FF] font-semibold'
                      : 'border-slate-200 hover:bg-slate-50 text-slate-700'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    value="KEYCLOAK_WALLET"
                    checked={formData.paymentMethod === 'KEYCLOAK_WALLET'}
                    onChange={() => {}}
                    className="accent-[#1A94FF]"
                  />
                  <span className="text-xs">Ví điện tử Nội bộ (Ví Keycloak OAuth2)</span>
                </label>
              </div>

              {/* Order Summary Box */}
              <div className="bg-slate-50 p-3.5 rounded-[4px] border border-slate-200 space-y-2 mt-4">
                <div className="text-xs font-bold text-slate-800 pb-1.5 border-b border-slate-200">
                  Tóm tắt đơn hàng ({items.length} món)
                </div>

                <div className="max-h-28 overflow-y-auto space-y-1.5 pr-1">
                  {items.map((item) => (
                    <div key={item.product.id} className="flex justify-between text-xs text-slate-600">
                      <span className="truncate max-w-[170px]">{item.product.name} x{item.quantity}</span>
                      <span className="font-medium">{formatVND((item.selectedPrice || item.product.salePrice) * item.quantity)}</span>
                    </div>
                  ))}
                </div>

                <div className="pt-2 border-t border-slate-200 space-y-1 text-xs">
                  <div className="flex justify-between text-slate-500">
                    <span>Tạm tính:</span>
                    <span>{formatVND(getSubtotalPrice())}</span>
                  </div>
                  {getDiscountAmount() > 0 && (
                    <div className="flex justify-between text-emerald-600 font-medium">
                      <span>Giảm giá Voucher:</span>
                      <span>-{formatVND(getDiscountAmount())}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-slate-900 font-bold text-sm pt-1 border-t border-dashed border-slate-300">
                    <span>Tổng thanh toán:</span>
                    <span className="text-[#FF424E]">{formatVND(getFinalPrice())}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Footer Submit Buttons */}
          <div className="pt-4 border-t border-slate-200 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-[4px] transition-colors"
            >
              HỦY BỎ
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="bg-[#FF424E] hover:bg-red-600 text-white text-xs font-bold px-6 py-2.5 rounded-[4px] shadow-sm transition-colors flex items-center gap-2"
            >
              {isSubmitting ? (
                <>
                  <span className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>ĐANG GỬI ĐƠN HÀNG...</span>
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
