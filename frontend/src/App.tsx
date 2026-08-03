import React, { useState } from 'react';
import { Header } from './components/Header';
import { FlashSaleSection } from './components/FlashSaleSection';
import { ProductCatalog } from './components/ProductCatalog';
import { CartDrawer } from './components/CartDrawer';
import { CheckoutModal } from './components/CheckoutModal';
import { ProductDetailModal } from './components/ProductDetailModal';
import { Product } from './types';
import { useCartStore } from './store/useCartStore';
import { CheckCircle2, Truck, ShieldCheck, RotateCcw, Headphones } from 'lucide-react';

export function App() {
  const [activeCategory, setActiveCategory] = useState<string>('Tất cả');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isCheckoutOpen, setIsCheckoutOpen] = useState<boolean>(false);
  const [lastCompletedOrder, setLastCompletedOrder] = useState<string | null>(null);

  const { addItem } = useCartStore();

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
    <div className="min-h-screen bg-slate-100 font-sans text-slate-900 flex flex-col selection:bg-rose-500 selection:text-white">
      {/* Top Main Navigation Header */}
      <Header
        activeCategory={activeCategory}
        onSelectCategory={(cat) => setActiveCategory(cat)}
        onSearch={(term) => setSearchTerm(term)}
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

        {/* Customer Promise Bar - Cam kết khách hàng (chuẩn E-commerce) */}
        <section className="bg-white border border-slate-200 rounded-md p-5 shadow-sm grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <div className="flex items-center gap-3">
            <div className="bg-slate-100 text-rose-600 p-2.5 rounded-md border border-slate-200">
              <Truck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-800">Giao hàng toàn quốc</h4>
              <p className="text-[11px] text-slate-500">Miễn phí ship từ 300.000đ</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="bg-slate-100 text-rose-600 p-2.5 rounded-md border border-slate-200">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-800">100% chính hãng</h4>
              <p className="text-[11px] text-slate-500">Cam kết hàng nhập khẩu chính ngạch</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="bg-slate-100 text-rose-600 p-2.5 rounded-md border border-slate-200">
              <RotateCcw className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-800">Đổi trả trong 30 ngày</h4>
              <p className="text-[11px] text-slate-500">Lỗi 1 đổi 1 trong 30 ngày đầu</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="bg-slate-100 text-rose-600 p-2.5 rounded-md border border-slate-200">
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
        <div className="max-w-7xl mx-auto px-4 grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h4 className="font-bold text-slate-200 text-sm mb-3">VỀ CHÚNG TÔI</h4>
            <p className="text-slate-400 leading-relaxed">
              Flash Sale - Nền tảng mua sắm trực tuyến với hàng nghìn sản phẩm chính hãng, giá ưu đãi hấp dẫn từ các thương hiệu hàng đầu.
            </p>
          </div>

          <div>
            <h4 className="font-bold text-slate-200 text-sm mb-3">HỖ TRỢ KHÁCH HÀNG</h4>
            <ul className="space-y-1.5 text-slate-400">
              <li className="hover:text-slate-200 cursor-pointer transition-colors">Hướng dẫn mua hàng</li>
              <li className="hover:text-slate-200 cursor-pointer transition-colors">Chính sách đổi trả</li>
              <li className="hover:text-slate-200 cursor-pointer transition-colors">Chính sách bảo hành</li>
              <li className="hover:text-slate-200 cursor-pointer transition-colors">Câu hỏi thường gặp (FAQ)</li>
            </ul>
          </div>

          <div>
            <h4 className="font-bold text-slate-200 text-sm mb-3">LIÊN HỆ</h4>
            <ul className="space-y-1.5 text-slate-400">
              <li>Hotline: <span className="text-rose-400 font-semibold">1900 6868</span></li>
              <li>Email: support@flashsale.vn</li>
              <li>Thời gian làm việc: 8:00 - 22:00 hàng ngày</li>
            </ul>
          </div>
        </div>

        <div className="max-w-7xl mx-auto px-4 mt-6 pt-4 border-t border-slate-800 text-center text-slate-500">
          <p>© 2024 Flash Sale. Tất cả các quyền được bảo lưu.</p>
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

      {/* Product Detail Modal */}
      <ProductDetailModal
        product={selectedProduct}
        onClose={() => setSelectedProduct(null)}
        onBuyNow={handleBuyNow}
      />

      {/* Success Order Toast Modal */}
      {lastCompletedOrder && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/70 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white max-w-md w-full rounded-md shadow-2xl p-6 text-center border border-slate-200">
            <CheckCircle2 className="w-16 h-16 text-emerald-500 mx-auto mb-3" />
            <h3 className="text-lg font-bold text-slate-900 mb-1">ĐẶT HÀNG THÀNH CÔNG!</h3>
            <p className="text-xs text-slate-600 mb-4">
              Mã đơn hàng của bạn là: <strong className="text-rose-600 bg-rose-50 px-2 py-0.5 rounded font-mono text-sm">{lastCompletedOrder}</strong>
            </p>
            <p className="text-xs text-slate-500 bg-slate-50 p-3 rounded-md border border-slate-200 mb-6">
              Đơn hàng đang được xử lý. Bạn sẽ nhận được thông báo khi đơn hàng sẵn sàng giao.
            </p>
            <button
              onClick={() => setLastCompletedOrder(null)}
              className="w-full bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs py-2.5 rounded-md transition-colors"
            >
              TIẾP TỤC MUA SẮM
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
