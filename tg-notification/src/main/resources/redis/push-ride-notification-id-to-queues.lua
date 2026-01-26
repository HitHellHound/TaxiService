-- KEYS -- driver queues
local rideId = ARGV[1]
local queueMaxSize = tonumber(ARGV[2])

for i, driverQueueKey in ipairs(KEYS) do
    local queueSize = redis.call('ZCARD', driverQueueKey)
    if queueSize <= queueMaxSize then
        local time = redis.call('TIME')
        local score = tonumber(time[1]) * 1000 + tonumber(time[2]) / 1000
        redis.call('ZADD', driverQueueKey, 'NX', score, rideId)
    end
end