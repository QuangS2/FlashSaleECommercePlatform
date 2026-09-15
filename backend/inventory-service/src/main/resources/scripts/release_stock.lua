-- KEYS[1]: stockKey (flashsale:{saleId}:item:{itemId}:stock)
-- KEYS[2]: userKey (flashsale:{saleId}:item:{itemId}:user:{customerId})
-- ARGV[1]: releaseQty (1)
redis.call('INCRBY', KEYS[1], ARGV[1])
local purchased = tonumber(redis.call('GET', KEYS[2]) or '0')
if purchased and purchased > 0 then
    local newPurchased = purchased - tonumber(ARGV[1])
    if newPurchased <= 0 then
        redis.call('DEL', KEYS[2])
    else
        redis.call('SET', KEYS[2], tostring(newPurchased), 'KEEPTTL')
    end
else
    redis.call('DEL', KEYS[2])
end
return 1
