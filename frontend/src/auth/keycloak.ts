import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:8180',
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
