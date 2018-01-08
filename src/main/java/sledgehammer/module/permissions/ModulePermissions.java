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
package sledgehammer.module.permissions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mongodb.DBCursor;

import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoCollection;
import sledgehammer.database.module.core.SledgehammerDatabase;
import sledgehammer.database.module.permissions.MongoPermissionGroup;
import sledgehammer.database.module.permissions.MongoPermissionUser;
import sledgehammer.enums.Result;
import sledgehammer.language.EntryField;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.permissions.PermissionGroup;
import sledgehammer.lua.permissions.PermissionUser;
import sledgehammer.plugin.MongoModule;
import sledgehammer.util.Response;

/**
 * Module class that handles operations for Permissions.
 *
 * @author Jab
 */
public class ModulePermissions extends MongoModule {

    /**
     * The MongoCollection storing the MongoPermissionGroup documents.
     */
    private MongoCollection collectionGroups;
    /**
     * The MongoCollection storing the MongoPermissionUser documents.
     */
    private MongoCollection collectionUsers;
    /**
     * The Map storing the MongoPermissionGroup documents.
     */
    private Map<UUID, MongoPermissionGroup> mapMongoPermissionGroups;
    /**
     * The Map storing the MongoPermissionUser documents.
     */
    private Map<UUID, MongoPermissionUser> mapMongoPermissionUsers;
    /**
     * The Map storing the PermissionGroup containers.
     */
    private Map<UUID, PermissionGroup> mapPermissionGroups;
    /**
     * The Map storing the PermissionUser containers.
     */
    private Map<UUID, PermissionUser> mapPermissionUsers;
    private PermissionsCommandListener permissionsCommandListener;
    /**
     * The default PermissionGroup to base decisions for players not assigned to
     * groups.
     */
    private PermissionGroup permissionGroupDefault;
    /**
     * The LanguagePackage for the Permissions Module.
     */
    private LanguagePackage lang;

    /**
     * Main constructor.
     */
    public ModulePermissions() {
        super(getDefaultDatabase());
    }

    @Override
    public void onLoad() {
        // Make sure that the core language file(s) are provided.
        saveResourceAs("lang/permissions_en.yml", new File(getLanguageDirectory(), "permissions_en.yml"), false);
        // Load the LanguagePackage.
        lang = new LanguagePackage(getLanguageDirectory(), "permissions");
        lang.load();
        // Grab the database we are using to store permission data.
        SledgehammerDatabase database = SledgeHammer.instance.getDatabase();
        // Grab the collection for permission groups.
        collectionGroups = database.createMongoCollection("sledgehammer_permission_groups");
        // Grab the collection for permission users.
        collectionUsers = database.createMongoCollection("sledgehammer_permission_users");
        // Load the Permission groups first.
        loadPermissionGroups();
        // Load the Permission users next.
        loadPermissionUsers();
        // Connect the data from groups to users.
        assignObjects();
        // Create the Default PermissionGroup.
        MongoPermissionGroup mongoPermissionGroupDefault = new MongoPermissionGroup(collectionGroups, "default");
        // The default PermissionGroup.
        permissionGroupDefault = new PermissionGroup(mongoPermissionGroupDefault);
        PermissionsListener permissionsListener = new PermissionsListener(this);
        setPermissionListener(permissionsListener);
        permissionsCommandListener = new PermissionsCommandListener(this);
        register(permissionsCommandListener);
    }

    @Override
    public void onUnload() {
        unregister(permissionsCommandListener);
        mapMongoPermissionGroups.clear();
        mapMongoPermissionUsers.clear();
        mapPermissionGroups.clear();
        mapPermissionUsers.clear();
        collectionGroups = null;
        collectionUsers = null;
        setPermissionListener(null);
    }

    /**
     * (Private Method)
     * <p>
     * Loads the MongoDocuments, and the Lua containers for permission groups.
     */
    private void loadPermissionGroups() {
        // Create the Map to store the MongoDB documents for groups.
        mapMongoPermissionGroups = new HashMap<>();
        // Create a query for all documents in the groups collection.
        DBCursor cursor = collectionGroups.find();
        // Go through all available documents.
        while (cursor.hasNext()) {
            // Wrap the document in the proper container object.
            MongoPermissionGroup mongoPermissionGroup = new MongoPermissionGroup(collectionGroups, cursor.next());
            // Assign it to the map for later reference.
            mapMongoPermissionGroups.put(mongoPermissionGroup.getUniqueId(), mongoPermissionGroup);
        }
        // We are done here with the query so we close to free resources and memory.
        cursor.close();
        // Create the Map to store the Lua container objects.
        mapPermissionGroups = new HashMap<>();
        // Go through each loaded MongoDB document.
        for (MongoPermissionGroup mongoPermissionGroup : mapMongoPermissionGroups.values()) {
            // Create a new Lua container with the MongoDB document.
            PermissionGroup permissionGroup = new PermissionGroup(mongoPermissionGroup);
            // Assign it to the map for later reference.
            mapPermissionGroups.put(permissionGroup.getUniqueId(), permissionGroup);
        }
    }

