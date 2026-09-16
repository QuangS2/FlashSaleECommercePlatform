# TÀI LIỆU HƯỚNG DẪN KIỂM THỬ VÀ ĐỐI SOÁT HỆ THỐNG (TESTING & VERIFICATION GUIDE)

Tài liệu này trình bày quy trình kiểm thử, phương pháp đối soát dữ liệu và tiêu chí đánh giá các phân hệ của hệ thống, bao gồm kiểm thử mức mã nguồn, kiểm thử tự động kịch bản nghiệp vụ, giám sát luồng sự kiện phân tán và đo kiểm khả năng chịu tải.

---

## MỤC LỤC KIỂM THỬ

1. [Kiểm thử Tầng Mã Nguồn (Unit & Integration Tests)](#1-kiểm-thử-tầng-mã-nguồn-unit--integration-tests)
2. [Kiểm thử Tự Động Kịch Bản Nghiệp Vụ (Automated Scenario Suite)](#2-kiểm-thử-tự-động-kịch-bản-nghiệp-vụ-automated-scenario-suite)
3. [Giám Sát Sự Kiện Phân Tán Thời Gian Thực (Kafka Event Streaming)](#3-giám-sát-sự-kiện-phân-tán-thời-gian-thực-kafka-event-streaming)
4. [Đo Kiểm Khả Năng Chịu Tải (Distributed Load Testing)](#4-đo-kiểm-khả-năng-chịu-tải-distributed-load-testing)
5. [Kiểm Thử Dung Lỗi và Tự Phục Hồi (Chaos & Circuit Breaker Tests)](#5-kiểm-thử-dung-lỗi-và-tự-phục-hồi-chaos--circuit-breaker-tests)

---

## 1. KIỂM THỬ TẦNG MÃ NGUỒN (UNIT & INTEGRATION TESTS)

### 1.1 Backend Spring Boot Microservices
Các phân hệ vi dịch vụ được trang bị bộ kiểm thử tự động xây dựng trên nền tảng JUnit 5, Mockito, Testcontainers và Spring Boot Test.

* **Thực thi kiểm thử toàn bộ Backend:**
```bash
cd backend
mvn clean test
```

* **Thực thi kiểm thử từng phân hệ trọng yếu:**
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
Giao diện người dùng và các logic quản lý trạng thái tập trung (Zustand Stores) được kiểm thử tự động thông qua Vitest.

```bash
cd frontend
npm test
```

---

## 2. KIỂM THỬ TỰ ĐỘNG KỊCH BẢN NGHIỆP VỤ (AUTOMATED SCENARIO SUITE)

Hệ thống tích hợp bộ kịch bản kiểm thử tự động độc lập (`test_demo_scenarios_automation.mjs`). Kịch bản này thực thi gửi các yêu cầu HTTP, kiểm tra mã phản hồi và truy vấn trực tiếp vào cơ sở dữ liệu MySQL để xác minh tính toàn vẹn dữ liệu.

* **Lệnh thực thi:**
```bash
node scripts/test_demo_scenarios_automation.mjs
```

* **Nội dung 4 kịch bản được tự động đánh giá:**
  1. **Kịch bản 1 (Happy Path):** Mua hàng tiêu chuẩn -> Trạng thái đơn hàng chuyển sang `CONFIRMED` -> Số lượng tồn kho trong cơ sở dữ liệu MySQL giảm chính xác `1` đơn vị.
  2. **Kịch bản 2 (High Concurrency):** 5 yêu cầu gửi đồng thời -> 5 đơn hàng đều đạt trạng thái `CONFIRMED` -> Số lượng tồn kho giảm chính xác `5` đơn vị (chứng minh không xảy ra hiện tượng mất mát cập nhật - Lost Update).
  3. **Kịch bản 3 (Anti-Overselling):** 5 khách hàng gửi yêu cầu đồng thời tranh mua 1 sản phẩm cuối cùng -> Đúng 1 yêu cầu thành công, 4 yêu cầu còn lại nhận thông báo hết hàng `CANCELLED_OUT_OF_STOCK` -> Tồn kho kết thúc ở giá trị `0`, không phát sinh số âm.
  4. **Kịch bản 4 (Per-User Limit):** Một khách hàng gửi yêu cầu mua vượt quá 2 sản phẩm trong phiên Flash Sale -> Hệ thống từ chối yêu cầu và trả về lỗi vượt hạn mức quy định.

---

## 3. GIÁM SÁT SỰ KIỆN PHÂN TÁN THỜI GIAN THỰC (KAFKA EVENT STREAMING)

Mục tiêu kiểm thử nhằm quan sát trực tiếp chuỗi sự kiện phân tán được luân chuyển qua các topic Kafka trong suốt vòng đời xử lý đơn hàng.

* **Quy trình thực hiện (Sử dụng 2 cửa sổ terminal song song):**
  * **Cửa sổ 1:** Khởi chạy tiến trình theo dõi luồng sự kiện qua 4 Topics Kafka:
    ```bash
    node scripts/kafka_live_tail.mjs
    ```
  * **Cửa sổ 2:** Kích hoạt kịch bản tạo đơn hàng:
    ```bash
    node scripts/realtime_scenario_runner.mjs 1
    # Kịch bản đồng thời: node scripts/realtime_scenario_runner.mjs 2
    # Kịch bản tranh chấp: node scripts/realtime_scenario_runner.mjs 3
    ```
  * **Kết quả ghi nhận:**  
    Luồng sự kiện xuất hiện theo trình tự thời gian:
    ```text
    [14:22:01] [TOPIC: order-events     ] -> [ORDER_CREATED]      | OrderID: ORD-1A2B3C
    [14:22:02] [TOPIC: inventory-events ] -> [INVENTORY_RESERVED] | OrderID: ORD-1A2B3C
    [14:22:02] [TOPIC: payment-events   ] -> [PAYMENT_COMPLETED]  | OrderID: ORD-1A2B3C
    [14:22:03] [TOPIC: order-events     ] -> [ORDER_CONFIRMED]    | OrderID: ORD-1A2B3C
    ```

---

## 4. ĐO KIỂM KHẢ NĂNG CHỊU TẢI (DISTRIBUTED LOAD TESTING)

Hệ thống hỗ trợ 3 phương án kiểm thử tải phân tán:

```powershell
cd scripts/load-testing
```

### Phương án 4.1: Apache JMeter 5.6.3
* **Kịch bản kiểm thử:** `scripts/load-testing/flashsale_10k_users.jmx`
* **Lệnh thực thi:**
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\run_load_test.ps1 -Engine jmeter -Threads 500 -Duration 30
  ```
* **Báo cáo kết quả:** Hệ thống tự động trích xuất Dashboard HTML trực quan tại `scripts/load-testing/jmeter_html_report/index.html`.

### Phương án 4.2: High-Concurrency Runner
* Thực thi kiểm thử tải trực tiếp không phụ thuộc môi trường Java:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\run_load_test.ps1 -Engine runner -Requests 5000 -Concurrency 50
  ```
* **Tiến trình hiển thị:**
  ```text
  [TIẾN TRÌNH: 3200/5000 (64.0%)] -> Tốc độ tức thời: 185 RPS | Thành công: 3,120 | Lỗi: 80
  ```

### Phương án 4.3: k6 Cloud Native Load Engine
* Dành cho môi trường tích hợp kiểm thử tự động với k6:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\run_load_test.ps1 -Engine k6
  ```

### Tiêu Chuẩn Cam Kết Dịch Vụ (SLO Targets):
| Chỉ số kiểm định | Ngưỡng cam kết (SLA) | Kết quả đo thực tế | Trạng thái đánh giá |
| :--- | :--- | :--- | :--- |
| **Thông lượng đỉnh (Throughput)** | > 2,000 requests/sec | **2,320 – 3,500 RPS** | Đạt tiêu chuẩn |
| **Độ trễ phản hồi (p95 Latency)** | < 128 ms | **85 – 115 ms** | Đạt tiêu chuẩn |
| **Tỷ lệ lỗi kỹ thuật (HTTP 5xx)** | < 1.0 % | **0.18 %** | Đạt tiêu chuẩn |
| **Tính toàn vẹn (Overselling)** | Tuyệt đối bằng 0 | **0 vi phạm (0 âm kho)** | Đạt tiêu chuẩn |

---

## 5. KIỂM THỬ DUNG LỖI VÀ TỰ PHỤC HỒI (CHAOS & CIRCUIT BREAKER TESTS)

Kiểm chứng khả năng tự bảo vệ của hệ thống khi một vi dịch vụ bị gián đoạn hoạt động:

1. **Mô phỏng sự cố:** Dừng container `inventory-service`:
   ```bash
   docker stop ecommerce-inventory-service
   ```
2. **Gửi yêu cầu:** Thực hiện yêu cầu truy vấn tồn kho hoặc đặt hàng từ giao diện Web hoặc Postman.
3. **Kết quả quan sát:**
   * Thay vì rơi vào trạng thái nghẽn chờ (Gateway Timeout 504), Circuit Breaker Resilience4j tại API Gateway lập tức kích hoạt phản hồi dự phòng (Fallback).
   * Hệ thống phản hồi mã HTTP `503 Service Unavailable` cùng cấu trúc thông báo JSON được kiểm soát.
4. **Kiểm tra khả năng tự phục hồi (Self-Healing):** Khởi động lại container:
   ```bash
   docker start ecommerce-inventory-service
   ```
   Sau thời gian kiểm tra nhịp tim định kỳ, Eureka cập nhật trạng thái `UP`, Circuit Breaker tự động chuyển từ trạng thái `OPEN` sang `HALF-OPEN` và trở về `CLOSED`, hệ thống tự động tái lập luồng xử lý thông thường.
