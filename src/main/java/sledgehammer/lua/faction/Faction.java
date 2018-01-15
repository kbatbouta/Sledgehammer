/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.lua.faction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.module.faction.MongoFaction;
import sledgehammer.lua.MongoLuaObject;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Color;
import sledgehammer.lua.core.Player;
import zombie.sledgehammer.util.MD5;

/**
 * MongoLuaObject that handles faction data and operations.
 *
 * @author Jab
 */
public class Faction extends MongoLuaObject<MongoFaction> {

    /**
     * The List of FactionMembers in the Faction.
     */
    private List<FactionMember> listMembers;
    /**
     * The ChatChannel assigned to the FactionMembers for the Faction.
     */
    private ChatChannel chatChannel;

    /**
     * Main constructor.
     *
     * @param mongoDocument The MongoFaction document in the MongoDB server.
     */
    public Faction(MongoFaction mongoDocument) {
        super(mongoDocument, "Faction");
        listMembers = new ArrayList<>();
    }

    /**
     * Lua load constructor.
     *
     * @param mongoDocument The MongoFaction document in the MongoDB server.
     * @param table         The KahluaTable storing the data for the Faction.
     */
    public Faction(MongoFaction mongoDocument, KahluaTable table) {
        super(mongoDocument, "Faction");
        onLoad(table);
    }

    @Override
    public void onLoad(KahluaTable table) {
        // TODO: Implement.
    }

    @Override
    public void onExport() {
        // TODO: Implement.
    }

    @Override
    public boolean equals(Object other) {
        boolean returned = false;
        if (other instanceof Faction) {
            returned = ((Faction) other).getUniqueId().equals(getUniqueId());
        }
        return returned;
    }

    /**
     * Adds a FactionMember to the Faction.
     *
     * @param factionMember The FactionMember to add.
     * @return Returns true if the FactionMember is successfully added to the
     * Faction.
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
                errln("addMember() -> Player is null.");
            }
        }
        return returned;
    }

    /**
     * Removes a FactionMember from the Faction.
     *
     * @param factionMember The FactionMember member being removed.
     */
    public void removeMember(FactionMember factionMember) {
        this.listMembers.remove(factionMember);
        // Remove the Player from the ChatChannel if he is online.
        Player player = SledgeHammer.instance.getPlayer(factionMember.getPlayerId());
        if (player != null) {
            getChatChannel().removePlayer(player);
        }
    }

    /**
     * @param password The String password to test.
     * @return Returns whether or not the provided String password matches the
     * encrypted password.
     */
    public boolean isPassword(String password) {
        // Grab the encrypted password for the faction.
        String passwordActualEncrypted = getEncryptedPassword();
        // If a password and one is not provided.
        if (password == null || password.isEmpty()) {
            // If the actual password is not defined, this situation is valid.
            // If the faction has a password and one isn't provided, this is invalid.
            return passwordActualEncrypted == null || passwordActualEncrypted.isEmpty();
        }
        // If the actual password is not defined.
        if (passwordActualEncrypted == null || passwordActualEncrypted.isEmpty()) {
            // If the password given is defined, this situation is invalid.
            return false;
        }
        // Encrypt the new password.
        String passwordEncrypted = MD5.encrypt(password);
        // Test if the both encrypted passwords match.
        return passwordActualEncrypted.equals(passwordEncrypted);
    }

    /**
     * Sets the String name of the Faction.
     *
     * @param factionName The String name to set.
     * @param save        The flag to save the document.
     */
    public void setFactionName(String factionName, boolean save) {
        getMongoDocument().setFactionName(factionName, save);
        ChatChannel chatChannel = getChatChannel();
        if (chatChannel != null) {
            chatChannel.rename("Faction_" + factionName, true);
        }
    }

