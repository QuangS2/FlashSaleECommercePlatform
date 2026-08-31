import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach } from 'vitest';
import { AuthModal } from './AuthModal';
import { useAuthStore } from '../store/useAuthStore';

describe('AuthModal Component', () => {
  beforeEach(() => {
    useAuthStore.setState({
      isAuthModalOpen: true,
      authModalTab: 'login',
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
    });
  });

  it('không render nếu isAuthModalOpen = false', () => {
    useAuthStore.setState({ isAuthModalOpen: false });
    render(<AuthModal />);
    expect(screen.queryByText('ĐĂNG NHẬP NGAY')).not.toBeInTheDocument();
  });

  it('render form đăng nhập đúng các trường', () => {
    render(<AuthModal />);
    expect(screen.getByText(/Đăng Nhập Tài Khoản/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/khachhang_demo/)).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Nhập mật khẩu...')).toBeInTheDocument();
    expect(screen.getByText(/Trải nghiệm nhanh/i)).toBeInTheDocument();
  });

  it('chuyển đổi tab sang Đăng ký', () => {
    render(<AuthModal />);
    const registerTabBtn = screen.getByText('ĐĂNG KÝ MỚI');
    fireEvent.click(registerTabBtn);

    expect(screen.getByText(/Đăng Ký Thành Viên/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Ví dụ: Nguyễn Văn An')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('an.nguyen@email.com')).toBeInTheDocument();
    expect(screen.getByText('TẠO TÀI KHOẢN MỚI')).toBeInTheDocument();
  });

  it('bấm nút 1-Click Demo Khách Hàng đăng nhập thành công', () => {
    render(<AuthModal />);
    const demoCustomerBtn = screen.getByText('Khách Hàng');
    fireEvent.click(demoCustomerBtn);

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user?.fullName).toBe('Lê Văn Khách');
    expect(state.isAuthModalOpen).toBe(false);
  });

  it('báo lỗi validation khi để trống thông tin đăng nhập', async () => {
    render(<AuthModal />);
    const submitBtn = screen.getByText('ĐĂNG NHẬP NGAY');
    fireEvent.click(submitBtn);

    expect(await screen.findByText('Vui lòng nhập tên đăng nhập hoặc email.')).toBeInTheDocument();
  });

  it('submit form đăng nhập với dữ liệu hợp lệ', async () => {
    render(<AuthModal />);
    const usernameInput = screen.getByPlaceholderText(/khachhang_demo/);
    const passwordInput = screen.getByPlaceholderText('Nhập mật khẩu...');

    fireEvent.change(usernameInput, { target: { value: 'nguyenvana' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });

    const submitBtn = screen.getByText('ĐĂNG NHẬP NGAY');
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(useAuthStore.getState().isAuthenticated).toBe(true);
    });
  });
});