    /**
     * (Private Method)
     * <p>
     * Loads the MongoDocuments, and the Lua containers for permission users.
     */
    private void loadPermissionUsers() {
        // Create the map to sore the MongoDB documents for users.
        mapMongoPermissionUsers = new HashMap<>();
        // Create a query for all documents in the users collection.
        DBCursor cursor = collectionUsers.find();
        // Go through all available documents.
        while (cursor.hasNext()) {
            // Wrap the document in the proper container object.
            MongoPermissionUser mongoPermissionUser = new MongoPermissionUser(collectionUsers, cursor.next());
            // Assign it to the map for later reference.
            mapMongoPermissionUsers.put(mongoPermissionUser.getUniqueId(), mongoPermissionUser);
        }
        // We are done here with the query so we close to free resources and memory.
        cursor.close();
        // Create the Map to store the Lua container objects.
        mapPermissionUsers = new HashMap<>();
        // Go through each loaded MongoDB document.
        for (MongoPermissionUser mongoPermissionUser : mapMongoPermissionUsers.values()) {
            // Create a new Lua container with the MongoDB document.
            PermissionUser permissionUser = new PermissionUser(mongoPermissionUser);
            // Assign it to the map for later reference.
            mapPermissionUsers.put(permissionUser.getUniqueId(), permissionUser);
        }
    }

    /**
     * (Private Method)
     * <p>
     * Pairs up PermissionUser to PermissionGroup and vice versa.
     */
    private void assignObjects() {
        // Go through each PermissionUser.
        for (PermissionUser permissionUser : mapPermissionUsers.values()) {
            // Grab the Group ID for the group that needs to be linked to.
            UUID groupId = permissionUser.getGroupId();
            // If the group is defined.
            if (groupId != null) {
                // Grab the permission Group.
                PermissionGroup group = getPermissionGroup(groupId);
                // If we have a UUID, but the UUID fails to return a result, this is a database
                // inconsistency,
                // and this is likely do to a bug in the code.
                if (group == null) {
                    errln("PermissionUser \"" + permissionUser.getUniqueId().toString() + "\""
                            + "is assigned to a group that does not exist: \"" + groupId.toString() + "\".");
                    errln("Setting groupId for the PermissionUser to null.");
                    // Set the group UUID to null.
                    permissionUser.setPermissionGroup(null, true);
                    // Continue to the next user.
                    continue;
                }
                // Set the member to the group. (This also sets the member's group object.
                // Note: We do not save this action because we are setting the data during
                // initializing the module.
                group.addMember(permissionUser, false);
            }
        }
    }

    /**
     * (CommandListener Method)
     * <p>
     * Attempts to create a PermissionUser.
     *
     * @param commander The Player that sent the command.
     * @param username  The Player's user-name.
     * @return Returns a Response that contains the Result of the Command, and the details for that Result.
     */
    public Response commandCreatePermissionUser(Player commander, String username) {
        // The Response to return.
        Response response = new Response();
        // Grab the Language set by the Player.
        Language language = commander.getLanguage();
        // Make sure the user-name given is valid.
        if (username == null || username.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permission_user_name_empty", language));
            return response;
        }
        // Pass this to the LanguagePackage to show the name passed when responding to the Player.
        EntryField fieldUsername = new EntryField("username", username);
        // Grab the Player by using the name-fragment search method.
        Player player = SledgeHammer.instance.getPlayerDirty(username);
        // If the Player is null, then search offline for the Player.
        if (player == null) {
            // Search offline with the strict username search method.
            player = SledgeHammer.instance.getOfflinePlayer(username);
        }
        // If the Player is null, then no Player could be found using the given user-name.
        if (player == null) {
            response.set(Result.FAILURE, lang.getString("player_not_found", language, fieldUsername));
            return response;
        }
        // Pass this to the LanguagePackage to show the proper name of the Player when responding
        // to the commanding Player.
        EntryField fieldPlayer = new EntryField("player", player.getName());
        // Grab the Unique ID of the Player. This is shared between the Player and the PermissionUser,
        // and is used for identification between the two.
        UUID playerId = player.getUniqueId();
        // Attempt to grab the PermissionUser if it exists.
        PermissionUser permissionUser = getPermissionUser(playerId);
        // If it exists, then let the commanding Player know.
        if (permissionUser != null) {
            response.set(Result.FAILURE, lang.getString("permission_user_exists", language, fieldPlayer));
            return response;
        }
        try {
            // Create the document.
            MongoPermissionUser mongoPermissionUser = new MongoPermissionUser(collectionUsers, playerId);
            // Create the container for the document.
            permissionUser = new PermissionUser(mongoPermissionUser);
            // Put the document in the map.
            mapMongoPermissionUsers.put(playerId, mongoPermissionUser);
            // Put the container in the map.
            mapPermissionUsers.put(playerId, permissionUser);
            // Save the document.
            mongoPermissionUser.save();
            // Set the response to success.
            response.set(Result.SUCCESS, lang.getString("command_permissions_user_create_success", language, fieldPlayer));
        } catch (Exception e) {
            stackTrace(e);
            response.set(Result.FAILURE, lang.getString("command_permissions_user_create_failure", language, fieldPlayer));
        }
        return response;
    }