    /**
     * Sets the String tag of the Faction.
     *
     * @param tag  The String tag to set.
     * @param save The flag to save the document.
     */
    public void setFactionTag(String tag, boolean save) {
        getMongoDocument().setFactionTag(tag, save);
        for (FactionMember factionMember : listMembers) {
            Player player = SledgeHammer.instance.getPlayer(factionMember.getPlayerId());
            if (player != null) {
                factionMember.setTag(player);
            }
        }
    }

    /**
     * Sets the String color of the Faction.
     *
     * @param colorNew The String color to set.
     * @param save     The flag to save the document.
     */
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
     * Sets the new String password for the Faction.
     *
     * @param password The String password to set.
     * @param save     The flag to save the document.
     */
    public void setPassword(String password, boolean save) {
        getMongoDocument().setPassword(password, save);
    }

    /**
     * @return Returns the String password in encrypted format.
     */
    private String getEncryptedPassword() {
        return getMongoDocument().getEncryptedPassword();
    }

    /**
     * Returns whether or not the String tag given is the same tag as the one the
     * Faction uses.
     *
     * @param tag The String tag being tested.
     * @return Returns true if the tag is the same as the tag being used by the
     * Faction.
     */
    public boolean isTag(String tag) {
        return getFactionTag().toUpperCase().equals(tag.toUpperCase());
    }

    /**
     * @return Returns the ChatChannel representing the Faction.
     */
    public ChatChannel getChatChannel() {
        return this.chatChannel;
    }

    /**
     * Sets the ChatChannel representing the Faction
     *
     * @param chatChannel The ChatChannel to set.
     */
    public void setChatChannel(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

    /**
     * @return Returns the String name of the Faction.
     */
    public String getFactionName() {
        return getMongoDocument().getFactionName();
    }

    /**
     * @return Returns the Unique ID of the Faction.
     */
    public UUID getUniqueId() {
        return getMongoDocument().getUniqueId();
    }

    /**
     * Sets the Unique ID of the Faction.
     *
     * @param uniqueId The Unique ID to set.
     * @param save     The flag to save the document.
     */
    public void setUniqueId(UUID uniqueId, boolean save) {
        getMongoDocument().setUniqueId(uniqueId, save);
    }

    /**
     * @return Returns the Unique ID of the Player who owns the Faction.
     */
    public UUID getOwnerId() {
        return getMongoDocument().getOwnerId();
    }

    /**
     * @param player The Player to test.
     * @return Returns true if the Player given is the owner of the Faction.
     */
    public boolean isOwner(Player player) {
        return isOwner(player.getUniqueId());
    }

    /**
     * @param factionMember The FactionMember to test.
     * @return Returns true if the FactionMember's Unique ID for the Player matches
     * the ownerId of the Faction.
     */
    public boolean isOwner(FactionMember factionMember) {
        return isOwner(factionMember.getPlayerId());
    }

    /**
     * @param playerId The Player's Unique ID to test.
     * @return Returns true if the Unique ID matches the owner's Unique ID of the Faction.
     */
    public boolean isOwner(UUID playerId) {
        return playerId.equals(getOwnerId());
    }

    /**
     * @return Returns the String tag of the Faction.
     */
    public String getFactionTag() {
        return getMongoDocument().getFactionTag();
    }

    /**
     * @return Returns the String color of the Faction.
     */
    public String getFactionColor() {
        return getMongoDocument().getFactionColor();
    }

    /**
     * @return Returns a List of FactionMember's in the Faction.
     */
    public List<FactionMember> getMembers() {
        return this.listMembers;
    }

    /**
     * Sets the Unique ID ownerId of the Faction with the FactionMember's Player's Unique ID.
     *
     * @param factionMember The FactionPlayer to set as the owner.
     * @param save          The flag to save the document.
     */
    public void setOwner(FactionMember factionMember, boolean save) {
        getMongoDocument().setOwnerId(factionMember.getPlayerId(), save);
    }

    /**
     * @return Returns the String encoded-color of the Faction.
     */
    public String getFactionRawColor() {
        return getMongoDocument().getFactionRawColor();
    }
}