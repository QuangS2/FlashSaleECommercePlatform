import { create } from 'zustand';
import keycloak from '../auth/keycloak';

export interface AuthUser {
  sub: string;
  name: string;
  email: string;
  username: string;
  roles: string[];
  isDemoUser?: boolean;
}

interface AuthState {
  isAuthenticated: boolean;
  user: AuthUser | null;
  isLoginModalOpen: boolean;
  isLoading: boolean;

  // Actions
  setAuthenticated: (user: AuthUser | null) => void;
  openLoginModal: () => void;
  closeLoginModal: () => void;
  loginWithKeycloak: () => void;
  demoLogin: (type: 'customer' | 'admin') => void;
  logout: () => void;
  syncKeycloakState: () => void;
}

const DEMO_USERS: Record<'customer' | 'admin', AuthUser> = {
  customer: {
    sub: '7a3a9926-218a-4d2c-88eb-123456789001',
    name: 'Lê Văn Khách',
    username: 'customer',
    email: 'customer@ecommerce.vn',
    roles: ['ROLE_CUSTOMER'],
    isDemoUser: true,
  },
  admin: {
    sub: '7a3a9926-218a-4d2c-88eb-123456789002',
    name: 'Quản Trị Viên',
    username: 'admin',
    email: 'admin@ecommerce.vn',
    roles: ['ROLE_ADMIN', 'ROLE_CUSTOMER'],
    isDemoUser: true,
  },
};

export const useAuthStore = create<AuthState>((set, get) => ({
  isAuthenticated: Boolean(keycloak.authenticated),
  user: null,
  isLoginModalOpen: false,
  isLoading: false,

  setAuthenticated: (user) =>
    set({
      isAuthenticated: Boolean(user),
      user,
    }),

  openLoginModal: () => set({ isLoginModalOpen: true }),
  closeLoginModal: () => set({ isLoginModalOpen: false }),

  loginWithKeycloak: () => {
    set({ isLoginModalOpen: false });
    keycloak.login({
      redirectUri: window.location.origin,
    });
  },

  demoLogin: (type) => {
    const selectedUser = DEMO_USERS[type];
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      localStorage.setItem('flsale_demo_user', JSON.stringify(selectedUser));
    }
    set({
      isAuthenticated: true,
      user: selectedUser,
      isLoginModalOpen: false,
    });
  },

  logout: () => {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      localStorage.removeItem('flsale_demo_user');
    }
    set({
      isAuthenticated: false,
      user: null,
    });
    if (keycloak.authenticated && typeof keycloak.logout === 'function') {
      keycloak.logout({
        redirectUri: typeof window !== 'undefined' ? window.location.origin : '/',
      });
    }
  },

  syncKeycloakState: () => {
    if (keycloak.authenticated && keycloak.tokenParsed) {
      const parsed = keycloak.tokenParsed;
      const user: AuthUser = {
        sub: parsed.sub || 'user-' + Date.now(),
        name: parsed.name || parsed.preferred_username || 'Khách hàng',
        username: parsed.preferred_username || 'customer',
        email: parsed.email || 'customer@ecommerce.vn',
        roles: (parsed.realm_access?.roles as string[]) || ['ROLE_CUSTOMER'],
        isDemoUser: false,
      };
      set({
        isAuthenticated: true,
        user,
      });
      return;
    }

    // Kiểm tra demo user đã lưu trong localStorage
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      const savedDemo = localStorage.getItem('flsale_demo_user');
      if (savedDemo) {
        try {
          const user = JSON.parse(savedDemo);
          set({
            isAuthenticated: true,
            user,
          });
        } catch {
          localStorage.removeItem('flsale_demo_user');
        }
      }
    }
  },
}));
