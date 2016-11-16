package sledgehammer.event;

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

import sledgehammer.wrapper.Player;

public class LogEvent extends Event {
	
	public static final String ID = "LogEvent";
	
	private LogType type;
	private Player player;
	private Event event;
	private String message;
	private boolean importance;
	
	public LogEvent(Event event) {
		super();
		setEvent(event);
		this.message = event.getLogMessage();
		this.importance = false;
	}
	
	public LogType getLogType() {
		return this.type;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Event getEvent() {
		return this.event;
	}
	
	public void setEvent(Event event) {
		this.event = event;
	}
	
//	public static enum LogType {
//		PLAYER,
//		COMMAND,
//		SERVER,
//		CHAT,
//		ITEM
//	}
	
	public static enum LogType {
		INFO,
		WARN,
		ERROR,
		CHEAT,
		STAFF
	}

	public boolean isImportant() {
		return this.importance;
	}
	
	public void setImportant(boolean flag) {
		this.importance = flag;
	}

	@Override
	public String getLogMessage() {
		return message;
	}

	@Override
	public String getID() {
		return ID;
	}
}
