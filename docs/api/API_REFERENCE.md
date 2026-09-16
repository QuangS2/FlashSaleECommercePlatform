# 📖 TÀI LIỆU TRA CỨU API HỆ THỐNG (API REFERENCE)

Tài liệu mô tả chi tiết danh mục các giao diện lập trình ứng dụng (RESTful APIs & WebSocket) được định tuyến thông qua **Spring Cloud API Gateway** (Port `8080`).

---

## 🌐 1. CƠ CHẾ ĐỊNH TUYẾN & XÁC THỰC

* **Base URL:** `http://localhost:8080`
* **Cơ chế Bảo mật:** OAuth2 / OpenID Connect (Keycloak). Các API yêu cầu xác thực nhận Header `Authorization: Bearer <JWT_ACCESS_TOKEN>`.
* **Định danh Người dùng:** Header `X-User-Id` được API Gateway tự động giải mã từ JWT Claims hoặc truyền trực tiếp trong các tình huống nội bộ.
* **Chống Gửi Đơn Trùng Lặp (Idempotency):** Các lệnh ghi đơn hàng nhận Header `Idempotency-Key: <UUID>` để đảm bảo tính duy nhất.

---

## 🔐 2. ĐỊNH DANH & XÁC THỰC (KEYCLOAK OIDC)

### 2.1 Lấy Token Đăng Nhập Khách Hàng (Resource Owner Password Credentials)
* **Endpoint:** `POST http://localhost:8180/realms/ecommerce-realm/protocol/openid-connect/token`
* **Content-Type:** `application/x-www-form-urlencoded`
* **Body Parameters:**
  | Tham số | Giá trị mẫu | Mô tả |
  | :--- | :--- | :--- |
  | `client_id` | `ecommerce-frontend` | Client ID đã cấu hình OIDC PKCE |
  | `grant_type` | `password` | Luồng cấp phép mật khẩu |
  | `username` | `customer` | Tên đăng nhập người dùng mẫu |
  | `password` | `password` | Mật khẩu người dùng |

* **Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsIn...",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsIn...",
  "token_type": "Bearer"
}
```

---

## 📦 3. PRODUCT CATALOG SERVICE (PORT 8081)

### 3.1 Danh Sách Sản Phẩm (Phân Trang)
* **Endpoint:** `GET /api/products`
* **Query Parameters:** `page=0&size=20&sort=createdAt,desc`
* **Response (200 OK):**
```json
{
  "content": [
    {
      "id": "PROD-101",
      "name": "Điện thoại iPhone 15 Pro Max 256GB",
      "category": "Điện thoại",
      "originalPrice": 34990000.0,
      "flashSalePrice": 29990000.0,
      "imageUrl": "/images/iphone-15.png",
      "stock": 100
    }
  ],
  "totalElements": 24,
  "totalPages": 2
}
```

### 3.2 Chi Tiết Sản Phẩm
* **Endpoint:** `GET /api/products/{id}`
* **Response (200 OK):** Đối tượng sản phẩm đầy đủ mô tả kỹ thuật và thông số.

---

## ⚡ 4. FLASH SALE & INVENTORY SERVICE (PORT 8083)

### 4.1 Lấy Danh Sách Phiên Flash Sale Đang Kích Hoạt
* **Endpoint:** `GET /api/flash-sales/active`
* **Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Phiên Giờ Vàng Công Nghệ",
    "startTime": "2026-09-16T09:00:00Z",
    "endTime": "2026-09-16T23:59:59Z",
    "status": "ACTIVE",
    "items": [
      {
        "itemId": 101,
        "productId": "PROD-101",
        "flashSalePrice": 29990000.0,
        "availableQuantity": 20,
        "soldQuantity": 0,
        "maxQuantityPerCustomer": 2
      }
    ]
  }
]
```

### 4.2 Kiểm Tra Tồn Kho Thực Tế
* **Endpoint:** `GET /api/inventory/{productId}`
* **Response (200 OK):**
```json
{
  "productId": "PROD-101",
  "quantity": 20,
  "reservedQuantity": 0,
  "status": "IN_STOCK"
}
```

---

## 🛒 5. CART SERVICE (PORT 8085 - REDIS O(1))

### 5.1 Lấy Giỏ Hàng Người Dùng
* **Endpoint:** `GET /api/cart`
* **Headers:** `X-User-Id: <user-id>`
* **Response (200 OK):**
```json
{
  "userId": "customer-user-uuid-101",
  "items": [
    {
      "productId": "PROD-101",
      "productName": "iPhone 15 Pro Max 256GB",
      "quantity": 1,
      "unitPrice": 29990000.0
    }
  ],
  "totalAmount": 29990000.0
}
```

