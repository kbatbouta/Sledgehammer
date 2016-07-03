package sledgehammer.modules.core;

import sledgehammer.script.LuaListener;

public class CoreLuaEventListener extends LuaListener {
	
	private ModuleCore module;

	CoreLuaEventListener(ModuleCore module) {
		this.module = module;
	}

	ModuleCore getModule() {
		return module;
	}
}
