import React, { useState, useEffect } from 'react';
import { Header } from './components/Header';
import { FlashSaleSection } from './components/FlashSaleSection';
import { ProductCatalog } from './components/ProductCatalog';
import { CartDrawer } from './components/CartDrawer';
import { CheckoutModal } from './components/CheckoutModal';
import { QueueModal } from './components/QueueModal';
import { ProductDetailModal } from './components/ProductDetailModal';
import { OrderHistoryDrawer } from './components/OrderHistoryDrawer';
import { OrderDetailModal } from './components/OrderDetailModal';
import { Product } from './types';
import { useCartStore } from './store/useCartStore';
import { useFlashSaleStore } from './store/useFlashSaleStore';
import { orderService, OrderDetailResponse } from './services/orderService';
import { CheckCircle2, Truck, ShieldCheck, RotateCcw, Headphones } from 'lucide-react';
import { Toaster } from 'react-hot-toast';

export function App() {
  const [activeCategory, setActiveCategory] = useState<string>('Tất cả');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isCheckoutOpen, setIsCheckoutOpen] = useState<boolean>(false);
  const [isOrderHistoryOpen, setIsOrderHistoryOpen] = useState<boolean>(false);
  const [selectedOrderForDetail, setSelectedOrderForDetail] = useState<OrderDetailResponse | null>(null);
  const [lastCompletedOrder, setLastCompletedOrder] = useState<string | null>(null);

  const { addItem } = useCartStore();
  const loadLiveProducts = useFlashSaleStore((state) => state.loadLiveProducts);

  useEffect(() => {
    loadLiveProducts();
  }, [loadLiveProducts]);

  const handleQuickView = (product: Product) => {
    setSelectedProduct(product);
  };

  const handleBuyNow = (product: Product) => {
    addItem(product, 1);
    setIsCheckoutOpen(true);
  };

  const handleOrderSuccess = async (orderId: string) => {
    setIsCheckoutOpen(false);
    setLastCompletedOrder(orderId);

    // Tự động tải chi tiết đơn hàng để người dùng có thể xem trực tiếp Saga trace
    const detail = await orderService.getOrderById(orderId);
    if (detail) {
      setSelectedOrderForDetail(detail);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 font-sans text-slate-900 flex flex-col selection:bg-blue-500 selection:text-white">
      {/* Top Main Navigation Header */}
      <Header
        activeCategory={activeCategory}
        onSelectCategory={(cat) => setActiveCategory(cat)}
        onSearch={(term) => setSearchTerm(term)}
        onOpenOrderHistory={() => setIsOrderHistoryOpen(true)}
      />

      {/* Main Page Body Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 py-6">
        {/* Flash Sale Banner & Product Grid */}
        <FlashSaleSection
          onQuickView={handleQuickView}
          onBuyNow={handleBuyNow}
        />

        {/* Standard Catalog Products Section */}
        <ProductCatalog
          activeCategory={activeCategory}
          searchTerm={searchTerm}
          onQuickView={handleQuickView}
          onBuyNow={handleBuyNow}
        />

        {/* Customer Promise Bar - Cam kết khách hàng */}
        <section className="mt-8 bg-white border border-slate-200 rounded-md p-6 grid grid-cols-2 md:grid-cols-4 gap-6 text-slate-700 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-blue-50 text-[#1A94FF] rounded-md shrink-0">
              <Truck className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wide text-slate-800">Giao Hàng Siêu Tốc 2H</h4>
              <p className="text-[11px] text-slate-500 mt-0.5">Nhận hàng nhanh chóng trong ngày</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div className="p-3 bg-blue-50 text-[#1A94FF] rounded-md shrink-0">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wide text-slate-800">100% Hàng Chính Hãng</h4>
              <p className="text-[11px] text-slate-500 mt-0.5">Hoàn tiền 200% nếu phát hiện giả</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div className="p-3 bg-blue-50 text-[#1A94FF] rounded-md shrink-0">
              <RotateCcw className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wide text-slate-800">30 Ngày Đổi Trả</h4>
              <p className="text-[11px] text-slate-500 mt-0.5">Thủ tục nhanh gọn, tiện lợi</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div className="p-3 bg-blue-50 text-[#1A94FF] rounded-md shrink-0">
              <Headphones className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wide text-slate-800">Hỗ Trợ Tận Tâm 24/7</h4>
              <p className="text-[11px] text-slate-500 mt-0.5">Tư vấn kỹ thuật chu đáo</p>
            </div>
          </div>
        </section>
      </main>

      {/* Footer Navigation */}
      <footer className="bg-white border-t border-slate-200 mt-12 py-8 text-xs text-slate-600">
        <div className="max-w-7xl mx-auto px-4 grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <div className="font-bold text-[#1A94FF] text-xl tracking-tight mb-3">FLSALE</div>
            <p className="text-slate-500 leading-relaxed">
              Nền tảng thương mại điện tử mua sắm Flash Sale trực tuyến hàng đầu, mang lại trải nghiệm mua sắm mượt mà với công nghệ xử lý giao dịch phân tán.
            </p>
          </div>
          <div>
            <h4 className="font-bold text-slate-800 uppercase tracking-wider mb-3">VỀ CHÚNG TÔI</h4>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-blue-600 transition-colors">Giới thiệu</a></li>
              <li><a href="#" className="hover:text-blue-600 transition-colors">Tuyển dụng</a></li>
              <li><a href="#" className="hover:text-blue-600 transition-colors">Chính sách bảo mật</a></li>
            </ul>
          </div>
          <div>
            <h4 className="font-bold text-slate-800 uppercase tracking-wider mb-3">HỖ TRỢ KHÁCH HÀNG</h4>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-blue-600 transition-colors">Trung tâm trợ giúp</a></li>
              <li><a href="#" className="hover:text-blue-600 transition-colors">Hướng dẫn mua hàng</a></li>
              <li><a href="#" className="hover:text-blue-600 transition-colors">Chính sách vận chuyển</a></li>
            </ul>
          </div>
          <div>
            <h4 className="font-bold text-slate-800 uppercase tracking-wider mb-3">THANH TOÁN AN TOÀN</h4>
            <div className="flex flex-wrap gap-2 text-slate-500 font-semibold">
              <span className="bg-slate-100 px-2 py-1 rounded border border-slate-200">COD</span>
              <span className="bg-slate-100 px-2 py-1 rounded border border-slate-200">VNPAY</span>
              <span className="bg-slate-100 px-2 py-1 rounded border border-slate-200">OAuth2 Wallet</span>
            </div>
          </div>
        </div>
        <div className="max-w-7xl mx-auto px-4 mt-8 pt-6 border-t border-slate-100 text-center text-slate-400">
          <p>© 2026 Flash Sale E-Commerce Platform. All rights reserved.</p>
        </div>
      </footer>

      {/* Cart Drawer */}
      <CartDrawer onCheckout={() => setIsCheckoutOpen(true)} />

      {/* Direct Order Checkout Modal */}
      <CheckoutModal
        isOpen={isCheckoutOpen}
        onClose={() => setIsCheckoutOpen(false)}
        onOrderSuccess={handleOrderSuccess}
      />

      {/* Queue Modal for Flash Sale Traffic Management */}
      <QueueModal onSuccessRedirect={handleOrderSuccess} />

      {/* Product Quick View Detail Modal */}
      <ProductDetailModal
        product={selectedProduct}
        onClose={() => setSelectedProduct(null)}
        onBuyNow={handleBuyNow}
      />

      {/* Order History Drawer */}
      <OrderHistoryDrawer
        isOpen={isOrderHistoryOpen}
        onClose={() => setIsOrderHistoryOpen(false)}
        onSelectOrder={(order) => {
          setSelectedOrderForDetail(order);
        }}
      />

      {/* Order Detail Modal with Saga Stepper Timeline */}
      <OrderDetailModal
        order={selectedOrderForDetail}
        onClose={() => setSelectedOrderForDetail(null)}
      />

      {/* Success Order Toast Modal */}
      {lastCompletedOrder && !selectedOrderForDetail && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white max-w-md w-full rounded-md shadow-2xl p-6 text-center border border-slate-200">
            <CheckCircle2 className="w-16 h-16 text-emerald-500 mx-auto mb-3" />
            <h3 className="text-lg font-bold text-slate-900 mb-1">ĐẶT HÀNG THÀNH CÔNG!</h3>
            <p className="text-sm text-slate-600 mb-4">
              Mã đơn hàng: <strong className="text-blue-600 bg-blue-50 px-2 py-0.5 rounded font-mono text-base tracking-wide">{lastCompletedOrder}</strong>
            </p>
            <p className="text-xs text-slate-500 bg-slate-50 p-3 rounded-md border border-slate-200 mb-6 leading-relaxed">
              Đơn hàng đang được xử lý qua chuỗi giao dịch Saga. Bạn có thể theo dõi trong mục Đơn mua.
            </p>
            <button
              onClick={() => setLastCompletedOrder(null)}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm py-2.5 rounded-md transition-colors"
            >
              TIẾP TỤC MUA SẮM
            </button>
          </div>
        </div>
      )}

      {/* Global Notification Toaster */}
      <Toaster 
        position="top-right" 
        toastOptions={{
          className: 'text-sm font-medium shadow-lg rounded-md border border-slate-100',
          duration: 4000,
        }} 
      />
    </div>
  );
}

export default App;
