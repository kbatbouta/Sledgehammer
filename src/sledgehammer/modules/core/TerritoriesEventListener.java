package sledgehammer.modules.core;

import sledgehammer.event.Event;
import sledgehammer.event.PlayerInfoEvent;
import sledgehammer.interfaces.EventListener;

public class TerritoriesEventListener implements EventListener {
	
	@SuppressWarnings("unused")
	private ModuleTerritories module;
	
	TerritoriesEventListener(ModuleTerritories module) {
		this.module = module;
	}

	@Override
	public String[] getTypes() {
		return new String[] { PlayerInfoEvent.ID };
	}

	@Override
	public void handleEvent(Event event) {
		if(event.getID() == PlayerInfoEvent.ID) {
			handlePlayerInfoEvent((PlayerInfoEvent) event);
		}
	}

	private void handlePlayerInfoEvent(PlayerInfoEvent event) {
//		module.println(event.getPosition());
//		module.println(event.getMetaPosition());
	}

	@Override public boolean runSecondary() {return false;}
}
