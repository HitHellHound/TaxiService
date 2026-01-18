local rideAcceptedKey = KEYS[1]
local driverNotificationQueue = KEYS[2]
local driverActiveNotification = KEYS[3]
local rideAcceptedKeyTtlInMinutes = tonumber(ARGV[1])

redis.call('SET', rideAcceptedKey, 'true', 'EX', rideAcceptedKeyTtlInMinutes * 60)
redis.call('UNLINK', driverNotificationQueue)
redis.call('UNLINK', driverActiveNotification)