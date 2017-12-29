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
package sledgehammer.lua.faction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.module.faction.MongoFaction;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Color;
import sledgehammer.lua.core.Player;
import sledgehammer.util.StringUtils;

/**
 * TODO: Document
 * 
 * @author Jab
 */
public class Faction extends LuaTable {

	private MongoFaction mongoFaction;

	private List<FactionMember> listMembers;

	private ChatChannel chatChannel;

	public Faction(MongoFaction mongoFaction) {
		super("Faction");
		setMongoDocument(mongoFaction);
		listMembers = new ArrayList<>();
	}

	public Faction(KahluaTable table) {
		super("Faction", table);
	}

	@Override
	public void onLoad(KahluaTable table) {
		// TODO: Implement.
	}

	@Override
	public void onExport() {
		// TODO: Implement.
	}

	/**
	 * TODO: Document
	 * 
	 * @param factionMember
	 * @return
	 */
	public boolean addMember(FactionMember factionMember) {
		boolean returned = false;
		if (!listMembers.contains(factionMember)) {
			listMembers.add(factionMember);
			returned = true;
			// Add the Player to the ChatChannel if he is online.
			Player player = SledgeHammer.instance.getPlayer(factionMember.getPlayerId());
			if (player != null) {
				getChatChannel().addPlayer(player, true);
			} else {
				errorln("addMember() -> Player is null.");
			}
		}
		return returned;
	}

	/**
	 * Removes a <FactionMember> from the <Faction>.
	 * 
	 * @param factionMember
	 *            The <FactionMember> member being removed.
	 */
	public void removeMember(FactionMember factionMember) {
		this.listMembers.remove(factionMember);
		// Remove the Player from the ChatChannel if he is online.
		Player player = SledgeHammer.instance.getPlayer(factionMember.getPlayerId());
		if (player != null) {
			getChatChannel().removePlayer(player);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Faction) {
			return ((Faction) other).getUniqueId().equals(getUniqueId());
		}
		return false;
	}

	/**
	 * @param password
	 *            The <String> password to test.
	 * @return Returns whether or not the provided <String> password matches the
	 *         encrypted password.
	 */
	public boolean isPassword(String password) {
		// Grab the encrypted password for the faction.
		String passwordActualEncrypted = getEncryptedPassword();
		// If a password and one is not provided.
		if (password == null || password.isEmpty()) {
			// If the actual password is not defined, this situation is valid.
			if (passwordActualEncrypted == null || passwordActualEncrypted.isEmpty()) {
				return true;
			}
			// If the faction has a password and one isn't provided, this is invalid.
			else {
				return false;
			}
		}
		// If the actual password is not defined.
		if (passwordActualEncrypted == null || passwordActualEncrypted.isEmpty()) {
			// If the password given is defined, this situation is invalid.
			if (password != null && !password.isEmpty()) {
				return false;
			}
		}
		// Encrypt the new password.
		String passwordEncrypted = StringUtils.md5(password);
		// Test if the both encrypted passwords match.
		return passwordActualEncrypted.equals(passwordEncrypted);
	}

	public void setFactionName(String factionName, boolean save) {
		getMongoDocument().setFactionName(factionName, save);
		ChatChannel chatChannel = getChatChannel();
		if (chatChannel != null) {
			chatChannel.rename("Faction_" + factionName, true);
		}
	}

	public void setFactionTag(String tag, boolean save) {
		getMongoDocument().setFactionTag(tag, save);
		for (FactionMember factionMember : listMembers) {
			Player player = SledgeHammer.instance.getPlayer(factionMember.getPlayerId());
			if (player != null) {
				player.setNickname("[" + getFactionTag() + "] " + player.getUsername());
			}
		}
	}

	public void setFactionColor(String colorNew, boolean save) {
		getMongoDocument().setFactionColorString(colorNew, save);
		Color color = Color.getColor(colorNew);
		for (FactionMember factionMember : listMembers) {
			Player player = SledgeHammer.instance.getPlayer(factionMember.getPlayerId());
			if (player != null) {
				player.setColor(color);
			}
		}
	}

	/**
	 * Sets the new <String> password for the <Faction>.
	 * 
	 * @param password
	 *            The <String> password to set.
	 * @param save
	 *            Whether or not to save the changes.
	 */
	public void setPassword(String password, boolean save) {
		getMongoDocument().setPassword(password, save);
	}

	private String getEncryptedPassword() {
		return getMongoDocument().getEncryptedPassword();
	}

	/**
	 * Returns whether or not the <String> tag given is the same tag as the one the
	 * <Faction> uses.
	 * 
	 * @param tag
	 *            The <String> tag being tested.
	 * @return Returns true if the tag is the same as the tag being used by the
	 *         <Faction>.
	 */
	public boolean isTag(String tag) {
		return getFactionTag().toUpperCase().equals(tag.toUpperCase());
	}

	/**
	 * @return Returns the <ChatChannel> representing the <Faction>.
	 */
	public ChatChannel getChatChannel() {
		return this.chatChannel;
	}

	/**
	 * Sets the <ChatChannel> representing the <Faction>
	 * 
	 * @param chatChannel
	 *            The <ChatChannel> to set.
	 */
	public void setChatChannel(ChatChannel chatChannel) {
		this.chatChannel = chatChannel;
	}

	public String getFactionName() {
		return getMongoDocument().getFactionName();
	}

	public UUID getUniqueId() {
		return getMongoDocument().getUniqueId();
	}

	public void setUniqueId(UUID uniqueId, boolean save) {
		getMongoDocument().setUniqueId(uniqueId, save);
	}

	public MongoFaction getMongoDocument() {
		return this.mongoFaction;
	}

	public void setMongoDocument(MongoFaction mongoFaction) {
		this.mongoFaction = mongoFaction;
	}

	public UUID getOwnerId() {
		return getMongoDocument().getOwnerId();
	}

	public boolean isOwner(Player player) {
		return isOwner(player.getUniqueId());
	}

	public boolean isOwner(FactionMember factionMember) {
		return isOwner(factionMember.getPlayerId());
	}

	public boolean isOwner(UUID playerId) {
		return playerId.equals(getOwnerId());
	}

	public String getFactionTag() {
		return getMongoDocument().getFactionTag();
	}

	public String getFactionColor() {
		return getMongoDocument().getFactionColor();
	}

	public List<FactionMember> getMembers() {
		return this.listMembers;
	}

	public void setOwner(FactionMember factionMember, boolean save) {
		getMongoDocument().setOwnerId(factionMember.getPlayerId(), save);
	}

	public String getFactionRawColor() {
		return getMongoDocument().getFactionRawColor();
	}
}