    /**
     * (CommandListener Method)
     * <p>
     * Attempts to delete a PermissionGroup.
     *
     * @param commander The Player that sent the command.
     * @param username  The Player's user-name.
     * @return Returns a Response that contains the Result of the Command, and the details for that Result.
     */
    public Response commandDeletePermissionUser(Player commander, String username) {
        // The Response to return.
        Response response = new Response();
        // Grab the Language set by the Player.
        Language language = commander.getLanguage();
        // Make sure the user-name given is valid.
        if (username == null || username.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permission_user_name_empty", language));
            return response;
        }
        // Pass this to the LanguagePackage to show the name passed when responding to the Player.
        EntryField fieldUsername = new EntryField("username", username);
        // Grab the Player by using the name-fragment search method.
        Player player = SledgeHammer.instance.getPlayerDirty(username);
        // If the Player is null, then search offline for the Player.
        if (player == null) {
            // Search offline with the strict username search method.
            player = SledgeHammer.instance.getOfflinePlayer(username);
        }
        // If the Player is null, then no Player could be found using the given user-name.
        if (player == null) {
            response.set(Result.FAILURE, lang.getString("player_not_found", language, fieldUsername));
            return response;
        }
        // Pass this to the LanguagePackage to show the proper name of the Player when responding
        // to the commanding Player.
        EntryField fieldPlayer = new EntryField("player", player.getName());
        // Grab the Unique ID of the Player. This is shared between the Player and the PermissionUser,
        // and is used for identification between the two.
        UUID playerId = player.getUniqueId();
        // Attempt to grab the PermissionUser if it exists.
        PermissionUser permissionUser = getPermissionUser(playerId);
        // If it exists, then let the commanding Player know.
        if (permissionUser == null) {
            response.set(Result.FAILURE, lang.getString("permission_user_not_found", language, fieldPlayer));
            return response;
        }
        try {
            // Remove the document from the map.
            mapMongoPermissionUsers.remove(playerId);
            // Remove the container from the map.
            mapPermissionUsers.remove(playerId);
            // Delete the document properly.
            permissionUser.getMongoDocument().delete();
            // Grab the group of the user.
            PermissionGroup permissionGroup = permissionUser.getPermissionGroup();
            // If the user is assigned to a group, unlink the user from the members list.
            if (permissionGroup != null) {
                permissionGroup.removeMember(permissionUser, true);
            }
            response.set(Result.SUCCESS, lang.getString("command_permissions_user_delete_success", language, fieldPlayer));
        } catch (Exception e) {
            stackTrace(e);
            response.set(Result.FAILURE, lang.getString("command_permissions_user_delete_failure", language, fieldPlayer));
        }
        return response;
    }

    /**
     * (CommandListener Method)
     * <p>
     * Sets a Permission Node definition for a PermissionUser.
     *
     * @param commander The Player that sent the command.
     * @param username  The Player's user-name.
     * @param node      The Permission Node to set.
     * @param flag      The Permission Node value.
     * @return Returns a Response that contains the Result of the Command, and the details for that Result.
     */
    public Response commandSetPermissionUserNode(Player commander, String username, String node, String flag) {
        // The Response to return.
        Response response = new Response();
        // Grab the Language set by the Player.
        Language language = commander.getLanguage();
        // Make sure the user-name given is valid.
        if (username == null || username.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permission_user_name_empty", language));
            return response;
        }
        // Make sure the node entry is valid.
        if (node == null || node.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_node_empty", language));
            return response;
        }
        node = node.toLowerCase();
        if (flag == null || flag.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_flag_empty", language));
            return response;
        }
        boolean remove = false;
        if (flag.equalsIgnoreCase("none") || flag.equalsIgnoreCase("null") || flag.equalsIgnoreCase("nil")) {
            remove = true;
        }
        boolean flagValue = !remove && (flag.equals("1") || flag.equalsIgnoreCase("true") || flag.equalsIgnoreCase("on") || flag.equalsIgnoreCase("yes"));
        // Pass these to the LanguagePackage to show the name passed when responding to the Player. @formatter:off
        EntryField fieldName = new EntryField("username", username );
        EntryField fieldNode = new EntryField("node"    , node     );
        EntryField fieldFlag = new EntryField("flag"    , flagValue);
        // Grab the Player by using the name-fragment search method. @formatter:on
        Player player = SledgeHammer.instance.getPlayerDirty(username);
        // If the Player is null, then search offline for the Player.
        if (player == null) {
            // Search offline with the strict username search method.
            player = SledgeHammer.instance.getOfflinePlayer(username);
        }
        // If the Player is null, then no Player could be found using the given user-name.
        if (player == null) {
            response.set(Result.FAILURE, lang.getString("player_not_found", language, fieldName));
            return response;
        }
        // Pass this to the LanguagePackage to show the proper name of the Player when responding
        // to the commanding Player.
        EntryField fieldPlayer = new EntryField("player", player.getName());
        // Grab the Unique ID of the Player. This is shared between the Player and the PermissionUser,
        // and is used for identification between the two.
        UUID playerId = player.getUniqueId();
        // Attempt to grab the PermissionUser if it exists.
        PermissionUser permissionUser = getPermissionUser(playerId);
        // If it exists, then let the commanding Player know.
        if (permissionUser == null) {
            response.set(Result.FAILURE, lang.getString("permission_user_not_found", language, fieldPlayer));
            return response;
        }
        try {
            // Set Permission Node.
            permissionUser.setPermission(node, remove ? null : flagValue, true);
            response.set(Result.SUCCESS, lang.getString("command_permissions_user_set_node_success", language, fieldPlayer, fieldNode, fieldFlag));
        } catch (Exception e) {
            stackTrace(e);
            response.set(Result.FAILURE, lang.getString("command_permissions_user_set_node_failure", language, fieldPlayer, fieldNode, fieldFlag));
        }
        return response;
    }

