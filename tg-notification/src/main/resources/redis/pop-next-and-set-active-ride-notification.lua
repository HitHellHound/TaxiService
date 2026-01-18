local driverNotificationQueueKey = KEYS[1]
local driverActiveNotificationKey = KEYS[2]
local rideInfoKeyTemplate = ARGV[1]
local rideAcceptedKeyTemplate = ARGV[2]
local onlyIfNoneActiveFlag = tonumber(ARGV[3]) == 1


if onlyIfNoneActiveFlag and redis.call('EXISTS', driverActiveNotificationKey) == 1 then
    return false
end

local nextNotificationId = redis.call('LPOP', driverNotificationQueueKey)
while nextNotificationId do
    local rideInfo = redis.call('GET', string.format(rideInfoKeyTemplate, nextNotificationId))
    local isAccepted = redis.call('EXISTS', string.format(rideAcceptedKeyTemplate, nextNotificationId))
    if rideInfo and isAccepted == 0 then
        redis.call('SET', driverActiveNotificationKey, nextNotificationId)
        return rideInfo
    else
        nextNotificationId = redis.call('LPOP', driverNotificationQueueKey)
    end
end

redis.call('UNLINK', driverActiveNotificationKey)
return false