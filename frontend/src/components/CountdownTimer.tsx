import React, { useState, useEffect } from 'react';
import { Clock } from 'lucide-react';

interface CountdownTimerProps {
  targetHours?: number;
  onExpire?: () => void;
}

export const CountdownTimer: React.FC<CountdownTimerProps> = ({ targetHours = 2, onExpire }) => {
  const [timeLeft, setTimeLeft] = useState({
    hours: targetHours,
    minutes: 45,
    seconds: 30,
  });

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev.seconds > 0) {
          return { ...prev, seconds: prev.seconds - 1 };
        } else if (prev.minutes > 0) {
          return { ...prev, minutes: 59, seconds: 59 };
        } else if (prev.hours > 0) {
          return { hours: prev.hours - 1, minutes: 59, seconds: 59 };
        } else {
          clearInterval(timer);
          if (onExpire) onExpire();
          return { hours: 0, minutes: 0, seconds: 0 };
        }
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [onExpire]);

  const pad = (n: number) => n.toString().padStart(2, '0');

  return (
    <div className="flex items-center gap-2 font-mono select-none">
      <Clock className="w-4 h-4 text-rose-500 animate-pulse" />
      <span className="text-xs font-semibold text-slate-600 hidden sm:inline">KẾT THÚC TRONG:</span>
      <div className="flex items-center gap-1 text-xs font-bold">
        <span className="bg-slate-900 text-white px-2 py-1 rounded-md shadow-inner">
          {pad(timeLeft.hours)}
        </span>
        <span className="text-slate-700 font-bold">:</span>
        <span className="bg-slate-900 text-white px-2 py-1 rounded-md shadow-inner">
          {pad(timeLeft.minutes)}
        </span>
        <span className="text-slate-700 font-bold">:</span>
        <span className="bg-rose-600 text-white px-2 py-1 rounded-md shadow-inner">
          {pad(timeLeft.seconds)}
        </span>
      </div>
    </div>
  );
};
