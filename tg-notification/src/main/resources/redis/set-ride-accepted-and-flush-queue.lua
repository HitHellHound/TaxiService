local rideAcceptedKey = KEYS[1]
local driverNotificationQueueKey = KEYS[2]
local driverActiveNotificationKey = KEYS[3]
local rideAcceptedKeyTtlInMinutes = tonumber(ARGV[1])

redis.call('SET', rideAcceptedKey, 'true', 'EX', rideAcceptedKeyTtlInMinutes * 60)
redis.call('UNLINK', driverNotificationQueueKey)
redis.call('UNLINK', driverActiveNotificationKey)