import React, { useEffect, createContext, useContext } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import { useFlashSaleStore } from '../store/useFlashSaleStore';

interface WebSocketContextType {
  isConnected: boolean;
}

const WebSocketContext = createContext<WebSocketContextType>({ isConnected: false });

export const useWebSocketContext = () => useContext(WebSocketContext);

export const WebSocketProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isConnected, subscribe } = useWebSocket({
    url: 'http://localhost:8085/ws', // Hardcoded for Week 4 demo
    onConnect: () => console.log('Connected to Real-time Notification Service'),
    onDisconnect: () => console.log('Disconnected from Real-time Notification Service'),
  });

  const updateProductStock = useFlashSaleStore((state) => state.updateProductStock);

  useEffect(() => {
    let stockSubscription: any = null;

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
    }

    return () => {
      if (stockSubscription) {
        stockSubscription.unsubscribe();
      }
    };
  }, [isConnected, subscribe, updateProductStock]);

  return (
    <WebSocketContext.Provider value={{ isConnected }}>
      {children}
    </WebSocketContext.Provider>
  );
};
