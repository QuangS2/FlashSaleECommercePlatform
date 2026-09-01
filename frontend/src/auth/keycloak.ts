import Keycloak from 'keycloak-js';

export const getKeycloakUrl = () => {
  if (import.meta.env.VITE_KEYCLOAK_URL) {
    return import.meta.env.VITE_KEYCLOAK_URL;
  }
  if (typeof window !== 'undefined' && window.location && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
    return window.location.origin;
  }
  return 'http://localhost:8180';
};

const keycloak = new Keycloak({
  url: getKeycloakUrl(),
  realm: 'ecommerce-realm',
  clientId: 'ecommerce-frontend',
});

type AuthListener = () => void;
const authListeners: AuthListener[] = [];

export const onAuthChange = (listener: AuthListener) => {
  authListeners.push(listener);
  return () => {
    const idx = authListeners.indexOf(listener);
    if (idx >= 0) authListeners.splice(idx, 1);
  };
};

const notifyAuthListeners = () => {
  authListeners.forEach((listener) => {
    try {
      listener();
    } catch (e) {
      console.error('[Keycloak] Lỗi khi thông báo auth listener:', e);
    }
  });
};

keycloak.onAuthSuccess = () => {
  console.log('[Keycloak] onAuthSuccess: Đã xác thực thành công');
  notifyAuthListeners();
};

keycloak.onAuthRefreshSuccess = () => {
  console.log('[Keycloak] onAuthRefreshSuccess: Đã làm mới token');
  notifyAuthListeners();
};

keycloak.onAuthLogout = () => {
  console.log('[Keycloak] onAuthLogout: Đã đăng xuất');
  notifyAuthListeners();
};

keycloak.onTokenExpired = () => {
  keycloak.updateToken(30).catch(() => {
    console.warn('[Keycloak] Không thể tự động làm mới token');
    notifyAuthListeners();
  });
};

/**
 * Khởi tạo Keycloak OAuth2 / PKCE.
 * Tự động đồng bộ trạng thái xác thực sau khi Keycloak hoàn tất xử lý Redirect / SSO.
 */
export const initKeycloak = (onAppReadyCallback: () => void) => {
  // Render app ngay lập tức - KHÔNG chặn UI
  onAppReadyCallback();

  // Khởi tạo Keycloak ở background
  keycloak
    .init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      silentCheckSsoRedirectUri:
        typeof window !== 'undefined'
          ? window.location.origin + '/silent-check-sso.html'
          : undefined,
      enableLogging: false,
    })
    .then((authenticated) => {
      console.log('[Keycloak] Trạng thái sau init:', authenticated ? 'ĐÃ ĐĂNG NHẬP' : 'CHƯA ĐĂNG NHẬP');
      notifyAuthListeners();
    })
    .catch((error) => {
      console.warn('[Keycloak] Server chưa sẵn sàng, bỏ qua xác thực:', error?.message || error);
      notifyAuthListeners();
    });
};

export default keycloak;
