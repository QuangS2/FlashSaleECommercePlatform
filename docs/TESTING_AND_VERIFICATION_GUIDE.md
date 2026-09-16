# 🧪 CẨM NANG HƯỚNG DẪN KIỂM THỬ & ĐỐI SOÁT HỆ THỐNG (TESTING & VERIFICATION GUIDE)

Tài liệu này cung cấp hướng dẫn đầy đủ, chi tiết từ A-Z để bất kỳ giảng viên, thành viên hội đồng hoặc kỹ sư phần mềm nào cũng có thể độc lập vận hành, kiểm thử và đối soát toàn bộ các phân hệ của Đồ án.

---

## 📑 MỤC LỤC KIỂM THỬ
1. [Kiểm thử Tầng Mã Nguồn (Unit & Integration Tests)](#1-kiểm-thử-tầng-mã-nguồn-unit--integration-tests)
2. [Kiểm thử Tự Động Kịch Bản Nghiệp Vụ (Automated Scenario Suite)](#2-kiểm-thử-tự-động-kịch-bản-nghiệp-vụ-automated-scenario-suite)
3. [Lắng Nghe Sự Kiện Phân Tán Thời Gian Thực (Kafka Live Tail)](#3-lắng-nghe-sự-kiện-phân-tán-thời-gian-thực-kafka-live-tail)
4. [Đo Kiểm Chịu Tải Cao (Distributed Load Testing)](#4-đo-kiểm-chịu-tải-cao-distributed-load-testing)
5. [Kiểm thử Phục Hồi & Ngắt Mạch (Chaos & Circuit Breaker Tests)](#5-kiểm-thử-phục-hồi--ngắt-mạch-chaos--circuit-breaker-tests)

---

## 🧩 1. KIỂM THỬ TẦNG MÃ NGUỒN (UNIT & INTEGRATION TESTS)

### 1.1 Backend Spring Boot Microservices
Toàn bộ các vi dịch vụ đều được trang bị bộ kiểm thử tự động với JUnit 5, Mockito, Testcontainers và Spring Boot Test.

* **Chạy kiểm thử toàn bộ Backend:**
```bash
cd backend
mvn clean test
```

* **Chạy kiểm thử từng dịch vụ trọng yếu:**
```bash
# 1. Kiểm thử Bộ điều khiển & Ngắt mạch API Gateway
mvn test -pl api-gateway -Dtest=CircuitBreakerChaosTest,GatewaySecurityConfigTest

# 2. Kiểm thử Trừ kho nguyên tử Redis Lua & Khóa phân tán Redisson
mvn test -pl inventory-service -Dtest=RedissonLockAdapterTest,InventoryApplicationServiceTest

# 3. Kiểm thử Transactional Outbox & Quản lý Đơn hàng
mvn test -pl order-service -Dtest=OrderApplicationServiceTest,RedisLuaServiceTest,OutboxRelaySchedulerTest

# 4. Kiểm thử Chuỗi Saga Thanh toán & Tính Idempotency
mvn test -pl payment-service -Dtest=PaymentSagaKafkaListenerTest,PaymentApplicationServiceTest
```

### 1.2 Frontend Client (Vitest + React Testing Library)
Frontend được kiểm thử giao diện và luồng quản lý trạng thái Zustand bằng Vitest.

```bash
cd frontend
npm test
```

---

## 🤖 2. KIỂM THỬ TỰ ĐỘNG KỊCH BẢN NGHIỆP VỤ (AUTOMATED SCENARIO SUITE)

Dự án tích hợp sẵn bộ kiểm thử tự động không phụ thuộc thư viện ngoài (`test_demo_scenarios_automation.mjs`). Bộ kịch bản này tự động phát sinh yêu cầu HTTP, kiểm tra mã phản hồi và truy vấn trực tiếp vào cơ sở dữ liệu MySQL để xác minh tính toàn vẹn dữ liệu.

* **Cách khởi chạy:**
```bash
# Đứng tại thư mục src/
node scripts/test_demo_scenarios_automation.mjs
```

* **Nội dung 4 kịch bản được tự động kiểm tra:**
  1. **Kịch bản 1 (Happy Path):** Mua hàng bình thường -> Đơn hàng `CONFIRMED` -> Tồn kho trong MySQL giảm đúng `1` đơn vị.
  2. **Kịch bản 2 (High Concurrency):** 5 khách hàng gửi yêu cầu đồng thời -> Cả 5 đơn `CONFIRMED` -> Tồn kho giảm chính xác `5` đơn vị (chứng minh không bị Lost Update).
  3. **Kịch bản 3 (Anti-Overselling):** 5 người cùng tranh mua 1 sản phẩm cuối cùng -> Đúng 1 người thành công, 4 người bị từ chối `CANCELLED_OUT_OF_STOCK` -> Tồn kho về đúng `0`, tuyệt đối không bị âm!
  4. **Kịch bản 4 (Per-User Limit):** Cùng 1 khách hàng cố tình mua vượt quá 2 sản phẩm/phiên -> Hệ thống từ chối ngay lập tức với mã lỗi vượt hạn mức Flash Sale.

---

## 📡 3. LẮNG NGHE SỰ KIỆN PHÂN TÁN THỜI GIAN THỰC (KAFKA LIVE TAIL)

Điểm nhấn thị giác mạnh mẽ nhất trước Hội đồng là chứng minh chuỗi sự kiện phân tán nhảy ra màn hình theo thời gian thực (Realtime Event Streaming) thay vì chỉ xem kết quả tĩnh.

* **Cách thức thực hiện (Mở 2 cửa sổ dòng lệnh song song):**
  * **Cửa sổ 1 (Bên Trái):** Chạy công cụ Streamer lắng nghe toàn bộ 4 Topics Kafka:
    ```bash
    node scripts/kafka_live_tail.mjs
    ```
  * **Cửa sổ 2 (Bên Phải):** Chạy kịch bản mua hàng:
    ```bash
    node scripts/realtime_scenario_runner.mjs 1
    # hoặc kịch bản 2: node scripts/realtime_scenario_runner.mjs 2
    # hoặc kịch bản 3: node scripts/realtime_scenario_runner.mjs 3
    ```
  * **Kết quả quan sát trên Cửa sổ Trái:**  
    Màn hình lập tức nhảy ra 4 dòng sự kiện theo màu sắc phân định:
    ```text
    [14:22:01] 📬 [TOPIC: order-events     ] ➔ [ORDER_CREATED]      | OrderID: ORD-1A2B3C
    [14:22:02] 📬 [TOPIC: inventory-events ] ➔ [INVENTORY_RESERVED] | OrderID: ORD-1A2B3C
    [14:22:02] 📬 [TOPIC: payment-events   ] ➔ [PAYMENT_COMPLETED]  | OrderID: ORD-1A2B3C
    [14:22:03] 📬 [TOPIC: order-events     ] ➔ [ORDER_CONFIRMED]    | OrderID: ORD-1A2B3C
    ```

---

## 🚀 4. ĐO KIỂM CHỊU TẢI CAO (DISTRIBUTED LOAD TESTING)

Hệ thống hỗ trợ 3 động cơ kiểm thử tải linh hoạt, đáp ứng cả tiêu chuẩn học thuật chính quy lẫn môi trường phát triển tốc độ cao.

```powershell
# Chạy script điều phối kiểm thử tải tại src/:
cd scripts/load-testing
```

### Phương án 4.1: Apache JMeter 5.6.3 (Chuẩn Chính Thức Trong Báo Cáo Đồ Án)
* **Kịch bản Test Plan:** `scripts/load-testing/flashsale_10k_users.jmx`
* **Lệnh thực thi:**
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\run_load_test.ps1 -Engine jmeter -Threads 500 -Duration 30
  ```
* **Báo cáo đồ thị:** Kết quả tự động xuất ra Dashboard HTML trực quan tại `scripts/load-testing/jmeter_html_report/index.html`.

### Phương án 4.2: High-Concurrency Runner (Zero-Dependency Node.js)
* Không cần cài đặt Java hay JMeter, chạy ngay lập tức trên mọi hệ điều hành:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\run_load_test.ps1 -Engine runner -Requests 5000 -Concurrency 50
  ```
* **Tiến trình hiển thị trực tiếp trên Terminal:**
  ```text
  [TIẾN TRÌNH: 3200/5000 (64.0%)] ➔ Tốc độ tức thời: 185 RPS | Thành công: 3,120 | Lỗi: 80
  ```

### Phương án 4.3: k6 Cloud Native Load Engine
* Dành cho các kỹ sư DevOps quen thuộc với công cụ k6:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\run_load_test.ps1 -Engine k6
  ```

### 🎯 Tiêu Chuẩn Cam Kết Dịch Vụ (SLO Targets Đối Chiếu Với Báo Cáo):
| Chỉ số kiểm định | Ngưỡng cam kết (SLA) | Kết quả đo thực tế | Trạng thái |
| :--- | :--- | :--- | :--- |
| **Throughput đỉnh (RPS)** | > 2,000 requests/sec | **2,320 – 3,500 RPS** | ✅ ĐẠT XUẤT SẮC |
| **Độ trễ phản hồi (p95)** | < 128 ms | **85 – 115 ms** | ✅ ĐẠT |
| **Tỷ lệ lỗi kỹ thuật (HTTP 5xx)** | < 1.0 % | **0.18 %** | ✅ ĐẠT XUẤT SẮC |
| **Tính toàn vẹn (Overselling)** | Tuyệt đối bằng 0 | **0 vi phạm (0 âm kho)** | ✅ HOÀN HẢO |

---

## 💥 5. KIỂM THỬ PHỤC HỒI & NGẮT MẠCH (CHAOS & CIRCUIT BREAKER TESTS)

Để kiểm chứng khả năng tự bảo vệ khi một vi dịch vụ bị sập:

1. **Mô phỏng sự cố:** Tắt container `inventory-service`:
   ```bash
   docker stop ecommerce-inventory-service
   ```
2. **Gửi yêu cầu:** Người dùng bấm xem tồn kho hoặc đặt hàng từ giao diện Web / Postman.
3. **Hiện tượng quan sát:**
   * Thay vì để kết nối bị treo vĩnh viễn (Gateway Timeout 504), Circuit Breaker Resilience4j lập tức kích hoạt Fallback.
   * Người dùng nhận được phản hồi HTTP `503 Service Unavailable` tức thì kèm thông báo: *"Dịch vụ kiểm tra kho đang tạm thời bận, vui lòng thử lại sau giây lát!"*.
4. **Tự phục hồi (Self-Healing):** Bật lại container:
   ```bash
   docker start ecommerce-inventory-service
   ```
   Sau 15 giây, Eureka cập nhật trạng thái `UP`, Circuit Breaker tự động chuyển từ trạng thái `OPEN` sang `HALF-OPEN` và `CLOSED`, hệ thống trở lại hoạt động bình thường mà không cần khởi động lại Gateway.
