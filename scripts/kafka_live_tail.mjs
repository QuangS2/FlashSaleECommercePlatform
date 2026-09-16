import { spawn } from 'child_process';

console.log('================================================================================');
console.log('📡 KAFKA LIVE EVENT STREAMER - LẮNG NGHE TỨC THỜI CÁC SỰ KIỆN PHÂN TÁN SAGA');
console.log('================================================================================');
console.log('Đang kết nối Kafka Broker [localhost:9092]...');
console.log('Lắng nghe 4 topics: [order-events] [inventory-events] [payment-events] [notification-events]\n');
console.log('⏳ MÀN HÌNH ĐANG ĐỨNG ĐỢI SỰ KIỆN MỚI. BẤM MUA HÀNG TRÊN WEB HOẶC CHẠY SCRIPT ĐỂ XEM TIN NHẮN NHẢY TỨC THÌ!\n');

const topics = ['order-events', 'inventory-events', 'payment-events', 'notification-events'];

topics.forEach(topic => {
  const kConsumer = spawn('docker', [
    'exec', '-i', 'ecommerce-kafka',
    'kafka-console-consumer',
    '--bootstrap-server', 'localhost:9092',
    '--topic', topic
  ]);

  kConsumer.stdout.on('data', (data) => {
    const lines = data.toString().split('\n');
    lines.forEach(line => {
      const trimmed = line.trim();
      if (!trimmed) return;
      try {
        const json = JSON.parse(trimmed);
        const eventType = json.eventType || 'EVENT';
        const orderId = json.payload ? (json.payload.orderId || json.payload.id) : (json.orderId || 'N/A');
        const now = new Date().toLocaleTimeString();

        let color = '\x1b[36m'; // Cyan
        if (eventType.includes('FAILED') || eventType.includes('CANCELLED')) color = '\x1b[31m'; // Red
        if (eventType.includes('CONFIRMED') || eventType.includes('COMPLETED')) color = '\x1b[32m'; // Green
        if (eventType.includes('RESERVED')) color = '\x1b[33m'; // Yellow

        console.log(`${color}[${now}] 📬 [TOPIC: ${topic.padEnd(17)}] ➔ [${eventType}] | OrderID: ${orderId}\x1b[0m`);
        if (json.payload) {
          console.log(`     Chi tiết: ${JSON.stringify(json.payload).substring(0, 110)}...`);
        }
      } catch (e) {
        console.log(`[RAW] [${topic}]: ${trimmed}`);
      }
    });
  });

  kConsumer.stderr.on('data', () => {});
});

process.on('SIGINT', () => {
  console.log('\nĐã dừng Kafka Live Streamer.');
  process.exit(0);
});
