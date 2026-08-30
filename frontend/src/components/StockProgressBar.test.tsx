import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { StockProgressBar } from './StockProgressBar';

describe('StockProgressBar', () => {
  it('hiển thị thanh tiến độ với tỷ lệ chính xác (50%)', () => {
    const { container } = render(<StockProgressBar soldStock={50} totalStock={100} />);

    // Kiểm tra xem đoạn text có đúng không
    expect(screen.getByText('Đã bán 50')).toBeInTheDocument();

    // Kiểm tra class width 50%
    const progressFill = container.querySelector('.bg-\\[\\#FF7200\\]');
    expect(progressFill).toHaveStyle({ width: '50%' });
  });

  it('hiển thị trạng thái sắp hết khi bán > 80%', () => {
    const { container } = render(<StockProgressBar soldStock={90} totalStock={100} />);

    expect(screen.getByText('SẮP CHÁY HÀNG')).toBeInTheDocument();
    const progressFill = container.querySelector('.bg-\\[\\#FF0000\\]');
    expect(progressFill).toHaveStyle({ width: '90%' });
  });

  it('hiển thị 100% khi bán hết', () => {
    render(<StockProgressBar soldStock={100} totalStock={100} />);
    expect(screen.getByText('HẾT HÀNG')).toBeInTheDocument();
  });
});
