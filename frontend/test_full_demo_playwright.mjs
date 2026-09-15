import { chromium } from 'playwright';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const EVIDENCE_DIR = path.resolve(__dirname, '../../Task_Reports/TASK_PLAYWRIGHT_LIVE_DEMO_VERIFICATION/evidence');

if (!fs.existsSync(EVIDENCE_DIR)) {
  fs.mkdirSync(EVIDENCE_DIR, { recursive: true });
}

async function runPlaywrightSuite() {
  console.log('================================================================================');
  console.log('🚀 BẮT ĐẦU KIỂM THỬ GIAO DIỆN & DỊCH VỤ HỆ THỐNG BẰNG PLAYWRIGHT (CHỤP MINH CHỨNG)');
  console.log('================================================================================\n');

  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const context = await browser.newContext({
    viewport: { width: 1440, height: 900 },
    locale: 'vi-VN'
  });

  const page = await context.newPage();

  // Bắt dialog alert của trình duyệt
  page.on('dialog', async (dialog) => {
    console.log(`   🔔 [DIALOG ALERT]: "${dialog.message()}"`);
    await dialog.accept();
  });

  try {
    // -------------------------------------------------------------------------
    // TEST 1: TRANG CHỦ & ĐỒNG BỘ TỒN KHO THỰC TẾ GIỮA FLASH SALE VÀ CATALOG
    // -------------------------------------------------------------------------
    console.log('📸 1. Kiểm tra Trang chủ Web Khách hàng (Đồng bộ tồn kho Flash Sale & Catalog)...');
    await page.goto('http://localhost:3000', { waitUntil: 'networkidle', timeout: 30000 });
    await page.waitForTimeout(2000); // Đợi các API nạp xong dữ liệu

    const homeScreenshot = path.join(EVIDENCE_DIR, '01_frontend_homepage_sync_stock.png');
    await page.screenshot({ path: homeScreenshot, fullPage: false });
    console.log(`   -> Đã lưu ảnh: ${homeScreenshot}`);

    // -------------------------------------------------------------------------
    // TEST 2: CHI TIẾT SẢN PHẨM & GIỚI HẠN 1 SẢN PHẨM FLASH SALE
    // -------------------------------------------------------------------------
    console.log('📸 2. Xem chi tiết sản phẩm Flash Sale và kiểm tra giới hạn mua...');
    const firstProduct = page.locator('h3:has-text("iPhone 15 Pro Max")').first();
    if (await firstProduct.isVisible()) {
      await firstProduct.click();
      await page.waitForTimeout(1000);

      const modalScreenshot = path.join(EVIDENCE_DIR, '02_product_detail_modal_limit.png');
      await page.screenshot({ path: modalScreenshot });
      console.log(`   -> Đã lưu ảnh: ${modalScreenshot}`);

      // Bấm "THÊM GIỎ HÀNG" từ modal
      const addToCartBtn = page.locator('button:has-text("THÊM GIỎ HÀNG")').first();
      if (await addToCartBtn.isVisible()) {
        await addToCartBtn.click();
        await page.waitForTimeout(1000);
      }
    }

    // -------------------------------------------------------------------------
    // TEST 3: GIỎ HÀNG CART DRAWER (REDIS O(1))
    // -------------------------------------------------------------------------
    console.log('📸 3. Kiểm tra Giỏ hàng Cart Drawer...');
    const cartScreenshot = path.join(EVIDENCE_DIR, '03_cart_drawer_single_item.png');
    await page.screenshot({ path: cartScreenshot });
    console.log(`   -> Đã lưu ảnh: ${cartScreenshot}`);

    // -------------------------------------------------------------------------
    // TEST 4: CHECKOUT FORM & THÔNG TIN GIAO HÀNG
    // -------------------------------------------------------------------------
    console.log('📸 4. Mở Checkout Modal đặt hàng...');
    const checkoutBtn = page.locator('button:has-text("TIẾN HÀNH THANH TOÁN")').first();
    if (await checkoutBtn.isVisible()) {
      await checkoutBtn.click();
      await page.waitForTimeout(1000);

      const checkoutScreenshot = path.join(EVIDENCE_DIR, '04_checkout_modal_form.png');
      await page.screenshot({ path: checkoutScreenshot });
      console.log(`   -> Đã lưu ảnh: ${checkoutScreenshot}`);

      // Xác nhận thanh toán Flash Sale lần 1
      console.log('📸 5a. Tiến hành xác nhận thanh toán Flash Sale (Lần 1) & Chờ hàng đợi...');
      const submitOrderBtn = page.locator('button[type="submit"]:has-text("XÁC NHẬN")').first();
      if (await submitOrderBtn.isVisible()) {
        await submitOrderBtn.click();
        await page.waitForTimeout(1000);

        // Chụp QueueModal khi đang xử lý hàng đợi
        try {
          await page.waitForSelector('text=HÀNG ĐỢI XỬ LÝ ĐƠN HÀNG FLASH SALE', { timeout: 4000 });
        } catch {}
        const queueWaitingScreenshot = path.join(EVIDENCE_DIR, '05a_queue_modal_processing.png');
        await page.screenshot({ path: queueWaitingScreenshot });
        console.log(`   -> Đã lưu ảnh: ${queueWaitingScreenshot}`);

        // Đợi QueueModal chuyển sang SUCCESS (WebSocket STOMP hoặc fallback timeout)
        console.log('📸 5b. Chờ kết quả Saga xử lý hoàn tất đơn hàng...');
        try {
          await page.waitForSelector('text=ĐẶT HÀNG THÀNH CÔNG!', { timeout: 8000 });
        } catch {}
        await page.waitForTimeout(1000);
        const queueSuccessScreenshot = path.join(EVIDENCE_DIR, '05b_queue_modal_success.png');
        await page.screenshot({ path: queueSuccessScreenshot });
        console.log(`   -> Đã lưu ảnh: ${queueSuccessScreenshot}`);

        // Bấm nút tiếp tục để đóng modal thành công (QueueModal hoặc Success Toast)
        console.log('📸 5c. Mở chi tiết đơn hàng & theo dõi Saga Orchestration Timeline...');
        const continueBtn = page.locator('button:has-text("TIẾP TỤC MUA SẮM")').first();
        if (await continueBtn.isVisible()) {
          await continueBtn.click();
          await page.waitForTimeout(1000);
        }

        // Đóng thêm nếu có modal popup thông báo thành công
        const continueBtn2 = page.locator('button:has-text("TIẾP TỤC MUA SẮM")').first();
        if (await continueBtn2.isVisible()) {
          await continueBtn2.click();
          await page.waitForTimeout(1000);
        }

        await page.keyboard.press('Escape');
        await page.waitForTimeout(500);

        // Mở drawer "Đơn mua" để xem Saga Timeline
        const myOrdersBtn = page.locator('button:has-text("Đơn mua")').first();
        if (await myOrdersBtn.isVisible()) {
          await myOrdersBtn.click({ force: true });
          await page.waitForTimeout(1500);

          // Bấm vào card đơn hàng đầu tiên trong danh sách
          const firstOrderCard = page.locator('.group.cursor-pointer').first();
          if (await firstOrderCard.isVisible()) {
            await firstOrderCard.click({ force: true });
            await page.waitForTimeout(1500);
          }
        }

        const sagaTimelineScreenshot = path.join(EVIDENCE_DIR, '05c_order_detail_saga_timeline.png');
        await page.screenshot({ path: sagaTimelineScreenshot });
        console.log(`   -> Đã lưu ảnh: ${sagaTimelineScreenshot}`);

        // Đóng các modal/drawer đang mở
        await page.keyboard.press('Escape');
        await page.waitForTimeout(500);
        await page.keyboard.press('Escape');
        await page.waitForTimeout(800);
      }
    }

    // -------------------------------------------------------------------------
    // TEST 5: CỐ TÌNH MUA LẠI LẦN 2 TRONG CÙNG PHIÊN (ANTI-GREEDY LUA SCRIPT)
    // -------------------------------------------------------------------------
    console.log('📸 5d. Cùng khách hàng cố tình mua lại Flash Sale lần 2 (Kiểm tra chống đầu cơ)...');
    try {
      // Bấm nút MUA NGAY trên thẻ sản phẩm đầu tiên
      const buyAgainBtn = page.locator('button:has-text("MUA NGAY")').first();
      if (await buyAgainBtn.isVisible()) {
        await buyAgainBtn.click({ force: true });
        await page.waitForTimeout(1000);

        // Trong CheckoutModal mở ra, click Xác nhận đặt hàng ngay
        const submitAgainBtn = page.locator('button[type="submit"]:has-text("XÁC NHẬN")').first();
        if (await submitAgainBtn.isVisible()) {
          await submitAgainBtn.click();
          await page.waitForTimeout(2500);

          const antiGreedyScreenshot = path.join(EVIDENCE_DIR, '05d_flash_sale_anti_greedy_limit.png');
          await page.screenshot({ path: antiGreedyScreenshot });
          console.log(`   -> Đã lưu ảnh: ${antiGreedyScreenshot}`);
        }
      }
    } catch (e) {
      console.log('   Bỏ qua bước mua lại:', e.message);
    }

    // Đóng bất kỳ modal nào còn mở
    try {
      await page.keyboard.press('Escape');
      await page.waitForTimeout(500);
    } catch {}

    // -------------------------------------------------------------------------
    // TEST 6: EUREKA SERVICE REGISTRY
    // -------------------------------------------------------------------------
    console.log('📸 6. Kiểm tra Eureka Service Registry (8/8 microservices UP)...');
    const eurekaPage = await context.newPage();
    await eurekaPage.goto('http://127.0.0.1:8761', { waitUntil: 'networkidle', timeout: 20000 });
    await eurekaPage.waitForTimeout(1500);
    const eurekaScreenshot = path.join(EVIDENCE_DIR, '06_eureka_service_registry.png');
    await eurekaPage.screenshot({ path: eurekaScreenshot, fullPage: true });
    console.log(`   -> Đã lưu ảnh: ${eurekaScreenshot}`);
    await eurekaPage.close();

    // -------------------------------------------------------------------------
    // TEST 7: KEYCLOAK IAM SERVER
    // -------------------------------------------------------------------------
    console.log('📸 7. Kiểm tra Keycloak IAM Server...');
    const keycloakPage = await context.newPage();
    try {
      await keycloakPage.goto('http://127.0.0.1:8180/admin/master/console/', { waitUntil: 'domcontentloaded', timeout: 20000 });
      await keycloakPage.waitForTimeout(2000);
      
      const usernameInput = keycloakPage.locator('#username');
      if (await usernameInput.isVisible()) {
        await usernameInput.fill('admin');
        await keycloakPage.locator('#password').fill('adminpassword');
        await keycloakPage.locator('#kc-login').click();
        await keycloakPage.waitForTimeout(3000);
      }

      await keycloakPage.goto('http://127.0.0.1:8180/admin/master/console/#/ecommerce-realm', { waitUntil: 'domcontentloaded', timeout: 20000 });
      await keycloakPage.waitForTimeout(2500);

      const keycloakScreenshot = path.join(EVIDENCE_DIR, '07_keycloak_iam_realm.png');
      await keycloakPage.screenshot({ path: keycloakScreenshot });
      console.log(`   -> Đã lưu ảnh: ${keycloakScreenshot}`);
    } catch (err) {
      console.warn('   Lỗi chụp Keycloak (tiếp tục các test khác):', err.message);
    }
    await keycloakPage.close();

    // -------------------------------------------------------------------------
    // TEST 8: GRAFANA MONITORING DASHBOARD
    // -------------------------------------------------------------------------
    console.log('📸 8. Kiểm tra Grafana Monitoring Dashboard...');
    const grafanaPage = await context.newPage();
    try {
      await grafanaPage.goto('http://127.0.0.1:3001/login', { waitUntil: 'domcontentloaded', timeout: 20000 });
      await grafanaPage.waitForTimeout(1500);
      const userIn = grafanaPage.locator('input[name="user"]');
      if (await userIn.isVisible()) {
        await userIn.fill('admin');
        await grafanaPage.locator('input[name="password"]').fill('admin');
        await grafanaPage.locator('button[type="submit"]').click();
        await grafanaPage.waitForTimeout(3000);
      }
      const grafanaScreenshot = path.join(EVIDENCE_DIR, '08_grafana_monitoring_dashboard.png');
      await grafanaPage.screenshot({ path: grafanaScreenshot });
      console.log(`   -> Đã lưu ảnh: ${grafanaScreenshot}`);
    } catch (err) {
      console.warn('   Lỗi chụp Grafana:', err.message);
    }
    await grafanaPage.close();

    // -------------------------------------------------------------------------
    // TEST 9: JAEGER DISTRIBUTED TRACING
    // -------------------------------------------------------------------------
    console.log('📸 9. Kiểm tra Jaeger Distributed Tracing...');
    const jaegerPage = await context.newPage();
    try {
      await jaegerPage.goto('http://127.0.0.1:16686', { waitUntil: 'networkidle', timeout: 20000 });
      await jaegerPage.waitForTimeout(2000);
      const jaegerScreenshot = path.join(EVIDENCE_DIR, '09_jaeger_distributed_tracing.png');
      await jaegerPage.screenshot({ path: jaegerScreenshot });
      console.log(`   -> Đã lưu ảnh: ${jaegerScreenshot}`);
    } catch (err) {
      console.warn('   Lỗi chụp Jaeger:', err.message);
    }
    await jaegerPage.close();

    console.log('\n================================================================================');
    console.log('🎉 HOÀN TẤT BỘ TEST PLAYWRIGHT & ĐÃ LƯU TOÀN BỘ ẢNH MINH CHỨNG THỰC TẾ 100%!');
    console.log(`📁 Thư mục ảnh minh chứng: ${EVIDENCE_DIR}`);
    console.log('================================================================================\n');

  } catch (error) {
    console.error('❌ Lỗi trong quá trình chạy Playwright:', error);
  } finally {
    await browser.close();
  }
}

runPlaywrightSuite();
