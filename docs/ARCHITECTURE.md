# 🏛️ TÀI LIỆU KIẾN TRÚC KỸ THUẬT HỆ THỐNG (SYSTEM ARCHITECTURE)

Hệ thống Thương Mại Điện Tử Hỗ Trợ Flash Sale Tải Cao được thiết kế theo mô hình **Kiến trúc Vi dịch vụ Hướng Sự kiện (Event-Driven Microservices Architecture)**, kết hợp các mẫu thiết kế công nghiệp tiên tiến để giải quyết triệt để bài toán thắt nút cổ chai (bottleneck), tranh chấp tài nguyên (race condition) và tính nhất quán dữ liệu phân tán (distributed data consistency).

---

## 📐 1. TỔNG QUAN KIẾN TRÚC ĐA TẦNG (HIGH-LEVEL ARCHITECTURE)

```mermaid
graph TD
    User["Khách Hàng (Web Browser)"]
    Admin["Quản Trị Viên (Admin Console)"]

    subgraph CDN_Inbound ["Tầng Cổng Vào & Định Tuyến"]
        Nginx["Nginx Reverse Proxy (Port 80)"]
        Gateway["Spring Cloud API Gateway (Port 8080)<br/>• Resilience4j Circuit Breaker<br/>• JWT Authentication Filter<br/>• Global Rate Limiter"]
    end

    subgraph Security_IAM ["Tầng Bảo Mật & Quản Trị Định Danh"]
        Keycloak["Keycloak IAM Server (Port 8180)<br/>• OAuth2 / OpenID Connect<br/>• PKCE (S256 Method)<br/>• Role-Based Access Control (RBAC)"]
    end

    subgraph Service_Mesh ["Tầng Nghiệp Vụ Vi Dịch Vụ (Core Microservices)"]
        Eureka["Eureka Discovery Server (Port 8761)"]
        ProdSvc["Product Service (Port 8081)<br/>Catalog & Search"]
        OrderSvc["Order Service (Port 8082)<br/>Transactional Outbox"]
        InvSvc["Inventory Service (Port 8083)<br/>Redis Lua & Redisson Lock"]
        PaySvc["Payment Service (Port 8084)<br/>Idempotent Payment Engine"]
        CartSvc["Cart Service (Port 8085)<br/>In-Memory Shopping Cart"]
        NotifSvc["Notification Service (Port 8086)<br/>WebSocket STOMP Broker"]
    end

    subgraph Event_Backbone ["Tầng Điều Phối Bất Đồng Bộ (Event Streaming)"]
        Kafka["Apache Kafka 3.6 (4 Topics, 3 Partitions)<br/>• order-events<br/>• inventory-events<br/>• payment-events<br/>• notification-events"]
        Zookeeper["Apache ZooKeeper (Port 2181)"]
    end

    subgraph Polyglot_Data ["Tầng Lưu Trữ Đa Hình (Polyglot Persistence)"]
        MySQL["MySQL 8.0 (ACID RDBMS)<br/>Orders, Inventory, Payments"]
        MongoDB["MongoDB 7.0 (Document DB)<br/>Product Catalog & Specs"]
        Redis["Redis 7.0 (In-Memory Cache & Lock)<br/>Stock Counters, User Limits, Cart"]
    end

    subgraph Observability ["Tầng Giám Sát & Truy Vết Toàn Diện"]
        Prometheus["Prometheus (Port 9090)"]
        Grafana["Grafana Dashboards (Port 3001)"]
        Jaeger["Jaeger Distributed Tracing (Port 16686)"]
    end

    User -->|HTTPS / WSS| Nginx
    Admin -->|HTTPS| Nginx
    Nginx --> Gateway
    Gateway <-->|Validate Tokens| Keycloak
    Gateway --> ProdSvc & OrderSvc & InvSvc & PaySvc & CartSvc & NotifSvc

    OrderSvc -.->|Publish Event| Kafka
    InvSvc -.->|Publish / Consume| Kafka
    PaySvc -.->|Publish / Consume| Kafka
    NotifSvc -.->|Consume Event| Kafka
    NotifSvc -->|WebSocket STOMP| User

    ProdSvc --> MongoDB
    OrderSvc --> MySQL & Redis
    InvSvc --> MySQL & Redis
    PaySvc --> MySQL
    CartSvc --> Redis

    Gateway & OrderSvc & InvSvc & PaySvc -.->|Metrics| Prometheus
    Prometheus --> Grafana
    Gateway & OrderSvc & InvSvc & PaySvc -.->|Trace Spans| Jaeger
```

---

## ⚡ 2. CƠ CHẾ SĂN HÀNG FLASH SALE ĐỘC QUYỀN (O(1) REDIS LUA SCRIPT)

