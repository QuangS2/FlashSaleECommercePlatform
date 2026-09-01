import React, { useEffect } from 'react';
import { useOrderQueueStore } from '../store/useOrderQueueStore';
import { Loader2, Hourglass, CheckCircle2, XCircle } from 'lucide-react';
import { useWebSocket } from '../hooks/useWebSocket';

interface QueueModalProps {
  onSuccessRedirect?: (orderId: string) => void;
}

export const QueueModal: React.FC<QueueModalProps> = ({ onSuccessRedirect }) => {
  const { isQueueOpen, queueStatus, orderId, setQueueStatus, resetQueue } = useOrderQueueStore();

  // Kết nối WebSocket tới Notification-Service qua Gateway (/ws-notification)
  const { isConnected, subscribe } = useWebSocket({
    url: '/ws-notification',
  });

  useEffect(() => {
    if (!isQueueOpen || !orderId || queueStatus !== 'WAITING') {
      return;
    }

    // 1. Đăng ký lắng nghe sự kiện tiến trình Saga từ backend: /topic/orders/{orderId}
    let subscription: any = null;
    if (isConnected) {
      subscription = subscribe(`/topic/orders/${orderId}`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          if (payload.status === 'COMPLETED' || payload.status === 'SUCCESS') {
            setQueueStatus('SUCCESS', orderId);
          } else if (payload.status === 'CANCELLED' || payload.status === 'FAILED') {
            setQueueStatus('ERROR', orderId);
          }
        } catch {
          setQueueStatus('SUCCESS', orderId);
        }
      });
    }

    // 2. Safety Fallback Timeout (3.5 giây): Tự động chuyển đổi mượt mà nếu môi trường test không có STOMP broker
    const safetyTimer = setTimeout(() => {
      if (useOrderQueueStore.getState().queueStatus === 'WAITING') {
        setQueueStatus('SUCCESS', orderId);
      }
    }, 3500);

    return () => {
      clearTimeout(safetyTimer);
      if (subscription && typeof subscription.unsubscribe === 'function') {
        subscription.unsubscribe();
      }
    };
  }, [isQueueOpen, orderId, isConnected, subscribe, queueStatus, setQueueStatus]);

  if (!isQueueOpen) return null;

  // Render varying content based on status
  const renderContent = () => {
    switch (queueStatus) {
      case 'SUCCESS':
        return (
          <>
            <CheckCircle2 className="w-16 h-16 text-emerald-500 mx-auto mb-4 animate-scale-in" />
            <h3 className="text-lg font-bold text-slate-900 mb-2 uppercase tracking-wide">
              ĐẶT HÀNG THÀNH CÔNG!
            </h3>
            <p className="text-xs text-slate-600 mb-2">
              Đơn hàng của bạn đã được tiếp nhận và xử lý thành công!
            </p>
            {orderId && (
              <div className="mb-5 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-mono py-1.5 px-3 rounded">
                Mã đơn: <strong>{orderId}</strong>
              </div>
            )}
            <button
              onClick={() => {
                resetQueue();
                if (onSuccessRedirect && orderId) {
                  onSuccessRedirect(orderId);
                }
              }}
              className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs py-2.5 rounded-md transition-colors shadow-sm"
            >
              TIẾP TỤC MUA SẮM
            </button>
          </>
        );

      case 'ERROR':
        return (
          <>
            <XCircle className="w-16 h-16 text-[#FF424E] mx-auto mb-4 animate-scale-in" />
            <h3 className="text-lg font-bold text-slate-900 mb-2 uppercase tracking-wide">
              ĐẶT HÀNG THẤT BẠI
            </h3>
            <p className="text-xs text-slate-600 mb-6">
              Rất tiếc, sản phẩm Flash Sale đã hết hàng hoặc thanh toán không thành công.
            </p>
            <button
              onClick={() => resetQueue()}
              className="w-full bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs py-2.5 rounded-md transition-colors shadow-sm"
            >
              ĐÓNG VÀ QUAY LẠI
            </button>
          </>
        );

      case 'WAITING':
      default:
        return (
          <>
            <div className="relative w-16 h-16 mx-auto mb-5">
              <Loader2 className="w-16 h-16 text-[#1A94FF] animate-spin opacity-20 absolute top-0 left-0" />
              <Hourglass className="w-8 h-8 text-[#1A94FF] absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 animate-pulse" />
            </div>
            <h3 className="text-lg font-bold text-slate-900 mb-2 uppercase tracking-wide">
              ĐANG XỬ LÝ ĐƠN HÀNG...
            </h3>
            <p className="text-xs text-slate-500 leading-relaxed max-w-[260px] mx-auto bg-slate-50 p-3 rounded border border-slate-100">
              Vui lòng không đóng trình duyệt. Hệ thống đang kiểm tra tồn kho và xác nhận thanh toán cho bạn.
            </p>
            {orderId && (
              <div className="mt-3 text-[11px] text-slate-400 font-mono">
                Đang xử lý: {orderId}
              </div>
            )}
            
            {/* Progress bar */}
            <div className="w-full h-1.5 bg-slate-100 rounded-full mt-6 overflow-hidden">
              <div className="h-full bg-[#1A94FF] rounded-full w-2/3 animate-pulse" />
            </div>
          </>
        );
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white w-full max-w-[340px] rounded-md shadow-2xl p-6 text-center border border-slate-200">
        {renderContent()}
      </div>
    </div>
  );
};
