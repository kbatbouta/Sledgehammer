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
package sledgehammer.module.faction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mongodb.DBCursor;

import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoCollection;
import sledgehammer.database.module.core.SledgehammerDatabase;
import sledgehammer.database.module.faction.MongoFaction;
import sledgehammer.database.module.faction.MongoFactionInvite;
import sledgehammer.database.module.faction.MongoFactionMember;
import sledgehammer.enums.Result;
import sledgehammer.event.ClientEvent;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.faction.Faction;
import sledgehammer.lua.faction.FactionInvite;
import sledgehammer.lua.faction.FactionMember;
import sledgehammer.plugin.MongoModule;
import sledgehammer.util.Response;

/**
 * Module responsible for managing factions.
 * <p>
 * TODO: Document.
 *
 * @author Jab
 */
public class ModuleFactions extends MongoModule {

    private FactionsCommandListener factionsConnectionListener;
    private FactionsEventHandler factionsEventHandler;
    private Map<UUID, MongoFaction> mapMongoFactions;
    private Map<UUID, MongoFactionMember> mapMongoFactionMembers;
    private Map<UUID, MongoFactionInvite> mapMongoFactionInvites;
    private Map<UUID, Faction> mapFactionsByUniqueId;
    private Map<UUID, FactionMember> mapFactionMembersByUniqueId;
    private Map<UUID, FactionInvite> mapFactionInvites;
    private Map<String, Faction> mapFactionsByTag;
    private Map<String, Faction> mapFactionsByName;
    private MongoCollection collectionFactions;
    private MongoCollection collectionFactionMembers;
    private MongoCollection collectionFactionInvites;
    private FactionActions actions;
    /**
     * The last time the Module has updated.
     */
    private long timeUpdatedLast = -1L;
    /**
     * 5 Minutes
     */
    private long timeUpdate = 300000L;
    /**
     * 3 Days
     */
    private long timeToLiveInvites = 360000L * 24L * 3L;

    private int tagCharactersMinimum = 2;
    private int tagCharactersMaximum = 16;
    private int nameCharactersMinimum = 3;
    private int nameCharactersMaximum = 16;

    /**
     * Main constructor.
     */
    public ModuleFactions() {
        super(getDefaultDatabase());
    }

    @Override
    public void onLoad() {
        actions = new FactionActions(this);
        factionsConnectionListener = new FactionsCommandListener(this);
        factionsEventHandler = new FactionsEventHandler(this);
        SledgehammerDatabase database = SledgeHammer.instance.getDatabase();
        // @formatter:off
		collectionFactions       = database.createMongoCollection("sledgehammer_factions");
		collectionFactionMembers = database.createMongoCollection("sledgehammer_faction_members");
		collectionFactionInvites = database.createMongoCollection("sledgehammer_faction_invites");
		// @formatter:on
        // Load the MongoDocuments from the database.
        loadMongoDocuments();
        // Create the Lua Objects to contain the Mongo information.
        createLuaObjects();
        // Add the chat permission as default.
        addDefaultPermission("sledgehammer.factions.chat");
    }

    @Override
    public void onStart() {
        register(factionsConnectionListener);
        register(factionsEventHandler);
    }

    @Override
    public void onUpdate(long delta) {
        if (!isLoaded()) {
            return;
        }
        // If it is time for another update check.
        if (System.currentTimeMillis() - timeUpdatedLast > timeUpdate) {
            // Process the invites.
            removeExpiredInvites();
            // Set the last time we updated to now.
            timeUpdatedLast = System.currentTimeMillis();
        }
    }

    @Override
    public void onStop() {
        unregister(factionsConnectionListener);
        unregister(factionsEventHandler);
    }

    @Override
    public void onUnload() {
        reset();
    }

    @Override
    public void onClientCommand(ClientEvent e) {

    }

    /**
     * (Private Method)
     * <p>
     * Loads Database Objects and constructs Lua Objects.
     */
    private void loadMongoDocuments() {
        // Load MongoDocuments.
        loadMongoFactions();
        loadMongoFactionMembers();
        loadMongoFactionInvites();
    }

