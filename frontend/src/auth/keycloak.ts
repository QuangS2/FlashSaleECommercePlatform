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

/**
 * Khởi tạo Keycloak KHÔNG chặn render giao diện.
 * Render app ngay lập tức, sau đó init Keycloak ở background.
 * Nếu Keycloak server không chạy, giao diện vẫn load bình thường.
 */
export const initKeycloak = (onAuthenticatedCallback: () => void) => {
  // Render app ngay lập tức - KHÔNG chờ Keycloak
  onAuthenticatedCallback();

  // Init Keycloak ở background (không block UI)
  keycloak
    .init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      silentCheckSsoRedirectUri:
        window.location.origin + '/silent-check-sso.html',
      enableLogging: false,
    })
    .then((authenticated) => {
      if (authenticated) {
        console.log('[Keycloak] Đã xác thực thành công:', keycloak.tokenParsed?.preferred_username);
      }
    })
    .catch((error) => {
      console.warn('[Keycloak] Server chưa sẵn sàng, bỏ qua xác thực:', error?.message || error);
    });
};

export default keycloak;
