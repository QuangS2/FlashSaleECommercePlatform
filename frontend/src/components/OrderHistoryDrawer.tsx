import React, { useEffect, useState } from 'react';
import { X, Package, Clock, CheckCircle2, AlertCircle, RefreshCw, ChevronRight, ShoppingBag, ShieldAlert } from 'lucide-react';
import { orderService, OrderDetailResponse } from '../services/orderService';
import { useAuthStore } from '../store/useAuthStore';

interface OrderHistoryDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectOrder: (order: OrderDetailResponse) => void;
}

export const OrderHistoryDrawer: React.FC<OrderHistoryDrawerProps> = ({
  isOpen,
  onClose,
  onSelectOrder,
}) => {
  const [orders, setOrders] = useState<OrderDetailResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [filterTab, setFilterTab] = useState<'ALL' | 'PENDING' | 'COMPLETED' | 'CANCELLED'>('ALL');

  const { isAuthenticated, user, openLoginModal } = useAuthStore();
  const userId = user?.sub || user?.username || '';

  const loadOrders = async () => {
    setLoading(true);
    try {
      if (isAuthenticated && userId) {
        // Tài khoản đã đăng nhập -> Lấy danh sách đơn hàng thực tế từ MySQL CSDL
        const data = await orderService.getUserOrders(userId);
        setOrders(data || []);
      } else {
        // Khách vãng lai (Guest) -> Chỉ lấy các mã đơn hàng mà khách đã đặt trong phiên này
        const storedGuestOrderIds = localStorage.getItem('flsale_guest_order_ids');
        if (storedGuestOrderIds) {
          const ids: string[] = JSON.parse(storedGuestOrderIds);
          const guestOrders: OrderDetailResponse[] = [];
          for (const id of ids) {
            const detail = await orderService.getOrderById(id);
            if (detail) {
              guestOrders.push(detail);
            }
          }
          setOrders(guestOrders);
        } else {
          // Chưa có đơn hàng nào -> Danh sách hoàn toàn trống (Không mock dữ liệu giả)
          setOrders([]);
        }
      }
    } catch {
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      loadOrders();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const formatVND = (num: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  const filteredOrders = orders.filter((order) => {
    if (filterTab === 'ALL') return true;
    return order.status === filterTab;
  });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
      case 'SUCCESS':
        return (
          <span className="inline-flex items-center gap-1 bg-emerald-50 text-emerald-700 font-semibold px-2 py-0.5 rounded text-[11px] border border-emerald-200">
            <CheckCircle2 className="w-3 h-3" />
            Đã hoàn tất
          </span>
        );
      case 'CANCELLED':
      case 'FAILED':
        return (
          <span className="inline-flex items-center gap-1 bg-red-50 text-red-700 font-semibold px-2 py-0.5 rounded text-[11px] border border-red-200">
            <AlertCircle className="w-3 h-3" />
            Đã hủy
          </span>
        );
      case 'PENDING':
      default:
        return (
          <span className="inline-flex items-center gap-1 bg-blue-50 text-[#1A94FF] font-semibold px-2 py-0.5 rounded text-[11px] border border-blue-200">
            <Clock className="w-3 h-3 animate-spin" />
            Đang xử lý Saga
          </span>
        );
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-slate-900/60 backdrop-blur-xs flex justify-end animate-fade-in">
      <div className="bg-white w-full max-w-md h-full shadow-2xl flex flex-col border-l border-slate-200 animate-slide-left">
        {/* Header */}
        <div className="p-4 bg-slate-900 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Package className="w-5 h-5 text-[#1A94FF]" />
            <h3 className="font-bold text-sm uppercase tracking-wide">
              ĐƠN HÀNG CỦA TÔI ({orders.length})
            </h3>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={loadOrders}
              disabled={loading}
              title="Làm mới"
              className="text-slate-400 hover:text-white p-1 rounded transition-colors"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-[#1A94FF]' : ''}`} />
            </button>
            <button onClick={onClose} className="text-slate-400 hover:text-white p-1 rounded transition-colors">
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Guest Session Notice */}
        {!isAuthenticated && (
          <div className="bg-amber-50 border-b border-amber-200 px-4 py-2.5 flex items-center justify-between text-xs text-amber-800">
            <div className="flex items-center gap-2">
              <ShieldAlert className="w-4 h-4 text-amber-600 shrink-0" />
              <span>Đang xem đơn phiên duyệt web khách.</span>
            </div>
            <button
              onClick={openLoginModal}
              className="font-bold text-[#1A94FF] hover:underline shrink-0"
            >
              Đăng nhập
            </button>
          </div>
        )}

        {/* Filter Tabs */}
        <div className="flex border-b border-slate-200 bg-slate-50 text-xs font-semibold text-slate-600">
          {(['ALL', 'PENDING', 'COMPLETED', 'CANCELLED'] as const).map((tab) => {
            const labels = {
              ALL: 'Tất cả',
              PENDING: 'Đang xử lý',
              COMPLETED: 'Hoàn tất',
              CANCELLED: 'Đã hủy',
            };
            return (
              <button
                key={tab}
                onClick={() => setFilterTab(tab)}
                className={`flex-1 py-2.5 text-center transition-colors border-b-2 ${
                  filterTab === tab
                    ? 'border-[#1A94FF] text-[#1A94FF] bg-white font-bold'
                    : 'border-transparent hover:text-slate-900'
                }`}
              >
                {labels[tab]}
              </button>
            );
          })}
        </div>

        {/* Order List */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-slate-100/60">
          {loading ? (
            <div className="py-12 text-center text-xs text-slate-500 flex flex-col items-center gap-2">
              <RefreshCw className="w-6 h-6 animate-spin text-[#1A94FF]" />
              <span>Đang tải danh sách đơn hàng từ Order Service...</span>
            </div>
          ) : filteredOrders.length === 0 ? (
            <div className="py-16 text-center text-xs text-slate-500 flex flex-col items-center gap-2 bg-white rounded border border-slate-200 p-6">
              <ShoppingBag className="w-10 h-10 text-slate-300 stroke-1" />
              <span className="font-semibold text-slate-700">Chưa có đơn hàng nào</span>
              <span className="text-[11px] text-slate-400 text-center">
                {isAuthenticated
                  ? 'Tài khoản của bạn chưa có đơn hàng nào. Hãy mua sắm các deal Flash Sale hot!'
                  : 'Các đơn hàng bạn mua sẽ xuất hiện tại đây hoặc được gửi qua Email xác nhận.'}
              </span>
            </div>
          ) : (
            filteredOrders.map((order) => (
              <div
                key={order.orderId}
                onClick={() => onSelectOrder(order)}
                className="bg-white rounded border border-slate-200 p-3.5 shadow-xs hover:border-[#1A94FF] hover:shadow-sm cursor-pointer transition-all space-y-2.5 group"
              >
                <div className="flex items-center justify-between text-xs pb-2 border-b border-slate-100">
                  <span className="font-mono font-bold text-slate-900">{order.orderId}</span>
                  {getStatusBadge(order.status)}
                </div>

                <div className="flex items-start justify-between text-xs gap-3">
                  <div>
                    <div className="font-semibold text-slate-800 line-clamp-1 group-hover:text-[#1A94FF] transition-colors">
                      {order.productTitle || 'Sản phẩm mua sắm'}
                    </div>
                    <div className="text-slate-400 text-[11px] mt-0.5">Số lượng: x{order.quantity}</div>
                  </div>
                  <div className="text-right shrink-0">
                    <div className="font-bold text-slate-900">
                      {formatVND(order.totalPrice || order.unitPrice * order.quantity)}
                    </div>
                  </div>
                </div>

                <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
                  <span>{new Date(order.createdAt || Date.now()).toLocaleDateString('vi-VN')}</span>
                  <span className="text-[#1A94FF] font-semibold flex items-center gap-0.5 group-hover:translate-x-0.5 transition-transform">
                    Chi tiết <ChevronRight className="w-3 h-3" />
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
