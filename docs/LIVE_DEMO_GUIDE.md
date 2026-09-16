# KỊCH BẢN THỰC NGHIỆM VÀ ĐÁNH GIÁ HỆ THỐNG (SYSTEM EVALUATION PLAYBOOK)

Tài liệu này hướng dẫn chi tiết các bước thực nghiệm, minh chứng kỹ thuật và đánh giá các chức năng trọng yếu của hệ thống phục vụ công tác nghiệm thu và bảo vệ kỹ thuật trước Hội đồng đánh giá.

---

## 1. DANH MỤC CÁC CỔNG TRUY CẬP VÀ PHÂN HỆ VẬN HÀNH

| Phân hệ / Dịch vụ | Địa chỉ truy cập (URL) | Tài khoản / Mật khẩu | Mục tiêu kiểm tra |
| :--- | :--- | :--- | :--- |
| **Giao diện khách hàng (Web Client)** | `http://localhost:3000` | `customer` / `password` | Thực nghiệm đặt hàng Flash Sale, giỏ hàng và theo dõi tiến trình Saga thời gian thực |
| **Eureka Service Discovery** | `http://localhost:8761` | Không yêu cầu xác thực | Xác minh trạng thái hoạt động (`UP`) của các phiên bản vi dịch vụ |
| **Keycloak IAM Admin Console** | `http://localhost:8180` | `admin` / `adminpassword` | Quản trị định danh tập trung, kiểm tra cấu hình OIDC PKCE (S256) và Realm Roles |
| **Grafana Monitoring Dashboard** | `http://localhost:3001` | `admin` / `admin123456` | Giám sát hiệu năng thời gian thực: RPS, Latency p95, HTTP Status Codes, Connection Pool |
| **Prometheus Metrics** | `http://localhost:9090` | Không yêu cầu xác thực | Thu thập và lưu trữ chuỗi thời gian chỉ số hiệu năng từ Spring Boot Actuator |
| **Jaeger Distributed Tracing** | `http://localhost:16686` | Không yêu cầu xác thực | Truy vết chuỗi Span phân tán xuyên suốt các vi dịch vụ |

---

## 2. CƠ CHẾ KHÔI PHỤC DỮ LIỆU THỰC NGHIỆM (RESET TEST DATA)

Trong quá trình thực nghiệm, các kịch bản kiểm thử tải hoặc đặt hàng liên tục có thể làm tiêu hao toàn bộ tồn kho hoặc đạt ngưỡng hạn mức mua tối đa của tài khoản (tối đa 2 sản phẩm/khách hàng). Hệ thống cung cấp cơ chế khôi phục dữ liệu nhanh chóng mà không cần khởi động lại các container hạ tầng:

* **Phương thức 1 (Thực hiện trên giao diện Web):**  
  Nhấp chọn chức năng **"Làm mới dữ liệu kiểm thử"** tại thanh công cụ hệ thống (`http://localhost:3000`). Hệ thống sẽ tự động khôi phục số lượng tồn kho ban đầu của 24 sản phẩm mẫu, xóa dữ liệu giỏ hàng và đặt lại bộ đếm hạn mức mua sắm.
* **Phương thức 2 (Thực hiện qua dòng lệnh):**  
  ```bash
  node scripts/reset_demo_data.mjs
  ```
* **Đặc tính kỹ thuật:** Toàn bộ thông tin tài khoản và cấu hình phân quyền trên Keycloak (`customer`, `admin`) được giữ nguyên, bảo đảm tính sẵn sàng cho các lượt thực nghiệm tiếp theo.

---

## 3. THỰC NGHIỆM 1: QUY TRÌNH ĐẶT HÀNG TRONG PHIÊN FLASH SALE (HAPPY PATH)

### Mục tiêu:
Đánh giá luồng xử lý đơn hàng hoàn chỉnh khi có yêu cầu hợp lệ trong phiên Flash Sale, từ khâu xác thực người dùng, trừ tồn kho nguyên tử, điều phối Saga đến phát thông báo qua WebSocket.

### Trình tự thực nghiệm:
1. **Kiểm tra phiên Flash Sale:**
   * Truy cập `http://localhost:3000`.
   * Quan sát bộ đếm ngược thời gian thực của phiên bán và thanh biểu thị trạng thái tồn kho theo thời gian thực.
2. **Xác thực người dùng qua Keycloak OIDC PKCE:**
   * Chọn **"Đăng nhập"** để chuyển hướng sang Keycloak Identity Server.
   * Đăng nhập bằng tài khoản: `customer` / `password`.
   * Phân tích kỹ thuật: Luồng xác thực tuân thủ chuẩn OAuth 2.1 với PKCE (phương thức băm SHA-256 `S256`), không lưu trữ Client Secret ở môi trường máy khách.
3. **Khởi tạo đơn hàng:**
   * Đưa sản phẩm Flash Sale vào giỏ hàng, áp dụng mã ưu đãi `FLASHSALE50`.
   * Nhấp chọn **"Xác nhận đặt hàng"**.
