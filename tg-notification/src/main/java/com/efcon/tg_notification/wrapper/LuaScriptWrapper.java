package com.efcon.tg_notification.wrapper;

import org.springframework.data.redis.core.script.RedisScript;

public interface LuaScriptWrapper {
    String getName();
    RedisScript<?> getScript();
}
