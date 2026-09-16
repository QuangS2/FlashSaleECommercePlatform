import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// Custom Metrics matching Table 26 SLOs
export const orderLatency = new Trend('order_latency_ms');
export const technicalErrorRate = new Rate('technical_errors');
export const oversellingCount = new Counter('overselling_violations');

// Test Stages matching Table 28 Load Progression
export const options = {
  scenarios: {
    flashsale_traffic: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 2500 },  // T = 02:00 -> 2,500 VU (950 RPS)
        { duration: '2m', target: 5000 },  // T = 04:00 -> 5,000 VU (1,850 RPS)
        { duration: '2m', target: 10000 }, // T = 06:00 -> 10,000 VU Peak (2,320 RPS)
        { duration: '6m', target: 1000 },  // T = 12:00 -> 1,000 VU
        { duration: '5m', target: 0 },     // T = 17:00 -> Scale-in cool down
      ],
    },
  },
  thresholds: {
    // SLO Targets from Table 26
    'http_req_duration{status:200}': ['p(95)<128', 'p(99)<215'],
    'order_latency_ms': ['p(95)<118'],
    'technical_errors': ['rate<0.01'], // HTTP 5xx < 1%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const userId = `user_${__VU}_${__ITER}`;
  const rand = Math.random() * 100;

  // Request Mix matching Table 22
  if (rand < 40) {
    // 40% - Browse Product Catalog
    const res = http.get(`${BASE_URL}/api/products?page=0&size=20`, {
      headers: { 'Accept': 'application/json' },
    });
    check(res, {
      'browse status 200': (r) => r.status === 200,
    });
    if (res.status >= 500) technicalErrorRate.add(1);
    else technicalErrorRate.add(0);

  } else if (rand < 75) {
    // 35% - View Active Flash Sale Detail
    const res = http.get(`${BASE_URL}/api/flash-sales/active`, {
      headers: { 'Accept': 'application/json' },
    });
    check(res, {
      'flashsale detail 200': (r) => r.status === 200,
    });
    if (res.status >= 500) technicalErrorRate.add(1);
    else technicalErrorRate.add(0);

  } else if (rand < 90) {
    // 15% - Place Flash Sale Order
    const orderPayload = JSON.stringify({
      flashSaleId: 1,
      itemId: 101,
      productId: 'PROD-101',
      quantity: 1,
      unitPrice: 199.00,
    });

    const idempotencyKey = `idemp_${userId}_${Date.now()}`;
    const start = Date.now();

    const res = http.post(`${BASE_URL}/api/v1/orders/flash-sale`, orderPayload, {
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': userId,
        'Idempotency-Key': idempotencyKey,
      },
    });

    orderLatency.add(Date.now() - start);

    check(res, {
      'order accepted (202) or out of stock (409)': (r) => r.status === 202 || r.status === 409,
    });

    if (res.status >= 500) technicalErrorRate.add(1);
    else technicalErrorRate.add(0);

  } else {
    // 10% - Payment Processing
    const paymentPayload = JSON.stringify({
      orderId: `ORD_MOCK_${userId}`,
      amount: 199.00,
      paymentMethod: 'MOCK_GATEWAY',
    });

    const res = http.post(`${BASE_URL}/api/payments/process`, paymentPayload, {
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': userId,
      },
    });

    check(res, {
      'payment status 200 or 400': (r) => r.status === 200 || r.status === 400,
    });

    if (res.status >= 500) technicalErrorRate.add(1);
    else technicalErrorRate.add(0);
  }

  sleep(1);
}