    /**
     * (CommandListener Method)
     * <p>
     * Sets a PermissionUser's PermissionGroup.
     *
     * @param commander           The Player that sent the command.
     * @param username            The Player's user-name.
     * @param permissionGroupName The name of the PermissionGroup
     * @return Returns a Response that contains the Result of the Command, and the details for that Result.
     */
    public Response commandSetPermissionUserGroup(Player commander, String username, String permissionGroupName) {
        // The Response to return.
        Response response = new Response();
        // Grab the Language set by the Player.
        Language language = commander.getLanguage();
        // Make sure the user-name given is valid.
        if (username == null || username.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permission_user_name_empty", language));
            return response;
        }
        // Make sure the name given is valid.
        if (permissionGroupName == null || permissionGroupName.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_name_empty", language));
            return response;
        }
        // Pass these to the LanguagePackage to show the name passed when responding to the Player.
        EntryField fieldName = new EntryField("name", permissionGroupName);
        EntryField fieldUsername = new EntryField("username", username);
        // Grab the Player by using the name-fragment search method.
        Player player = SledgeHammer.instance.getPlayerDirty(username);
        // If the Player is null, then search offline for the Player.
        if (player == null) {
            // Search offline with the strict username search method.
            player = SledgeHammer.instance.getOfflinePlayer(username);
        }
        // If the Player is null, then no Player could be found using the given user-name.
        if (player == null) {
            response.set(Result.FAILURE, lang.getString("player_not_found", language, fieldUsername));
            return response;
        }
        // Pass this to the LanguagePackage to show the proper name of the Player when responding
        // to the commanding Player.
        EntryField fieldPlayer = new EntryField("player", player.getName());
        // Grab the Unique ID of the Player. This is shared between the Player and the PermissionUser,
        // and is used for identification between the two.
        UUID playerId = player.getUniqueId();
        // Attempt to grab the PermissionUser if it exists.
        PermissionUser permissionUser = getPermissionUser(playerId);
        // If it exists, then let the commanding Player know.
        if (permissionUser == null) {
            response.set(Result.FAILURE, lang.getString("permission_user_not_found", language, fieldPlayer));
            return response;
        }
        PermissionGroup permissionGroup = getPermissionGroup(permissionGroupName);
        // Make sure that a PermissionGroup exists with the given name.
        if (permissionGroup == null) {
            response.set(Result.FAILURE, lang.getString("permissions_group_not_found", language, fieldName));
            throw new IllegalArgumentException("PermissionGroup given is null.");
        }
        try {
            permissionGroup.addMember(permissionUser, true);
            response.set(Result.FAILURE, lang.getString("command_permissions_user_set_group_success", language, fieldPlayer, fieldName));
        } catch (Exception e) {
            stackTrace(e);
            response.set(Result.FAILURE, lang.getString("command_permissions_user_set_group_failure", language, fieldPlayer, fieldName));
        }
        return response;
    }