### 5.2 Thêm / Cập Nhật Sản Phẩm Vào Giỏ
* **Endpoint:** `POST /api/cart/items`
* **Headers:** `X-User-Id: <user-id>`
* **Request Body:**
```json
{
  "productId": "PROD-101",
  "productName": "iPhone 15 Pro Max 256GB",
  "quantity": 1,
  "unitPrice": 29990000.0
}
```
* **Response (200 OK):** Cập nhật nguyên tử trên Redis Hash Key `cart:{userId}` với thời gian phản hồi < 2ms.

---

## 📑 6. ORDER SERVICE & SAGA CHOREOGRAPHY (PORT 8082)

### 6.1 Đặt Hàng Flash Sale (Kích Hoạt Chuỗi Phân Tán Saga)
* **Endpoint:** `POST /api/v1/orders/flash-sale`
* **Headers:**
  * `Content-Type: application/json`
  * `X-User-Id: customer-user-uuid-101`
  * `Idempotency-Key: idemp-20260916-001`
* **Request Body:**
```json
{
  "flashSaleId": 1,
  "itemId": 101,
  "productId": "PROD-101",
  "quantity": 1,
  "unitPrice": 29990000.0,
  "customerName": "Lê Văn Khách",
  "customerEmail": "customer@ecommerce.vn",
  "shippingAddress": "123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM"
}
```

* **Mã Trạng Thái Trả Về:**
  * **`202 Accepted`:** Đơn hàng được tiếp nhận thành công vào Transactional Outbox, sinh mã đơn hàng `ORD-XXXXXX` trạng thái `PENDING`. Chuỗi Saga được đẩy lên Kafka bất đồng bộ.
  * **`409 Conflict`:** Bán âm kho hoặc vượt quá hạn mức tối đa cho phép của người dùng trong phiên Flash Sale.
  * **`429 Too Many Requests`:** Bị chặn bởi Rate Limiter của API Gateway khi số lượng request vượt ngưỡng cho phép.

* **Response (202 Accepted):**
```json
{
  "orderId": "ORD-7A9B1C",
  "userId": "customer-user-uuid-101",
  "status": "PENDING",
  "totalAmount": 29990000.0,
  "message": "Đơn hàng đã được tiếp nhận và đưa vào hàng đợi Saga xử lý bất đồng bộ."
}
```

### 6.2 Tra Cứu Tiến Độ Saga & Chi Tiết Đơn Hàng
* **Endpoint:** `GET /api/v1/orders/{orderId}`
* **Response (200 OK):**
```json
{
  "orderId": "ORD-7A9B1C",
  "status": "CONFIRMED",
  "sagaStep": "COMPLETED",
  "items": [
    {
      "productId": "PROD-101",
      "quantity": 1,
      "unitPrice": 29990000.0
    }
  ],
  "paymentId": "PAY-88231",
  "createdAt": "2026-09-16T14:30:00Z",
  "confirmedAt": "2026-09-16T14:30:01Z"
}
```

---

## 🔔 7. WEBSOCKET REALTIME NOTIFICATION (PORT 8085 / 8086)

* **Giao thức:** STOMP qua WebSocket (SockJS Fallback)
* **Kết nối URL:** `ws://localhost:8085/ws` hoặc `http://localhost:8085/ws`
* **User Queue cá nhân:** `/user/{userId}/queue/orders`
* **Topic công khai:** `/topic/flashsale-stock` (đồng bộ tồn kho thời gian thực tới tất cả client trên giao diện)

---

## 📊 8. HẠ TẦNG GIÁM SÁT & OBSERVABILITY

| Dịch vụ | URL | Mô tả chức năng |
| :--- | :--- | :--- |
| **Prometheus Metrics** | `http://localhost:9090` | Thu thập chỉ số phân tích RPS, JVM, Connection Pool |
| **Grafana Dashboards** | `http://localhost:3001` | Dashboard đo tải thời gian thực (User: `admin` / Pass: `admin123456`) |
| **Jaeger Tracing** | `http://localhost:16686` | Truy vết đường đi của Request xuyên qua các vi dịch vụ |
| **Eureka Registry** | `http://localhost:8761` | Đăng ký dịch vụ và giám sát nhịp tim (Heartbeat) |
