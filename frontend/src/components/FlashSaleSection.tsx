import React from 'react';
import { Flame, Clock, ShieldAlert } from 'lucide-react';
import { useFlashSaleStore } from '../store/useFlashSaleStore';
import { ProductCard } from './ProductCard';
import { CountdownTimer } from './CountdownTimer';
import { Product } from '../types';

interface FlashSaleSectionProps {
  onQuickView?: (product: Product) => void;
  onBuyNow?: (product: Product) => void;
}

export const FlashSaleSection: React.FC<FlashSaleSectionProps> = ({ onQuickView, onBuyNow }) => {
  const { slots, activeSlotId, setActiveSlot, products } = useFlashSaleStore();

  const activeSlot = slots.find((s) => s.id === activeSlotId) || slots[1];
  const activeProducts = products.filter((p) => p.slotId === activeSlotId);

  return (
    <section className="bg-white border border-rose-200 rounded-md overflow-hidden shadow-sm mb-6">
      {/* Flash Sale Header Banner */}
      <div className="bg-gradient-to-r from-rose-900 via-rose-700 to-rose-900 text-white p-4 flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-rose-800">
        <div className="flex items-center gap-3">
          <div className="bg-amber-400 text-slate-900 p-2 rounded-md font-extrabold flex items-center gap-1.5 shadow">
            <Flame className="w-6 h-6 text-rose-600 fill-rose-600 animate-bounce" />
            <span className="text-xl tracking-tight">FLASH SALE</span>
          </div>
          <div>
            <h2 className="text-lg font-bold text-white leading-tight">SẢN PHẨM GIÁ SỐC ĐANG MỞ BÁN</h2>
            <p className="text-xs text-rose-200 flex items-center gap-1 mt-0.5">
              <ShieldAlert className="w-3.5 h-3.5 text-amber-300" />
              <span>Giới hạn 1 sản phẩm / tài khoản</span>
            </p>
          </div>
        </div>

        {/* Countdown Timer */}
        <div className="bg-slate-900/80 px-4 py-2 rounded-md border border-slate-700 flex items-center justify-center">
          <CountdownTimer targetHours={2} />
        </div>
      </div>

      {/* Time Slots Selector Tabs */}
      <div className="bg-slate-100 border-b border-slate-200 overflow-x-auto scrollbar-none flex">
        {slots.map((slot) => {
          const isActive = slot.id === activeSlotId;
          return (
            <button
              key={slot.id}
              onClick={() => setActiveSlot(slot.id)}
              className={`flex-1 min-w-[140px] py-3 px-4 text-center transition-all border-b-2 font-medium flex flex-col items-center justify-center gap-0.5 ${
                isActive
                  ? 'bg-white border-rose-600 text-rose-600 font-bold shadow-sm'
                  : 'border-transparent text-slate-600 hover:bg-slate-200/60'
              }`}
            >
              <div className="flex items-center gap-1 text-sm">
                <Clock className="w-3.5 h-3.5" />
                <span>{slot.label}</span>
              </div>
              <span className={`text-[10px] uppercase font-bold px-1.5 py-0.2 rounded ${
                slot.status === 'ACTIVE'
                  ? 'bg-rose-100 text-rose-700'
                  : slot.status === 'ENDED'
                  ? 'bg-slate-200 text-slate-500'
                  : 'bg-amber-100 text-amber-800'
              }`}>
                {slot.status === 'ACTIVE' ? 'ĐANG MỞ BÁN' : slot.status === 'ENDED' ? 'ĐÃ KẾT THÚC' : 'SẮP DIỄN RA'}
              </span>
            </button>
          );
        })}
      </div>

      {/* Product Items Grid */}
      <div className="p-4 bg-slate-50">
        {activeProducts.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {activeProducts.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onQuickView={onQuickView}
                onBuyNow={onBuyNow}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-12 text-slate-500 bg-white rounded-md border border-slate-200">
            <Clock className="w-10 h-10 mx-auto text-slate-300 mb-2" />
            <p className="font-semibold text-slate-700">Khung giờ này chưa mở bán sản phẩm Flash Sale.</p>
            <p className="text-xs text-slate-400">Vui lòng chọn khung giờ đang mở bán để mua sắm ngay!</p>
          </div>
        )}
      </div>
    </section>
  );
};
