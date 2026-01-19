local driverNotificationQueueKey = KEYS[1]
local driverActiveNotificationKey = KEYS[2]
local rideInfoKeyTemplate = ARGV[1]
local rideAcceptedKeyTemplate = ARGV[2]
local onlyIfNoneActiveFlag = tonumber(ARGV[3]) == 1


if redis.call('EXISTS', driverActiveNotificationKey) == 1 then
    local activeNotificationStatus = redis.call('HGET', driverActiveNotificationKey, 'status')

    if activeNotificationStatus == 'ACCEPTING' then
        local acceptanceExpireTime = tonumber(redis.call('HGET', driverActiveNotificationKey, 'acceptanceExpireTime'))
        local time = redis.call('TIME')
        local timeInMillis = tonumber(time[1]) * 1000 + tonumber(time[2]) / 1000
        if timeInMillis < acceptanceExpireTime then
            return nil
        end
    elseif onlyIfNoneActiveFlag then
        return nil
    end
end

local nextNotificationTuple = redis.call('ZPOPMIN', driverNotificationQueueKey)
while #nextNotificationTuple == 2 do
    local nextNotificationId = tonumber(nextNotificationTuple[1])
    local rideInfo = redis.call('GET', string.format(rideInfoKeyTemplate, nextNotificationId))
    local isAccepted = redis.call('EXISTS', string.format(rideAcceptedKeyTemplate, nextNotificationId))
    if rideInfo and isAccepted == 0 then
        redis.call('HSET', driverActiveNotificationKey, 'ride-id', nextNotificationId)
        redis.call('HSET', driverActiveNotificationKey, 'status', 'ACTIVE')
        return rideInfo
    else
        nextNotificationTuple = redis.call('ZPOPMIN', driverNotificationQueueKey)
    end
end

redis.call('DEL', driverActiveNotificationKey)
return nil