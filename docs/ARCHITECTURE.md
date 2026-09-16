# TÀI LIỆU KIẾN TRÚC KỸ THUẬT HỆ THỐNG (SYSTEM ARCHITECTURE)

Hệ thống Thương Mại Điện Tử Hỗ Trợ Flash Sale Tải Cao được thiết kế theo mô hình **Kiến trúc Vi dịch vụ Hướng Sự kiện (Event-Driven Microservices Architecture)**, kết hợp các mẫu thiết kế công nghiệp nhằm giải quyết triệt để bài toán thắt nút cổ chai (bottleneck), tranh chấp tài nguyên (race condition) và tính nhất quán dữ liệu phân tán (distributed data consistency).

---

## 1. TỔNG QUAN KIẾN TRÚC HỆ THỐNG (HIGH-LEVEL ARCHITECTURE)

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

## 2. CƠ CHẾ XỬ LÝ ĐỒNG THỜI VÀ TRỪ TỒN KHO NGUYÊN TỬ (REDIS LUA SCRIPT)

Trong các phiên bán hàng Flash Sale, lượng truy cập đồng thời lớn tập trung vào một số lượng sản phẩm hữu hạn. Việc cập nhật trực tiếp xuống cơ sở dữ liệu quan hệ (RDBMS) bằng các truy vấn khóa dòng `SELECT ... FOR UPDATE` sẽ dẫn đến hiện tượng nghẽn hàng đợi (Deadlock/Row-Lock Contention) và suy giảm hiệu năng toàn hệ thống.

Hệ thống triển khai giải pháp **Cách ly xử lý trên bộ nhớ (In-Memory Isolation)**:

1. **Khởi tạo dữ liệu trên bộ nhớ đệm (Cache Warming):** Trước khi phiên bán diễn ra, chỉ số tồn kho khả dụng và hạn mức mua tối đa được đồng bộ nạp sẵn vào Redis.
2. **Thực thi nguyên tử qua Lua Script:** Thuật toán kiểm tra điều kiện, trừ tồn kho và ghi nhận hạn mức được đóng gói trong một kịch bản Lua Script thực thi trực tiếp trên Redis Engine:
   * **Tính nguyên tử (Atomicity):** Redis xử lý lệnh trên mô hình vòng lặp sự kiện đơn luồng (Single-Threaded Event Loop), đảm bảo kịch bản Lua được thực thi trọn vẹn mà không bị gián đoạn, loại bỏ hoàn toàn hiện tượng Race Condition.
   * **Độ phức tạp $O(1)$:** Thời gian phản hồi đạt từ 0.8ms đến 1.5ms, giải phóng cơ sở dữ liệu quan hệ khỏi áp lực tải đỉnh điểm.
3. **Quy trình kiểm soát 3 tầng trong Lua Script:**
   * **Tầng 1:** Xác thực sự tồn tại của phiên Flash Sale và sản phẩm tương ứng.
   * **Tầng 2 (Hạn mức người dùng):** Kiểm tra điều kiện `user_purchased_count + requested_qty <= max_limit` (mặc định tối đa 2 sản phẩm/khách hàng).
   * **Tầng 3 (Chống bán âm kho):** Kiểm tra điều kiện `current_stock >= requested_qty`. Khi thỏa mãn, hệ thống thực hiện giảm trừ tồn kho và cập nhật số lượng đã mua của người dùng trong một thao tác duy nhất.

---

## 3. QUẢN LÝ GIAO DỊCH PHÂN TÁN: SAGA CHOREOGRAPHY VÀ TRANSACTIONAL OUTBOX

Hệ thống áp dụng mô hình **Saga Choreography** kết hợp mẫu thiết kế **Transactional Outbox Pattern** để đảm bảo tính nhất quán dữ liệu cuối cùng (Eventual Consistency) giữa các vi dịch vụ mà không cần sử dụng giao thức khóa phân tán hai pha (2-Phase Commit):

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
            Notif-->>Client: Thông báo STOMP WebSocket: "ĐẶT HÀNG THÀNH CÔNG"
        else Thanh toán thất bại (Compensating Transaction)
            Pay->>Kafka: Phát PaymentFailedEvent -> [topic: payment-events]
            Kafka->>Order: Cập nhật Order Status = CANCELLED
            Kafka->>Inv: Hoàn trả số lượng tồn kho (Release Stock)
            Notif-->>Client: Thông báo STOMP WebSocket: "ĐẶT HÀNG THẤT BẠI"
        end
    else Hết hàng tồn kho
        Inv->>Kafka: Phát InventoryReservationFailedEvent
        Kafka->>Order: Cập nhật Order Status = CANCELLED_OUT_OF_STOCK
        Notif-->>Client: Thông báo STOMP WebSocket: "SẢN PHẨM ĐÃ HẾT HÀNG"
    end
