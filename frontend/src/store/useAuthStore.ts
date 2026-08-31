import { create } from 'zustand';
import { UserProfile } from '../types';

export interface LoginCredentials {
  username: string;
  password?: string;
  rememberMe?: boolean;
}

export interface RegisterData {
  fullName: string;
  email: string;
  username: string;
  password?: string;
  phone?: string;
  address?: string;
}

interface AuthState {
  user: UserProfile | null;
  token: string | null;
  isAuthenticated: boolean;
  isAuthModalOpen: boolean;
  authModalTab: 'login' | 'register';
  isLoading: boolean;
  error: string | null;

  // Actions
  openAuthModal: (tab?: 'login' | 'register') => void;
  closeAuthModal: () => void;
  login: (credentials: LoginCredentials) => Promise<boolean>;
  register: (data: RegisterData) => Promise<boolean>;
  loginAsDemo: (role: 'CUSTOMER' | 'ADMIN') => void;
  logout: () => void;
  initializeAuth: () => void;
  clearError: () => void;
}

const DEMO_CUSTOMER: UserProfile = {
  id: 'user-demo-customer-01',
  username: 'khachhang_demo',
  fullName: 'Lê Văn Khách',
  email: 'customer.demo@ecommerce.vn',
  roles: ['ROLE_CUSTOMER'],
  phone: '0987654321',
  address: '123 Đường Nguyễn Văn Cừ, Quận 5, TP. Hồ Chí Minh',
};

const DEMO_ADMIN: UserProfile = {
  id: 'user-demo-admin-01',
  username: 'admin_system',
  fullName: 'Quản Trị Hệ Thống',
  email: 'admin@ecommerce.vn',
  roles: ['ROLE_ADMIN', 'ROLE_CUSTOMER'],
  phone: '0909123456',
  address: 'Tòa nhà Công Nghệ, Quận 1, TP. Hồ Chí Minh',
};

const STORAGE_KEYS = {
  USER: 'flsale_auth_user',
  TOKEN: 'flsale_auth_token',
};

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  isAuthenticated: false,
  isAuthModalOpen: false,
  authModalTab: 'login',
  isLoading: false,
  error: null,

  openAuthModal: (tab = 'login') =>
    set({
      isAuthModalOpen: true,
      authModalTab: tab,
      error: null,
    }),

  closeAuthModal: () =>
    set({
      isAuthModalOpen: false,
      error: null,
    }),

  clearError: () => set({ error: null }),

  login: async (credentials: LoginCredentials): Promise<boolean> => {
    set({ isLoading: true, error: null });

    // Giả lập network delay ngắn cho trải nghiệm mượt mà
    await new Promise((resolve) => setTimeout(resolve, 300));

    if (!credentials.username || credentials.username.trim() === '') {
      set({ isLoading: false, error: 'Vui lòng nhập tên đăng nhập hoặc email.' });
      return false;
    }

    if (credentials.password !== undefined && credentials.password.length < 3) {
      set({ isLoading: false, error: 'Mật khẩu phải có ít nhất 3 ký tự.' });
      return false;
    }

    // Xác thực tài khoản
    const usernameLower = credentials.username.toLowerCase().trim();
    let authenticatedUser: UserProfile;
    let token: string;

    if (usernameLower === 'admin' || usernameLower === 'admin_system') {
      authenticatedUser = { ...DEMO_ADMIN };
      token = 'mock_jwt_token_admin_' + Date.now();
    } else {
      authenticatedUser = {
        id: 'usr-' + Math.floor(100000 + Math.random() * 900000),
        username: credentials.username.trim(),
        fullName: credentials.username.includes('@')
          ? credentials.username.split('@')[0]
          : credentials.username,
        email: credentials.username.includes('@')
          ? credentials.username
          : `${credentials.username}@ecommerce.vn`,
        roles: ['ROLE_CUSTOMER'],
        phone: '0987654321',
        address: 'Địa chỉ nhận hàng mặc định',
      };
      token = 'mock_jwt_token_user_' + Date.now();
    }

    // Lưu vào LocalStorage
    try {
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(authenticatedUser));
      localStorage.setItem(STORAGE_KEYS.TOKEN, token);
    } catch {
      // Bỏ qua nếu môi trường test không hỗ trợ localStorage
    }

    set({
      user: authenticatedUser,
      token: token,
      isAuthenticated: true,
      isLoading: false,
      isAuthModalOpen: false,
      error: null,
    });

    return true;
  },

  register: async (data: RegisterData): Promise<boolean> => {
    set({ isLoading: true, error: null });

    await new Promise((resolve) => setTimeout(resolve, 400));

    if (!data.fullName || data.fullName.trim() === '') {
      set({ isLoading: false, error: 'Vui lòng nhập họ và tên.' });
      return false;
    }

    if (!data.email || !data.email.includes('@')) {
      set({ isLoading: false, error: 'Địa chỉ email không hợp lệ.' });
      return false;
    }

    if (!data.username || data.username.trim().length < 3) {
      set({ isLoading: false, error: 'Tên đăng nhập phải từ 3 ký tự trở lên.' });
      return false;
    }

    const newUser: UserProfile = {
      id: 'usr-' + Math.floor(100000 + Math.random() * 900000),
      username: data.username.trim(),
      fullName: data.fullName.trim(),
      email: data.email.trim(),
      roles: ['ROLE_CUSTOMER'],
      phone: data.phone?.trim() || '0987654321',
      address: data.address?.trim() || 'Chưa cập nhật địa chỉ',
    };

    const token = 'mock_jwt_token_registered_' + Date.now();

    try {
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(newUser));
      localStorage.setItem(STORAGE_KEYS.TOKEN, token);
    } catch {
      // Bỏ qua
    }

    set({
      user: newUser,
      token: token,
      isAuthenticated: true,
      isLoading: false,
      isAuthModalOpen: false,
      error: null,
    });

    return true;
  },

  loginAsDemo: (role: 'CUSTOMER' | 'ADMIN') => {
    const demoUser = role === 'ADMIN' ? { ...DEMO_ADMIN } : { ...DEMO_CUSTOMER };
    const token = `mock_jwt_token_${role.toLowerCase()}_${Date.now()}`;

    try {
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(demoUser));
      localStorage.setItem(STORAGE_KEYS.TOKEN, token);
    } catch {
      // Bỏ qua
    }

    set({
      user: demoUser,
      token: token,
      isAuthenticated: true,
      isAuthModalOpen: false,
      error: null,
    });
  },

  logout: () => {
    try {
      localStorage.removeItem(STORAGE_KEYS.USER);
      localStorage.removeItem(STORAGE_KEYS.TOKEN);
    } catch {
      // Bỏ qua
    }

    set({
      user: null,
      token: null,
      isAuthenticated: false,
      error: null,
    });
  },

  initializeAuth: () => {
    try {
      const savedUserStr = localStorage.getItem(STORAGE_KEYS.USER);
      const savedToken = localStorage.getItem(STORAGE_KEYS.TOKEN);

      if (savedUserStr && savedToken) {
        const savedUser: UserProfile = JSON.parse(savedUserStr);
        set({
          user: savedUser,
          token: savedToken,
          isAuthenticated: true,
        });
      }
    } catch {
      // Bỏ qua
    }
  },
}));