    /**
     * (Private Method)
     * <p>
     * Constructs Lua wrapper Objects to represent the data.
     */
    private void createLuaObjects() {
        // Create HashMap(s).
        mapFactionsByUniqueId = new HashMap<>();
        mapFactionsByTag = new HashMap<>();
        mapFactionsByName = new HashMap<>();
        mapFactionMembersByUniqueId = new HashMap<>();
        mapFactionInvites = new HashMap<>();
        // Go through each MongoFaction.
        for (MongoFaction mongoFaction : mapMongoFactions.values()) {
            // Create the Lua container.
            Faction faction = new Faction(mongoFaction);
            // Place it in the HashMap.
            mapFactionsByUniqueId.put(faction.getUniqueId(), faction);
            mapFactionsByTag.put(faction.getFactionTag(), faction);
            mapFactionsByName.put(faction.getFactionName().toLowerCase(), faction);
            // Grab the ChatChannel for the Faction.
            ChatChannel chatChannel = getChatChannel(faction);
            // If the ChatChannel does not exist, create it.
            if (chatChannel == null) {
                chatChannel = createChatChannel(faction);
            }
            // Set the ChatChannel in the Faction container.
            faction.setChatChannel(chatChannel);
        }
        // Go through each MongoFactionMember.
        for (MongoFactionMember mongoFactionMember : mapMongoFactionMembers.values()) {
            // Create the Lua container.
            FactionMember factionMember = new FactionMember(mongoFactionMember);
            // Place it in the HashMap.
            mapFactionMembersByUniqueId.put(factionMember.getPlayerId(), factionMember);
            // Grab the Faction for the Member.
            Faction faction = getFaction(factionMember.getFactionId());
            // Set the member's faction. (This adds it to the list in the Faction).
            factionMember.setFaction(faction, false);
        }
        // Go through each MongoFactionInvite.
        for (MongoFactionInvite mongoFactionInvite : mapMongoFactionInvites.values()) {
            FactionInvite factionInvite = new FactionInvite(mongoFactionInvite);
            mapFactionInvites.put(factionInvite.getUniqueId(), factionInvite);
        }
    }

    /**
     * (Private Method)
     * <p>
     * Loads MongoFaction Objects from the database.
     */
    private void loadMongoFactions() {
        println("Loading Faction(s)...");
        // Create HashMap.
        mapMongoFactions = new HashMap<>();
        // Initiate collection query.
        DBCursor cursor = collectionFactions.find();
        // Go through each entry.
        while (cursor.hasNext()) {
            // Create an object for each entry.
            MongoFaction mongoFaction = new MongoFaction(collectionFactions, cursor.next());
            // Add to the map with the UUID of the Faction.
            mapMongoFactions.put(mongoFaction.getUniqueId(), mongoFaction);
        }
        // Close the query.
        cursor.close();
        // Report statistics.
        int size = mapMongoFactions.size();
        println("Loaded " + (size == 0 ? "no" : size + "") + " Faction" + (size == 1 ? "" : "s") + ".");
    }

    /**
     * (Private Method)
     * <p>
     * Loads MongoFactionMember Objects from the database.
     */
    private void loadMongoFactionMembers() {
        println("Loading Faction Member(s)...");
        // Create HashMap.
        mapMongoFactionMembers = new HashMap<>();
        // Initiate collection query.
        DBCursor cursor = collectionFactionMembers.find();
        // Go through each entry.
        while (cursor.hasNext()) {
            // Create an object for each entry.
            MongoFactionMember mongoFactionMember = new MongoFactionMember(collectionFactionMembers, cursor.next());
            // Add to the map with the UUID of the FactionMember.
            mapMongoFactionMembers.put(mongoFactionMember.getPlayerId(), mongoFactionMember);
        }
        // Close the query.
        cursor.close();
        // Report statistics.
        int size = mapMongoFactionMembers.size();
        println("Loaded " + (size == 0 ? "no" : size + "") + " Faction Member" + (size == 1 ? "" : "s") + ".");
    }

    /**
     * (Private Method)
     * <p>
     * Loads MongoFactionInvite Objects from the database.
     */
    private void loadMongoFactionInvites() {
        println("Loading Faction Invite(s)...");
        // Create HashMap.
        mapMongoFactionInvites = new HashMap<>();
        // Initiate collection query.
        DBCursor cursor = collectionFactionInvites.find();
        // Go through each entry.
        while (cursor.hasNext()) {
            // Create an object for each entry.
            MongoFactionInvite mongoFactionInvite = new MongoFactionInvite(collectionFactionMembers, cursor.next());
            // Add to the map with the UUID of the MongoFactionInvite.
            mapMongoFactionInvites.put(mongoFactionInvite.getUniqueId(), mongoFactionInvite);
        }
        // Close the query.
        cursor.close();
        // Report statistics.
        int size = mapMongoFactionInvites.size();
        println("Loaded " + (size == 0 ? "no" : size + "") + " Faction Invite" + (size == 1 ? "" : "s") + ".");
    }

