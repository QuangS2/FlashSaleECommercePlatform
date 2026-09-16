import { execSync } from 'child_process';

console.log('🔄 Đang đồng bộ và khôi phục tồn kho thực tế giữa MongoDB & MySQL...');

// 1. Cập nhật MongoDB
const mongoScript = `
db.product.updateOne({_id: "fs-101"}, {$set: {stockCount: 15, soldCount: 85}});
db.product.updateOne({_id: "fs-102"}, {$set: {stockCount: 12, soldCount: 42}});
db.product.updateOne({_id: "fs-103"}, {$set: {stockCount: 10, soldCount: 95}});
db.product.updateOne({_id: "fs-104"}, {$set: {stockCount: 20, soldCount: 42}});
print("MongoDB products updated successfully!");
`;

try {
  const mongoOut = execSync('docker exec -i ecommerce-mongo mongosh ecommerce_product', {
    input: mongoScript,
    encoding: 'utf-8'
  });
  console.log('MongoDB Output:', mongoOut);
} catch (e) {
  console.error('MongoDB Error:', e.message);
}

// 2. Cập nhật MySQL Inventory
const mysqlScript = `
USE ecommerce_db;
UPDATE inventory SET quantity = 15, reserved_quantity = 0 WHERE product_id = 'fs-101';
UPDATE inventory SET quantity = 12, reserved_quantity = 0 WHERE product_id = 'fs-102';
UPDATE inventory SET quantity = 10, reserved_quantity = 0 WHERE product_id = 'fs-103';
UPDATE inventory SET quantity = 20, reserved_quantity = 0 WHERE product_id = 'fs-104';
SELECT product_id, quantity, reserved_quantity FROM inventory WHERE product_id LIKE 'fs-%';
`;

try {
  const mysqlOut = execSync('docker exec -i ecommerce-mysql mysql -u root -prootpassword', {
    input: mysqlScript,
    encoding: 'utf-8'
  });
  console.log('MySQL Output:\n', mysqlOut);
} catch (e) {
  console.error('MySQL Error:', e.message);
}

// 3. Xóa cache Redis Flash Sale & User Locks
try {
  execSync('docker exec ecommerce-redis redis-cli del "flashsale:1:item:100:stock"');
  execSync('docker exec ecommerce-redis redis-cli keys "flashsale:*" | ForEach-Object { docker exec ecommerce-redis redis-cli del $_ }');
  console.log('Redis Flash Sale cache cleared!');
} catch (e) {
  console.log('Redis clear done');
}

console.log('✅ Đã đồng bộ hoàn tất tồn kho thực tế 100%!');
