# TÀI LIỆU ĐẶC TẢ API HỆ THỐNG (API REFERENCE)

Tài liệu mô tả chi tiết danh mục các giao diện lập trình ứng dụng (RESTful APIs và WebSocket STOMP) được định tuyến thông qua **Spring Cloud API Gateway** (Port `8080`).

---

## 1. CƠ CHẾ ĐỊNH TUYẾN VÀ XÁC THỰC

* **Base URL:** `http://localhost:8080`
* **Cơ chế bảo mật:** OAuth2 / OpenID Connect (Keycloak). Các API yêu cầu xác thực cần kèm Header `Authorization: Bearer <JWT_ACCESS_TOKEN>`.
* **Định danh người dùng:** Header `X-User-Id` được API Gateway tự động giải mã từ JWT Claims hoặc chuyển tiếp trực tiếp trong các lời gọi nội bộ.
* **Đảm bảo tính Idempotency:** Các yêu cầu khởi tạo đơn hàng cần gửi kèm Header `Idempotency-Key: <UUID>` để ngăn chặn việc xử lý trùng lặp giao dịch.

---

## 2. DỊCH VỤ ĐỊNH DANH VÀ XÁC THỰC (KEYCLOAK OIDC)

### 2.1 Yêu cầu cấp Token xác thực người dùng (Resource Owner Password Credentials)
* **Endpoint:** `POST http://localhost:8180/realms/ecommerce-realm/protocol/openid-connect/token`
* **Content-Type:** `application/x-www-form-urlencoded`
* **Tham số Body:**
  | Tham số | Giá trị mẫu | Mô tả |
  | :--- | :--- | :--- |
  | `client_id` | `ecommerce-frontend` | Client ID cấu hình phương thức OIDC PKCE |
  | `grant_type` | `password` | Luồng cấp phép xác thực mật khẩu |
  | `username` | `customer` | Tên đăng nhập người dùng mẫu |
  | `password` | `password` | Mật khẩu tài khoản |

* **Phản hồi mẫu (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsIn...",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsIn...",
  "token_type": "Bearer"
}
```

---

## 3. PRODUCT CATALOG SERVICE (PORT 8081)

### 3.1 Truy vấn danh sách sản phẩm (Phân trang)
* **Endpoint:** `GET /api/products`
* **Tham số truy vấn:** `page=0&size=20&sort=createdAt,desc`
* **Phản hồi mẫu (200 OK):**
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

### 3.2 Truy vấn thông tin chi tiết sản phẩm
* **Endpoint:** `GET /api/products/{id}`
* **Phản hồi mẫu (200 OK):** Đối tượng thông tin chi tiết sản phẩm bao gồm đặc tả kỹ thuật và phân loại.

---

## 4. FLASH SALE & INVENTORY SERVICE (PORT 8083)

### 4.1 Truy vấn danh sách phiên Flash Sale đang diễn ra
* **Endpoint:** `GET /api/flash-sales/active`
* **Phản hồi mẫu (200 OK):**
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

### 4.2 Truy vấn trạng thái tồn kho thực tế
* **Endpoint:** `GET /api/inventory/{productId}`
* **Phản hồi mẫu (200 OK):**
```json
{
  "productId": "PROD-101",
  "quantity": 20,
  "reservedQuantity": 0,
  "status": "IN_STOCK"
}
```

---

## 5. CART SERVICE (PORT 8085)

### 5.1 Lấy dữ liệu giỏ hàng người dùng
* **Endpoint:** `GET /api/cart`
* **Headers:** `X-User-Id: <user-id>`
* **Phản hồi mẫu (200 OK):**
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

### 5.2 Thêm hoặc cập nhật mặt hàng trong giỏ
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
* **Phản hồi mẫu (200 OK):** Cập nhật dữ liệu trên cấu trúc Redis Hash Key `cart:{userId}` với thời gian phản hồi dưới 2ms.

---

## 6. ORDER SERVICE & SAGA CHOREOGRAPHY (PORT 8082)

### 6.1 Khởi tạo đơn hàng Flash Sale (Kích hoạt chuỗi Saga)
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

* **Các mã phản hồi:**
  * **`202 Accepted`:** Yêu cầu đặt hàng được tiếp nhận vào bảng Transactional Outbox, tạo mã đơn hàng `ORD-XXXXXX` với trạng thái ban đầu `PENDING`. Chuỗi Saga được kích hoạt bất đồng bộ qua Kafka.
  * **`409 Conflict`:** Từ chối yêu cầu do hết tồn kho hoặc vượt quá hạn mức mua quy định trên mỗi tài khoản trong phiên Flash Sale.
  * **`429 Too Many Requests`:** Yêu cầu bị giới hạn bởi bộ điều phối lưu lượng (Rate Limiter) tại API Gateway.

* **Phản hồi mẫu (202 Accepted):**
```json
{
  "orderId": "ORD-7A9B1C",
  "userId": "customer-user-uuid-101",
  "status": "PENDING",
  "totalAmount": 29990000.0,
  "message": "Đơn hàng đã được tiếp nhận và chuyển vào hàng đợi xử lý phân tán."
}
```

### 6.2 Truy vấn chi tiết đơn hàng và trạng thái Saga
* **Endpoint:** `GET /api/v1/orders/{orderId}`
* **Phản hồi mẫu (200 OK):**
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

## 7. WEBSOCKET REALTIME NOTIFICATION (PORT 8085 / 8086)

* **Giao thức:** STOMP qua WebSocket (hỗ trợ SockJS Fallback)
* **URL kết nối:** `ws://localhost:8085/ws` hoặc `http://localhost:8085/ws`
* **Kênh riêng người dùng (User Queue):** `/user/{userId}/queue/orders`
* **Kênh công khai (Public Topic):** `/topic/flashsale-stock` (đồng bộ trạng thái tồn kho thời gian thực tới toàn bộ các máy khách kết nối)

---

## 8. HẠ TẦNG GIÁM SÁT VÀ TRUY VẾT PHÂN TÁN

| Phân hệ / Dịch vụ | URL | Chức năng giám sát |
| :--- | :--- | :--- |
| **Prometheus Metrics** | `http://localhost:9090` | Thu thập chỉ số phân tích: Thông lượng RPS, trạng thái JVM, Connection Pool |
| **Grafana Dashboards** | `http://localhost:3001` | Trực quan hóa dữ liệu đo tải thời gian thực (Tài khoản: `admin` / `admin123456`) |
| **Jaeger Tracing** | `http://localhost:16686` | Truy vết chuỗi thực thi của các yêu cầu xuyên suốt các vi dịch vụ |
| **Eureka Registry** | `http://localhost:8761` | Giám sát trạng thái đăng ký dịch vụ và cơ chế kiểm tra nhịp tim (Heartbeat) |