    /**
     * (CommandListener Method)
     * <p>
     * Attempts to create a PermissionGroup with the given name.
     *
     * @param commander           The Player that sent the command.
     * @param permissionGroupName The name of the PermissionGroup to create.
     * @return Returns a Response that contains the Result of the Command, and the details for that Result.
     */
    public Response commandCreatePermissionGroup(Player commander, String permissionGroupName) {
        // The Response to return.
        Response response = new Response();
        // Grab the Language set by the Player.
        Language language = commander.getLanguage();
        // Make sure the name given is valid.
        if (permissionGroupName == null || permissionGroupName.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_name_empty", language));
            return response;
        }
        // Pass this to the LanguagePackage to show the name passed when responding to the Player.
        EntryField fieldName = new EntryField("name", permissionGroupName);
        // Attempt grabbing a PermissionGroup with the name provided.
        PermissionGroup permissionGroup = getPermissionGroup(permissionGroupName);
        // If a PermissionGroup is returned from the search, the name is already in use.
        if (permissionGroup != null) {
            response.set(Result.FAILURE, lang.getString("permissions_group_name_exists", language));
            return response;
        }
        try {
            // Create the document.
            MongoPermissionGroup mongoPermissionGroup = new MongoPermissionGroup(collectionGroups, permissionGroupName);
            // Create the container.
            permissionGroup = new PermissionGroup(mongoPermissionGroup);
            // Add the document to the map.
            mapMongoPermissionGroups.put(mongoPermissionGroup.getUniqueId(), mongoPermissionGroup);
            // Add the container to the map.
            mapPermissionGroups.put(permissionGroup.getUniqueId(), permissionGroup);
            // Save the document.
            mongoPermissionGroup.save();
            // Set the response to success.
            response.set(Result.SUCCESS, lang.getString("command_permissions_group_create_success", language, fieldName));
        } catch (Exception e) {
            stackTrace(e);
            response.set(Result.FAILURE, lang.getString("command_permissions_group_create_failure", language, fieldName));
        }
        return response;
    }

    /**
     * (CommandListener Method)
     * <p>
     * Attempts to delete a PermissionGroup with the given name.
     *
     * @param commander           The Player that sent the command.
     * @param permissionGroupName The name of the PermissionGroup to delete.
     * @return Returns a Response that contains the Result of the Command, and the details for that Result.
     */
    public Response commandDeletePermissionGroup(Player commander, String permissionGroupName) {
        // The Response to return.
        Response response = new Response();
        // Grab the Language set by the Player.
        Language language = commander.getLanguage();
        // Make sure the name given is valid.
        if (permissionGroupName == null || permissionGroupName.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_name_empty", language));
            return response;
        }
        // Pass this to the LanguagePackage to show the name passed when responding to the Player.
        EntryField fieldName = new EntryField("name", permissionGroupName);
        PermissionGroup permissionGroup = getPermissionGroup(permissionGroupName);
        // Make sure that a PermissionGroup exists with the given name.
        if (permissionGroup == null) {
            response.set(Result.FAILURE, lang.getString("permissions_group_not_found", language, fieldName));
            throw new IllegalArgumentException("PermissionGroup given is null.");
        }
        try {
            // Grab the ID for the PermissionGroup.
            UUID groupId = permissionGroup.getUniqueId();
            // Remove the document from the map.
            mapMongoPermissionGroups.remove(groupId);
            // Remove the container from the map.
            mapPermissionGroups.remove(groupId);
            // Delete the document properly.
            permissionGroup.getMongoDocument().delete();
            // Grab the parent of the group being deleted.
            PermissionGroup parent = permissionGroup.getParent();
            // Go through every registered PermissionGroup.
            for (PermissionGroup permissionGroupNext : mapPermissionGroups.values()) {
                // If the group next is the one we are deleting, or the parent of the one being
                // deleted, skip it.
                if (permissionGroupNext.equals(permissionGroup) || permissionGroupNext.equals(parent)) {
                    continue;
                }
                // If the next group's parent is the group being deleted, set the parent of the
                // group to the one of the group being deleted.
                if (permissionGroupNext.getParent().equals(permissionGroup)) {
                    permissionGroupNext.setParent(parent, true);
                }
            }
            // Sets the User to the parent group.
            for (PermissionUser member : permissionGroup.getMembers()) {
                member.setPermissionGroup(parent, true);
            }
            // Set the Response successful.
            response.set(Result.SUCCESS, lang.getString("command_permissions_group_delete_success", language, fieldName));

        } catch (Exception e) {
            stackTrace(e);
            response.set(Result.FAILURE, lang.getString("command_permissions_group_delete_failure", language, fieldName));
        }
        return response;
    }

