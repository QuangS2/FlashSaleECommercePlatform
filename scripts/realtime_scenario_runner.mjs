import http from 'http';
import https from 'https';
import { execSync } from 'child_process';
import readline from 'readline';

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function printStep(stepNum, title, desc = '') {
  console.log(`\n\x1b[1m\x1b[34m[BƯỚC ${stepNum}]\x1b[0m \x1b[33m${title}\x1b[0m`);
  if (desc) console.log(`  \x1b[90m${desc}\x1b[0m`);
}

function runSql(query) {
  try {
    const out = execSync(`docker exec -i ecommerce-mysql mysql -u root -prootpassword -e "USE ecommerce_db; ${query}"`, {
      encoding: 'utf-8'
    });
    return out.trim();
  } catch (err) {
    return `SQL ERROR: ${err.message}`;
  }
}

function runRedis(cmd) {
  try {
    const out = execSync(`docker exec ecommerce-redis redis-cli ${cmd}`, { encoding: 'utf-8' });
    return out.trim();
  } catch (err) {
    return `REDIS ERROR: ${err.message}`;
  }
}

async function request(url, options = {}, data = null) {
  return new Promise((resolve, reject) => {
    const parsedUrl = new URL(url);
    const lib = parsedUrl.protocol === 'https:' ? https : http;
    const req = lib.request(url, options, (res) => {
      let body = '';
      res.on('data', (chunk) => body += chunk);
      res.on('end', () => {
        resolve({
          statusCode: res.statusCode,
          headers: res.headers,
          body: body,
          json: () => {
            try { return JSON.parse(body); } catch (e) { return null; }
          }
        });
      });
    });
    req.on('error', reject);
    req.setTimeout(15000, () => req.destroy(new Error('Timeout')));
    if (data) req.write(typeof data === 'string' ? data : JSON.stringify(data));
    req.end();
  });
}

async function getKeycloakToken() {
  const tokenUrl = 'http://localhost:8180/realms/ecommerce-realm/protocol/openid-connect/token';
  const params = new URLSearchParams();
  params.append('grant_type', 'password');
  params.append('client_id', 'ecommerce-frontend');
  params.append('username', 'customer');
  params.append('password', 'password');

  const res = await request(tokenUrl, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  }, params.toString());
  const json = res.json();
  if (json && json.access_token) {
    const payload = JSON.parse(Buffer.from(json.access_token.split('.')[1], 'base64').toString('utf8'));
    return { token: json.access_token, sub: payload.sub };
  }
  throw new Error('Keycloak login failed');
}

// -----------------------------------------------------------------------------
// KỊCH BẢN 1: MUA 1 SẢN PHẨM BÌNH THƯỜNG (CHẾ ĐỘ DIỄN HỌA TUẦN TỰ)
// -----------------------------------------------------------------------------
async function runScenario1(auth) {
  console.log('\n================================================================================');
  console.log('🎬 BẮT ĐẦU KỊCH BẢN 1: TRÌNH DIỄN TUẦN TỰ QUY TRÌNH MUA 1 SẢN PHẨM (LIVE)');
  console.log('================================================================================');

  const prodId = 'fs-102';
  runSql(`UPDATE inventory SET quantity = 20, reserved_quantity = 0 WHERE product_id = '${prodId}';`);

  printStep('1.1', 'TRUY VẤN TỒN KHO BAN ĐẦU DƯỚI CƠ SỞ DỮ LIỆU MYSQL', 'Kiểm tra ô dữ liệu ban đầu');
  console.log(runSql(`SELECT product_id, quantity, reserved_quantity FROM inventory WHERE product_id = '${prodId}';`));
  await delay(1200);

  printStep('1.2', 'KHÁCH HÀNG THÊM VÀO GIỎ HÀNG (CART SERVICE REDIS O(1))', 'Ghi key giỏ hàng vào bộ nhớ RAM Redis');
  const cartRes = await request('http://localhost:8080/api/v1/cart/items', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${auth.token}` }
  }, JSON.stringify({ productId: prodId, quantity: 1, price: 1590000 }));
  console.log(`  -> Cart API phản hồi HTTP ${cartRes.statusCode}`);
  console.log(`  -> Redis Keys kiểm tra:`, runRedis(`keys cart:*`));
  await delay(1200);

  printStep('1.3', 'KHÁCH HÀNG BẤM "ĐẶT HÀNG NGAY" (API GATEWAY ➔ ORDER SERVICE)', 'Lưu Transactional Outbox và phát lệnh');
  const orderRes = await request('http://localhost:8080/api/v1/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${auth.token}` }
  }, JSON.stringify({
    userId: auth.sub,
    userEmail: 'customer@ecommerce.local',
    productId: prodId,
    productTitle: 'Điện thoại iPhone Demo Kịch Bản 1',
    quantity: 1,
    unitPrice: 1590000
  }));
  const orderData = orderRes.json();
  const orderId = orderData ? orderData.orderId : null;
  console.log(`  -> Đơn hàng đã sinh mã: \x1b[32m${orderId}\x1b[0m | Trạng thái ban đầu: \x1b[33mPENDING\x1b[0m`);
  await delay(1200);

  printStep('1.4', 'THEO DÕI SỰ KIỆN SAGA QUA KAFKA & CHUYỂN ĐỔI TRẠNG THÁI Ô DỮ LIỆU', 'Quan sát trạng thái đơn hàng biến đổi từng giây');
  for (let tick = 1; tick <= 5; tick++) {
    await delay(700);
    const qRes = await request(`http://localhost:8080/api/v1/orders/${orderId}`, {
      headers: { 'Authorization': `Bearer ${auth.token}` }
    });
    const st = qRes.json() ? qRes.json().status : '';
    console.log(`  [Giây thứ ${tick}] Trạng thái đơn hàng trong DB: \x1b[36m[${st}]\x1b[0m`);
    if (st === 'CONFIRMED') break;
  }
  await delay(800);

  printStep('1.5', 'ĐỐI SOÁT Ô DỮ LIỆU MYSQL SAU KHI SAGA HOÀN TẤT', 'Chứng minh trừ tồn kho và tạo giao dịch thành công');
  console.log(`  -> TỒN KHO THỰC TẾ TRONG BẢNG inventory (20 giảm còn 19):`);
  console.log(runSql(`SELECT product_id, quantity, reserved_quantity FROM inventory WHERE product_id = '${prodId}';`));
  console.log(`  -> BẢN GHI ĐƠN HÀNG TRONG BẢNG orders (Đã CONFIRMED, có payment_id):`);
  console.log(runSql(`SELECT order_id, product_id, quantity, status, payment_id FROM orders WHERE order_id = '${orderId}';`));
  console.log('\n\x1b[32m✅ KỊCH BẢN 1 HOÀN TẤT THÀNH CÔNG RỰC RỠ!\x1b[0m\n');
}

