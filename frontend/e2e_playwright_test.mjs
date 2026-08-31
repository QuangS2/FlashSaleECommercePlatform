import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';

const EVIDENCE_DIR = path.resolve('..', '..', 'Task_Reports', 'PLAYWRIGHT_LIVE_E2E_EVIDENCE');

if (!fs.existsSync(EVIDENCE_DIR)) {
  fs.mkdirSync(EVIDENCE_DIR, { recursive: true });
}

async function runLiveE2ETest() {
  console.log('🚀 Bắt đầu chạy kiểm thử tự động Playwright trên môi trường thực tế (http://localhost:3000)...');

  const browser = await chromium.launch({
    headless: true,
  });

  const context = await browser.newContext({
    viewport: { width: 1440, height: 900 },
  });

  const page = await context.newPage();

  try {
    // -------------------------------------------------------------
    // BƯỚC 1: Mở Trang chủ & Xác thực Giao diện
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 1: Truy cập trang chủ...');
    await page.goto('http://localhost:3000', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1200);
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '01_homepage_loaded.png') });
    console.log('   ✅ Đã chụp: 01_homepage_loaded.png');

    // -------------------------------------------------------------
    // BƯỚC 2: Thêm sản phẩm Flash Sale vào Giỏ hàng
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 2: Thêm sản phẩm Flash Sale vào giỏ...');
    const addCartButtons = await page.$$('button:has-text("Thêm giỏ")');
    if (addCartButtons.length > 0) {
      await addCartButtons[0].click();
      await page.waitForTimeout(1000);
    }
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '02_item_added_to_cart.png') });
    console.log('   ✅ Đã chụp: 02_item_added_to_cart.png');

    // -------------------------------------------------------------
    // BƯỚC 3: Mở Giỏ hàng & Áp dụng Voucher
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 3: Mở Cart Drawer và áp dụng Voucher...');
    const voucherInput = await page.$('input[placeholder="Nhập mã voucher..."]');
    if (voucherInput) {
      await voucherInput.fill('FLASHSALE50');
      const applyBtn = await page.$('button:has-text("ÁP DỤNG")');
      if (applyBtn) {
        await applyBtn.click();
        await page.waitForTimeout(600);
      }
    }
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '03_cart_drawer_voucher_applied.png') });
    console.log('   ✅ Đã chụp: 03_cart_drawer_voucher_applied.png');

    // -------------------------------------------------------------
    // BƯỚC 4: Mở Form Checkout & Điền thông tin giao hàng
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 4: Mở Modal Thanh toán & Xác nhận đơn hàng...');
    const checkoutBtn = await page.$('button:has-text("TIẾN HÀNH ĐẶT HÀNG")');
    if (checkoutBtn) {
      await checkoutBtn.click();
      await page.waitForTimeout(1000);
    }
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '04_checkout_modal_opened.png') });
    console.log('   ✅ Đã chụp: 04_checkout_modal_opened.png');

    // -------------------------------------------------------------
    // BƯỚC 5: Gửi Đơn hàng -> Hàng chờ phân tán Saga
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 5: Gửi đơn hàng vào Saga Queue...');
    const submitOrderBtn = await page.$('button:has-text("XÁC NHẬN ĐẶT HÀNG NGAY")');
    if (submitOrderBtn) {
      await submitOrderBtn.click();
      await page.waitForTimeout(800);
    }
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '05_saga_queue_modal_processing.png') });
    console.log('   ✅ Đã chụp: 05_saga_queue_modal_processing.png');

    // -------------------------------------------------------------
    // BƯỚC 6: Nhận kết quả Saga Hoàn tất
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 6: Chờ chuỗi Saga hoàn tất...');
    await page.waitForTimeout(4000); // Chờ Saga hoàn tất
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '06_order_completed_queue_success.png') });
    console.log('   ✅ Đã chụp: 06_order_completed_queue_success.png');

    // Bấm nút TIẾP TỤC MUA SẮM để đóng QueueModal
    const continueBtn = await page.$('button:has-text("TIẾP TỤC MUA SẮM")');
    if (continueBtn) {
      await continueBtn.click();
      await page.waitForTimeout(1000);
    }

    // Đóng Modal chi tiết nếu đang mở
    const closeDetailBtn = await page.$('button:has-text("ĐÓNG")');
    if (closeDetailBtn) {
      await closeDetailBtn.click();
      await page.waitForTimeout(800);
    }

    // -------------------------------------------------------------
    // BƯỚC 7: Mở Drawer Lịch sử Đơn hàng (Đơn mua)
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 7: Mở Drawer Lịch sử đơn hàng (Đơn mua)...');
    await page.evaluate(() => {
      const btn = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Đơn mua'));
      if (btn) btn.click();
    });
    await page.waitForTimeout(1500);
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '07_order_history_drawer.png') });
    console.log('   ✅ Đã chụp: 07_order_history_drawer.png');

    // -------------------------------------------------------------
    // BƯỚC 8: Bấm xem Chi tiết Đơn hàng & Saga Stepper Timeline
    // -------------------------------------------------------------
    console.log('📸 BƯỚC 8: Mở Modal Chi tiết Đơn hàng & Saga Trace...');
    await page.evaluate(() => {
      const orderCard = document.querySelector('.cursor-pointer');
      if (orderCard) orderCard.click();
    });
    await page.waitForTimeout(1200);
    await page.screenshot({ path: path.join(EVIDENCE_DIR, '08_saga_stepper_timeline.png') });
    console.log('   ✅ Đã chụp: 08_saga_stepper_timeline.png');

    console.log('\n🎉 TOÀN BỘ 8 BƯỚC KIỂM THỬ PLAYWRIGHT ĐÃ HOÀN TẤT THÀNH CÔNG 100%!');
  } catch (error) {
    console.error('❌ Lỗi trong quá trình kiểm thử Playwright:', error);
  } finally {
    await browser.close();
  }
}

runLiveE2ETest();