    /**
     * (Private Method)
     * <p>
     * Resets the maps and fields for the module.
     */
    private void reset() {
        mapMongoFactions.clear();
        mapMongoFactionMembers.clear();
        mapMongoFactionInvites.clear();
        mapFactionsByUniqueId.clear();
        mapFactionsByTag.clear();
        mapFactionsByName.clear();
        mapFactionMembersByUniqueId.clear();
        mapFactionInvites.clear();
    }

    /**
     * Checks and removes expired FactionInvites.
     */
    public void removeExpiredInvites() {
        // Our count to store the amount of removed invites.
        int count = 0;
        println("Removing expired invite(s)...");
        // Go through each invite.
        for (FactionInvite factionInvite : mapFactionInvites.values()) {
            // Check if the invite is expired.
            if (factionInvite.isExpired(timeToLiveInvites)) {
                // Delete the invite.
                deleteInvite(factionInvite);
                count++;
            }
        }
        // Print out statistics.
        String plural = count != 1 ? "s" : "";
        println("Removed " + count + " expired invite" + plural + ".");
    }

    /**
     * Creates a Faction with given parameters, along with a new FactionMember,
     * if one for the owner does not exist.
     *
     * @param factionName The String name of the Faction.
     * @param tag         The String tag of the Faction.
     * @param password    The administrator String password of the Faction.
     * @param ownerId     The Unique ID of the Player that is set to as the owner of the
     *                    Faction.
     * @return The new Faction.
     */
    public Faction createFaction(String factionName, String tag, String password, UUID ownerId) {
        String color = "white";
        MongoFaction mongoFaction;
        MongoFactionMember mongoFactionMember;
        Faction faction;
        FactionMember factionMember;
        mongoFaction = new MongoFaction(collectionFactions, factionName, tag, color, ownerId, password);
        mongoFaction.save();
        mapMongoFactions.put(mongoFaction.getUniqueId(), mongoFaction);
        faction = new Faction(mongoFaction);
        faction.setChatChannel(createChatChannel(faction));
        mapFactionsByUniqueId.put(mongoFaction.getUniqueId(), faction);
        mapFactionsByTag.put(faction.getFactionTag(), faction);
        mapFactionsByName.put(faction.getFactionName().toLowerCase(), faction);
        mongoFactionMember = mapMongoFactionMembers.get(ownerId);
        if (mongoFactionMember == null) {
            mongoFactionMember = new MongoFactionMember(collectionFactionMembers, ownerId, faction.getUniqueId());
            mongoFactionMember.save();
            mapMongoFactionMembers.put(mongoFactionMember.getPlayerId(), mongoFactionMember);
        }
        factionMember = mapFactionMembersByUniqueId.get(ownerId);
        if (factionMember == null) {
            factionMember = new FactionMember(mongoFactionMember);
            mapFactionMembersByUniqueId.put(factionMember.getPlayerId(), factionMember);
        }
        factionMember.setFaction(faction, false);
        return faction;
    }

    /**
     * Removes a Faction from the database, and removes FactionMembers from the
     * Faction.
     *
     * @param faction The Faction being deleted.
     */
    public void deleteFaction(Faction faction) {
        if (faction == null) {
            throw new IllegalArgumentException("Faction given is null!");
        }
        // Remove the faction from the map(s) that store it.
        mapFactionsByUniqueId.remove(faction.getUniqueId());
        mapFactionsByTag.remove(faction.getFactionTag());
        mapFactionsByName.remove(faction.getFactionName().toLowerCase());
        MongoFaction mongoFaction = faction.getMongoDocument();
        mongoFaction.delete();
        mapMongoFactions.remove(mongoFaction.getUniqueId());
        // Grab the faction members.
        List<FactionMember> listMembers = new ArrayList<>(faction.getMembers());
        // Go through each one.
        for (FactionMember factionMember : listMembers) {
            factionMember.setFaction(null, false);
            // Grab the database document.
            MongoFactionMember mongoFactionMember = factionMember.getMongoDocument();
            // delete this.
            mongoFactionMember.delete();
            // Remove the document from the map.
            mapMongoFactionMembers.remove(mongoFactionMember.getPlayerId());
            // Remove the lua object from the map.
            mapFactionMembersByUniqueId.remove(factionMember.getPlayerId());
        }
        // Remove the ChatChannel for the Faction.
        removeChatChannel(faction);
    }

