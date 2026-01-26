package com.efcon.tg_notification.wrapper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

public class DefaultLuaScriptWrapper implements LuaScriptWrapper {
    private final String name;
    private final RedisScript<?> script;

    public DefaultLuaScriptWrapper(String name, RedisScript<?> script) {
        this.name = name;
        this.script = script;
    }

    public DefaultLuaScriptWrapper(String name, String scriptClassPath, Class<?> resultType) {
        this.name = name;
        this.script = RedisScript.of(new ClassPathResource(scriptClassPath), resultType);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RedisScript<?> getScript() {
        return script;
    }
}