Trong các phiên bán hàng chớp nhoáng (Flash Sale), hàng chục ngàn người dùng cùng tranh mua số lượng sản phẩm có hạn trong tích tắc. Nếu ghi thẳng xuống cơ sở dữ liệu quan hệ (RDBMS) bằng các câu lệnh `SELECT ... FOR UPDATE`, hệ thống sẽ lập tức rơi vào trạng thái nghẽn khóa (Deadlock/Row-Lock Contention) và suy sụp (Cascading Failure).

Hệ thống áp dụng giải pháp **Tách rời luồng xử lý bộ nhớ (In-Memory Isolation)**:

1. **Khởi tạo bộ nhớ (Cache Warming):** Trước khi phiên mở, số lượng tồn kho mở bán và hạn mức mua tối đa được nạp sẵn vào Redis.
2. **Thực thi nguyên tử qua Lua Script:** Thuật toán trừ tồn kho và kiểm soát hạn mức được gói gọn trong 1 kịch bản Lua Script duy nhất chạy trực tiếp trên Redis Engine:
   * **Nguyên tử tuyệt đối (Atomicity):** Do Redis xử lý đơn luồng sự kiện (Single-Threaded Event Loop), mã Lua được đảm bảo không bị xen ngang bởi bất kỳ tiến trình nào khác, triệt tiêu 100% rủi ro Race Condition.
   * **Độ phức tạp $O(1)$:** Thời gian thực thi chỉ từ 0.8ms đến 1.5ms, giải phóng hoàn toàn cơ sở dữ liệu quan hệ khỏi áp lực tải đỉnh.
3. **Thuật toán kiểm soát 3 lớp trong Lua Script:**
   * **Lớp 1:** Kiểm tra sự tồn tại của phiên Flash Sale và sản phẩm.
   * **Lớp 2 (Hạn mức cá nhân):** Kiểm tra `user_purchased_count + requested_qty <= max_limit` (mặc định tối đa 2 sản phẩm/khách hàng).
   * **Lớp 3 (Chống bán âm kho):** Kiểm tra `current_stock >= requested_qty`. Nếu đủ, thực hiện trừ kho và tăng biến đếm của user ngay trong 1 bước duy nhất.

---

## 🔄 3. QUẢN LÝ GIAO DỊCH PHÂN TÁN: SAGA CHOREOGRAPHY & TRANSACTIONAL OUTBOX

Hệ thống áp dụng mô hình **Saga Choreography (Biên đạo múa)** phối hợp cùng mẫu thiết kế **Transactional Outbox Pattern** để đảm bảo tính nhất quán cuối cùng (Eventual Consistency) mà không dùng đến 2-Phase Commit (2PC) vốn làm chậm hệ thống.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Khách Hàng (Web UI)
    participant Gateway as API Gateway
    participant Order as Order Service
    participant MySQL as MySQL (Outbox Table)
    participant Kafka as Kafka Event Broker
    participant Inv as Inventory Service
    participant Pay as Payment Service
    participant Notif as Notification Service

    Client->>Gateway: POST /api/v1/orders/flash-sale (JWT + Idempotency-Key)
    Gateway->>Order: Điều phối Request
    Note over Order: Thực hiện trong 1 Local Transaction:<br/>1. Lưu Order (Status = PENDING)<br/>2. Ghi OrderCreatedEvent vào Outbox Table
    Order->>MySQL: COMMIT TRANSACTION
    Order-->>Client: 202 Accepted (ORD-XXXXXX, PENDING)

    Note over Order: Outbox Relay Worker quét bảng Outbox định kỳ
    Order->>Kafka: Phát OrderCreatedEvent -> [topic: order-events]
    
    Kafka->>Inv: Consumer nhận OrderCreatedEvent
    Note over Inv: 1. Khóa phân tán Redisson Lock<br/>2. Kiểm tra Inbox (Chống trùng lặp)<br/>3. Giữ tồn kho thực tế trong MySQL
    alt Giữ kho thành công
        Inv->>Kafka: Phát InventoryReservedEvent -> [topic: inventory-events]
        Kafka->>Pay: Consumer nhận InventoryReservedEvent
        Note over Pay: Xử lý thanh toán mô phỏng VNPay<br/>Đảm bảo Idempotency
        alt Thanh toán thành công
            Pay->>Kafka: Phát PaymentCompletedEvent -> [topic: payment-events]
            Kafka->>Order: Order Service nhận PaymentCompletedEvent
            Note over Order: Cập nhật Order Status = CONFIRMED
            Kafka->>Notif: Notification Service nhận PaymentCompletedEvent
            Notif-->>Client: Bắn tin STOMP WebSocket: "ĐẶT HÀNG THÀNH CÔNG!"
        else Thanh toán thất bại (Compensating Transaction)
            Pay->>Kafka: Phát PaymentFailedEvent -> [topic: payment-events]
            Kafka->>Order: Cập nhật Order Status = CANCELLED
            Kafka->>Inv: Hoàn trả số lượng tồn kho (Release Stock)
            Notif-->>Client: Bắn tin STOMP: "ĐẶT HÀNG THẤT BẠI"
        end
    else Hết hàng tồn kho
        Inv->>Kafka: Phát InventoryReservationFailedEvent
        Kafka->>Order: Cập nhật Order Status = CANCELLED_OUT_OF_STOCK
        Notif-->>Client: Bắn tin STOMP: "SẢN PHẨM ĐÃ HẾT HÀNG"
    end
