# HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ HỖ TRỢ FLASH SALE TẢI CAO

Dự án Đồ án Tốt nghiệp E-Commerce Flash Sale được thiết kế theo kiến trúc Microservices phân rã, hỗ trợ xử lý tải cao bất đồng bộ, bảo mật bằng **OAuth2 PKCE (Keycloak)** và tự động vận hành CI/CD lên máy chủ **Ubuntu Linux Server**.

---

## 📁 CẤU TRÚC THƯ MỤC NGUỒN CHUẨN (CLEAN CODEBASE)

```text
src/
├── .github/
│   └── workflows/
│       └── ci-cd.yml             # Flow tự động Build Docker & Deploy lên Ubuntu Server
├── .gitignore                    # Bộ lọc file rác chuẩn cho Java, Node, Docker, IDEs
├── docker-compose.yml            # Điều phối MySQL 8.0, Keycloak, Redis, Mongo, Kafka
├── README.md                     # Hướng dẫn chạy & vận hành
├── frontend/                     # Mã nguồn Web Client (React 18 + TS + Vite + Zustand)
│   ├── Dockerfile
│   ├── package.json
│   └── src/
└── backend/                      # Các Spring Boot Microservices
    ├── eureka-server/            # Service Discovery (Port 8761)
    ├── api-gateway/              # Spring Cloud Gateway (Port 8080)
    ├── product-service/          # Catalog Service (MongoDB)
    ├── inventory-service/        # Inventory & Flash Sale (MySQL 8.0 + Redisson)
    ├── order-service/            # Order Service (MySQL 8.0 + Kafka)
    ├── payment-service/          # Payment Service (MySQL 8.0)
    └── notification-service/     # WebSocket Push Notification (Kafka Consumer)
```

---

## 🛠 HƯỚNG DẪN KHỞI CHẠY DỰ ÁN CỤC BỘ (LOCAL DEVELOPMENT)

### 1. Khởi tạo Hạ tầng Docker:
```bash
# Đứng tại thư mục src/
docker compose up -d
```

### 2. Cấu hình Keycloak Admin Server:
* Truy cập: `http://localhost:8180` (Tài khoản: `admin` / Mật khẩu: `adminpassword`).
* Tạo Realm: `ecommerce-realm`.
* Tạo Client: `ecommerce-frontend` (Public Client, PKCE Enabled).

---

## 🚀 QUY TRÌNH DEPLOY TỰ ĐỘNG (CI/CD GITHUB ACTIONS)

1. Đưa toàn bộ nội dung trong thư mục `src/` làm gốc của Repository trên GitHub (`git init` inside `src/` hoặc đẩy thư mục `src/` lên Repo).
2. Thiết lập các **Secrets** trong GitHub Repository Settings (`Settings -> Secrets and variables -> Actions`):
   - `SERVER_HOST`: IP máy chủ Ubuntu Server của bạn.
   - `SERVER_USER`: Username SSH (`root` hoặc `ubuntu`).
   - `SSH_PRIVATE_KEY`: Khóa SSH riêng tư để kết nối máy chủ.
3. Thực hiện Push code:
   ```bash
   git add .
   git commit -m "feat: setup clean codebase and CI/CD workflow"
   git push origin main
   ```
4. GitHub Actions sẽ tự động Build Docker Image, đẩy lên **GitHub Container Registry (GHCR.io)** và SSH vào máy chủ Ubuntu của bạn để `docker compose pull && docker compose up -d` tự động!
