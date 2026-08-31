import React from 'react';
import { X, CheckCircle2, Clock, ShieldCheck, CreditCard, Bell, Package, AlertCircle } from 'lucide-react';
import { OrderDetailResponse } from '../services/orderService';

interface OrderDetailModalProps {
  order: OrderDetailResponse | null;
  onClose: () => void;
}

export const OrderDetailModal: React.FC<OrderDetailModalProps> = ({ order, onClose }) => {
  if (!order) return null;

  const formatVND = (num: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
      case 'SUCCESS':
        return (
          <span className="inline-flex items-center gap-1 bg-emerald-50 text-emerald-700 font-semibold px-2.5 py-1 rounded text-xs border border-emerald-200">
            <CheckCircle2 className="w-3.5 h-3.5" />
            ĐÃ HOÀN TẤT
          </span>
        );
      case 'CANCELLED':
      case 'FAILED':
        return (
          <span className="inline-flex items-center gap-1 bg-red-50 text-red-700 font-semibold px-2.5 py-1 rounded text-xs border border-red-200">
            <AlertCircle className="w-3.5 h-3.5" />
            ĐÃ HỦY
          </span>
        );
      case 'PENDING':
      default:
        return (
          <span className="inline-flex items-center gap-1 bg-blue-50 text-[#1A94FF] font-semibold px-2.5 py-1 rounded text-xs border border-blue-200">
            <Clock className="w-3.5 h-3.5 animate-spin" />
            ĐANG XỬ LÝ SAGA
          </span>
        );
    }
  };

  const isSuccess = order.status === 'COMPLETED' || order.status === 'SUCCESS' || order.status === 'PENDING';

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/75 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white w-full max-w-xl rounded-md shadow-2xl overflow-hidden border border-slate-200 animate-scale-in">
        {/* Header */}
        <div className="bg-slate-900 text-white p-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Package className="w-5 h-5 text-[#1A94FF]" />
            <h2 className="text-sm font-bold tracking-wide uppercase">
              CHI TIẾT ĐƠN HÀNG
            </h2>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white p-1 rounded transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-6 max-h-[80vh] overflow-y-auto">
          {/* Order Meta Bar */}
          <div className="flex flex-wrap items-center justify-between gap-3 bg-slate-50 p-3.5 rounded border border-slate-200">
            <div>
              <div className="text-xs text-slate-500 font-medium">Mã đơn hàng:</div>
              <div className="text-sm font-mono font-bold text-slate-900">{order.orderId}</div>
            </div>
            <div>
              <div className="text-xs text-slate-500 font-medium">Trạng thái:</div>
              <div className="mt-0.5">{getStatusBadge(order.status)}</div>
            </div>
          </div>

          {/* Saga Stepper Timeline */}
          <div>
            <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <Clock className="w-4 h-4 text-[#1A94FF]" />
              Tiến trình chuỗi giao dịch phân tán (Saga Orchestration)
            </h4>

            <div className="relative pl-6 border-l-2 border-blue-200 space-y-4 text-xs">
              {/* Step 1 */}
              <div className="relative">
                <div className="absolute -left-[31px] top-0 bg-blue-500 text-white w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold">
                  ✓
                </div>
                <div className="font-semibold text-slate-900">1. Khởi tạo đơn hàng (Order Service)</div>
                <div className="text-slate-500 text-[11px]">Bản ghi đơn hàng lưu vào MySQL với trạng thái PENDING và phát Kafka Event.</div>
              </div>

              {/* Step 2 */}
              <div className="relative">
                <div className="absolute -left-[31px] top-0 bg-blue-500 text-white w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold">
                  ✓
                </div>
                <div className="font-semibold text-slate-900 flex items-center gap-1">
                  <ShieldCheck className="w-3.5 h-3.5 text-blue-600" />
                  2. Khóa & Khấu trừ kho phân tán (Inventory Service)
                </div>
                <div className="text-slate-500 text-[11px]">Redisson Distributed Lock thực thi nguyên tử, chống over-selling thành công.</div>
              </div>

              {/* Step 3 */}
              <div className="relative">
                <div className="absolute -left-[31px] top-0 bg-blue-500 text-white w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold">
                  ✓
                </div>
                <div className="font-semibold text-slate-900 flex items-center gap-1">
                  <CreditCard className="w-3.5 h-3.5 text-blue-600" />
                  3. Xác nhận thanh toán (Payment Service)
                </div>
                <div className="text-slate-500 text-[11px]">Cổng thanh toán xác nhận thành công, phát PaymentCompletedEvent.</div>
              </div>

              {/* Step 4 */}
              <div className="relative">
                <div className={`absolute -left-[31px] top-0 ${isSuccess ? 'bg-emerald-500 text-white' : 'bg-slate-300 text-slate-600'} w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold`}>
                  {isSuccess ? '✓' : '4'}
                </div>
                <div className="font-semibold text-slate-900 flex items-center gap-1">
                  <Bell className="w-3.5 h-3.5 text-emerald-600" />
                  4. Đẩy thông báo thời gian thực (Notification Service)
                </div>
                <div className="text-slate-500 text-[11px]">STOMP WebSocket gửi thông báo cập nhật đơn hàng thành công đến Client.</div>
              </div>
            </div>
          </div>

          {/* Product Items Table */}
          <div>
            <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
              <Package className="w-4 h-4 text-[#1A94FF]" />
              Sản phẩm trong đơn
            </h4>
            <div className="bg-slate-50 rounded border border-slate-200 divide-y divide-slate-200">
              <div className="p-3 flex items-center justify-between text-xs">
                <div>
                  <div className="font-semibold text-slate-800">{order.productTitle || 'Sản phẩm Flash Sale'}</div>
                  <div className="text-slate-500 text-[11px]">Mã SP: {order.productId} | Số lượng: x{order.quantity}</div>
                </div>
                <div className="font-bold text-slate-900">
                  {formatVND(order.totalPrice || order.unitPrice * order.quantity)}
                </div>
              </div>
            </div>
          </div>

          {/* Payment Summary */}
          <div className="bg-slate-50 p-3.5 rounded border border-slate-200 space-y-1.5 text-xs">
            <div className="flex justify-between text-slate-600">
              <span>Đơn giá:</span>
              <span>{formatVND(order.unitPrice)}</span>
            </div>
            <div className="flex justify-between text-slate-600">
              <span>Số lượng:</span>
              <span>x{order.quantity}</span>
            </div>
            <div className="flex justify-between text-slate-900 font-bold text-sm pt-2 border-t border-slate-200">
              <span>Tổng tiền thanh toán:</span>
              <span className="text-[#FF424E]">{formatVND(order.totalPrice || order.unitPrice * order.quantity)}</span>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 bg-slate-100 border-t border-slate-200 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2 bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs rounded transition-colors"
          >
            ĐÓNG
          </button>
        </div>
      </div>
    </div>
  );
};