    /**
     * @param tag The String Faction tag being validated.
     * @return Returns a Response that is never null. If the validation is a
     * success, response.getResult() should return Result.SUCCESS.
     */
    public Response validateFactionTag(String tag) {
        // Create the valid Response. All subsequent checks will return a Result.FAILURE
        // if the tag fails a validation check.
        Response response = new Response("Success!", "", Result.SUCCESS);
        // Check and see if the tag is null or is a blank entry.
        if (tag == null || tag.isEmpty()) {
            return new Response("Faction tag is invalid.", "", Result.FAILURE);
        }
        // Grab the current set range for characters in a tag.
        int charsMin = getTagMinimumCharacterCount();
        int charsMax = getTagMaximumCharacterCount();
        // Trim the tag, just in-case spaces are passed.
        tag = tag.trim();
        // Grab the length of the tag.
        int lengthTag = tag.length();
        // If the Tag is too small or too large, prompt the user with the current limits
        // set.
        if (lengthTag < charsMin || lengthTag > charsMax) {
            return new Response(
                    "Faction tags need to be between " + charsMin + " and " + charsMax + " characters long.", "",
                    Result.FAILURE);
        }
        // All tags are forced upper-case.
        tag = tag.toUpperCase();

        // Check to see if the tag is already being used by another Faction.
        if (tagExists(tag)) {
            return new Response("Faction tag is already taken: " + tag, "", Result.FAILURE);
        }
        return response;
    }

    /**
     * Properly assigns a new String tag for a Faction.
     *
     * @param faction The Faction being re-tagged.
     * @param tagNew  The String tag to set for the Faction.
     */
    public void setFactionTag(Faction faction, String tagNew) {
        // Remove the old tag key.
        mapFactionsByTag.remove(faction.getFactionTag());
        // Set the new tag.
        faction.setFactionTag(tagNew, true);
        // Place the faction back into the tag map with the nww tag.
        mapFactionsByTag.put(faction.getFactionTag(), faction);
    }

    /**
     * @param name The String Faction name being validated.
     * @return Returns a Response that is never null. If the validation is a
     * success, response.getResult() should return Result.SUCCESS.
     */
    public Response validateFactionName(String name) {
        // Create the valid Response. All subsequent checks will return a Result.FAILURE
        // if the tag fails a validation check.
        Response response = new Response("Success!", "", Result.SUCCESS);
        // Check and see if the tag is null or is a blank entry.
        if (name == null || name.isEmpty()) {
            return new Response("Faction name is invalid.", "", Result.FAILURE);
        }
        // Grab the current set range for characters in a name.
        int charsMin = getNameMinimumCharacterCount();
        int charsMax = getNameMaximumCharacterCount();
        // Trim the name, just in-case spaces are passed.
        name = name.trim();
        // Grab the length of the name.
        int lengthTag = name.length();
        // If the Tag is too small or too large, prompt the user with the current limits
        // set.
        if (lengthTag < charsMin || lengthTag > charsMax) {
            return new Response(
                    "Faction names need to be between " + charsMin + " and " + charsMax + " characters long.", "",
                    Result.FAILURE);
        }
        // Check to see if the tag is already being used by another Faction.
        if (factionNameExists(name)) {
            return new Response("Faction name is already taken: " + name, "", Result.FAILURE);
        }
        return response;
    }

