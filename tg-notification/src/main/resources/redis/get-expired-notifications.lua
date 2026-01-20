local driverActiveNotificationTimeoutsKey = KEYS[1]

local time = redis.call('TIME')
local timeInMillis = tonumber(time[1]) * 1000 + tonumber(time[2]) / 1000

local expired = redis.call('ZRANGE', driverActiveNotificationTimeoutsKey, '-inf', timeInMillis, 'BYSCORE')

if #expired > 0 then
  redis.call('ZREMRANGEBYSCORE', driverActiveNotificationTimeoutsKey, '-inf', timeInMillis)
end

return expired