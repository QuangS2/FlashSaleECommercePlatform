import React from 'react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { LoginModal } from './LoginModal';
import { useAuthStore } from '../store/useAuthStore';

// Mock Keycloak
vi.mock('../auth/keycloak', () => ({
  default: {
    authenticated: false,
    login: vi.fn(),
    logout: vi.fn(),
  },
  getKeycloakUrl: vi.fn(() => 'https://flashale.quangs2.cloud'),
}));

describe('LoginModal Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      isAuthenticated: false,
      user: null,
      isLoginModalOpen: true,
    });
  });

  it('renders correctly when open', () => {
    render(<LoginModal />);
    expect(screen.getByText('Đăng Nhập Tài Khoản')).toBeInTheDocument();
    expect(screen.getByText('Đăng Nhập Bằng Tài Khoản & Mật Khẩu')).toBeInTheDocument();
    expect(screen.getByText('Tài khoản & Mật khẩu mẫu đã cấp sẵn:')).toBeInTheDocument();
    expect(screen.getByText('customer')).toBeInTheDocument();
    expect(screen.getByText('admin')).toBeInTheDocument();
  });

  it('does not render when isLoginModalOpen is false', () => {
    useAuthStore.setState({ isLoginModalOpen: false });
    const { container } = render(<LoginModal />);
    expect(container.firstChild).toBeNull();
  });

  it('calls loginWithKeycloak when clicking primary Keycloak login button', () => {
    render(<LoginModal />);
    const loginBtn = screen.getByText('Đăng Nhập Bằng Tài Khoản & Mật Khẩu');
    fireEvent.click(loginBtn);
    // Modal should close on login click
    expect(useAuthStore.getState().isLoginModalOpen).toBe(false);
  });

  it('allows 1-click quick demo login for customer', () => {
    render(<LoginModal />);
    const customerBtn = screen.getByText('Tài khoản Khách hàng (Customer)');
    fireEvent.click(customerBtn);

    expect(useAuthStore.getState().isAuthenticated).toBe(true);
    expect(useAuthStore.getState().user?.username).toBe('customer');
    expect(useAuthStore.getState().isLoginModalOpen).toBe(false);
  });

  it('allows 1-click quick demo login for admin', () => {
    render(<LoginModal />);
    const adminBtn = screen.getByText('Tài khoản Quản trị (Admin)');
    fireEvent.click(adminBtn);

    expect(useAuthStore.getState().isAuthenticated).toBe(true);
    expect(useAuthStore.getState().user?.username).toBe('admin');
    expect(useAuthStore.getState().user?.roles).toContain('ROLE_ADMIN');
    expect(useAuthStore.getState().isLoginModalOpen).toBe(false);
  });
});
