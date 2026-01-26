package com.efcon.tg_notification.dao;

import com.efcon.tg_notification.wrapper.LuaScriptWrapper;

import java.util.List;

public interface LuaScriptAware {
    void registerLuaScripts(List<LuaScriptWrapper> luaScripts);
}
