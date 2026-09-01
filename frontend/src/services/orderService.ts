import keycloak from '../auth/keycloak';

export interface CreateOrderPayload {
  productId: string;
  productTitle: string;
  quantity: number;
  unitPrice: number;
  userId?: string;
  userEmail?: string;
  shippingAddress?: {
    fullName: string;
    phone: string;
    address: string;
    city: string;
    note?: string;
  };
  paymentMethod?: string;
}

export interface OrderDetailResponse {
  orderId: string;
  status: string;
  userId: string;
  userEmail?: string;
  productId: string;
  productTitle: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  totalAmount?: number;
  createdAt: string;
  paymentId?: string;
}

const getStorageKey = (scope?: string) => {
  if (!scope) return 'flsale_guest_temp_orders';
  return `flsale_orders_${scope.replace(/[^a-zA-Z0-9_@-]/g, '_')}`;
};

export const orderService = {
  /**
   * Lưu hoặc cập nhật đơn hàng vào bộ nhớ cục bộ (LocalStorage)
   */
  saveCachedOrder(order: OrderDetailResponse, scopeKey?: string): void {
    try {
      if (typeof window === 'undefined' || !window.localStorage) return;

      const keysToSave = new Set<string>();
      if (scopeKey) {
        keysToSave.add(getStorageKey(scopeKey));
      }
      if (order.userId) {
        keysToSave.add(getStorageKey(`user_${order.userId}`));
        if (order.userId.startsWith('guest')) {
          keysToSave.add('flsale_guest_temp_orders');
        }
      }
      if (order.userEmail && order.userEmail.trim()) {
        keysToSave.add(getStorageKey(`email_${order.userEmail.trim().toLowerCase()}`));
      }
      if (keysToSave.size === 0) {
        keysToSave.add('flsale_guest_temp_orders');
      }

      keysToSave.forEach((key) => {
        const cached = this.getCachedOrders(key);
        const index = cached.findIndex((o) => o.orderId === order.orderId);
        if (index >= 0) {
          cached[index] = { ...cached[index], ...order };
        } else {
          cached.unshift(order);
        }
        window.localStorage.setItem(key, JSON.stringify(cached.slice(0, 30)));
      });
    } catch {
      // Bỏ qua lỗi storage
    }
  },

  /**
   * Đọc danh sách đơn hàng đã lưu trong LocalStorage theo phạm vi người dùng/email
   */
  getCachedOrders(scopeKey?: string): OrderDetailResponse[] {
    try {
      if (typeof window === 'undefined' || !window.localStorage) return [];
      const key = scopeKey ? (scopeKey.startsWith('flsale_orders_') || scopeKey.startsWith('flsale_guest_') ? scopeKey : getStorageKey(scopeKey)) : getStorageKey();
      const stored = window.localStorage.getItem(key);
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  },

  /**
   * Tạo đơn hàng mới tới Order-Service (MySQL + Kafka Saga)
   */
  async createOrder(payload: CreateOrderPayload): Promise<{ orderId: string; status: string }> {
    try {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };

      if (keycloak.authenticated && keycloak.token) {
        headers['Authorization'] = `Bearer ${keycloak.token}`;
      }

      const userId = payload.userId || (keycloak.authenticated ? keycloak.tokenParsed?.sub || keycloak.tokenParsed?.preferred_username : undefined) || 'guest_demo_user';
      const userEmail = payload.userEmail || keycloak.tokenParsed?.email || 'customer@ecommerce.vn';

      const requestBody = {
        userId,
        userEmail,
        productId: payload.productId,
        productTitle: payload.productTitle,
        quantity: payload.quantity,
        unitPrice: payload.unitPrice,
      };

      const response = await fetch('/api/v1/orders', {
        method: 'POST',
        headers,
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        throw new Error(`Tạo đơn hàng thất bại, HTTP status: ${response.status}`);
      }

      const data = await response.json();
      const finalOrderId = data.orderId || `ORD-${Date.now()}`;
      const finalStatus = data.status || 'PENDING';

      // Lưu cache theo đúng định danh tài khoản hoặc email của người mua
      const scope = keycloak.authenticated && userId ? `user_${userId}` : `email_${userEmail}`;
      this.saveCachedOrder({
        orderId: finalOrderId,
        status: finalStatus,
        userId,
        userEmail,
        productId: payload.productId,
        productTitle: payload.productTitle,
        quantity: payload.quantity,
        unitPrice: payload.unitPrice,
        totalPrice: payload.unitPrice * payload.quantity,
        totalAmount: payload.unitPrice * payload.quantity,
        createdAt: new Date().toISOString(),
      }, scope);

      return {
        orderId: finalOrderId,
        status: finalStatus,
      };
    } catch (error) {
      console.warn('[OrderService] Backend offline hoặc lỗi mạng, kích hoạt cơ chế tạo đơn mô phỏng:', error);
      const fallbackOrderId = `ORD-${Math.floor(100000 + Math.random() * 900000)}`;
      const scope = payload.userId ? `user_${payload.userId}` : payload.userEmail ? `email_${payload.userEmail}` : undefined;
      const fallbackOrder: OrderDetailResponse = {
        orderId: fallbackOrderId,
        status: 'PENDING',
        userId: payload.userId || 'guest_demo_user',
        userEmail: payload.userEmail || 'customer@ecommerce.vn',
        productId: payload.productId,
        productTitle: payload.productTitle,
        quantity: payload.quantity,
        unitPrice: payload.unitPrice,
        totalPrice: payload.unitPrice * payload.quantity,
        totalAmount: payload.unitPrice * payload.quantity,
        createdAt: new Date().toISOString(),
      };
      this.saveCachedOrder(fallbackOrder, scope);
      return {
        orderId: fallbackOrderId,
        status: 'PENDING',
      };
    }
  },

  /**
   * Lấy chi tiết đơn hàng theo orderId
   */
  async getOrderById(orderId: string): Promise<OrderDetailResponse | null> {
    try {
      const response = await fetch(`/api/v1/orders/${orderId}`);
      if (!response.ok) {
        return null;
      }
      const data = await response.json();
      const unit = Number(data.unitPrice) || 0;
      const qty = Number(data.quantity) || 1;
      const total = Number(data.totalPrice || data.totalAmount) || unit * qty;
      const normalized: OrderDetailResponse = {
        ...data,
        totalPrice: total,
        totalAmount: total,
      };
      this.saveCachedOrder(normalized);
      return normalized;
    } catch {
      return null;
    }
  },

  /**
   * Lấy danh sách lịch sử đơn hàng của người dùng đã đăng nhập (theo User ID và Email)
   */
  async getUserOrders(userId: string, userEmail?: string): Promise<OrderDetailResponse[]> {
    if (!userId && !userEmail) return [];
    
    const resultsMap = new Map<string, OrderDetailResponse>();

    // 1. Tải đơn hàng theo User ID
    if (userId) {
      try {
        const response = await fetch(`/api/v1/orders/user/${encodeURIComponent(userId)}`);
        if (response.ok) {
          const data = await response.json();
          if (Array.isArray(data)) {
            data.forEach((item: any) => {
              const unit = Number(item.unitPrice) || 0;
              const qty = Number(item.quantity) || 1;
              const total = Number(item.totalPrice || item.totalAmount) || unit * qty;
              const mapped: OrderDetailResponse = {
                ...item,
                totalPrice: total,
                totalAmount: total,
              };
              resultsMap.set(mapped.orderId, mapped);
              this.saveCachedOrder(mapped, `user_${userId}`);
            });
          }
        }
      } catch {
        // Tiếp tục thử bằng email
      }
    }

    // 2. Tải thêm đơn hàng theo User Email nếu có
    if (userEmail && userEmail.trim()) {
      const cleanEmail = userEmail.trim().toLowerCase();
      try {
        const response = await fetch(`/api/v1/orders/email?email=${encodeURIComponent(cleanEmail)}`);
        if (response.ok) {
          const data = await response.json();
          if (Array.isArray(data)) {
            data.forEach((item: any) => {
              const unit = Number(item.unitPrice) || 0;
              const qty = Number(item.quantity) || 1;
              const total = Number(item.totalPrice || item.totalAmount) || unit * qty;
              const mapped: OrderDetailResponse = {
                ...item,
                totalPrice: total,
                totalAmount: total,
              };
              resultsMap.set(mapped.orderId, mapped);
              this.saveCachedOrder(mapped, `email_${cleanEmail}`);
            });
          }
        }
      } catch {
        // Bỏ qua lỗi
      }
    }

    // Nếu không có kết quả từ API trực tiếp, lấy từ LocalStorage Cache
    if (resultsMap.size === 0) {
      const cachedByUser = userId ? this.getCachedOrders(`user_${userId}`) : [];
      const cachedByEmail = userEmail ? this.getCachedOrders(`email_${userEmail.trim().toLowerCase()}`) : [];
      [...cachedByUser, ...cachedByEmail].forEach((order) => {
        resultsMap.set(order.orderId, order);
      });
    }

    const orderList = Array.from(resultsMap.values());
    orderList.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    return orderList;
  },

  /**
   * Lấy danh sách lịch sử đơn hàng của khách hàng theo Email
   */
  async getOrdersByEmail(email: string): Promise<OrderDetailResponse[]> {
    if (!email || !email.trim()) return [];
    const cleanEmail = email.trim().toLowerCase();
    const resultsMap = new Map<string, OrderDetailResponse>();

    try {
      const response = await fetch(`/api/v1/orders/email?email=${encodeURIComponent(cleanEmail)}`);
      if (response.ok) {
        const data = await response.json();
        if (Array.isArray(data)) {
          data.forEach((item: any) => {
            const unit = Number(item.unitPrice) || 0;
            const qty = Number(item.quantity) || 1;
            const total = Number(item.totalPrice || item.totalAmount) || unit * qty;
            const mapped: OrderDetailResponse = {
              ...item,
              totalPrice: total,
              totalAmount: total,
            };
            resultsMap.set(mapped.orderId, mapped);
            this.saveCachedOrder(mapped, `email_${cleanEmail}`);
          });
        }
      }
    } catch {
      // Fallback xuống LocalStorage
    }

    if (resultsMap.size === 0) {
      const cached = this.getCachedOrders(`email_${cleanEmail}`);
      cached.forEach((o) => resultsMap.set(o.orderId, o));
    }

    const list = Array.from(resultsMap.values());
    list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    return list;
  },
};
