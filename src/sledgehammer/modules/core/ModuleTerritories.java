package sledgehammer.modules.core;

import sledgehammer.module.Module;

public class ModuleTerritories extends Module {

	public static final String ID = "ModuleTerritories";
	public static final String VERSION = "1.00";
	public static final String NAME = "Territories";
	
	private TerritoriesEventListener eventListener;
	
	@Override
	public void onLoad() {
		eventListener = new TerritoriesEventListener(this);
		
	}

	@Override
	public void onStart() {
		register(eventListener);
	}

	@Override
	public void onUpdate(long delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop() {
		unregister(eventListener);
	}

	@Override
	public void onUnload() {
		eventListener = null;
	}

	@Override public String getID()      { return ID;      }
	@Override public String getVersion() { return VERSION; }
	@Override public String getName()    { return NAME;    }
	
}