4. **Tiến trình xử lý phân tán Saga:**
   * Giao diện hiển thị tiến trình xử lý phân tán qua các bước:
     * **Bước 1 (Tiếp nhận):** Order Service ghi nhận đơn hàng ở trạng thái `PENDING` vào bảng Transactional Outbox.
     * **Bước 2 (Giữ kho nguyên tử):** Redis Lua Script thực thi kiểm tra và trừ tồn kho $O(1)$, Inventory Service xác nhận giữ chỗ hàng hóa.
     * **Bước 3 (Xử lý thanh toán):** Payment Service xử lý thanh toán bảo đảm tính Idempotent.
     * **Bước 4 (Thông báo thời gian thực):** Notification Service nhận sự kiện từ Kafka và truyền thông báo qua kết nối WebSocket STOMP tới máy khách.
   * Kết quả: Đơn hàng hoàn tất với trạng thái `CONFIRMED` và mã định danh đơn hàng `ORD-XXXXXX`.

---

## 4. THỰC NGHIỆM 2: CƠ CHẾ CHỐNG VƯỢT HẠN MỨC VÀ GIAO DỊCH BÙ TRỪ SAGA

### Tình huống 4.1: Kiểm soát hạn mức tối đa trên từng tài khoản
* **Mô tả:** Quy định hạn mức trong mỗi phiên Flash Sale tối đa là 2 sản phẩm/khách hàng.
* **Thao tác:** Sử dụng tài khoản `customer` đã mua trước đó, gửi tiếp yêu cầu mua thêm 2 đơn vị sản phẩm cùng loại.
* **Kết quả:** Hệ thống từ chối yêu cầu và thông báo vượt quá hạn mức cho phép. Logic kiểm soát được thực thi trực tiếp trong Redis Lua Script ở độ phức tạp $O(1)$, ngăn chặn truy vấn quá tải xuống tầng cơ sở dữ liệu.

### Tình huống 4.2: Giao dịch bù trừ Saga (Compensating Transaction) khi hủy giao dịch
* **Mô tả:** Đánh giá tính nhất quán dữ liệu khi chuỗi giao dịch phân tán gặp lỗi tại một mắt xích trung gian.
* **Thao tác:** Khởi tạo đơn hàng nhưng mô phỏng tình huống thanh toán thất bại hoặc người dùng hủy đơn trước khi thanh toán.
* **Cơ chế hoạt động:** Sự kiện bù trừ `OrderCancelledEvent` được phát lên Kafka topic `order-events`. Inventory Service tiêu thụ sự kiện và tự động hoàn trả số lượng tồn kho đã giữ chỗ (Compensating Action), đơn hàng chuyển về trạng thái `CANCELLED`.

---

## 5. THỰC NGHIỆM 3: GIÁM SÁT DÒNG SỰ KIỆN PHÂN TÁN VÀ ĐỐI SOÁT TỒN KHO

### Mục tiêu:
Minh chứng tính toàn vẹn dữ liệu và phòng chống bán âm kho (Anti-Overselling) dưới điều kiện tranh chấp tài nguyên cao độ.

### Trình tự thực nghiệm (Sử dụng 2 terminal song song):
* **Terminal 1:** Khởi chạy tiến trình theo dõi luồng sự kiện qua Kafka:
  ```powershell
  node scripts/kafka_live_tail.mjs
  ```
* **Terminal 2:** Kích hoạt kịch bản 5 yêu cầu đồng thời tranh mua 1 đơn vị tồn kho còn lại:
  ```powershell
  node scripts/realtime_scenario_runner.mjs 3
  ```
* **Kết quả quan sát:**
  * Terminal 1 ghi nhận đúng 1 sự kiện `INVENTORY_RESERVED` thành công và 4 sự kiện `INVENTORY_RESERVATION_FAILED` bị từ chối.
  * Terminal 2 hiển thị bảng đối soát dữ liệu: Số lượng tồn kho trong bảng `inventory` của MySQL giảm từ 1 về đúng **0**, bảo đảm không xảy ra hiện tượng tồn kho âm.

---

## 6. THỰC NGHIỆM 4: ĐO LƯỜNG HIỆU NĂNG VÀ KHẢ NĂNG CHỊU TẢI TRÊN GRAFANA

### Mục tiêu:
Đo lường năng lực chịu tải của hệ thống qua các chỉ số thông lượng, độ trễ và tỷ lệ lỗi kỹ thuật dưới áp lực nhiều yêu cầu đồng thời.

### Trình tự thực nghiệm:
1. **Truy cập bảng điều khiển Grafana:**
   * Địa chỉ: `http://localhost:3001` (Tài khoản: `admin` / `admin123456`).
   * Chọn Dashboard: **Flash Sale Microservices Overview**, thiết lập khoảng thời gian `Last 5 minutes`, chu kỳ làm mới tự động `5s`.
2. **Kích hoạt kịch bản kiểm thử tải:**
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\load-testing\run_load_test.ps1 -Engine runner -Requests 5000 -Concurrency 50
   ```
3. **Đánh giá các chỉ số trên bảng điều khiển:**
   * **Thông lượng (Throughput RPS):** Đo lường số lượng yêu cầu xử lý thành công mỗi giây tại thời điểm đỉnh tải (đạt từ 2.000 đến 3.500 RPS).
   * **Thời gian phản hồi (p95 Latency):** Phân vị trễ 95% duy trì dưới ngưỡng 128ms.
   * **Tỷ lệ lỗi kỹ thuật (HTTP 5xx):** Duy trì dưới 0.5% nhờ cơ chế ngắt mạch tự động của Resilience4j Circuit Breaker.
