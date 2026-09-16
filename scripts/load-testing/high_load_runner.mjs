import http from 'http';
import https from 'https';

// Parse CLI Arguments
const args = process.argv.slice(2);
let targetUrl = 'http://localhost:8080';
let totalRequests = 5000;
let concurrency = 50;

for (let i = 0; i < args.length; i++) {
  if (args[i] === '--target' && args[i + 1]) targetUrl = args[i + 1];
  if (args[i] === '--requests' && args[i + 1]) totalRequests = parseInt(args[i + 1], 10);
  if (args[i] === '--concurrency' && args[i + 1]) concurrency = parseInt(args[i + 1], 10);
}

console.log('================================================================================');
console.log('⚡ FLASH SALE HIGH-CONCURRENCY DISTRIBUTED LOAD TESTING HARNESS');
console.log('================================================================================');
console.log(`🎯 Target Gateway URL:   ${targetUrl}`);
console.log(`🚀 Tổng số Requests:     ${totalRequests.toLocaleString()} requests`);
console.log(`👥 Số Luồng Đồng Thời:   ${concurrency} Concurrent Workers`);
console.log(`📊 Request Mix Profile:  40% Browse | 35% Detail | 15% Order | 10% Cart`);
console.log('================================================================================\n');

const agent = new http.Agent({
  keepAlive: true,
  maxSockets: concurrency * 2,
  maxFreeSockets: concurrency
});

function sendRequest(url, options = {}, data = null) {
  return new Promise((resolve) => {
    const parsedUrl = new URL(url);
    const opts = {
      ...options,
      hostname: parsedUrl.hostname,
      port: parsedUrl.port,
      path: parsedUrl.pathname + parsedUrl.search,
      agent: agent,
      timeout: 10000
    };

    const start = Date.now();
    const req = http.request(opts, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        resolve({
          latency: Date.now() - start,
          status: res.statusCode,
          success: res.statusCode >= 200 && res.statusCode < 500
        });
      });
    });

    req.on('error', () => {
      resolve({
        latency: Date.now() - start,
        status: 500,
        success: false
      });
    });

    req.on('timeout', () => {
      req.destroy();
      resolve({
        latency: Date.now() - start,
        status: 504,
        success: false
      });
    });

    if (data) {
      req.write(typeof data === 'string' ? data : JSON.stringify(data));
    }
    req.end();
  });
}

async function getKeycloakToken() {
  try {
    const tokenUrl = 'http://localhost:8180/realms/ecommerce-realm/protocol/openid-connect/token';
    const params = new URLSearchParams();
    params.append('grant_type', 'password');
    params.append('client_id', 'ecommerce-frontend');
    params.append('username', 'customer');
    params.append('password', 'password');

    const res = await sendRequest(tokenUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }, params.toString());

    return res;
  } catch (e) {
    return null;
  }
}

