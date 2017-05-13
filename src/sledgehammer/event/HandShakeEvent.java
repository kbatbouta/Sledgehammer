package sledgehammer.event;

import sledgehammer.objects.Player;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public class HandShakeEvent extends PlayerEvent {

	public static final String ID = "HandShakeEvent";
	
	public HandShakeEvent(Player player) {
		super(player);
	}

	public String getLogMessage() { return null; }

	public String getID() { return ID; }

}
