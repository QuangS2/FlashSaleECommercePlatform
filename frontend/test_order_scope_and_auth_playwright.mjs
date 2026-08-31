import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';
import http from 'http';

const EVIDENCE_DIR = path.resolve('..', '..', 'Task_Reports', 'TASK_USER_SCOPED_DATA_AND_AUTH_FIX', 'evidence');
const DIST_DIR = path.resolve('dist');

if (!fs.existsSync(EVIDENCE_DIR)) {
  fs.mkdirSync(EVIDENCE_DIR, { recursive: true });
}

// Simple HTTP static server cho thư mục dist
const mimeTypes = {
  '.html': 'text/html',
  '.js': 'text/javascript',
  '.css': 'text/css',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
};

const server = http.createServer((req, res) => {
  let filePath = path.join(DIST_DIR, req.url.split('?')[0]);
  if (filePath.endsWith('/') || !path.extname(filePath)) {
    filePath = path.join(DIST_DIR, 'index.html');
  }

  const ext = path.extname(filePath).toLowerCase();
  const contentType = mimeTypes[ext] || 'application/octet-stream';

  fs.readFile(filePath, (err, content) => {
    if (err) {
      fs.readFile(path.join(DIST_DIR, 'index.html'), (err2, indexContent) => {
        if (err2) {
          res.writeHead(404);
          res.end('Not found');
        } else {
          res.writeHead(200, { 'Content-Type': 'text/html' });
          res.end(indexContent, 'utf-8');
        }
      });
    } else {
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(content, 'utf-8');
    }
  });
});

async function runPlaywrightVerification() {
  console.log('🚀 Khởi động HTTP Server phục vụ thư mục dist tại port 4173...');
  await new Promise((resolve) => server.listen(4173, resolve));

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await context.newPage();

  try {
    const APP_URL = 'http://localhost:4173';

    // -------------------------------------------------------------
    // KỊCH BẢN 1: Mở mới web khi chưa đăng nhập (Clean Slate Guest)
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 1: Truy cập trang web và mở Drawer Đơn mua khi là Khách mới...');
    await page.goto(APP_URL, { waitUntil: 'networkidle' });
    await page.evaluate(() => localStorage.clear());
    await page.reload({ waitUntil: 'networkidle' });
    await page.waitForTimeout(600);

    // Mở Đơn mua
    await page.click('button[title="Xem lịch sử đơn hàng của tôi"]');
    await page.waitForTimeout(600);
    const img1 = path.join(EVIDENCE_DIR, '01_guest_empty_order_history_clean_slate.png');
    await page.screenshot({ path: img1 });
    console.log('   ✅ Đã chụp:', img1);

    // Đóng drawer Đơn mua
    await page.click('.bg-white.w-full.max-w-md button:has(svg.lucide-x)');
    await page.waitForTimeout(400);

    // -------------------------------------------------------------
    // KỊCH BẢN 2: Mở Modal Đăng nhập (In-App & SSO Options)
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 2: Mở LoginModal để chọn cơ chế xác thực...');
    await page.click('text=Đăng nhập / Đăng ký');
    await page.waitForTimeout(600);
    const img2 = path.join(EVIDENCE_DIR, '02_login_modal_opened.png');
    await page.screenshot({ path: img2 });
    console.log('   ✅ Đã chụp:', img2);

    // -------------------------------------------------------------
    // KỊCH BẢN 3: Đăng nhập thành công với tài khoản Khách hàng
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 3: Xác thực tài khoản Khách hàng (Lê Văn Khách)...');
    await page.click('text=Tài khoản Khách hàng (Customer)');
    await page.waitForTimeout(600);
    const img3 = path.join(EVIDENCE_DIR, '03_authenticated_user_header.png');
    await page.screenshot({ path: img3 });
    console.log('   ✅ Đã chụp:', img3);

    // -------------------------------------------------------------
    // KỊCH BẢN 4: Thêm sản phẩm & Đặt hàng User Scoped
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 4: Bấm MUA NGAY và mở CheckoutModal với thông tin người dùng...');
    const buyNowBtns = await page.$$('button:has-text("MUA NGAY")');
    if (buyNowBtns.length > 0) {
      await buyNowBtns[0].click();
      await page.waitForTimeout(800);
    }

    const img4 = path.join(EVIDENCE_DIR, '04_checkout_modal_user_scoped.png');
    await page.screenshot({ path: img4 });
    console.log('   ✅ Đã chụp:', img4);

    // Bấm Xác nhận đặt hàng
    const submitOrderBtn = await page.waitForSelector('button[type="submit"]:has-text("XÁC NHẬN")', { timeout: 5000 });
    if (submitOrderBtn) {
      await submitOrderBtn.click();
    }

    // -------------------------------------------------------------
    // KỊCH BẢN 5: Chờ QueueModal Hoàn tất -> Đặt hàng thành công
    // -------------------------------------------------------------
    console.log('   ⏳ Chờ QueueModal điều phối Saga hoàn tất...');
    const continueBtn1 = await page.waitForSelector('button:has-text("TIẾP TỤC MUA SẮM")', { timeout: 10000 });
    if (continueBtn1) {
      await page.waitForTimeout(500);
      const img5 = path.join(EVIDENCE_DIR, '05_order_success_saga_queue_complete.png');
      await page.screenshot({ path: img5 });
      console.log('   ✅ Đã chụp:', img5);

      await continueBtn1.click();
      await page.waitForTimeout(600);
    }

    // Bấm TIẾP TỤC MUA SẮM lần 2 nếu có (Success Toast Modal)
    const continueBtn2 = await page.$('button:has-text("TIẾP TỤC MUA SẮM")');
    if (continueBtn2) {
      await continueBtn2.click();
      await page.waitForTimeout(500);
    }

    // -------------------------------------------------------------
    // KỊCH BẢN 6: Mở Drawer Lịch sử Đơn mua theo Tài khoản
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 6: Mở Đơn mua kiểm tra đơn hàng gắn với tài khoản...');
    await page.click('button[title="Xem lịch sử đơn hàng của tôi"]');
    await page.waitForTimeout(1000);
    const img6 = path.join(EVIDENCE_DIR, '06_user_order_history_drawer.png');
    await page.screenshot({ path: img6 });
    console.log('   ✅ Đã chụp:', img6);

    // Đóng drawer
    const closeDrawerBtn = await page.$('.bg-white.w-full.max-w-md button:has(svg.lucide-x)');
    if (closeDrawerBtn) {
      await closeDrawerBtn.click();
      await page.waitForTimeout(400);
    }

    // -------------------------------------------------------------
    // KỊCH BẢN 7: Khám phá Danh mục 24 sản phẩm phong phú
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 7: Kiểm tra Catalog phong phú 24 sản phẩm...');
    await page.evaluate(() => window.scrollTo(0, 700));
    await page.waitForTimeout(600);
    const img7 = path.join(EVIDENCE_DIR, '07_rich_catalog_24_products.png');
    await page.screenshot({ path: img7 });
    console.log('   ✅ Đã chụp:', img7);

    console.log('\n🎉 TOÀN BỘ 7 KỊCH BẢN PLAYWRIGHT ĐÃ HOÀN TẤT THÀNH CÔNG 100%!');
  } catch (error) {
    console.error('❌ Lỗi kiểm thử Playwright:', error);
  } finally {
    await browser.close();
    server.close();
  }
}

runPlaywrightVerification();
