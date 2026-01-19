local rideInfoKey = KEYS[1]
local rideAcceptedKey = KEYS[2]

if redis.call('EXISTS', rideAcceptedKey) == 1 then
    return nil
end

return redis.call('GET', rideInfoKey)