import Keycloak from 'keycloak-js';

const getKeycloakUrl = () => {
  const metaEnv = (import.meta as any).env;
  if (metaEnv && metaEnv.VITE_KEYCLOAK_URL) {
    return metaEnv.VITE_KEYCLOAK_URL;
  }
  if (typeof window !== 'undefined' && window.location.hostname !== 'localhost') {
    return `${window.location.protocol}//${window.location.hostname}:8180`;
  }
  return 'http://localhost:8180';
};

const keycloak = new Keycloak({
  url: getKeycloakUrl(),
  realm: 'ecommerce-realm',
  clientId: 'ecommerce-frontend',
});

export const initKeycloak = (onAuthenticatedCallback: () => void) => {
  keycloak
    .init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
    })
    .then(() => {
      onAuthenticatedCallback();
    })
    .catch((error) => {
      console.error('Keycloak init failed:', error);
      onAuthenticatedCallback();
    });
};

export default keycloak;
