local driverActiveNotificationKey = KEYS[1]
local expiredRideId = ARGV[1]

if redis.call('EXISTS', driverActiveNotificationKey) == 0 then
    return 0
end

local activeNotificationRideId = redis.call('HGET', driverActiveNotificationKey, 'ride-id')
local activeNotificationStatus = redis.call('HGET', driverActiveNotificationKey, 'status')

if expiredRideId ~= activeNotificationRideId or activeNotificationStatus ~= 'ACTIVE' then
    return 0
end

redis.call('DEL', driverActiveNotificationKey)
return 1