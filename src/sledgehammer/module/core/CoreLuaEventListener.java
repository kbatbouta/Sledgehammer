package sledgehammer.module.core;

import sledgehammer.lua.LuaListener;

public class CoreLuaEventListener extends LuaListener {
	
	private ModuleCore module;

	CoreLuaEventListener(ModuleCore module) {
		this.module = module;
	}

	ModuleCore getModule() {
		return module;
	}
}
