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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mongodb.DBCursor;

import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoCollection;
import sledgehammer.database.module.core.SledgehammerDatabase;
import sledgehammer.database.module.permissions.MongoPermissionGroup;
import sledgehammer.database.module.permissions.MongoPermissionUser;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.permissions.PermissionGroup;
import sledgehammer.lua.permissions.PermissionUser;
import sledgehammer.plugin.MongoModule;

/**
 * Module class that handles operations for Permissions.
 * 
 * TODO: Implement Commands.
 * 
 * @author Jab
 */
public class ModulePermissions extends MongoModule {

	/** The <MongoCollection> storing the <MongoPermissionGroup> documents. */
	private MongoCollection collectionGroups;
	/** The <MongoCollection> storing the <MongoPermissionUser> documents. */
	private MongoCollection collectionUsers;
	/** The <Map> storing the <MongoPermissionGroup> documents. */
	private Map<UUID, MongoPermissionGroup> mapMongoPermissionGroups;
	/** The <Map> storing the <MongoPermissionUser> documents. */
	private Map<UUID, MongoPermissionUser> mapMongoPermissionUsers;
	/** The <Map> storing the <PermissionGroup> containers. */
	private Map<UUID, PermissionGroup> mapPermissionGroups;
	/** The <Map> storing the <Permissionuser> containers. */
	private Map<UUID, PermissionUser> mapPermissionUsers;
	/**
	 * The <PermissionListener> implementation to hook into Sledgehammer's core when
	 * checking permissions.
	 */
	private PermissionsListener permissionsListener;
	/**
	 * The default <PermissionGroup> to base decisions for players not assigned to
	 * groups.
	 */
	private PermissionGroup permissionGroupDefault;

	/**
	 * Main constructor.
	 */
	public ModulePermissions() {
		super(getDefaultDatabase());
	}

	@Override
	public void onLoad() {
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
		permissionGroupDefault = new PermissionGroup(mongoPermissionGroupDefault);
		permissionsListener = new PermissionsListener(this);
		setPermissionListener(permissionsListener);
	}

	@Override
	public void onUnload() {
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
	 * 
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
	 * 
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
	 * 
	 * Pairs up <PermissionUser> to <PermissionGroup> and vice versa.
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
					errorln("PermissionUser \"" + permissionUser.getUniqueId().toString() + "\""
							+ "is assigned to a group that does not exist: \"" + groupId.toString() + "\".");
					errorln("Setting groupId for the PermissionUser to null.");
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
	 * Creates a new <PermissionUser>. If the <UUID> provided is already in use,
	 * <IllegalArgumentException> is thrown.
	 * 
	 * @param userId
	 *            The <UUID> identifier assigned to the <PermissionUser>.
	 * @return Returns a new <PermissionUser>.
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
	 * Deletes a <PermissionUser>. If the <PermissionUser> is assigned to a
	 * <PermissionGroup>, it will be removed from the list of members in that group.
	 * 
	 * @param permissionUser
	 *            The <PermissionUser> to delete. If it is null,
	 *            <IllegalArgumentException> is thrown. If the <PermissionUser>
	 *            isn't registered properly, a <IllegalStateException> is thrown.
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
	 * Creates a <PermissionGroup> with a given <String> name.
	 * 
	 * @param name
	 *            The <String> name of the <PermissionGroup>. If this is null,
	 *            empty, or if the name is already in use, an
	 *            <IllegalArgumentException> is thrown.
	 * @return Returns a new <PermissionGroup>, if no errors occur during creation.
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
	 * Deletes a <PermissionGroup>. If the group deleted is the parent of other
	 * groups, then the groups are assigned to the parent of the group being
	 * deleted. If the group does not have a parent, then the children groups will
	 * be unassigned and have no parent.
	 * 
	 * @param permissionGroup
	 *            The <PermissionGroup> being deleted.
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
	 * @param name
	 *            The <String> name of the <PermissionGroup>.
	 * @return Returns a <PermissionGroup> using the given <String> name, if one
	 *         exists. If one does not exist, null is returned.
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
	 * @param player
	 *            The <Player> linked to the <PermissionUser> being retrieved.
	 * @return Returns a <PermissionUser> associated for the given <Player>, if
	 *         registered in Permissions as a <PermissionsUser>.
	 */
	public PermissionUser getPermissionUser(Player player) {
		return getPermissionUser(player.getUniqueId());
	}

	/**
	 * @param playerId
	 *            The <UUID> identifier of a <Player> linked to the <PermissionUser>
	 *            being retrieved.
	 * @return Returns a <PermissionUser> associated with the given <UUID>
	 *         identifier for a <Player>, if registered in Permissions as a
	 *         <PermissionUser>.
	 */
	private PermissionUser getPermissionUser(UUID playerId) {
		return this.mapPermissionUsers.get(playerId);
	}

	/**
	 * @return Returns the default <PermissionGroup> instance.
	 */
	public PermissionGroup getDefaultPermissionGroup() {
		return this.permissionGroupDefault;
	}

	/**
	 * @param groupId
	 *            The <UUID> identifier for the <PermissionGroup>.
	 * @return Returns a <PermissionGroup> if one exists with the <UUID> provided.
	 */
	public PermissionGroup getPermissionGroup(UUID groupId) {
		return this.mapPermissionGroups.get(groupId);
	}
}