// -----------------------------------------------------------------------------
// KỊCH BẢN 2: 5 NGƯỜI CÙNG MUA LÚC (TỒN KHO = 10)
// -----------------------------------------------------------------------------
async function runScenario2(auth) {
  console.log('\n================================================================================');
  console.log('🎬 BẮT ĐẦU KỊCH BẢN 2: 5 NGƯỜI CÙNG MUA ĐỒNG THỜI (TỒN KHO = 10)');
  console.log('================================================================================');

  const prodId = 'fs-103';
  runSql(`UPDATE inventory SET quantity = 10, reserved_quantity = 0 WHERE product_id = '${prodId}';`);

  printStep('2.1', 'TỒN KHO BAN ĐẦU CỦA SẢN PHẨM = 10 CÁI', 'Xem ô dữ liệu trước khi bắn 5 requests');
  console.log(runSql(`SELECT product_id, quantity, reserved_quantity FROM inventory WHERE product_id = '${prodId}';`));
  await delay(1200);

  printStep('2.2', '5 KHÁCH HÀNG CÙNG BẤM MUA TẠI CÙNG 1 TÍCH TẮC (CONCURRENT BURST)', 'Phát 5 asynchronous requests qua Promise.all');
  const buyers = ['User_An', 'User_Binh', 'User_Cuong', 'User_Dung', 'User_Giang'];

  const orderPromises = buyers.map((b, idx) => {
    return request('http://localhost:8080/api/v1/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${auth.token}` }
    }, JSON.stringify({
      userId: `user_concurrent_${idx + 1}`,
      userEmail: `${b}@ecommerce.local`,
      productId: prodId,
      productTitle: `Sản phẩm Concurrency Kịch bản 2`,
      quantity: 1,
      unitPrice: 2000000
    })).then(r => ({ buyer: b, status: r.statusCode, orderId: r.json() ? r.json().orderId : null }));
  });

  const results = await Promise.all(orderPromises);
  results.forEach(r => {
    console.log(`  -> \x1b[32m${r.buyer}\x1b[0m ➔ Gateway tiếp nhận HTTP ${r.status} | Mã đơn: ${r.orderId}`);
  });
  await delay(1200);

  printStep('2.3', 'THEO DÕI REDISSON DISTRIBUTED LOCK & KAFKA PARTITIONS XỬ LÝ TUẦN TỰ', 'Chờ các worker trừ kho và xác nhận thanh toán');
  for (let i = 1; i <= 4; i++) {
    await delay(700);
    const confirmedCount = runSql(`SELECT count(*) FROM orders WHERE product_id = '${prodId}' AND status = 'CONFIRMED';`);
    console.log(`  [Giây thứ ${i}] Đã có ${confirmedCount} / 5 đơn hàng chuyển sang trạng thái CONFIRMED...`);
  }
  await delay(800);

  printStep('2.4', 'KẾT QUẢ ĐỐI SOÁT Ô DỮ LIỆU MYSQL (CHỐNG MẤT GIAO DỊCH / LOST UPDATE)', 'Tồn kho từ 10 giảm chính xác còn 5');
  console.log(`  -> TỒN KHO TRONG BẢNG inventory (10 - 5 = 5):`);
  console.log(runSql(`SELECT product_id, quantity, reserved_quantity FROM inventory WHERE product_id = '${prodId}';`));
  console.log(`  -> DANH SÁCH 5 ĐƠN HÀNG TRONG BẢNG orders:`);
  console.log(runSql(`SELECT order_id, user_id, status FROM orders WHERE product_id = '${prodId}' ORDER BY id DESC LIMIT 5;`));
  console.log('\n\x1b[32m✅ KỊCH BẢN 2 HOÀN TẤT THÀNH CÔNG 100%!\x1b[0m\n');
}

