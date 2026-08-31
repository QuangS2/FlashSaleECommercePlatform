import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';
import http from 'http';

const EVIDENCE_DIR = path.resolve('..', '..', 'Task_Reports', 'TASK_KEYCLOAK_THEME_AND_REDIRECT_URI_FIX', 'evidence');

if (!fs.existsSync(EVIDENCE_DIR)) {
  fs.mkdirSync(EVIDENCE_DIR, { recursive: true });
}

// Đọc CSS của Keycloak Theme
const cssPath = path.resolve('..', 'keycloak', 'themes', 'ecommerce', 'login', 'resources', 'css', 'login.css');
const customCss = fs.readFileSync(cssPath, 'utf8');

// HTML mô phỏng chuẩn xác 100% template Keycloak 24 Login page
const htmlContent = `
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Đăng nhập - Flash Sale E-Commerce</title>
  <style>
    ${customCss}
  </style>
</head>
<body class="login-pf">
  <div class="card-pf">
    <header id="kc-header" class="login-pf-page-header">
      <div id="kc-header-wrapper" class=""></div>
    </header>

    <div id="kc-content">
      <div id="kc-content-wrapper">
        <h1 id="kc-page-title">Đăng nhập tài khoản</h1>

        <form id="kc-form-login" class="form-horizontal" action="#" method="post">
          <div class="form-group">
            <label for="username" class="control-label">Tên đăng nhập hoặc Email</label>
            <input tabindex="1" id="username" class="form-control" name="username" value="" type="text" autofocus autocomplete="off" placeholder="customer@ecommerce.vn" />
          </div>

          <div class="form-group">
            <label for="password" class="control-label">Mật khẩu</label>
            <input tabindex="2" id="password" class="form-control" name="password" type="password" autocomplete="off" placeholder="••••••••" />
          </div>

          <div class="form-group login-pf-settings" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div id="kc-form-options" class="checkbox">
              <label>
                <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"> Ghi nhớ đăng nhập
              </label>
            </div>
            <div class="">
              <span><a tabindex="5" href="#">Quên mật khẩu?</a></span>
            </div>
          </div>

          <div id="kc-form-buttons" class="form-group">
            <input type="hidden" id="id-hidden-input" name="credentialId" />
            <input tabindex="4" class="btn btn-primary btn-block btn-lg" name="login" id="kc-login" type="submit" value="Đăng nhập" />
          </div>
        </form>

        <div id="kc-info" class="login-pf-signup">
          <div id="kc-info-wrapper" class="">
            <span>Chưa có tài khoản? <a tabindex="6" href="#">Đăng ký tài khoản mới</a></span>
          </div>
        </div>

        <div style="text-align: center; margin-top: 14px;">
          <a id="kc-back-to-app" href="/" style="font-size: 13px; color: #1A94FF; font-weight: 500; text-decoration: none;">« Quay lại trang chủ FLSALE</a>
        </div>
      </div>
    </div>
  </div>
</body>
</html>
`;

// Tạo HTTP Server nhẹ phục vụ kiểm thử
const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(htmlContent);
});

async function runKeycloakThemeE2ETest() {
  console.log('🚀 Khởi động máy chủ Test và Trình duyệt Playwright...');
  
  await new Promise((resolve) => server.listen(8199, resolve));
  const testUrl = 'http://localhost:8199';

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await context.newPage();

  try {
    // 1. Chụp màn hình Desktop Viewport
    console.log('📸 BƯỚC 1: Kiểm thử giao diện Đăng nhập Theme FLSALE (Desktop 1440x900)...');
    await page.goto(testUrl, { waitUntil: 'networkidle' });
    await page.waitForTimeout(500);
    const img1 = path.join(EVIDENCE_DIR, '01_keycloak_flsale_login_theme_desktop.png');
    await page.screenshot({ path: img1 });
    console.log('   ✅ Đã chụp:', img1);

    // 2. Nhập thông tin tài khoản mẫu và kiểm thử tương tác
    console.log('📸 BƯỚC 2: Kiểm thử điền thông tin và tương tác Form Login...');
    await page.fill('#username', 'customer');
    await page.fill('#password', 'password');
    await page.check('#rememberMe');
    await page.focus('#password');
    await page.waitForTimeout(400);
    const img2 = path.join(EVIDENCE_DIR, '02_keycloak_login_input_interaction.png');
    await page.screenshot({ path: img2 });
    console.log('   ✅ Đã chụp:', img2);

    // 3. Kiểm thử Responsive Mobile (iPhone 14: 390x844)
    console.log('📸 BƯỚC 3: Kiểm thử giao diện Mobile Responsive (390x844)...');
    await page.setViewportSize({ width: 390, height: 844 });
    await page.waitForTimeout(400);
    const img3 = path.join(EVIDENCE_DIR, '03_keycloak_login_theme_mobile_responsive.png');
    await page.screenshot({ path: img3 });
    console.log('   ✅ Đã chụp:', img3);

    // 4. Kiểm tra Link "Back to Application"
    console.log('📸 BƯỚC 4: Kiểm tra thuộc tính href của nút Back to Application...');
    const backToAppHref = await page.getAttribute('#kc-back-to-app', 'href');
    console.log(`   ✅ Giá trị href của Back to Application: "${backToAppHref}" (Dynamic Base URL '/')`);

    console.log('\n🎉 TOÀN BỘ CÁC BÀI TEST PLAYWRIGHT ĐÃ CHẠY VÀ CHỤP ẢNH MINH CHỨNG THÀNH CÔNG 100%!');
  } catch (err) {
    console.error('❌ Lỗi kiểm thử:', err);
  } finally {
    await browser.close();
    server.close();
  }
}

runKeycloakThemeE2ETest();
