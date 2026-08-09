import React from 'react';
import { useOrderQueueStore } from '../store/useOrderQueueStore';
import { Loader2, Hourglass, CheckCircle2, XCircle } from 'lucide-react';

interface QueueModalProps {
  onSuccessRedirect?: (orderId: string) => void;
}

export const QueueModal: React.FC<QueueModalProps> = ({ onSuccessRedirect }) => {
  const { isQueueOpen, queueStatus, orderId, resetQueue } = useOrderQueueStore();

  if (!isQueueOpen) return null;

  // Render varying content based on status
  const renderContent = () => {
    switch (queueStatus) {
      case 'SUCCESS':
        return (
          <>
            <CheckCircle2 className="w-16 h-16 text-emerald-500 mx-auto mb-4" />
            <h3 className="text-lg font-bold text-slate-900 mb-2 uppercase tracking-wide">
              ĐẶT HÀNG THÀNH CÔNG
            </h3>
            <p className="text-xs text-slate-600 mb-6">
              Bạn đã mua thành công sản phẩm Flash Sale!
            </p>
            <button
              onClick={() => {
                resetQueue();
                if (onSuccessRedirect && orderId) {
                  onSuccessRedirect(orderId);
                }
              }}
              className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs py-2.5 rounded-md transition-colors shadow-sm"
            >
              XEM CHI TIẾT ĐƠN HÀNG
            </button>
          </>
        );

      case 'ERROR':
        return (
          <>
            <XCircle className="w-16 h-16 text-rose-500 mx-auto mb-4" />
            <h3 className="text-lg font-bold text-slate-900 mb-2 uppercase tracking-wide">
              ĐẶT HÀNG THẤT BẠI
            </h3>
            <p className="text-xs text-slate-600 mb-6">
              Rất tiếc, sản phẩm Flash Sale đã hết hàng hoặc xảy ra lỗi hệ thống. Vui lòng thử lại sau.
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
              <Loader2 className="w-16 h-16 text-rose-600 animate-spin opacity-20 absolute top-0 left-0" />
              <Hourglass className="w-8 h-8 text-rose-600 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 animate-pulse" />
            </div>
            <h3 className="text-lg font-bold text-slate-900 mb-2 uppercase tracking-wide">
              ĐANG XẾP HÀNG CHỜ...
            </h3>
            <p className="text-xs text-slate-500 leading-relaxed max-w-[260px] mx-auto bg-slate-50 p-3 rounded border border-slate-100">
              Vui lòng không đóng trình duyệt. Hệ thống đang xử lý đơn hàng của bạn để đảm bảo công bằng trong phiên tải cao.
            </p>
            
            {/* Fake progress bar */}
            <div className="w-full h-1.5 bg-slate-100 rounded-full mt-6 overflow-hidden">
              <div className="h-full bg-rose-500 rounded-full w-2/3 animate-pulse" />
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
