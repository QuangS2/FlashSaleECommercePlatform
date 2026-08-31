import React, { useState, useEffect } from 'react';
import { Header } from './components/Header';
import { FlashSaleSection } from './components/FlashSaleSection';
import { ProductCatalog } from './components/ProductCatalog';
import { CartDrawer } from './components/CartDrawer';
import { CheckoutModal } from './components/CheckoutModal';
import { QueueModal } from './components/QueueModal';
import { ProductDetailModal } from './components/ProductDetailModal';
import { AuthModal } from './components/AuthModal';
import { UserProfileDrawer } from './components/UserProfileDrawer';
import { Product } from './types';
import { useCartStore } from './store/useCartStore';
import { useAuthStore } from './store/useAuthStore';
import { CheckCircle2, Truck, ShieldCheck, RotateCcw, Headphones } from 'lucide-react';
import { Toaster } from 'react-hot-toast';

export function App() {
  const [activeCategory, setActiveCategory] = useState<string>('Tất cả');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isCheckoutOpen, setIsCheckoutOpen] = useState<boolean>(false);
  const [isProfileOpen, setIsProfileOpen] = useState<boolean>(false);
  const [lastCompletedOrder, setLastCompletedOrder] = useState<string | null>(null);

  const { addItem } = useCartStore();
  const { initializeAuth } = useAuthStore();

  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  const handleQuickView = (product: Product) => {
    setSelectedProduct(product);
  };

  const handleBuyNow = (product: Product) => {
    addItem(product, 1);
    setIsCheckoutOpen(true);
  };

  const handleOrderSuccess = (orderId: string) => {
    setIsCheckoutOpen(false);
    setLastCompletedOrder(orderId);
  };

  return (
    <div className="min-h-screen bg-slate-100 font-sans text-slate-900 flex flex-col selection:bg-blue-500 selection:text-white">
      {/* Top Main Navigation Header */}
      <Header
        activeCategory={activeCategory}
        onSelectCategory={(cat) => setActiveCategory(cat)}
        onSearch={(term) => setSearchTerm(term)}
        onOpenProfile={() => setIsProfileOpen(true)}
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

        {/* Customer Promise Bar - Cam kết khách hàng (chuẩn E-commerce Tiki/Shopee) */}
        <section className="bg-white border border-slate-200 rounded-md p-5 shadow-sm grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <div className="flex items-center gap-3">
            <div className="bg-slate-50 text-blue-600 p-2.5 rounded-md border border-slate-100">
              <Truck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-800">Giao hàng toàn quốc</h4>
              <p className="text-[11px] text-slate-500">Miễn phí ship từ 300.000đ</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="bg-slate-50 text-blue-600 p-2.5 rounded-md border border-slate-100">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-800">100% chính hãng</h4>
              <p className="text-[11px] text-slate-500">Cam kết hàng nhập khẩu chính ngạch</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="bg-slate-50 text-blue-600 p-2.5 rounded-md border border-slate-100">
              <RotateCcw className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-800">Đổi trả trong 30 ngày</h4>
              <p className="text-[11px] text-slate-500">Lỗi 1 đổi 1 trong 30 ngày đầu</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="bg-slate-50 text-blue-600 p-2.5 rounded-md border border-slate-100">
              <Headphones className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-800">Hỗ trợ 24/7</h4>
              <p className="text-[11px] text-slate-500">Tư vấn nhiệt tình, chu đáo</p>
            </div>
          </div>
        </section>
      </main>

      {/* Footer - Chuẩn E-commerce */}
      <footer className="bg-slate-900 text-slate-400 text-xs py-8 border-t border-slate-800">
        <div className="max-w-7xl mx-auto px-4 grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="col-span-1 md:col-span-2">
            <h4 className="font-bold text-slate-200 text-sm mb-3">VỀ FLSALE</h4>
            <p className="text-slate-400 leading-relaxed max-w-sm">
              Nền tảng mua sắm trực tuyến với hàng nghìn sản phẩm chính hãng, giá ưu đãi hấp dẫn từ các thương hiệu hàng đầu. Trải nghiệm mua sắm mượt mà, tiện lợi.
            </p>
          </div>

          <div>
            <h4 className="font-bold text-slate-200 text-sm mb-3">HỖ TRỢ KHÁCH HÀNG</h4>
            <ul className="space-y-2 text-slate-400">
              <li className="hover:text-blue-400 cursor-pointer transition-colors">Hướng dẫn mua hàng</li>
              <li className="hover:text-blue-400 cursor-pointer transition-colors">Chính sách đổi trả</li>
              <li className="hover:text-blue-400 cursor-pointer transition-colors">Chính sách bảo hành</li>
              <li className="hover:text-blue-400 cursor-pointer transition-colors">Câu hỏi thường gặp (FAQ)</li>
            </ul>
          </div>

          <div>
            <h4 className="font-bold text-slate-200 text-sm mb-3">LIÊN HỆ</h4>
            <ul className="space-y-2 text-slate-400">
              <li>Hotline: <span className="text-blue-400 font-bold">1900 6868</span></li>
              <li>Email: support@ecommerce.vn</li>
              <li>Thời gian làm việc: 8:00 - 22:00 hàng ngày</li>
            </ul>
          </div>
        </div>

        <div className="max-w-7xl mx-auto px-4 mt-8 pt-4 border-t border-slate-800 text-center text-slate-500">
          <p>© 2024 Ecommerce Platform. Tất cả các quyền được bảo lưu.</p>
        </div>
      </footer>

      {/* Slide-over Cart Drawer */}
      <CartDrawer
        onCheckout={() => setIsCheckoutOpen(true)}
      />

      {/* Order Checkout Modal */}
      <CheckoutModal
        isOpen={isCheckoutOpen}
        onClose={() => setIsCheckoutOpen(false)}
        onOrderSuccess={handleOrderSuccess}
      />

      {/* Queue Waiting Room Modal */}
      <QueueModal
        onSuccessRedirect={handleOrderSuccess}
      />

      {/* Product Detail Modal */}
      <ProductDetailModal
        product={selectedProduct}
        onClose={() => setSelectedProduct(null)}
        onBuyNow={handleBuyNow}
      />

      {/* In-App Authentication Modal */}
      <AuthModal />

      {/* User Profile & Account Drawer */}
      <UserProfileDrawer
        isOpen={isProfileOpen}
        onClose={() => setIsProfileOpen(false)}
      />

      {/* Success Order Toast Modal */}
      {lastCompletedOrder && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white max-w-md w-full rounded-md shadow-2xl p-6 text-center border border-slate-200">
            <CheckCircle2 className="w-16 h-16 text-emerald-500 mx-auto mb-3" />
            <h3 className="text-lg font-bold text-slate-900 mb-1">ĐẶT HÀNG THÀNH CÔNG!</h3>
            <p className="text-sm text-slate-600 mb-4">
              Mã đơn hàng: <strong className="text-blue-600 bg-blue-50 px-2 py-0.5 rounded font-mono text-base tracking-wide">{lastCompletedOrder}</strong>
            </p>
            <p className="text-xs text-slate-500 bg-slate-50 p-3 rounded-md border border-slate-200 mb-6 leading-relaxed">
              Đơn hàng đang được xử lý. Bạn sẽ nhận được thông báo khi đơn hàng sẵn sàng giao.
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
