-- KEYS -- driver queues
local rideId = ARGV[1]
local queueMaxSize = tonumber(ARGV[2])

for i, driverQueueKey in ipairs(KEYS) do
    local queueSize = redis.call('LLEN', driverQueueKey)
    if queueSize <= queueMaxSize then
        redis.call('RPUSH', driverQueueKey, rideId)
    end
end