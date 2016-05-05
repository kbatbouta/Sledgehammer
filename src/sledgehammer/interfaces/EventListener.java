package sledgehammer.interfaces;

import sledgehammer.event.Event;

public interface EventListener {
	String[] getTypes();
	void handleEvent(Event event);
	
}
