import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { QueueModal } from './QueueModal';
import { useOrderQueueStore } from '../store/useOrderQueueStore';

// Mock useWebSocket hook
vi.mock('../hooks/useWebSocket', () => ({
  useWebSocket: () => ({
    isConnected: true,
    subscribe: vi.fn(),
  }),
}));

describe('QueueModal Component', () => {
  beforeEach(() => {
    useOrderQueueStore.getState().resetQueue();
  });

  it('không render khi isQueueOpen là false', () => {
    const { container } = render(<QueueModal />);
    expect(container.firstChild).toBeNull();
  });

  it('hiển thị trạng thái ĐANG XỬ LÝ GIAO DỊCH SAGA (WAITING)', () => {
    useOrderQueueStore.getState().setQueueOpen(true);
    useOrderQueueStore.getState().setQueueStatus('WAITING', 'ORD-TEST-999');

    render(<QueueModal />);

    expect(screen.getByText(/ĐANG XỬ LÝ GIAO DỊCH SAGA.../i)).toBeInTheDocument();
    expect(screen.getByText(/ORD-TEST-999/i)).toBeInTheDocument();
  });

  it('hiển thị trạng thái ĐẶT HÀNG THÀNH CÔNG (SUCCESS)', () => {
    useOrderQueueStore.getState().setQueueOpen(true);
    useOrderQueueStore.getState().setQueueStatus('SUCCESS', 'ORD-SUCCESS-123');

    const onSuccessSpy = vi.fn();
    render(<QueueModal onSuccessRedirect={onSuccessSpy} />);

    expect(screen.getByText(/ĐẶT HÀNG THÀNH CÔNG!/i)).toBeInTheDocument();

    const continueBtn = screen.getByText('TIẾP TỤC MUA SẮM');
    fireEvent.click(continueBtn);

    expect(onSuccessSpy).toHaveBeenCalledWith('ORD-SUCCESS-123');
  });

  it('hiển thị trạng thái ĐẶT HÀNG THẤT BẠI (ERROR)', () => {
    useOrderQueueStore.getState().setQueueOpen(true);
    useOrderQueueStore.getState().setQueueStatus('ERROR', 'ORD-FAIL-123');

    render(<QueueModal />);

    expect(screen.getByText(/ĐẶT HÀNG THẤT BẠI/i)).toBeInTheDocument();
  });
});