```

### Các Nguyên Tắc Thiết Kế Trọng Yếu:
1. **Transactional Outbox Pattern:** Giải quyết bài toán ghi dữ liệu kép (Dual-Write Problem). Bản ghi sự kiện được lưu vào bảng `outbox_events` trong cùng transaction cục bộ của RDBMS. Tiến trình background định kỳ quét bảng outbox và chuyển tiếp thông điệp lên Kafka với cơ chế bảo đảm phân phát ít nhất một lần (**At-Least-Once Delivery**).
2. **Idempotent Consumer:** Các dịch vụ nhận thông điệp (Inventory, Payment, Notification) đều lưu trữ mã định danh thông điệp vào bảng `inbox_events`. Khi gặp lại thông điệp trùng lặp từ Kafka, consumer chủ động nhận diện và bỏ qua (Deduplication).
3. **Compensating Transactions (Giao dịch bù trừ):** Khi có bước xử lý gặp sự cố (thanh toán thất bại hoặc hủy đơn), hệ thống phát sự kiện bù trừ ngược lại nhằm hoàn trả tài nguyên kho đã giữ chỗ và cập nhật trạng thái đơn hàng sang `CANCELLED`.

---

## 4. MÔ HÌNH BẢO MẬT VÀ QUẢN LÝ ĐỊNH DANH (KEYCLOAK OIDC PKCE)

1. **Chuẩn Authorization Code Flow kết hợp PKCE (RFC 7636):**
   * Phía giao diện Single Page Application đóng vai trò là Public Client, không lưu trữ Client Secret để loại bỏ nguy cơ lộ mã bảo mật.
   * Trao đổi mã cấp phép sử dụng cặp khóa `code_verifier` và `code_challenge` mã hóa qua thuật toán SHA-256 (`S256`).
2. **Xác thực JWT không trạng thái (Stateless JWT Verification):**
   * API Gateway nạp Public Key (JWKS) từ Keycloak để thẩm định tính toàn vẹn của chữ ký điện tử trên Access Token.
   * Các vi dịch vụ nghiệp vụ nội bộ tiếp nhận các trường định danh như `X-User-Id` và các Claim do Gateway chuyển tiếp sau khi đã thẩm định an toàn.
3. **Phân quyền dựa trên vai trò (RBAC):**
   * Thiết lập phân quyền qua Realm Roles: `ROLE_CUSTOMER` (người dùng mua sắm) và `ROLE_ADMIN` (quản trị viên cấu hình phiên bán và số liệu tồn kho).

---

## 5. KHẢ NĂNG CHỐNG CHỊU VÀ DUNG LỖI (RESILIENCE & FAULT TOLERANCE)

1. **Circuit Breaker (Resilience4j):**
   * Được cấu hình tại tầng API Gateway cho toàn bộ các tuyến định tuyến dịch vụ.
   * Điều kiện kích hoạt trạng thái mở mạch (Open): Khi tỷ lệ lỗi kỹ thuật vượt ngưỡng 50% hoặc thời gian phản hồi vượt quá 2 giây trong cửa sổ trượt (Sliding Window), mạch tự động mở nhằm ngăn chặn tình trạng suy sụp dây chuyền (Cascading Failure).
2. **Cơ chế phản hồi dự phòng (Fallback Mechanism):**
   * Khi mạch mở, Gateway chuyển hướng yêu cầu sang các bộ xử lý dự phòng (`FallbackController`), phản hồi mã HTTP `503 Service Unavailable` cùng cấu trúc dữ liệu JSON xác định thay vì để kết nối bị quá hạn (Gateway Timeout).

---

## 6. HẠ TẦNG GIÁM SÁT VÀ QUAN SÁT HỆ THỐNG (FULL-STACK OBSERVABILITY)

* **Thu thập chỉ số hiệu năng (Prometheus):** Tự động thu thập chỉ số định kỳ mỗi 5 giây từ các điểm cuối `/actuator/prometheus` của từng phiên bản Spring Boot (bộ nhớ JVM, chu kỳ Garbage Collection, kết nối HikariCP và tần suất yêu cầu HTTP).
* **Trực quan hóa chỉ số (Grafana):** Bảng điều khiển chuyên dụng **Flash Sale System Overview** hiển thị thông lượng (RPS), phân vị trễ p95/p99, tỷ lệ phản hồi lỗi HTTP 5xx và phân bổ lưu lượng truy cập.
* **Truy vết phân tán (Jaeger Tracing):** Sử dụng OpenTelemetry Java Agent tự động thu thập Span Tree từ Gateway xuyên qua chuỗi Order, Kafka, Inventory và Payment, hỗ trợ cô lập và phân tích điểm nghẽn hiệu năng.
