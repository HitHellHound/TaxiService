local rideInfoKey = KEYS[1]
local rideAcceptedKey = KEYS[2]
local driverActiveNotificationKey = KEYS[3]
local acceptanceRideId = ARGV[1]
local timeToAcceptanceInSeconds = tonumber(ARGV[2])

if redis.call('EXISTS', rideAcceptedKey) == 1 then
    return nil
end

local rideInfo = redis.call('GET', rideInfoKey)
if not rideInfo then
    return nil
end

if redis.call('EXISTS', driverActiveNotificationKey) == 0 then
    return nil
end

local activeNotificationRideId = redis.call('HGET', driverActiveNotificationKey, 'ride-id')
local activeNotificationStatus = redis.call('HGET', driverActiveNotificationKey, 'status')

if acceptanceRideId ~= activeNotificationRideId or activeNotificationStatus ~= 'ACTIVE' then
    return nil
end

local time = redis.call('TIME')
local timeInMillis = tonumber(time[1]) * 1000 + tonumber(time[2]) / 1000
local acceptanceExpireTime = timeInMillis + timeToAcceptanceInSeconds * 1000

redis.call('HSET', driverActiveNotificationKey, 'status', 'ACCEPTING')
redis.call('HSET', driverActiveNotificationKey, 'acceptanceExpireTime', acceptanceExpireTime)

return rideInfo