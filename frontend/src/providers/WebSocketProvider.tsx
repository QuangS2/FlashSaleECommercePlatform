import React, { useEffect, createContext, useContext } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import { useFlashSaleStore } from '../store/useFlashSaleStore';
import toast from 'react-hot-toast';

interface WebSocketContextType {
  isConnected: boolean;
}

const WebSocketContext = createContext<WebSocketContextType>({ isConnected: false });

export const useWebSocketContext = () => useContext(WebSocketContext);

const getWebSocketUrl = (): string => {
  if (import.meta.env.VITE_WS_URL) {
    return import.meta.env.VITE_WS_URL;
  }
  if (typeof window !== 'undefined' && window.location && window.location.origin) {
    return `${window.location.origin}/ws`;
  }
  return 'http://localhost:8085/ws';
};

export const WebSocketProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isConnected, subscribe } = useWebSocket({
    url: getWebSocketUrl(),
    onConnect: () => console.log('Connected to Real-time Notification Service'),
    onDisconnect: () => console.log('Disconnected from Real-time Notification Service'),
  });

  const updateProductStock = useFlashSaleStore((state) => state.updateProductStock);

  useEffect(() => {
    let stockSubscription: any = null;
    let personalSubscription: any = null;

    if (isConnected) {
      // Subscribe to public flash sale stock updates
      stockSubscription = subscribe('/topic/flashsale-stock', (message) => {
        try {
          const data = JSON.parse(message.body);
          if (data.productId && data.remainingStock !== undefined) {
            updateProductStock(data.productId, data.remainingStock);
          }
        } catch (err) {
          console.error('Failed to parse stock update message', err);
        }
      });

      // Subscribe to personal order notifications
      personalSubscription = subscribe('/user/queue/notifications', (message) => {
        try {
          const data = JSON.parse(message.body);
          const msgText = data.message || data.content || 'Bạn có thông báo mới';
          
          if (data.status === 'SUCCESS' || data.type === 'SUCCESS') {
            toast.success(msgText);
          } else if (data.status === 'ERROR' || data.type === 'ERROR') {
            toast.error(msgText);
          } else {
            toast(msgText, {
              icon: 'ℹ️',
              style: {
                background: '#f8fafc',
                color: '#334155',
                borderColor: '#e2e8f0',
              },
            });
          }
        } catch (err) {
          // Fallback if not JSON
          toast(message.body, { icon: '🔔' });
        }
      });
    }

    return () => {
      if (stockSubscription) {
        stockSubscription.unsubscribe();
      }
      if (personalSubscription) {
        personalSubscription.unsubscribe();
      }
    };
  }, [isConnected, subscribe, updateProductStock]);

  return (
    <WebSocketContext.Provider value={{ isConnected }}>
      {children}
    </WebSocketContext.Provider>
  );
};
