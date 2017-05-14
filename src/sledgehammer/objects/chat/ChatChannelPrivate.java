package sledgehammer.objects.chat;

import java.util.List;

import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.event.HandShakeEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.objects.Player;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public class ChatChannelPrivate extends ChatChannel implements EventListener {

	/**
	 * List of Player Objects for when the Player is 
	 * online.
	 */
	private List<Player> listPlayers;
	
	/**
	 * List of Player IDs to know who is in the private
	 * ChatChannel.
	 */
	private List<Integer> listPlayerIDs;
	
	/**
	 * Main Constructor.
	 */
	public ChatChannelPrivate() {
		super("Private");
	}
	
	public void addPlayerID(int id) {
		if(!listPlayerIDs.contains(id)) {
			listPlayerIDs.add(id);
		}
	}

	@Override
	public String[] getTypes() {
		return new String[] {HandShakeEvent.ID, DisconnectEvent.ID};
	}

	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean runSecondary() {
		return false;
	}

}
