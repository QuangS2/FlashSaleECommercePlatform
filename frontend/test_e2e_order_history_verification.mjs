import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';

const EVIDENCE_DIR = path.resolve('..', '..', 'Task_Reports', 'TASK_USER_SCOPED_DATA_AND_AUTH_FIX', 'evidence');

if (!fs.existsSync(EVIDENCE_DIR)) {
  fs.mkdirSync(EVIDENCE_DIR, { recursive: true });
}

async function runOrderHistoryVerification() {
  console.log('🚀 Bắt đầu kịch bản kiểm thử E2E Playwright: Mua hàng -> Kiểm tra xuất hiện trong Đơn mua...');
  
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await context.newPage();

  try {
    const APP_URL = 'http://localhost:3000';
    console.log(`🌐 Truy cập ứng dụng tại: ${APP_URL}`);
    await page.goto(APP_URL, { waitUntil: 'domcontentloaded', timeout: 20000 });
    await page.waitForTimeout(2000);

    // 1. Chụp ảnh màn hình trang chủ
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '01_homepage_loaded.png'), fullPage: true });
    console.log('📸 Đã chụp: 01_homepage_loaded.png');

    // 2. Tìm nút "Mua ngay" trên sản phẩm đầu tiên của danh mục
    console.log('🛒 Tìm sản phẩm và bấm "Mua ngay"...');
    const buyNowBtn = page.locator('button:has-text("Mua ngay")').first();
    await buyNowBtn.waitFor({ state: 'visible', timeout: 10000 });
    await buyNowBtn.click();

    // 3. Chờ Modal Checkout xuất hiện
    console.log('📝 Điền thông tin vào Checkout Modal...');
    const checkoutModal = page.locator('text=XÁC NHẬN VÀ THANH TOÁN ĐƠN HÀNG');
    await checkoutModal.waitFor({ state: 'visible', timeout: 10000 });

    // Điền thông tin giao hàng
    await page.fill('input[placeholder="Ví dụ: Nguyễn Văn A"]', 'Lê Anh Quang');
    await page.fill('input[placeholder="Ví dụ: 0987654321"]', '0987654321');
    await page.fill('input[placeholder="Ví dụ: email@example.com"]', 'lequang_test@ecommerce.vn');
    await page.fill('input[placeholder="Số nhà, tên đường, phường/xã..."]', 'Số 123 Đường Nguyễn Huệ, Phường Bến Nghé');

    await page.screenshot({ path: path.join(EVIDENCE_DIR, '02_checkout_form_filled.png') });
    console.log('📸 Đã chụp: 02_checkout_form_filled.png');

    // 4. Nhấn nút "XÁC NHẬN ĐẶT HÀNG"
    console.log('💳 Nhấn nút "XÁC NHẬN ĐẶT HÀNG"...');
    const submitBtn = page.locator('button:has-text("XÁC NHẬN ĐẶT HÀNG")');
    await submitBtn.click();

    // 5. Chờ phản hồi tạo đơn hoàn tất
    await page.waitForTimeout(3000);
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '03_order_placed_result.png') });
    console.log('📸 Đã chụp: 03_order_placed_result.png');

    // Đóng bất kỳ modal nào đang che màn hình
    const detailModalClose = page.locator('div.fixed:has-text("CHI TIẾT ĐƠN HÀNG") button').first();
    if (await detailModalClose.isVisible({ timeout: 2000 }).catch(() => false)) {
      console.log('🚪 Đóng OrderDetailModal...');
      await detailModalClose.click({ force: true });
      await page.waitForTimeout(1000);
    }

    const continueBtn = page.locator('button:has-text("TIẾP TỤC MUA SẮM")');
    if (await continueBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
      console.log('🛍️ Đóng Toast modal...');
      await continueBtn.click({ force: true });
      await page.waitForTimeout(1000);
    }

    // 6. Nhấn vào nút "Đơn mua" trên Header
    console.log('📋 Nhấn vào nút "Đơn mua" trên Header để mở OrderHistoryDrawer...');
    const orderHistoryBtn = page.locator('button:has-text("Đơn mua")');
    await orderHistoryBtn.waitFor({ state: 'visible', timeout: 10000 });
    await orderHistoryBtn.click({ force: true });

    // 7. Chờ Drawer mở ra và tải danh sách đơn hàng
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '04_order_history_drawer_populated.png') });
    console.log('📸 Đã chụp: 04_order_history_drawer_populated.png');

    // Kiểm tra xem trong Drawer có đơn hàng xuất hiện không
    const orderItems = page.locator('div:has-text("ORD-")').last();
    await orderItems.waitFor({ state: 'visible', timeout: 10000 });
    const orderItemText = await orderItems.innerText();
    console.log(`✅ Đã tìm thấy đơn hàng trong "Đơn mua":\n${orderItemText.split('\n').slice(0, 4).join(' | ')}`);

    // 8. Bấm vào đơn hàng để mở chi tiết đơn hàng (OrderDetailModal)
    console.log('🔍 Bấm vào đơn hàng để kiểm tra OrderDetailModal...');
    await orderItems.click({ force: true });
    await page.waitForTimeout(1500);

    const orderDetailHeading = page.locator('text=CHI TIẾT ĐƠN HÀNG');
    await orderDetailHeading.waitFor({ state: 'visible', timeout: 10000 });

    await page.screenshot({ path: path.join(EVIDENCE_DIR, '05_order_detail_modal_stepper.png') });
    console.log('📸 Đã chụp: 05_order_detail_modal_stepper.png');

    console.log('🎉 Toàn bộ kịch bản kiểm thử Playwright đã hoàn tất 100% THÀNH CÔNG!');
  } catch (error) {
    console.error('❌ Lỗi trong quá trình kiểm thử Playwright:', error);
    await page.screenshot({ path: path.join(EVIDENCE_DIR, 'error_screenshot.png') });
    process.exit(1);
  } finally {
    await browser.close();
  }
}

runOrderHistoryVerification();