    /**
     * (CommandListener Method)
     * <p>
     * Attempts to rename a PermissionGroup with the given name.
     *
     * @param commander              The Player that sent the command.
     * @param permissionGroupName    The name of the PermissionGroup to rename.
     * @param permissionGroupNameNew The name to set for the PermissionGroup.
     * @return Returns a Response that contains the Result of the Command, and the details for that Result.
     */
    public Response commandRenamePermissionGroup(Player commander, String permissionGroupName, String permissionGroupNameNew) {
        // The Response to return.
        Response response = new Response();
        // Grab the Language set by the Player.
        Language language = commander.getLanguage();
        // Make sure the name given is valid.
        if (permissionGroupName == null || permissionGroupName.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_name_empty", language));
            return response;
        }
        // Make sure the new name given is valid.
        if (permissionGroupNameNew == null || permissionGroupNameNew.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_name_empty", language));
            return response;
        }
        // Pass these to the LanguagePackage to show the name passed when responding to the Player.
        EntryField fieldName = new EntryField("name", permissionGroupName);
        EntryField fieldNameNew = new EntryField("name_new", permissionGroupNameNew);
        // Attempt grabbing a PermissionGroup with the name provided.
        PermissionGroup permissionGroup = getPermissionGroup(permissionGroupName);
        // If a PermissionGroup is returned from the search, the name is already in use.
        if (permissionGroup == null) {
            response.set(Result.FAILURE, lang.getString("permissions_group_not_found", language, fieldName));
            return response;
        }
        try {
            // Set the new name for the PermissionGroup.
            permissionGroup.setGroupName(permissionGroupNameNew, true);
            // Set the success message.
            response.set(Result.SUCCESS, lang.getString("command_permissions_group_rename_success", language, fieldName, fieldNameNew));
        } catch (Exception e) {
            stackTrace(e);
            // Set the failure message.
            response.set(Result.FAILURE, lang.getString("command_permissions_group_rename_failure", language, fieldName));
        }
        return response;
    }

    /**
     * (CommandListener Method)
     * <p>
     * Attempts to set the parent of a PermissionGroup.
     *
     * @param commander                 The Player that sent the command.
     * @param permissionGroupName       The name of the PermissionGroup to rename.
     * @param permissionGroupNameParent The name of the parent PermissionGroup to set.
     * @return Returns a Response that contains the Result of the Command, and the details for that Result.
     */
    public Response commandSetPermissionGroupParent(Player commander, String permissionGroupName, String permissionGroupNameParent) {
        // The Response to return.
        Response response = new Response();
        // Grab the Language set by the Player.
        Language language = commander.getLanguage();
        // Make sure the name given is valid.
        if (permissionGroupName == null || permissionGroupName.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_name_empty", language));
            return response;
        }
        // Make sure the parent name given is valid.
        if (permissionGroupNameParent == null || permissionGroupNameParent.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_name_empty", language));
            return response;
        }
        // Pass these to the LanguagePackage to show the name passed when responding to the Player.
        EntryField fieldName = new EntryField("name", permissionGroupName);
        EntryField fieldNameParent = new EntryField("name_parent", permissionGroupNameParent);
        // Grab the affected PermissionGroup.
        PermissionGroup permissionGroup = getPermissionGroup(permissionGroupName);
        // Make sure the affected PermissionGroup exists.
        if (permissionGroup == null) {
            response.set(Result.FAILURE, lang.getString("permissions_group_not_found", language, fieldName));
            return response;
        }
        PermissionGroup permissionGroupParent = null;
        if (!permissionGroupNameParent.equalsIgnoreCase("none")) {
            // Grab the parent PermissionGroup to set.
            permissionGroupParent = getPermissionGroup(permissionGroupNameParent);
            if (permissionGroupParent == null) {
                response.set(Result.FAILURE, lang.getString("permissions_group_not_found", language, fieldNameParent));
                return response;
            }
        }
        // If the parent to set is none, and the PermissionGroup already has no parent, then let the commanding Player know.
        else if (!permissionGroup.hasParent()) {
            response.set(Result.FAILURE, lang.getString("permissions_group_parent_already_not_set", language, fieldName));
            return response;
        }
        // Make sure that the parent and the child aren't the same.
        if (permissionGroupParent != null && permissionGroup.equals(permissionGroupParent)) {
            response.set(Result.FAILURE, lang.getString("permissions_group_parent_identical", language, fieldName, fieldNameParent));
            return response;
        }
        // Make sure that the parent to set isn't a child of the permission group affected.
        if (permissionGroupParent != null && permissionGroupParent.isChildOf(permissionGroupParent)) {
            response.set(Result.FAILURE, lang.getString("permissions_group_parent_cyclic", language, fieldName, fieldNameParent));
            return response;
        }
        try {
            permissionGroup.setParent(permissionGroupParent, true);
            response.set(Result.SUCCESS, lang.getString("command_permissions_group_set_parent_success", language, fieldName, fieldNameParent));
        } catch (Exception e) {
            stackTrace(e);
            // Set the failure message.
            response.set(Result.FAILURE, lang.getString("command_permissions_group_set_parent_failure", language, fieldName));
        }
        return response;
    }

