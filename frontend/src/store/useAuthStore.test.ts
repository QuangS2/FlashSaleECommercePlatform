import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAuthStore } from './useAuthStore';

const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString();
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(globalThis, 'localStorage', {
  value: localStorageMock,
  writable: true,
});

describe('useAuthStore', () => {
  beforeEach(() => {
    localStorageMock.clear();
    useAuthStore.setState({
      user: null,
      token: null,
      isAuthenticated: false,
      isAuthModalOpen: false,
      authModalTab: 'login',
      isLoading: false,
      error: null,
    });
  });

  it('mở và đóng Auth Modal đúng tab', () => {
    useAuthStore.getState().openAuthModal('register');
    expect(useAuthStore.getState().isAuthModalOpen).toBe(true);
    expect(useAuthStore.getState().authModalTab).toBe('register');

    useAuthStore.getState().closeAuthModal();
    expect(useAuthStore.getState().isAuthModalOpen).toBe(false);
  });

  it('đăng nhập 1-Click Demo với vai trò CUSTOMER thành công', () => {
    useAuthStore.getState().loginAsDemo('CUSTOMER');

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user).not.toBeNull();
    expect(state.user?.fullName).toBe('Lê Văn Khách');
    expect(state.user?.roles).toContain('ROLE_CUSTOMER');
    expect(state.token).toContain('mock_jwt_token_customer');
  });

  it('đăng nhập 1-Click Demo với vai trò ADMIN thành công', () => {
    useAuthStore.getState().loginAsDemo('ADMIN');

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user?.username).toBe('admin_system');
    expect(state.user?.roles).toContain('ROLE_ADMIN');
  });

  it('đăng nhập với username và password hợp lệ', async () => {
    const success = await useAuthStore.getState().login({
      username: 'test_user',
      password: 'password123',
    });

    expect(success).toBe(true);
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user?.username).toBe('test_user');
  });

  it('báo lỗi khi đăng nhập không có username', async () => {
    const success = await useAuthStore.getState().login({
      username: '',
      password: '123',
    });

    expect(success).toBe(false);
    expect(useAuthStore.getState().error).toBe('Vui lòng nhập tên đăng nhập hoặc email.');
  });

  it('đăng ký tài khoản mới thành công', async () => {
    const success = await useAuthStore.getState().register({
      fullName: 'Trần Văn Nam',
      email: 'nam.tran@example.com',
      username: 'namtran99',
      password: 'password123',
      phone: '0912345678',
    });

    expect(success).toBe(true);
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user?.fullName).toBe('Trần Văn Nam');
    expect(state.user?.username).toBe('namtran99');
  });

  it('đăng xuất xóa sạch dữ liệu người dùng', () => {
    useAuthStore.getState().loginAsDemo('CUSTOMER');
    expect(useAuthStore.getState().isAuthenticated).toBe(true);

    useAuthStore.getState().logout();
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
  });
});