    /**
     * Returns whether or not the Player is valid to create a Faction
     *
     * @param player The Player being validated.
     * @return Returns a Response based on the results of the tests. If all tests
     * pass, Result.SUCCESS is passed to the Response.
     */
    public Response validatePlayerFactionCreate(Player player) {
        // Create the valid Response. All subsequent checks will return a Result.FAILURE
        // if the tag fails a validation check.
        Response response = new Response("Success!", "", Result.SUCCESS);
        UUID playerId = player.getUniqueId();
        // If the player is a member of a faction, he cannot create one. Attempt to grab
        // the member from the module.
        FactionMember factionMember = getFactionMember(player.getUniqueId());
        // If the player is indeed a member of a faction, display the proper failure
        // message.
        if (factionMember != null) {
            // Grab the Faction for the FactionMember.
            Faction faction = factionMember.getFaction();
            // Data consistency check.
            if (faction == null) {
                throw new IllegalStateException("FactionMember exists for player ID: \"" + playerId.toString()
                        + "\", yet the Faction representing the member is null.");
            }
            // If the member is the owner, describe how to get to the point to make a new
            // faction.
            if (faction.isOwner(factionMember)) {
                return new Response("You already own a faction: \"" + faction.getFactionName()
                        + "\". In order to create a new faction, you must first disband it, or transfer ownership and leave it.",
                        "", Result.FAILURE);
            }
            // If the member is not the owner, describe leaving the faction in order to make
            // a new faction.
            else {
                return new Response("You're already in a faction, and must leave in order to create a new faction.", "",
                        Result.FAILURE);
            }
        }
        return response;
    }

    /**
     * @param playerId The Unique ID of the Player being checked.
     * @return Returns true if the Player being checked owns a Faction.
     */
    public boolean playerOwnsFaction(UUID playerId) {
        boolean returned = false;
        // Grab the member using the ID.
        FactionMember member = getFactionMember(playerId);
        // If the member container is null, the Player is currently not in a Faction,
        // and thus not an owner.
        if (member != null) {
            // Grab the Faction.
            Faction faction = member.getFaction();
            // Data consistency check.
            if (faction == null) {
                throw new IllegalStateException("FactionMember exists for player ID: \"" + playerId.toString()
                        + "\", yet the Faction representing the member is null.");
            }
            // Check if the OwnerID is the member's ID.
            returned = faction.getOwnerId().equals(member.getPlayerId());
        }
        return returned;
    }

    /**
     * Properly removes a FactionMember from a Faction, and removes references.
     *
     * @param factionMember The FactionMember being processed.
     */
    public void removeFactionMember(FactionMember factionMember) {
        factionMember.setFaction(null, false);
        // Grab the FactionMember container.
        MongoFactionMember mongoFactionMember = factionMember.getMongoDocument();
        // Delete the document.
        mongoFactionMember.delete();
        mapMongoFactionMembers.remove(mongoFactionMember.getPlayerId());
        // Grab the Faction.
        Faction faction = factionMember.getFaction();
        // Remove the FactionMember from the Faction member list.
        faction.removeMember(factionMember);
        // Remove the Member from the map.
        mapFactionMembersByUniqueId.remove(factionMember.getPlayerId());
    }

    /**
     * Creates a FactionMember container for the given Player, and adds this to
     * the Faction provided.
     *
     * @param player  The Player being given a representing FactionMember container.
     * @param faction The Faction the result FactionMember is being assigned to.
     * @return A FactionMember container for the given Player.
     */
    public FactionMember createFactionMember(Player player, Faction faction) {
        return createFactionMember(player.getUniqueId(), faction);
    }

    /**
     * Creates a FactionMember container for the given Player, and adds this to
     * the Faction provided.
     *
     * @param playerId The Unique ID of the Player.
     * @param faction  The Faction the result FactionMember is being assigned to.
     * @return A FactionMember container for the given Player.
     */
    public FactionMember createFactionMember(UUID playerId, Faction faction) {
        // Create & add the MongoDocument.
        MongoFactionMember mongoFactionMember = new MongoFactionMember(collectionFactionMembers, playerId,
                faction.getUniqueId());
        mapMongoFactionMembers.put(mongoFactionMember.getPlayerId(), mongoFactionMember);
        // Create the container.
        FactionMember factionMember = new FactionMember(mongoFactionMember);
        // Assign the faction to the container.
        factionMember.setFaction(faction, true);
        // Add the container to the map.
        mapFactionMembersByUniqueId.put(factionMember.getPlayerId(), factionMember);
        return factionMember;
    }

    /**
     * @param tag The String tag of the Faction.
     * @return Returns a Faction if one exists using the tag provided. Returns
     * null if no Faction uses the tag provided.
     */
    public Faction getFactionByTag(String tag) {
        // Consistency check. All Tags are in upper-case.
        tag = tag.toUpperCase();
        return mapFactionsByTag.get(tag);
    }

