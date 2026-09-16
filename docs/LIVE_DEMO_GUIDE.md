# 🎓 KỊCH BẢN TRÌNH CHIẾU DEMO THỰC CHIẾN (LIVE DEMO PLAYBOOK)

Cẩm nang chuẩn bị cho buổi thuyết minh và bảo vệ đồ án trước Hội đồng chấm thi hoặc người đánh giá kỹ thuật.

---

## 📌 1. BẢNG ĐIỀU HƯỚNG TRUY CẬP HỆ THỐNG (BOOKMARK SẴN TRÊN TRÌNH DUYỆT)

| Phân hệ / Dịch vụ | Địa chỉ Web (URL) | Tài khoản / Mật khẩu | Điểm nhấn trình chiếu |
| :--- | :--- | :--- | :--- |
| **Giao diện Khách hàng (Web Client)** | `http://localhost:3000` | `customer` / `password` | Demo săn Flash Sale, giỏ hàng, đặt hàng & xem tiến trình Saga realtime |
| **Eureka Service Discovery** | `http://localhost:8761` | *Không cần mật khẩu* | Chứng minh 8 microservices đang chạy và đăng ký trạng thái `UP` |
| **Keycloak IAM Admin Console** | `http://localhost:8180` | `admin` / `adminpassword` | Quản trị định danh tập trung, cấu hình OIDC PKCE (S256), Realm Roles |
| **Grafana Monitoring Dashboard** | `http://localhost:3001` | `admin` / `admin123456` | Dashboard đo tải thời gian thực: RPS, Latency p95, HTTP Codes, DB Pool |
| **Prometheus Metrics** | `http://localhost:9090` | *Không cần mật khẩu* | Giám sát Actuator Metrics scraping định kỳ từ các Spring Boot instances |
| **Jaeger Distributed Tracing** | `http://localhost:16686` | *Không cần mật khẩu* | Quan sát Span Tree chuỗi phân tán xuyên suốt các microservices |

---

## 🎯 2. KỊCH BẢN DEMO 1: KHÁCH HÀNG SĂN FLASH SALE THÀNH CÔNG (HAPPY PATH)

### Lời mở đầu gợi ý:
> *"Kính thưa Thầy/Cô, sau đây em xin phép thực hiện kịch bản demo luồng người dùng săn mua một mặt hàng trong phiên Flash Sale cao điểm. Hệ thống kích hoạt chuỗi xử lý bất đồng bộ Saga Choreography kết hợp Redis Lua Script để xử lý hàng ngàn đơn hàng đồng thời mà không xảy ra nghẽn cơ sở dữ liệu hay bán âm kho."*

### Thao tác trên Web:
1. **Trang chủ & Countdown:**
   * Mở `http://localhost:3000`.
   * Thuyết minh: Bộ đếm thời gian thực (Countdown Timer) của phiên Flash Sale và thanh tiến độ tồn kho (Stock Progress Bar) đổi màu linh hoạt theo số lượng hàng.
2. **Đăng nhập qua Keycloak OIDC PKCE:**
   * Bấm nút **"Đăng nhập"** -> Chuyển hướng sang trang đăng nhập Keycloak.
   * Đăng nhập tài khoản: `customer` / `password`.
   * Thuyết minh: Chuẩn OpenID Connect kết hợp PKCE (Proof Key for Code Exchange). Phía React hoàn toàn không lưu trữ Client Secret.
3. **Thêm giỏ hàng & Đặt hàng:**
   * Thêm sản phẩm Flash Sale vào giỏ, nhập mã giảm giá `FLASHSALE50`.
   * Bấm **"XÁC NHẬN ĐẶT HÀNG NGAY"**.
