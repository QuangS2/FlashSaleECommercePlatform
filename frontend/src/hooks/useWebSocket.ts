import { useEffect, useRef, useState, useCallback } from 'react';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface WebSocketConfig {
  url: string;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: string) => void;
}

export const useWebSocket = ({ url, onConnect, onDisconnect, onError }: WebSocketConfig) => {
  const [isConnected, setIsConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    // Vite fix for sockjs global variable issue
    if (typeof (window as any).global === 'undefined') {
        (window as any).global = window;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(url),
      debug: (str) => {
        // Uncomment to debug STOMP traffic
        // console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        setIsConnected(true);
        if (onConnect) onConnect();
      },
      onDisconnect: () => {
        setIsConnected(false);
        if (onDisconnect) onDisconnect();
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
        if (onError) onError(frame.headers['message']);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [url]);

  const subscribe = useCallback((destination: string, callback: (message: IMessage) => void): StompSubscription | null => {
    if (clientRef.current && isConnected) {
      return clientRef.current.subscribe(destination, callback);
    }
    return null;
  }, [isConnected]);

  return { isConnected, subscribe };
};
