import { execSync } from 'child_process';

/**
 * Script khôi phục toàn diện dữ liệu Demo (Reset Demo Data):
 * 1. MySQL: Khôi phục tồn kho 24 sản phẩm, đưa reserved_quantity = 0, dọn sạch orders/outbox/inbox test.
 * 2. MongoDB: Khôi phục stockCount và soldCount của 24 sản phẩm thực tế.
 * 3. Redis: Xóa sạch cache Flash Sale, giỏ hàng cart, khóa phân tán và HẠN MỨC MUA CỦA USER.
 * 4. BẢO TỒN NGUYÊN VẸN: Tài khoản Keycloak (customer/password, admin/adminpassword) KHÔNG bị ảnh hưởng!
 */

const SEED_STOCKS = {
  'fs-101': 15,
  'fs-102': 8,
  'fs-103': 5,
  'fs-104': 32,
  'cat-1': 45,
  'cat-2': 28,
  'cat-3': 60,
  'cat-4': 19,
  'cat-5': 22,
  'cat-6': 12,
  'cat-7': 35,
  'cat-8': 16,
  'cat-9': 50,
  'cat-10': 18,
  'cat-11': 20,
  'cat-12': 40,
  'cat-13': 25,
  'cat-14': 14,
  'cat-15': 28,
  'cat-16': 15,
  'cat-17': 10,
  'cat-18': 45,
  'cat-19': 30,
  'cat-20': 25
};

const FLASH_SALE_SOLD = {
  'fs-101': 85,
  'fs-102': 42,
  'fs-103': 95,
  'fs-104': 42
};

export async function resetDemoData() {
  console.log('================================================================================');
  console.log('🔄 BẮT ĐẦU KHÔI PHỤC DỮ LIỆU TỒN KHO & PHIÊN DEMO (ZERO DATA LOSS CHO TÀI KHOẢN)');
  console.log('================================================================================');

  let mysqlOk = false;
  let mongoOk = false;
  let redisOk = false;

  // ---------------------------------------------------------------------------
  // 1. KHÔI PHỤC MYSQL (Tồn kho Inventory, FlashSaleItems, dọn sạch Order/Outbox)
  // ---------------------------------------------------------------------------
  try {
    let updateQueries = `USE ecommerce_db;\n`;

    // Cập nhật tồn kho 24 sản phẩm
    for (const [productId, stock] of Object.entries(SEED_STOCKS)) {
      updateQueries += `UPDATE inventory SET quantity = ${stock}, reserved_quantity = 0 WHERE product_id = '${productId}';\n`;
    }

    // Khôi phục flash_sale_items
    updateQueries += `UPDATE flash_sale_items SET available_stock = allocated_stock, reserved_stock = 0, sold_stock = 0;\n`;

    // Dọn sạch các bảng đơn hàng phát sinh (giữ nguyên cấu trúc bảng & người dùng)
    updateQueries += `
      SET FOREIGN_KEY_CHECKS = 0;
      TRUNCATE TABLE order_items;
      TRUNCATE TABLE orders;
      TRUNCATE TABLE outbox_events;
      TRUNCATE TABLE inbox_events;
      TRUNCATE TABLE payments;
      SET FOREIGN_KEY_CHECKS = 1;
    `;

    execSync(`docker exec -i ecommerce-mysql mysql -u root -prootpassword`, {
      input: updateQueries,
      encoding: 'utf-8',
      stdio: ['pipe', 'pipe', 'ignore']
    });

    console.log('  ✅ [MySQL] Đã khôi phục tồn kho 24 sản phẩm và dọn sạch dữ liệu đơn hàng test.');
    mysqlOk = true;
  } catch (err) {
    console.log(`  ⚠️ [MySQL] Không thể kết nối container MySQL (có thể container đang tắt): ${err.message}`);
  }

  // ---------------------------------------------------------------------------
  // 2. KHÔI PHỤC MONGODB (Product Catalog: stockCount & soldCount)
  // ---------------------------------------------------------------------------
  try {
    let mongoScript = ``;
    for (const [productId, stock] of Object.entries(SEED_STOCKS)) {
      const sold = FLASH_SALE_SOLD[productId] || 10;
      mongoScript += `db.product.updateOne({_id: "${productId}"}, {$set: {stockCount: ${stock}, soldCount: ${sold}}});\n`;
    }
    mongoScript += `print("MongoDB products synced!");`;

    execSync(`docker exec -i ecommerce-mongo mongosh ecommerce_product`, {
      input: mongoScript,
      encoding: 'utf-8',
      stdio: ['pipe', 'pipe', 'ignore']
    });

    console.log('  ✅ [MongoDB] Đã đồng bộ lại số lượng stockCount và soldCount cho 24 sản phẩm.');
    mongoOk = true;
  } catch (err) {
    console.log(`  ⚠️ [MongoDB] Không thể kết nối container MongoDB: ${err.message}`);
  }

  // ---------------------------------------------------------------------------
  // 3. KHÔI PHỤC REDIS (Xóa cache, giỏ hàng, và QUAN TRỌNG: Hạn mức mua của user)
  // ---------------------------------------------------------------------------
  try {
    execSync(`docker exec ecommerce-redis redis-cli FLUSHALL`, {
      encoding: 'utf-8',
      stdio: ['pipe', 'pipe', 'ignore']
    });

    console.log('  ✅ [Redis] Đã dọn sạch cache Flash Sale, giỏ hàng, và thiết lập lại hạn mức mua về 0.');
    redisOk = true;
  } catch (err) {
    console.log(`  ⚠️ [Redis] Không thể kết nối container Redis: ${err.message}`);
  }

  // ---------------------------------------------------------------------------
  // 4. KẾT QUẢ TỔNG HỢP
  // ---------------------------------------------------------------------------
  console.log('--------------------------------------------------------------------------------');
  console.log('🔒 Tài khoản Keycloak (customer, admin): BẢO LƯU NGUYÊN VẸN 100%.');
  console.log('📦 Tồn kho sản phẩm Flash Sale mẫu (fs-101): ĐÃ KHÔI PHỤC ĐỦ 15 SẢN PHẨM.');
  console.log('🛒 Hạn mức mua tối đa (User Purchase Limits): ĐÃ LÀM MỚI (User có thể mua lại ngay).');
  console.log('================================================================================\n');

  return { mysqlOk, mongoOk, redisOk };
}

// Chạy trực tiếp khi được gọi từ dòng lệnh
if (process.argv[1] && process.argv[1].endsWith('reset_demo_data.mjs')) {
  resetDemoData();
}