```

### Các Nguyên Tắc Thiết Kế Trọng Yếu:
1. **Transactional Outbox Pattern:** Giải quyết bài toán Dual-Write Problem (vừa lưu DB vừa bắn message). Message được ghi vào bảng `outbox_events` trong cùng một transaction cục bộ của RDBMS. Tiến trình background đọc outbox và đẩy lên Kafka với bảo đảm **At-Least-Once Delivery**.
2. **Idempotent Consumer (Mẫu thiết kế Xử lý Bất Biến):** Mọi consumer (Inventory, Payment, Notification) đều ghi nhận khóa thông điệp vào bảng `inbox_events`. Nếu Kafka gửi lại một sự kiện đã xử lý, consumer lập tức nhận diện và bỏ qua (Deduplication).
3. **Compensating Transactions (Giao dịch Bù trừ):** Khi bất kỳ bước nào trong chuỗi Saga gặp sự cố (như thanh toán thất bại, user hủy thanh toán), hệ thống phát sự kiện bù trừ ngược lại để hoàn trả tồn kho và cập nhật trạng thái đơn hàng.

---

## 🔒 4. MÔ HÌNH BẢO MẬT & ĐỊNH DANH (KEYCLOAK OIDC PKCE)

1. **Chuẩn Authorization Code Flow với PKCE (RFC 7636):**
   * Frontend (Single Page Application) là Public Client. Hệ thống **tuyệt đối không nhúng Client Secret** vào mã nguồn JS/TS để loại bỏ hoàn toàn nguy cơ rò rỉ mã bí mật.
   * Trao đổi mã cấp phép sử dụng cặp `code_verifier` và `code_challenge` theo thuật toán băm SHA-256 (`S256`).
2. **Xác thực Không Trạng Thái (Stateless JWT Verification):**
   * API Gateway tự động lấy Public Key (JWKS) từ Keycloak để thẩm định tính hợp lệ của chữ ký điện tử trên Access Token.
   * Các microservices tầng dưới tin tưởng Header `X-User-Id` và các Claim được Gateway chuyển tiếp sau khi đã qua bước lọc an ninh.
3. **Phân Quyền Dựa Trên Vai Trò (RBAC):**
   * Phân quyền chặt chẽ thông qua Realm Roles: `ROLE_CUSTOMER` (người dùng mua sắm) và `ROLE_ADMIN` (quản trị viên cấu hình tồn kho, phiên bán).

---

## 🛡️ 5. KHẢ NĂNG CHỐNG CHỊU & PHỤC HỒI (RESILIENCE & FAULT TOLERANCE)

1. **Circuit Breaker (Bộ ngắt mạch Resilience4j):**
   * Được thiết lập tại tầng API Gateway cho toàn bộ các route điều hướng.
   * Ngưỡng mở mạch (Open State): Khi tỷ lệ lỗi vượt quá 50% hoặc thời gian phản hồi vượt quá 2 giây trong cửa sổ trượt (Sliding Window), mạch tự động mở, ngăn chặn hiện tượng quá tải dây chuyền (Cascading Failure).
2. **Fallback Mechanism:**
   * Khi mạch mở, Gateway tự động chuyển hướng request sang các Controller dự phòng (`FallbackController`), trả về mã HTTP `503 Service Unavailable` kèm phản hồi JSON thân thiện thay vì để kết nối bị treo (Connection Timeout).

---

## 📈 6. BẢN ĐỒ GIÁM SÁT TOÀN DIỆN (FULL-STACK OBSERVABILITY)

* **Metrics Scraping (Prometheus):** Tự động thu thập chỉ số định kỳ mỗi 5 giây từ `/actuator/prometheus` của từng Spring Boot container (JVM Memory, GC Pauses, HikariCP Connection Pool, HTTP Request Rates).
* **Trực quan hóa (Grafana):** Dashboard thiết kế chuyên dụng **Flash Sale System Overview** giám sát: Throughput (RPS), Phân vị Latency P95/P99, Tỷ lệ lỗi 5xx, Tỷ trọng loại yêu cầu (Request Mix).
* **Truy vết Phân tán (Jaeger Tracing):** Sử dụng OpenTelemetry Java Agent tự động thu thập Span Tree xuyên suốt từ Gateway -> Order -> Kafka -> Inventory -> Payment, giúp cô lập điểm nghẽn độ trễ tức thời.
