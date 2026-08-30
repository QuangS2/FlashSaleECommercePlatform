import React from 'react';
import { Flame } from 'lucide-react';

interface StockProgressBarProps {
  soldStock: number;
  totalStock: number;
}

export const StockProgressBar: React.FC<StockProgressBarProps> = ({ soldStock, totalStock }) => {
  const percent = Math.min(100, Math.max(0, Math.round((soldStock / totalStock) * 100)));
  const remaining = totalStock - soldStock;

  let barColor = 'bg-[#FF424E]';
  let badgeText = `Đã bán ${soldStock}`;

  if (remaining === 0) {
    barColor = 'bg-slate-400';
    badgeText = 'HẾT HÀNG';
  } else if (percent >= 80) {
    barColor = 'bg-[#FF0000]';
    badgeText = `SẮP CHÁY HÀNG`;
  } else if (percent >= 50) {
    barColor = 'bg-[#FF7200]';
    badgeText = `Đã bán ${soldStock}`;
  }

  return (
    <div className="w-full">
      <div className="relative w-full h-[18px] bg-[#FFCAD4] rounded-full overflow-hidden flex items-center justify-center">
        {/* Progress Fill */}
        <div
          className={`absolute left-0 top-0 bottom-0 ${barColor} transition-all duration-500 ease-out`}
          style={{ width: `${percent}%` }}
        />

        {/* Text Badge */}
        <div className="relative z-10 text-[10px] font-bold text-white flex items-center justify-center gap-1 uppercase tracking-tight w-full h-full">
          {percent >= 80 && remaining > 0 && <Flame className="w-3 h-3 text-yellow-300 animate-pulse" />}
          <span className="drop-shadow-sm">{badgeText}</span>
        </div>
      </div>
    </div>
  );
};
