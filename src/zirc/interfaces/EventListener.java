package zirc.interfaces;

import zirc.event.Event;

public interface EventListener {
	String[] getTypes();
	void handleEvent(Event event);
	
}
