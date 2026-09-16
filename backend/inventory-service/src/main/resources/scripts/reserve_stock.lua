-- KEYS[1]: stockKey (flashsale:{saleId}:item:{itemId}:stock)
-- KEYS[2]: userKey (flashsale:{saleId}:item:{itemId}:user:{customerId})
-- ARGV[1]: purchaseQty (1), ARGV[2]: userLimit (1), ARGV[3]: userLockTtl (300)
local currentStock = tonumber(redis.call('GET', KEYS[1]))
if not currentStock or currentStock < tonumber(ARGV[1]) then
    return -1 -- Không đủ tồn kho khả dụng
end
local purchased = tonumber(redis.call('GET', KEYS[2]) or '0')
if purchased + tonumber(ARGV[1]) > tonumber(ARGV[2]) then
    return -2 -- Vượt giới hạn mua của người dùng
end
redis.call('DECRBY', KEYS[1], ARGV[1])
local newPurchased = purchased + tonumber(ARGV[1])
redis.call('SET', KEYS[2], tostring(newPurchased), 'EX', ARGV[3])
return 1 -- Thành công