    /**
     * (CommandListener Method)
     * <p>
     * Sets a Permission Node definition for a PermissionGroup.
     *
     * @param commander           The Player that sent the command.
     * @param permissionGroupName The name of the PermissionGroup to rename.
     * @param node                The Permission Node to set.
     * @param flag                The Permission Node value.
     * @return Returns a Response that contains the Result of the Command, and the details for that Result.
     */
    public Response commandSetPermissionGroupNode(Player commander, String permissionGroupName, String node, String flag) {
        // The Response to return.
        Response response = new Response();
        // Grab the Language set by the Player.
        Language language = commander.getLanguage();
        // Make sure the name given is valid.
        if (permissionGroupName == null || permissionGroupName.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_name_empty", language));
            return response;
        }
        // Make sure the node entry is valid.
        if (node == null || node.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_node_empty", language));
            return response;
        }
        node = node.toLowerCase();
        if (flag == null || flag.isEmpty()) {
            response.set(Result.FAILURE, lang.getString("permissions_flag_empty", language));
            return response;
        }
        boolean remove = false;
        if (flag.equalsIgnoreCase("none") || flag.equalsIgnoreCase("null") || flag.equalsIgnoreCase("nil")) {
            remove = true;
        }
        boolean flagValue = !remove && (flag.equals("1") || flag.equalsIgnoreCase("true") || flag.equalsIgnoreCase("on") || flag.equalsIgnoreCase("yes"));
        // Pass these to the LanguagePackage to show the name passed when responding to the Player. @formatter:off
        EntryField fieldName = new EntryField("name", permissionGroupName);
        EntryField fieldNode = new EntryField("node", node               );
        EntryField fieldFlag = new EntryField("flag", flagValue          );
        // Attempt grabbing a PermissionGroup with the name provided. @formatter:on
        PermissionGroup permissionGroup = getPermissionGroup(permissionGroupName);
        // If a PermissionGroup is returned from the search, the name is already in use.
        if (permissionGroup == null) {
            response.set(Result.FAILURE, lang.getString("permissions_group_not_found", language, fieldName));
            return response;
        }
        try {
            // Set the permission.
            permissionGroup.setPermission(node, remove ? null : flagValue, true);
            response.set(Result.SUCCESS, lang.getString("command_permissions_group_set_node_success", language, fieldName, fieldNode, fieldFlag));
        } catch (Exception e) {
            stackTrace(e);
            response.set(Result.FAILURE, lang.getString("command_permissions_group_set_node_failure", language, fieldName, fieldNode, fieldFlag));
        }
        return response;
    }

    /**
     * Creates a new PermissionUser. If the Unique ID provided is already in use,
     * IllegalArgumentException is thrown.
     *
     * @param userId The Unique ID identifier assigned to the PermissionUser.
     * @return Returns a new PermissionUser.
     */
    public PermissionUser createPermissionUser(UUID userId) {
        // Validate UUID argument.
        if (mapMongoPermissionUsers.containsKey(userId) || mapPermissionUsers.containsKey(userId)) {
            throw new IllegalArgumentException("UUID already in use: \"" + userId.toString() + "\".");
        }
        // Create the document.
        MongoPermissionUser mongoPermissionUser = new MongoPermissionUser(collectionUsers, userId);
        // Create the container for the document.
        PermissionUser permissionUser = new PermissionUser(mongoPermissionUser);
        // Put the document in the map.
        mapMongoPermissionUsers.put(userId, mongoPermissionUser);
        // Put the container in the map.
        mapPermissionUsers.put(userId, permissionUser);
        // Save the document.
        mongoPermissionUser.save();
        // Return the new container.
        return permissionUser;
    }

    /**
     * Deletes a PermissionUser. If the PermissionUser is assigned to a
     * PermissionGroup, it will be removed from the list of members in that group.
     *
     * @param permissionUser The PermissionUser to delete. If it is null,
     *                       IllegalArgumentException is thrown. If the PermissionUser
     *                       isn't registered properly, a IllegalStateException is thrown.
     */
    public void deletePermissionUser(PermissionUser permissionUser) {
        // Validate the PermissionUser argument.
        if (permissionUser == null) {
            throw new IllegalArgumentException("PermissionUser given is null.");
        }
        // Grab the ID for the PermissionUser.
        UUID userId = permissionUser.getUniqueId();
        // If this is not a key in either map, something is wrong. Throw an exception.
        if (!mapMongoPermissionUsers.containsKey(userId) || !mapPermissionUsers.containsKey(userId)) {
            throw new IllegalStateException(
                    "PermissionUser is not registered with UUID: \"" + userId.toString() + "\".");
        }
        // Remove the document from the map.
        mapMongoPermissionUsers.remove(userId);
        // Remove the container from the map.
        mapPermissionUsers.remove(userId);
        // Delete the document properly.
        permissionUser.getMongoDocument().delete();
        // Grab the group of the user.
        PermissionGroup permissionGroup = permissionUser.getPermissionGroup();
        // If the user is assigned to a group, unlink the user from the members list.
        if (permissionGroup != null) {
            permissionGroup.removeMember(permissionUser, true);
        }
    }

