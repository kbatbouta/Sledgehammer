package sledgehammer.modules.core;

import sledgehammer.event.ClientEvent;
import sledgehammer.module.Module;

public class ModuleTerritories extends Module {

	public static final String ID      = "ModuleTerritories";
	public static final String NAME    = "Territories";
	public static final String MODULE  = "Territories";
	public static final String VERSION = "1.00";
	
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

	public String getID()         { return ID;      }
	public String getName()       { return NAME;    }
	public String getModuleName() { return MODULE;  }
	public String getVersion()    { return VERSION; }

	public void onClientCommand(ClientEvent e) {}
}