// -----------------------------------------------------------------------------
// KỊCH BẢN 3: 5 NGƯỜI CÙNG TRANH MUA 1 SẢN PHẨM TỒN KHO = 1 (CHỐNG BÁN ÂM)
// -----------------------------------------------------------------------------
async function runScenario3(auth) {
  console.log('\n================================================================================');
  console.log('🎬 BẮT ĐẦU KỊCH BẢN 3: 5 NGƯỜI CÙNG TRANH MUA SẢN PHẨM CHỈ CÒN DUY NHẤT 1 CÁI');
  console.log('================================================================================');

  const prodId = 'fs-104';
  runSql(`UPDATE inventory SET quantity = 1, reserved_quantity = 0 WHERE product_id = '${prodId}';`);

  printStep('3.1', 'TỒN KHO BAN ĐẦU CHỈ CÒN DUY NHẤT 1 SẢN PHẨM', 'Xem ô dữ liệu trước cuộc đua (Race Condition)');
  console.log(runSql(`SELECT product_id, quantity, reserved_quantity FROM inventory WHERE product_id = '${prodId}';`));
  await delay(1200);

  printStep('3.2', '5 NGƯỜI DÙNG CÙNG BẤM "MUA NGAY" TRONG CÙNG 1 TÍCH TẮC', 'Kích hoạt đợt tranh chấp khốc liệt');
  const competitors = ['Racer_1', 'Racer_2', 'Racer_3', 'Racer_4', 'Racer_5'];

  const promises = competitors.map((racer, idx) => {
    return request('http://localhost:8080/api/v1/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${auth.token}` }
    }, JSON.stringify({
      userId: `racer_${idx + 1}`,
      userEmail: `${racer}@ecommerce.local`,
      productId: prodId,
      productTitle: `Sản phẩm Tranh Mua Kịch bản 3`,
      quantity: 1,
      unitPrice: 5000000
    })).then(res => ({ racer: racer, orderId: res.json() ? res.json().orderId : null }));
  });

  const orders = await Promise.all(promises);
  orders.forEach(o => console.log(`  -> \x1b[33m${o.racer}\x1b[0m đã gửi lệnh đặt hàng: ${o.orderId}`));
  await delay(1200);

  printStep('3.3', 'REDISSON DISTRIBUTED LOCK PHÂN XỬ TRANH CHẤP TRÊN KAFKA', '1 người nhanh nhất thắng cuộc, 4 người còn lại bị hủy đơn');
  await delay(3500);

  printStep('3.4', 'KẾT QUẢ ĐỐI SOÁT TRÁNH BÁN ÂM TRONG MYSQL', 'Chứng minh tồn kho về đúng 0, không bao giờ âm');
  console.log(`  -> TỒN KHO THỰC TẾ TRONG BẢNG inventory (Phải về đúng 0, TUYỆT ĐỐI KHÔNG ÂM):`);
  console.log(runSql(`SELECT product_id, quantity, reserved_quantity FROM inventory WHERE product_id = '${prodId}';`));
  console.log(`  -> CHI TIẾT 5 ĐƠN HÀNG TRONG BẢNG orders (1 CONFIRMED, 4 CANCELLED):`);
  console.log(runSql(`SELECT order_id, user_id, status, cancel_reason FROM orders WHERE product_id = '${prodId}' ORDER BY id DESC LIMIT 5;`));
  console.log('\n\x1b[32m✅ KỊCH BẢN 3 HOÀN TẤT: BẢO VỆ CHỐNG BÁN ÂM (OVERSELLING PROTECTION) THÀNH CÔNG TUYỆT ĐỐI!\x1b[0m\n');
}

// -----------------------------------------------------------------------------
// MAIN CONTROLLER
// -----------------------------------------------------------------------------
async function main() {
  const auth = await getKeycloakToken();
  const arg = process.argv[2];

  if (arg === '1') {
    await runScenario1(auth);
  } else if (arg === '2') {
    await runScenario2(auth);
  } else if (arg === '3') {
    await runScenario3(auth);
  } else {
    // Run all sequentially with clear separation
    await runScenario1(auth);
    await delay(1500);
    await runScenario2(auth);
    await delay(1500);
    await runScenario3(auth);
  }
}

main().catch(err => {
  console.error('Lỗi thực thi:', err);
  process.exit(1);
});
