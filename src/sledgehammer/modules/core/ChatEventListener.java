package sledgehammer.modules.core;

import sledgehammer.event.ChatMessageEvent;
import sledgehammer.event.Event;
import sledgehammer.event.HandShakeEvent;
import sledgehammer.interfaces.EventListener;

public class ChatEventListener implements EventListener {

	@Override
	public String[] getTypes() {
		return new String[] {ChatMessageEvent.ID, HandShakeEvent.ID};
	}

	@Override
	public void handleEvent(Event event) {
	}

	@Override
	public boolean runSecondary() {
		// TODO Auto-generated method stub
		return false;
	}

}
