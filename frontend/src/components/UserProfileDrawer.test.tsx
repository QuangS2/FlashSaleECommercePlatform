import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { UserProfileDrawer } from './UserProfileDrawer';
import { useAuthStore } from '../store/useAuthStore';

describe('UserProfileDrawer Component', () => {
  beforeEach(() => {
    useAuthStore.setState({
      user: {
        id: 'usr-1',
        username: 'nguyenvana',
        fullName: 'Nguyễn Văn A',
        email: 'vana@example.com',
        roles: ['ROLE_CUSTOMER'],
        phone: '0901234567',
        address: 'Số 1 Đại Cồ Việt, Hà Nội',
      },
      isAuthenticated: true,
    });
  });

  it('không render nếu isOpen = false', () => {
    render(<UserProfileDrawer isOpen={false} onClose={vi.fn()} />);
    expect(screen.queryByText('Thông Tin Tài Khoản')).not.toBeInTheDocument();
  });

  it('render thông tin tài khoản người dùng', () => {
    render(<UserProfileDrawer isOpen={true} onClose={vi.fn()} />);
    expect(screen.getByText('Thông Tin Tài Khoản')).toBeInTheDocument();
    expect(screen.getByText('Nguyễn Văn A')).toBeInTheDocument();
    expect(screen.getByText('@nguyenvana')).toBeInTheDocument();
    expect(screen.getByText('vana@example.com')).toBeInTheDocument();
    expect(screen.getByText('0901234567')).toBeInTheDocument();
    expect(screen.getByText('Số 1 Đại Cồ Việt, Hà Nội')).toBeInTheDocument();
  });

  it('bấm nút Đăng xuất sẽ gọi logout và onClose', () => {
    const onCloseSpy = vi.fn();
    render(<UserProfileDrawer isOpen={true} onClose={onCloseSpy} />);

    const logoutBtn = screen.getByText('ĐĂNG XUẤT TÀI KHOẢN');
    fireEvent.click(logoutBtn);

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(onCloseSpy).toHaveBeenCalled();
  });
});
