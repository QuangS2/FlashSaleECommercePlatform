import React from 'react';
import { Flame } from 'lucide-react';

interface StockProgressBarProps {
  soldStock: number;
  totalStock: number;
}

export const StockProgressBar: React.FC<StockProgressBarProps> = ({ soldStock, totalStock }) => {
  const percent = Math.min(100, Math.max(0, Math.round((soldStock / totalStock) * 100)));
  const remaining = totalStock - soldStock;

  let barColor = 'bg-rose-500';
  let badgeText = `Đã bán ${soldStock}`;

  if (remaining === 0) {
    barColor = 'bg-slate-500';
    badgeText = 'HẾT HÀNG';
  } else if (percent >= 80) {
    barColor = 'bg-rose-600';
    badgeText = `SẮP CHÁY HÀNG (Còn ${remaining})`;
  } else if (percent >= 50) {
    barColor = 'bg-amber-500';
    badgeText = `Đã bán ${soldStock}`;
  }

  return (
    <div className="w-full">
      <div className="relative w-full h-5 bg-rose-100/80 rounded-md overflow-hidden border border-rose-200 shadow-inner flex items-center justify-center">
        {/* Progress Fill */}
        <div
          className={`absolute left-0 top-0 bottom-0 ${barColor} transition-all duration-500 ease-out`}
          style={{ width: `${percent}%` }}
        />

        {/* Text Badge */}
        <div className="relative z-10 text-[11px] font-bold text-slate-800 drop-shadow-sm flex items-center justify-center gap-1 uppercase tracking-tight">
          {percent >= 80 && remaining > 0 && <Flame className="w-3 h-3 text-amber-300 animate-bounce" />}
          <span className={percent > 50 ? 'text-white' : 'text-rose-900'}>{badgeText}</span>
        </div>
      </div>
    </div>
  );
};