    /**
     * @param playerId The Unique ID of the Player.
     * @return Returns a FactionMember. Returns null
     * if the Player is not a member of a Faction.
     */
    public FactionMember getFactionMember(UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Unique ID given is null!");
        }
        return mapFactionMembersByUniqueId.get(playerId);
    }

    /**
     * @param player The Player being looked up.
     * @return Returns a FactionMember with a given Player. Returns null if the
     * Player is not a member of a Faction.
     */
    public FactionMember getFactionMember(Player player) {
        return getFactionMember(player.getUniqueId());
    }

    /**
     * @param factionId The Unique ID of the Faction.
     * @return Returns the Faction. If there is
     * none, null is returned.
     */
    public Faction getFaction(UUID factionId) {
        return mapFactionsByUniqueId.get(factionId);
    }

    /**
     * @return Returns the minimum amount of characters in a Faction tag.
     */
    public int getTagMinimumCharacterCount() {
        return this.tagCharactersMinimum;
    }

    /**
     * @return Returns the maximum amount of characters in a Faction tag.
     */
    public int getTagMaximumCharacterCount() {
        return this.tagCharactersMaximum;
    }

    /**
     * @return Returns the minimum amount of characters in a Faction name.
     */
    public int getNameMinimumCharacterCount() {
        return this.nameCharactersMinimum;
    }

    /**
     * @return Returns the maximum amount of characters in a Faction name.
     */
    public int getNameMaximumCharacterCount() {
        return this.nameCharactersMaximum;
    }

    /**
     * TODO: Document.
     *
     * @param factionName The name of the Faction.
     * @return Returns true if a Faction exists with the given name.
     */
    public boolean factionExists(String factionName) {
        return this.getFactionByName(factionName) != null;
    }

    /**
     * @param tag The String tag of the Faction.
     * @return Returns true if a Faction is currently using the provided tag. (The
     * tag is forced upper-case on the check. All Faction tags are
     * upper-case).
     */
    public boolean tagExists(String tag) {
        return getFactionByTag(tag) != null;
    }

    /**
     * @param name The String name to be tested.
     * @return Returns true if A Faction uses the String name given.
     */
    private boolean factionNameExists(String name) {
        return getFactionByName(name) != null;
    }

    /**
     * @param name The String name of the Faction.
     * @return Returns a Faction, if one uses this name.
     */
    public Faction getFactionByName(String name) {
        return this.mapFactionsByName.get(name.toLowerCase());
    }

    @Override
    public String getName() {
        return "ModuleFactions";
    }

    public FactionActions getActions() {
        return this.actions;
    }

    public List<FactionInvite> getInvitesForPlayer(Player player) {
        return getInvitesForPlayer(player.getUniqueId());
    }

    /**
     * @param playerId The Unique ID of the Player.
     * @return A LinkedList of FactionInvites for a Player. If the Player has
     * no invites, the list will return empty.
     */
    public List<FactionInvite> getInvitesForPlayer(UUID playerId) {
        // Create the List.
        List<FactionInvite> listInvites = new LinkedList<>();
        // Go through every Invite.
        for (FactionInvite factionInvite : mapFactionInvites.values()) {
            // If the UUID of the invite's invited matches the given playerId, then add this
            // to the list. This is a invite for the player.
            if (factionInvite.getInvitedId().equals(playerId)) {
                listInvites.add(factionInvite);
            }
        }
        // Return the result list with the Invites.
        return listInvites;
    }

    /**
     * Returns a FactionInvite for a given Player from the given Faction.
     *
     * @param player  The Player being invited to the Faction.
     * @param faction The Faction inviting the Player.
     * @return Returns a FactionInvite if a invite exists for the Player from
     * the Faction. Returns null if a invite does not exist.
     */
    public FactionInvite getFactionInvite(Player player, Faction faction) {
        // The Invite Object to return.
        FactionInvite factionInviteReturned = null;
        // Grab all the Invites associated with the Player.
        List<FactionInvite> listFactionInvites = getInvitesForPlayer(player);
        for (FactionInvite factionInvite : listFactionInvites) {
            // If the Invite is for this Faction, Select it.
            if (factionInvite.getFactionId().equals(faction.getUniqueId())) {
                factionInviteReturned = factionInvite;
                break;
            }
        }
        // Return the result.
        return factionInviteReturned;
    }

    /**
     * @param factionInvite The FactionInvite being accepted.
     * @return Returns a Response. If the accept is successful, the Result
     * included will be SUCCESS.
     */
    public Response acceptInvite(FactionInvite factionInvite) {

        UUID playerId = factionInvite.getInvitedId();
        // Check to make sure the Player still exists.
        boolean exists = SledgeHammer.instance.playerExists(playerId);
        if (!exists) {
            deleteInvite(factionInvite);
        }
        // Grab the Faction representing the invite.
        Faction faction = this.getFaction(factionInvite.getFactionId());
        if (faction == null) {
            // The Faction probably does not exist anymore. Remove the invite.
            deleteInvite(factionInvite);
            // Return this response.
            return new Response("Faction does not exist.", "", Result.FAILURE);
        }
        // Attempt to grab a player container.
        FactionMember factionMember = getFactionMember(playerId);
        if (factionMember != null) {
            factionMember.setFaction(faction, true);
        } else {
            createFactionMember(playerId, faction);
        }
        // Delete the Invite.
        deleteInvite(factionInvite);
        // Return success.
        return new Response("Invite accepted.", "", Result.SUCCESS);
    }

    /**
     * Deletes and properly removes a FactionInvite, and its representing document.
     *
     * @param factionInvite The FactionInvite being deleted and removed.
     */
    public void deleteInvite(FactionInvite factionInvite) {
        // Grab the MongoDocument.
        MongoFactionInvite mongoFactionInvite = factionInvite.getMongoDocument();
        // Delete this in the database.
        mongoFactionInvite.delete();
        // Remove the MongoDocument from the map.
        mapMongoFactionInvites.remove(mongoFactionInvite.getUniqueId());
        // Remove the container Object from the map.
        mapFactionInvites.remove(factionInvite.getUniqueId());
    }

    /**
     * Sets a Faction's name.
     *
     * @param faction The Faction to change.
     * @param nameNew The name to set for the Faction.
     */
    public void setFactionName(Faction faction, String nameNew) {
        this.mapFactionsByName.remove(faction.getFactionName().toLowerCase());
        faction.setFactionName(nameNew, true);
    }

    /**
     * @param faction The Faction that is using the ChatChannel
     * @return Returns a ChatChannel for the given Faction.
     */
    public ChatChannel getChatChannel(Faction faction) {
        return getChatChannel("Faction_" + faction.getFactionName());
    }

    /**
     * Creates a new ChatChannel for a given Faction.
     *
     * @param faction The Faction the ChatChannel is for.
     * @return Returns a ChatChannel for the given Faction.
     */
    public ChatChannel createChatChannel(Faction faction) {
        // Creates a new ChatChannel wrapper for the Faction.
//		ChatChannel chatChannel = createChatChannel("Faction_" + faction.getFactionName());
        // new FactionChatChannel("Faction_" + faction.getFactionName());
//		chatChannel.setPermissionNode("sledgehammer.factions.chat", false);
//		chatChannel.setPublicChannel(false, false);
//		chatChannel.setGlobalChannel(true, false);
        // Return the new ChatChannel.
//		return chatChannel;
        return null;
    }

    /**
     * Unregisters and removes the ChatChannel associated with the given Faction.
     *
     * @param faction The Faction that the ChatChannel being unregistered is being
     *                removed.
     * @return Returns the ChatChannel associated with the Faction being
     * removed.
     */
    public ChatChannel removeChatChannel(Faction faction) {
        // Grab the channel.
        ChatChannel chatChannel = faction.getChatChannel();
        // Data consistency check.
        if (chatChannel == null) {
            throw new IllegalStateException("Faction ChatChannel is null!");
        }
        // Unregister the channel entirely.
        unregisterChatChannel(chatChannel);
        // Return the unregistered channel.
        return chatChannel;
    }

    /**
     * Creates and registers a FactionInvite.
     *
     * @param factionOwner    The Faction being invited to.
     * @param playerIdOwner   The Unique ID of the Player inviting.
     * @param playerIdInvited The Unique ID of the Player invited.
     */
    public void createFactionInvite(Faction factionOwner, UUID playerIdOwner, UUID playerIdInvited) {
        // Create the MongoDB document.
        MongoFactionInvite mongoFactionInvite = new MongoFactionInvite(collectionFactionInvites,
                factionOwner.getUniqueId(), playerIdOwner, playerIdInvited);
        // Add the document to the map.
        mapMongoFactionInvites.put(mongoFactionInvite.getUniqueId(), mongoFactionInvite);
        // Create the Lua container.
        FactionInvite factionInvite = new FactionInvite(mongoFactionInvite);
        // Add the container to the map.
        mapFactionInvites.put(factionInvite.getUniqueId(), factionInvite);
    }
}