4. **Hiệu ứng Saga Queue Modal:**
   * Modal hàng chờ phân tán xuất hiện, hiển thị 4 bước xử lý trực quan:
     * **Bước 1 (Tiếp nhận):** Order Service lưu `PENDING` vào Transactional Outbox.
     * **Bước 2 (Giữ kho O(1)):** Redis Lua Script trừ kho nguyên tử, Inventory Service giữ chỗ.
     * **Bước 3 (Thanh toán):** Payment Service xử lý thanh toán đảm bảo tính Idempotency.
     * **Bước 4 (Thông báo):** Notification Service bắn tin STOMP qua WebSocket về trình duyệt.
   * Modal chuyển sang tích xanh: **"ĐẶT HÀNG THÀNH CÔNG!"** kèm mã đơn hàng `ORD-XXXXXX`.

---

## ⚡ 3. KỊCH BẢN DEMO 2: CHỐNG BÁN VƯỢT KHO & BÙ TRỪ SAGA

### Tình huống 2.1: Chống mua vượt quá hạn mức Flash Sale (Max 2 sản phẩm/khách)
1. Dùng cùng tài khoản `customer`, bấm đặt mua tiếp sản phẩm đó với số lượng 2 cái.
2. Hệ thống chặn ngay lập tức và hiển thị: *"Bạn đã vượt quá hạn mức mua tối đa trong phiên Flash Sale!"*.
3. Thuyết minh: Thuật toán kiểm soát hạn mức được thực thi trực tiếp trong Redis Lua Script nguyên tử O(1).

### Tình huống 2.2: Giao dịch Bù trừ Saga khi Hủy đơn (Compensating Transaction)
1. Trong form Checkout, chọn phương thức hủy hoặc để đơn hàng quá hạn thanh toán.
2. Đơn hàng chuyển sang trạng thái: **`CANCELLED`**.
3. Thuyết minh: Khi có sự cố thanh toán, sự kiện bù trừ `OrderCancelledEvent` được phát lên Kafka topic `order-events`. Inventory Service lắng nghe và tự động hoàn trả đúng số lượng tồn kho (Compensating Action).

---

## 🔴 4. KỊCH BẢN DEMO 3: TRỰC QUAN HÓA SỰ KIỆN PHÂN TÁN BẰNG TERMINAL

Mở 2 cửa sổ PowerShell song song để Hội đồng nhìn thấy dòng dữ liệu di chuyển tuần tự:

* **Cửa sổ 1 (Trái):**
  ```powershell
  node scripts/kafka_live_tail.mjs
  ```
* **Cửa sổ 2 (Phải):**
  ```powershell
  # Chạy tình huống 5 người cùng tranh mua 1 sản phẩm còn lại duy nhất
  node scripts/realtime_scenario_runner.mjs 3
  ```
* **Hiện tượng quan sát:**
  * Cửa sổ Trái nhảy ra 1 thông điệp xanh `[INVENTORY_RESERVED]` và 4 thông điệp đỏ `[INVENTORY_RESERVATION_FAILED]`.
  * Cửa sổ Phải in bảng đối soát: Tồn kho trong MySQL giảm từ 1 về đúng **0** (**tuyệt đối không âm `-1`, `-2`**).

---

## 📊 5. KỊCH BẢN DEMO 4: ĐO TẢI CHỊU LỰC TRÊN GRAFANA

1. **Mở Grafana (`http://localhost:3001`):**
   * Đăng nhập: `admin` / `admin123456`.
   * Mở Dashboard: **Flash Sale Microservices Overview**. Chọn `Last 5 minutes`, auto-refresh `5s`.
2. **Kích hoạt Bắn Tải:**
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\load-testing\run_load_test.ps1 -Engine runner -Requests 5000 -Concurrency 50
   ```
3. **Thuyết minh các Panel trên Dashboard:**
   * **Global RPS:** Biểu đồ vọt lên hình ngọn sóng đạt đỉnh từ 2.000 đến 3.500 requests/giây.
   * **Response Time (p95):** Duy trì ổn định dưới 128ms.
   * **HTTP Error Rate (5xx):** Dưới 0.5% nhờ cơ chế Circuit Breaker của Resilience4j.