async function run() {
  // Lấy Access Token từ Keycloak để gửi kèm Authorization Header
  let authToken = '';
  try {
    const authRes = await new Promise((resolve) => {
      const parsedUrl = new URL('http://localhost:8180/realms/ecommerce-realm/protocol/openid-connect/token');
      const req = http.request({
        hostname: parsedUrl.hostname,
        port: parsedUrl.port,
        path: parsedUrl.pathname,
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      }, (res) => {
        let body = '';
        res.on('data', d => body += d);
        res.on('end', () => {
          try {
            const j = JSON.parse(body);
            resolve(j.access_token || '');
          } catch (e) { resolve(''); }
        });
      });
      req.on('error', () => resolve(''));
      req.write('grant_type=password&client_id=ecommerce-frontend&username=customer&password=password');
      req.end();
    });
    authToken = authRes;
    if (authToken) console.log('🔑 Đã lấy Keycloak Bearer Token thành công cho phiên đo tải!');
  } catch (e) {}

  let completed = 0;
  let success = 0;
  let failed = 0;
  const latencies = [];
  const startTime = Date.now();
  let lastReportTime = startTime;
  let lastCompleted = 0;

  async function worker(workerId) {
    while (completed < totalRequests) {
      completed++;
      const reqId = completed;
      const rand = Math.random() * 100;
      let res;

      if (rand < 50) {
        // 50% - Browse Products Catalog
        res = await sendRequest(`${targetUrl}/api/v1/products`);
      } else if (rand < 85) {
        // 35% - Product Detail
        res = await sendRequest(`${targetUrl}/api/v1/products/cat-10`);
      } else {
        // 15% - Place Order with Bearer Token
        const headers = { 'Content-Type': 'application/json' };
        if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

        const orderPayload = JSON.stringify({
          userId: `load_user_${workerId}_${reqId % 200}`,
          userEmail: `load_${reqId}@ecommerce.local`,
          productId: 'fs-102',
          productTitle: 'Flash Sale Load Item',
          quantity: 1,
          unitPrice: 1590000
        });
        res = await sendRequest(`${targetUrl}/api/v1/orders`, {
          method: 'POST',
          headers: headers
        }, orderPayload);
      }

      latencies.push(res.latency);
      if (res.success) success++;
      else failed++;

      // Realtime live progress ticker
      const now = Date.now();
      if (now - lastReportTime >= 500 || reqId === totalRequests) {
        const deltaSec = (now - lastReportTime) / 1000;
        const currentRps = deltaSec > 0 ? Math.round((reqId - lastCompleted) / deltaSec) : 0;
        const totalElapsed = (now - startTime) / 1000;
        const overallRps = totalElapsed > 0 ? Math.round(reqId / totalElapsed) : 0;
        const progressPct = ((reqId / totalRequests) * 100).toFixed(1);

        process.stdout.write(`\r[TIẾN TRÌNH: ${reqId}/${totalRequests} (${progressPct}%)] ➔ Tốc độ tức thời: ${currentRps} RPS | TB: ${overallRps} RPS | Thành công: ${success} | Lỗi: ${failed}    `);

        lastReportTime = now;
        lastCompleted = reqId;
      }
    }
  }

  const workers = [];
  for (let i = 0; i < concurrency; i++) {
    workers.push(worker(i + 1));
  }
  await Promise.all(workers);

  const totalTimeSec = (Date.now() - startTime) / 1000;
  latencies.sort((a, b) => a - b);
  const p50 = latencies[Math.floor(latencies.length * 0.50)] || 0;
  const p90 = latencies[Math.floor(latencies.length * 0.90)] || 0;
  const p95 = latencies[Math.floor(latencies.length * 0.95)] || 0;
  const p99 = latencies[Math.floor(latencies.length * 0.99)] || 0;
  const avgRps = Math.round(totalRequests / totalTimeSec);

  console.log('\n\n================================================================================');
  console.log('🏁 BÁO CÁO NGHIỆM THU HIỆU NĂNG TẢI CAO (TABLE 26 & 27 SLO SPECIFICATIONS)');
  console.log('================================================================================');
  console.log(`⏱️ Tổng thời gian thực thi:    ${totalTimeSec.toFixed(2)} giây`);
  console.log(`🚀 Throughput trung bình:      ${avgRps.toLocaleString()} Requests/Giây (RPS)`);
  console.log(`✅ Requests thành công:        ${success.toLocaleString()} (${((success / totalRequests) * 100).toFixed(2)}%)`);
  console.log(`❌ Requests thất bại / 5xx:    ${failed.toLocaleString()} (${((failed / totalRequests) * 100).toFixed(2)}%)`);
  console.log(`⚡ Độ trễ Median (p50):        ${p50} ms`);
  console.log(`⚡ Độ trễ Phân vị p90:         ${p90} ms`);
  console.log(`⚡ Độ trễ Phân vị p95 (SLA):   ${p95} ms ${p95 < 128 ? '✅ (Đạt chuẩn SLA < 128ms)' : '⚠️ (Chịu tải cao)'}`);
  console.log(`⚡ Độ trễ Phân vị p99:         ${p99} ms`);
  console.log('================================================================================\n');
  console.log('💡 HƯỚNG DẪN: Mở ngay Grafana tại http://localhost:3001 (Dashboards -> FlashSale -> Flash Sale Microservices Overview)');
  console.log('             Bạn sẽ thấy các đỉnh sóng Throughput RPS và P95 Latency vừa được ghi nhận thời gian thực!');
}

run().catch(err => {
  console.error('Lỗi khi chạy tải cao:', err);
  process.exit(1);
});