    /**
     * Creates a PermissionGroup with a given String name.
     *
     * @param name The String name of the PermissionGroup. If this is null,
     *             empty, or if the name is already in use, an
     *             IllegalArgumentException is thrown.
     * @return Returns a new PermissionGroup, if no errors occur during creation.
     */
    public PermissionGroup createPermissionGroup(String name) {
        // Validate the name argument.
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name given is null or empty.");
        }
        // Attempt grabbing a PermissionGroup with the name provided.
        PermissionGroup permissionGroup = getPermissionGroup(name);
        // If a PermissionGroup is returned from the search, the name is already in use.
        if (permissionGroup != null) {
            throw new IllegalArgumentException("Name given is already in use.");
        }
        // Create the document.
        MongoPermissionGroup mongoPermissionGroup = new MongoPermissionGroup(collectionGroups, name);
        // Create the container.
        permissionGroup = new PermissionGroup(mongoPermissionGroup);
        // Add the document to the map.
        mapMongoPermissionGroups.put(mongoPermissionGroup.getUniqueId(), mongoPermissionGroup);
        // Add the container to the map.
        mapPermissionGroups.put(permissionGroup.getUniqueId(), permissionGroup);
        // Save the document.
        mongoPermissionGroup.save();
        // Return the new PermissionGroup.
        return permissionGroup;
    }

    /**
     * Deletes a PermissionGroup. If the group deleted is the parent of other
     * groups, then the groups are assigned to the parent of the group being
     * deleted. If the group does not have a parent, then the children groups will
     * be unassigned and have no parent.
     *
     * @param permissionGroup The PermissionGroup being deleted.
     */
    public void deletePermissionGroup(PermissionGroup permissionGroup) {
        // Validate the PermissionGroup argument.
        if (permissionGroup == null) {
            throw new IllegalArgumentException("PermissionGroup given is null.");
        }
        // Grab the ID for the PermissionGroup.
        UUID groupId = permissionGroup.getUniqueId();
        // If this is not a key in either map, something is wrong. Throw an exception.
        if (!mapMongoPermissionGroups.containsKey(groupId) || !mapPermissionGroups.containsKey(groupId)) {
            throw new IllegalStateException(
                    "PermissionGroup is not registered with UUID: \"" + groupId.toString() + "\".");
        }
        // Remove the document from the map.
        mapMongoPermissionGroups.remove(groupId);
        // Remove the container from the map.
        mapPermissionGroups.remove(groupId);
        // Delete the document properly.
        permissionGroup.getMongoDocument().delete();
        // Grab the parent of the group being deleted.
        PermissionGroup parent = permissionGroup.getParent();
        // Go through every registered PermissionGroup.
        for (PermissionGroup permissionGroupNext : mapPermissionGroups.values()) {
            // If the group next is the one we are deleting, or the parent of the one being
            // deleted, skip it.
            if (permissionGroupNext.equals(permissionGroup) || permissionGroupNext.equals(parent)) {
                continue;
            }
            // If the next group's parent is the group being deleted, set the parent of the
            // group to the one of the group being deleted.
            if (permissionGroupNext.getParent().equals(permissionGroup)) {
                permissionGroupNext.setParent(parent, true);
            }
        }
        // Sets the User to the parent group.
        for (PermissionUser member : permissionGroup.getMembers()) {
            member.setPermissionGroup(parent, true);
        }
    }

    /**
     * @param name The String name of the PermissionGroup.
     * @return Returns a PermissionGroup using the given String name, if one
     * exists. If one does not exist, null is returned.
     */
    public PermissionGroup getPermissionGroup(String name) {
        // The PermissionGroup to return.
        PermissionGroup returned = null;
        // Go through every loaded PermissionGroup.
        for (PermissionGroup permissionGroupNext : mapPermissionGroups.values()) {
            // If the names match without being case-sensitive.
            if (permissionGroupNext.getGroupName().equalsIgnoreCase(name)) {
                // This is our group. Set the return variable.
                returned = permissionGroupNext;
                // Break out of the loop. We found the group.
                break;
            }
        }
        // Return the result of the search.
        return returned;
    }

    /**
     * @param player The Player linked to the PermissionUser being retrieved.
     * @return Returns a PermissionUser associated for the given Player, if
     * registered in Permissions as a PermissionsUser.
     */
    public PermissionUser getPermissionUser(Player player) {
        return getPermissionUser(player.getUniqueId());
    }

    /**
     * @param playerId The Unique ID of a Player linked to the PermissionUser
     *                 being retrieved.
     * @return Returns a PermissionUser associated with the given Unique ID
     * for a Player, if registered in Permissions as a
     * PermissionUser.
     */
    private PermissionUser getPermissionUser(UUID playerId) {
        return this.mapPermissionUsers.get(playerId);
    }

    /**
     * @return Returns the default PermissionGroup instance.
     */
    public PermissionGroup getDefaultPermissionGroup() {
        return this.permissionGroupDefault;
    }

    /**
     * @param groupId The Unique ID identifier for the PermissionGroup.
     * @return Returns a PermissionGroup if one exists with the Unique ID provided.
     */
    public PermissionGroup getPermissionGroup(UUID groupId) {
        return this.mapPermissionGroups.get(groupId);
    }

    /**
     * @return Returns the LanguagePackage for the Permissions Module.
     */
    public LanguagePackage getLanguagePackage() {
        return this.lang;
    }
}