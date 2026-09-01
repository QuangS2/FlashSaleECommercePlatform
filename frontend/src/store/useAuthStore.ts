import { create } from 'zustand';
import keycloak, { onAuthChange } from '../auth/keycloak';
import toast from 'react-hot-toast';

export interface AuthUser {
  sub: string;
  name: string;
  email: string;
  username: string;
  roles: string[];
}

interface AuthState {
  isAuthenticated: boolean;
  user: AuthUser | null;
  isLoading: boolean;

  // Actions
  setAuthenticated: (user: AuthUser | null) => void;
  loginWithKeycloak: () => void;
  logout: () => void;
  syncKeycloakState: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  isAuthenticated: Boolean(keycloak.authenticated),
  user: null,
  isLoading: false,

  setAuthenticated: (user) =>
    set({
      isAuthenticated: Boolean(user),
      user,
    }),

  loginWithKeycloak: () => {
    keycloak.login({
      redirectUri: window.location.origin,
    });
  },

  logout: () => {
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
      const fullName = parsed.name || [parsed.family_name, parsed.given_name].filter(Boolean).join(' ') || parsed.preferred_username || 'Khách hàng';
      const user: AuthUser = {
        sub: parsed.sub || 'user-' + Date.now(),
        name: fullName,
        username: parsed.preferred_username || 'customer',
        email: parsed.email || 'customer@ecommerce.vn',
        roles: (parsed.realm_access?.roles as string[]) || ['ROLE_CUSTOMER'],
      };
      
      const wasAuthenticated = get().isAuthenticated;
      set({
        isAuthenticated: true,
        user,
      });

      if (!wasAuthenticated) {
        toast.success(`Đăng nhập thành công! Xin chào ${user.name}`, {
          duration: 3000,
          position: 'top-right',
        });
      }
    } else {
      set({
        isAuthenticated: false,
        user: null,
      });
    }
  },
}));

// Tự động đồng bộ trạng thái bất cứ khi nào Keycloak thay đổi token/session
onAuthChange(() => {
  useAuthStore.getState().syncKeycloakState();
});

