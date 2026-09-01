import React, { useEffect, useState, useCallback } from 'react';
import { X, Package, Clock, CheckCircle2, AlertCircle, RefreshCw, ChevronRight, ShoppingBag, Search, Mail, User } from 'lucide-react';
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
  const [guestEmailInput, setGuestEmailInput] = useState<string>('');

  const { isAuthenticated, user, loginWithKeycloak } = useAuthStore();
  const userId = user?.sub || user?.username || '';

  const loadOrders = useCallback(async (overrideEmail?: string) => {
    setLoading(true);

    try {
      if (isAuthenticated && userId) {
        // 1. Người dùng đã đăng nhập -> Tải đơn hàng theo User ID và User Email
        const userEmail = user?.email || '';
        const cachedUser = orderService.getCachedOrders(`user_${userId}`);
        const cachedEmail = userEmail ? orderService.getCachedOrders(`email_${userEmail.trim().toLowerCase()}`) : [];
        const combinedCached = Array.from(new Map([...cachedUser, ...cachedEmail].map((o) => [o.orderId, o])).values());
        
        if (combinedCached.length > 0) {
          setOrders(combinedCached);
        }

        const liveUserOrders = await orderService.getUserOrders(userId, userEmail);
        setOrders(liveUserOrders);
      } else {
        // 2. Khách vãng lai -> Tải đơn hàng theo Email hoặc phiên khách hiện tại
        const savedEmail = typeof window !== 'undefined' && window.localStorage
          ? (window.localStorage.getItem('flsale_last_guest_email') || '')
          : '';
        const targetEmail = (overrideEmail !== undefined ? overrideEmail : (guestEmailInput || savedEmail)).trim().toLowerCase();

        if (savedEmail && !guestEmailInput) {
          setGuestEmailInput(savedEmail);
        }

        // Đọc cache phiên khách cục bộ trước
        const guestId = typeof window !== 'undefined' && window.localStorage ? window.localStorage.getItem('flsale_guest_user_id') : null;
        const cachedSession = [
          ...orderService.getCachedOrders('flsale_guest_temp_orders'),
          ...(guestId ? orderService.getCachedOrders(`user_${guestId}`) : []),
          ...(targetEmail ? orderService.getCachedOrders(`email_${targetEmail}`) : []),
        ];
        const initialCached = Array.from(new Map(cachedSession.map((o) => [o.orderId, o])).values());
        if (initialCached.length > 0) {
          setOrders(initialCached);
        }

        if (targetEmail) {
          const emailOrders = await orderService.getOrdersByEmail(targetEmail);
          if (emailOrders.length > 0) {
            setOrders(emailOrders);
          } else if (initialCached.length > 0) {
            setOrders(initialCached);
          } else {
            setOrders([]);
          }
        } else if (initialCached.length > 0) {
          setOrders(initialCached);
        } else {
          setOrders([]);
        }
      }
    } catch {
      // Bỏ qua lỗi
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, userId, user?.email, guestEmailInput]);

  useEffect(() => {
    if (isOpen) {
      // Đọc email đã lưu trước khi gọi loadOrders
      try {
        if (typeof window !== 'undefined' && window.localStorage) {
          const savedEmail = window.localStorage.getItem('flsale_last_guest_email') || '';
          if (savedEmail) {
            setGuestEmailInput(savedEmail);
          }
        }
      } catch {
        // fallback
      }
      loadOrders();
    } else {
      setFilterTab('ALL');
    }
  }, [isOpen, isAuthenticated, userId]);

  if (!isOpen) return null;

  const handleGuestSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (guestEmailInput.trim()) {
      const cleanEmail = guestEmailInput.trim().toLowerCase();
      try {
        if (typeof window !== 'undefined' && window.localStorage) {
          window.localStorage.setItem('flsale_last_guest_email', cleanEmail);
        }
      } catch {
        // ignore
      }
      loadOrders(cleanEmail);
    }
  };

  const formatVND = (num: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  const isOrderCompleted = (status: string) => {
    const s = (status || '').toUpperCase();
    return s === 'COMPLETED' || s === 'CONFIRMED' || s === 'SUCCESS';
  };

  const isOrderCancelled = (status: string) => {
    const s = (status || '').toUpperCase();
    return s === 'CANCELLED' || s === 'FAILED' || s === 'CANCELLED_OUT_OF_STOCK' || s === 'PAYMENT_FAILED';
  };

  const isOrderPending = (status: string) => {
    const s = (status || '').toUpperCase();
    return s === 'PENDING' || s === 'INVENTORY_RESERVED' || s === 'PAYMENT_PENDING' || (!isOrderCompleted(s) && !isOrderCancelled(s));
  };

  const filteredOrders = orders.filter((order) => {
    if (filterTab === 'ALL') return true;
    if (filterTab === 'COMPLETED') return isOrderCompleted(order.status);
    if (filterTab === 'CANCELLED') return isOrderCancelled(order.status);
    if (filterTab === 'PENDING') return isOrderPending(order.status);
    return true;
  });

  const getStatusBadge = (status: string) => {
    if (isOrderCompleted(status)) {
      return (
        <span className="inline-flex items-center gap-1 bg-emerald-50 text-emerald-700 font-semibold px-2 py-0.5 rounded text-[11px] border border-emerald-200">
          <CheckCircle2 className="w-3 h-3" />
          Đã hoàn tất
        </span>
      );
    }
    if (isOrderCancelled(status)) {
      return (
        <span className="inline-flex items-center gap-1 bg-red-50 text-red-700 font-semibold px-2 py-0.5 rounded text-[11px] border border-red-200">
          <AlertCircle className="w-3 h-3" />
          Đã hủy
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 bg-blue-50 text-[#1A94FF] font-semibold px-2 py-0.5 rounded text-[11px] border border-blue-200">
        <Clock className="w-3 h-3 animate-spin" />
        Đang xử lý
      </span>
    );
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-slate-900/60 backdrop-blur-xs flex justify-end animate-fade-in">
      <div className="bg-white w-full max-w-md h-full shadow-2xl flex flex-col border-l border-slate-200 animate-slide-left">
        {/* Header */}
        <div className="p-4 bg-slate-900 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Package className="w-5 h-5 text-[#1A94FF]" />
            <h3 className="font-bold text-sm uppercase tracking-wide">
              {isAuthenticated ? `ĐƠN HÀNG CỦA TÔI (${orders.length})` : `TRA CỨU ĐƠN HÀNG (${orders.length})`}
            </h3>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => loadOrders()}
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

        {/* User / Guest Status Banner */}
        {isAuthenticated ? (
          <div className="bg-blue-50/70 border-b border-blue-100 px-4 py-2.5 flex items-center justify-between text-xs text-blue-900">
            <div className="flex items-center gap-2 truncate">
              <User className="w-4 h-4 text-[#1A94FF] shrink-0" />
              <span className="truncate">
                Tài khoản: <strong>{user?.name || user?.username}</strong>
              </span>
            </div>
            <span className="text-[11px] text-blue-600 shrink-0 font-medium">Đã đăng nhập</span>
          </div>
        ) : (
          <div className="bg-slate-50 border-b border-slate-200 p-3">
            <form onSubmit={handleGuestSearch} className="flex gap-2">
              <div className="relative flex-1">
                <Mail className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  value={guestEmailInput}
                  onChange={(e) => setGuestEmailInput(e.target.value)}
                  placeholder="Nhập email khi mua để tra cứu..."
                  className="w-full text-xs bg-white border border-slate-300 rounded pl-8 pr-2.5 py-1.5 focus:outline-none focus:border-[#1A94FF] focus:ring-1 focus:ring-[#1A94FF]"
                />
              </div>
              <button
                type="submit"
                disabled={loading || !guestEmailInput.trim()}
                className="bg-[#1A94FF] hover:bg-[#0074DA] disabled:bg-slate-300 text-white text-xs font-semibold px-3 py-1.5 rounded transition-colors flex items-center gap-1"
              >
                <Search className="w-3.5 h-3.5" />
                <span>Tìm</span>
              </button>
            </form>
            <div className="flex items-center justify-between mt-2 text-[11px] text-slate-500">
              <span>Hoặc đăng nhập để tự động lưu:</span>
              <button
                type="button"
                onClick={loginWithKeycloak}
                className="font-bold text-[#1A94FF] hover:underline"
              >
                Đăng nhập ngay
              </button>
            </div>
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
              <span>Đang tải danh sách đơn hàng...</span>
            </div>
          ) : filteredOrders.length === 0 ? (
            <div className="py-16 text-center text-xs text-slate-500 flex flex-col items-center gap-2 bg-white rounded border border-slate-200 p-6">
              <ShoppingBag className="w-10 h-10 text-slate-300 stroke-1" />
              <span className="font-semibold text-slate-700">Chưa có đơn hàng nào</span>
              <span className="text-[11px] text-slate-400 text-center">
                {isAuthenticated
                  ? 'Tài khoản của bạn chưa có đơn hàng nào. Hãy mua sắm các deal Flash Sale hot!'
                  : guestEmailInput.trim()
                  ? `Không tìm thấy đơn hàng nào thuộc email "${guestEmailInput}".`
                  : 'Vui lòng nhập Email bạn đã dùng khi đặt hàng để tra cứu danh sách đơn mua.'}
              </span>
            </div>
          ) : (
            filteredOrders.map((order) => (
              <div
                key={order.orderId}
                onClick={() => onSelectOrder(order)}
                className="bg-white border border-slate-200 hover:border-blue-400 rounded-md p-3.5 shadow-xs hover:shadow-md transition-all cursor-pointer space-y-2 group"
              >
                {/* Header item: Mã đơn + Trạng thái */}
                <div className="flex items-center justify-between pb-2 border-b border-slate-100">
                  <span className="text-xs font-mono font-bold text-slate-800">
                    {order.orderId}
                  </span>
                  {getStatusBadge(order.status)}
                </div>

                {/* Body item: Tên sản phẩm & Thông số */}
                <div className="flex items-start justify-between gap-2">
                  <div className="space-y-0.5">
                    <h4 className="text-xs font-semibold text-slate-900 line-clamp-1 group-hover:text-blue-600 transition-colors">
                      {order.productTitle || 'Sản phẩm Flash Sale'}
                    </h4>
                    <p className="text-[11px] text-slate-500">
                      Số lượng: <strong className="text-slate-700">x{order.quantity}</strong> | Đơn giá: {formatVND(order.unitPrice)}
                    </p>
                  </div>
                  <div className="text-right shrink-0">
                    <span className="text-xs font-bold text-[#FF424E]">
                      {formatVND(order.totalPrice || order.unitPrice * order.quantity)}
                    </span>
                  </div>
                </div>

                {/* Footer item: Ngày đặt & Action */}
                <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-[11px] text-slate-400">
                  <span>
                    {order.createdAt ? new Date(order.createdAt).toLocaleString('vi-VN') : 'Vừa xong'}
                  </span>
                  <span className="text-[#1A94FF] font-medium flex items-center gap-0.5 group-hover:translate-x-0.5 transition-transform">
                    Xem chi tiết <ChevronRight className="w-3.5 h-3.5" />
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
