package sledgehammer.objects.chat;

/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
*/

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
	
	public boolean hasPlayer(Player player) {
		return this.listPlayers.contains(player);
	}
	
	public List<Player> getPlayers() {
		return this.listPlayers;
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
