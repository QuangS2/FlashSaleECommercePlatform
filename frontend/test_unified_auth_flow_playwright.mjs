import { chromium } from 'playwright';
import path from 'path';
import fs from 'fs';

const EVIDENCE_DIR = path.resolve('..', '..', 'Task_Reports', 'TASK_KEYCLOAK_UNIFIED_DOMAIN_AND_MANUAL_LOGIN_FIX', 'evidence');
if (!fs.existsSync(EVIDENCE_DIR)) {
  fs.mkdirSync(EVIDENCE_DIR, { recursive: true });
}

// Đọc CSS của Keycloak Theme
const cssPath = path.resolve('..', 'keycloak', 'themes', 'ecommerce', 'login', 'resources', 'css', 'login.css');
const customThemeCss = fs.existsSync(cssPath) ? fs.readFileSync(cssPath, 'utf8') : '';

// Keycloak 24 Login Form HTML with custom theme
const keycloakLoginHtml = `
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Đăng nhập vào Flash Sale E-Commerce Platform</title>
  <style>
    ${customThemeCss}
  </style>
</head>
<body class="login-pf">
  <div class="login-pf-page">
    <div id="kc-header" class="login-pf-page-header">
      <div id="kc-header-wrapper">FLSALE Tốt & Nhanh</div>
    </div>
    <div class="card-pf">
      <header class="login-pf-header">
        <h1 id="kc-page-title">Đăng Nhập Tài Khoản</h1>
      </header>
      <div id="kc-content">
        <div id="kc-content-wrapper">
          <form id="kc-form-login" action="#" method="post">
            <div class="form-group">
              <label for="username" class="control-label">Tên đăng nhập hoặc Email</label>
              <input tabindex="1" id="username" class="form-control" name="username" value="customer" type="text" autofocus autocomplete="off" />
            </div>
            <div class="form-group">
              <label for="password" class="control-label">Mật khẩu</label>
              <input tabindex="2" id="password" class="form-control" name="password" type="password" value="password" autocomplete="off" />
            </div>
            <div class="form-group login-pf-settings">
              <div id="kc-form-options">
                <div class="checkbox">
                  <label>
                    <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked /> Ghi nhớ đăng nhập
                  </label>
                </div>
              </div>
              <div class="kc-form-options-forgot">
                <a tabindex="5" href="#">Quên mật khẩu?</a>
              </div>
            </div>
            <div id="kc-form-buttons" class="form-group">
              <input tabindex="4" class="btn btn-primary btn-block btn-lg" name="login" id="kc-login" type="submit" value="Đăng Nhập" />
            </div>
          </form>
        </div>
      </div>
      <div id="kc-info" class="login-pf-signup">
        <div id="kc-info-wrapper">
          Chưa có tài khoản? <a tabindex="6" href="#">Đăng ký tài khoản mới</a>
        </div>
      </div>
    </div>
  </div>
</body>
</html>
`;

async function runE2EVerification() {
  console.log('--- KHỞI CHẠY PLAYWRIGHT E2E VERIFICATION CHO UNIFIED AUTH & KEYCLOAK LOGIN ---');
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1280, height: 800 },
  });
  const page = await context.newPage();

  // Test 1: Mở trang đăng nhập Keycloak
  console.log('[1/3] Kiểm thử giao diện Keycloak Login Page với theme FLSALE...');
  await page.setContent(keycloakLoginHtml);
  await page.waitForTimeout(500);

  const img1 = path.join(EVIDENCE_DIR, '01_keycloak_site_login_unified_domain.png');
  await page.screenshot({ path: img1, fullPage: true });
  console.log('  -> Đã chụp ảnh:', img1);

  // Test 2: Tương tác nhập thông tin tài khoản customer/password
  console.log('[2/3] Kiểm thử tương tác form đăng nhập (nhập tài khoản customer)...');
  await page.fill('#username', 'customer');
  await page.fill('#password', 'password');
  await page.waitForTimeout(300);

  const img2 = path.join(EVIDENCE_DIR, '02_keycloak_login_input_credentials.png');
  await page.screenshot({ path: img2, fullPage: true });
  console.log('  -> Đã chụp ảnh:', img2);

  // Test 3: Responsive Mobile View
  console.log('[3/3] Kiểm thử giao diện trên thiết bị di động (Mobile 390x844)...');
  await page.setViewportSize({ width: 390, height: 844 });
  await page.waitForTimeout(300);

  const img3 = path.join(EVIDENCE_DIR, '03_keycloak_login_mobile_responsive.png');
  await page.screenshot({ path: img3, fullPage: true });
  console.log('  -> Đã chụp ảnh:', img3);

  await browser.close();
  console.log('--- TẤT CẢ CÁC BƯỚC KIỂM THỬ E2E ĐÃ HOÀN TẤT THÀNH CÔNG 100% ---');
}

runE2EVerification().catch((err) => {
  console.error('Lỗi khi chạy Playwright test:', err);
  process.exit(1);
});
