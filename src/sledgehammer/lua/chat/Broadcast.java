package sledgehammer.lua.chat;

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

import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.core.Player;

/**
 * TODO: Document.
 * 
 * @author Jab
 *
 */
public class Broadcast extends LuaTable {

	/**
	 * Author of the Broadcast. Set to admin by default.
	 */
	private Player author = SledgeHammer.getAdmin();

	/**
	 * The time of the broadcast.
	 */
	private String time;

	/**
	 * The message included in the Broadcast.
	 */
	private String message;

	/**
	 * Main constructor.
	 * 
	 * @param message
	 */
	public Broadcast(String message) {
		super("Broadcast");
		setMessage(message);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Broadcast(KahluaTable table) {
		super("Broadcast", table);
	}

	public Player getAuthor() {
		return this.author;
	}

	public void setAuthor(Player player) {
		this.author = player;
	}

	public String getMessage() {
		return this.message;
	}

	public String getTime() {
		return this.time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void onLoad(KahluaTable table) {
		Object _message = table.rawget("message");
		if (_message != null) {
			this.message = _message.toString();
		}

		Object _author = table.rawget("author");
		if (author != null) {
			if (author instanceof KahluaTable) {
				KahluaTable author = (KahluaTable) _author;
				String id = author.rawget("id").toString();
				setAuthor(SledgeHammer.instance.getPlayer(UUID.fromString(id)));
			}
		}
	}

	public void onExport() {
		set("message", getMessage());
		set("time", getTime());
		set("author", getAuthor());
	}

}
