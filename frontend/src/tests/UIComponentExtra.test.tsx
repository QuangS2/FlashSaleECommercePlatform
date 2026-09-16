import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { StockProgressBar } from '../components/StockProgressBar';
import { CountdownTimer } from '../components/CountdownTimer';

describe('Frontend UI Component Extras Test Suite (Bảng 24 - 8 Tests)', () => {
  it('1. StockProgressBar: Hiển thị đúng màu sắc thanh tiến độ khi bán dưới 50%', () => {
    const { container } = render(<StockProgressBar soldStock={20} totalStock={100} />);
    const fill = container.querySelector('.bg-\\[\\#FF424E\\]');
    expect(fill).toHaveStyle({ width: '20%' });
    expect(screen.getByText('Đã bán 20')).toBeInTheDocument();
  });

  it('2. StockProgressBar: Hiển thị icon ngọn lửa Flame khi tỷ lệ bán >= 80%', () => {
    const { container } = render(<StockProgressBar soldStock={85} totalStock={100} />);
    const flameIcon = container.querySelector('svg');
    expect(flameIcon).toBeInTheDocument();
    expect(screen.getByText('SẮP CHÁY HÀNG')).toBeInTheDocument();
  });

  it('3. StockProgressBar: Hiển thị trạng thái HẾT HÀNG với màu xám khi số lượng còn lại = 0', () => {
    const { container } = render(<StockProgressBar soldStock={50} totalStock={50} />);
    const fill = container.querySelector('.bg-slate-400');
    expect(fill).toBeInTheDocument();
    expect(screen.getByText('HẾT HÀNG')).toBeInTheDocument();
  });

  it('4. CountdownTimer: Định dạng hiển thị giờ với targetHours = 1', () => {
    render(<CountdownTimer targetHours={1} />);
    expect(screen.getByText('01')).toBeInTheDocument();
    expect(screen.getByText('45')).toBeInTheDocument();
    expect(screen.getByText('30')).toBeInTheDocument();
  });

  it('5. CountdownTimer: Render các nhãn và dấu phân cách thời gian', () => {
    render(<CountdownTimer targetHours={3} />);
    expect(screen.getByText('03')).toBeInTheDocument();
    expect(screen.getByText('KẾT THÚC TRONG:')).toBeInTheDocument();
  });

  it('6. StockProgressBar: Xử lý tỷ lệ phần trăm an toàn không vượt quá 100%', () => {
    const { container } = render(<StockProgressBar soldStock={150} totalStock={100} />);
    const fill = container.querySelector('.transition-all');
    expect(fill).toHaveStyle({ width: '100%' });
  });

  it('7. StockProgressBar: Giữ vững layout khi soldStock = 0', () => {
    render(<StockProgressBar soldStock={0} totalStock={100} />);
    expect(screen.getByText('Đã bán 0')).toBeInTheDocument();
  });

  it('8. CountdownTimer: Render targetHours mặc định là 2 khi không truyền prop', () => {
    render(<CountdownTimer />);
    expect(screen.getByText('02')).toBeInTheDocument();
  });
});
