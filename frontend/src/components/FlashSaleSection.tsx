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
    <section className="bg-white border border-slate-200 rounded-md overflow-hidden mb-8 shadow-sm">
      {/* Flash Sale Header Banner */}
      <div className="bg-[#1A94FF] text-white p-4 flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-blue-600">
        <div className="flex items-center gap-3">
          <div className="bg-yellow-400 text-[#1A94FF] p-2 rounded font-extrabold flex items-center gap-1.5">
            <Flame className="w-6 h-6 text-[#1A94FF] fill-current animate-pulse" />
            <span className="text-xl tracking-tight">FLASH SALE</span>
          </div>
          <div>
            <h2 className="text-lg font-bold text-white leading-tight">SẢN PHẨM GIÁ SỐC ĐANG MỞ BÁN</h2>
            <p className="text-xs text-blue-100 flex items-center gap-1 mt-0.5">
              <ShieldAlert className="w-3.5 h-3.5 text-yellow-300" />
              <span>Giới hạn 1 sản phẩm / tài khoản</span>
            </p>
          </div>
        </div>

        {/* Countdown Timer */}
        <div className="bg-[#0074DA] px-4 py-2 rounded-md border border-blue-400 flex items-center justify-center">
          <CountdownTimer targetHours={2} />
        </div>
      </div>

      {/* Time Slots Selector Tabs */}
      <div className="bg-slate-50 border-b border-slate-200 overflow-x-auto scrollbar-none flex">
        {slots.map((slot) => {
          const isActive = slot.id === activeSlotId;
          return (
            <button
              key={slot.id}
              onClick={() => setActiveSlot(slot.id)}
              className={`flex-1 min-w-[140px] py-3 px-4 text-center transition-all border-b-[3px] font-medium flex flex-col items-center justify-center gap-1 ${
                isActive
                  ? 'bg-white border-[#1A94FF] text-[#1A94FF]'
                  : 'border-transparent text-slate-600 hover:bg-slate-100'
              }`}
            >
              <div className="flex items-center gap-1 text-sm font-bold">
                <Clock className="w-4 h-4" />
                <span>{slot.label}</span>
              </div>
              <span className={`text-[10px] uppercase font-bold px-2 py-0.5 rounded-full ${
                slot.status === 'ACTIVE'
                  ? 'bg-[#1A94FF] text-white'
                  : slot.status === 'ENDED'
                  ? 'bg-slate-200 text-slate-500'
                  : 'bg-yellow-100 text-yellow-800'
              }`}>
                {slot.status === 'ACTIVE' ? 'ĐANG MỞ BÁN' : slot.status === 'ENDED' ? 'ĐÃ KẾT THÚC' : 'SẮP DIỄN RA'}
              </span>
            </button>
          );
        })}
      </div>

      {/* Product Items Grid */}
      <div className="p-4 bg-white">
        {activeProducts.length > 0 ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
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
          <div className="text-center py-12 text-slate-500 bg-slate-50 rounded border border-dashed border-slate-300">
            <Clock className="w-10 h-10 mx-auto text-slate-400 mb-2" />
            <p className="font-semibold text-slate-700">Khung giờ này chưa mở bán sản phẩm Flash Sale.</p>
            <p className="text-xs text-slate-500">Vui lòng chọn khung giờ đang mở bán để mua sắm ngay!</p>
          </div>
        )}
      </div>
    </section>
  );
};
