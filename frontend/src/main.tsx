import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import { initKeycloak } from './auth/keycloak';
import { WebSocketProvider } from './providers/WebSocketProvider';

const renderApp = () => {
  ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
      <WebSocketProvider>
        <App />
      </WebSocketProvider>
    </React.StrictMode>
  );
};

initKeycloak(renderApp);
