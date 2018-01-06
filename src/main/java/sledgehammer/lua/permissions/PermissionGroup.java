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
package sledgehammer.lua.permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.module.permissions.MongoPermissionGroup;

/**
 * PermissionObject designed to handle permission-group data and operations for
 * the Permissions Module.
 *
 * @author Jab
 */
public class PermissionGroup extends PermissionObject<MongoPermissionGroup> {

    /**
     * The List of PermissionUser members assigned to the group.
     */
    private List<PermissionUser> listPermissionUsers;
    /**
     * The PermissionGroup parent that the group inherits permissions from.
     */
    private PermissionGroup parent;

    /**
     * Load constructor.
     *
     * @param mongoDocument The MongoDocument to set.
     */
    public PermissionGroup(MongoPermissionGroup mongoDocument) {
        super(mongoDocument, "PermissionGroup");
        listPermissionUsers = new ArrayList<>();
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
    public boolean hasPermission(String node) {
        // The flag to return. (If no Nodes are defined from the parent and the group,
        // the permission is denied)
        boolean returned = false;
        // The node to get from the parent, if the group has a parent.
        Node parentNodeSpecific = null;
        // The node to get directly from the group, if the node is defined.
        Node nodeSpecific = null;
        // If the group has a parent, grab this one first.
        if (hasParent()) {
            // Grab the most specific node that exists from the parent.
            parentNodeSpecific = getParent().getPermissionNode(node);
            // If the parent has a node that implicitly defines the node being tested, or
            // contains an explicit definition, use this first.
            if (parentNodeSpecific != null) {
                // Set the value to the flag defined by the node.
                returned = parentNodeSpecific.getFlag();
            }
        }
        // Next, grab the definition for the group.
        nodeSpecific = getPermissionNode(node);
        if (nodeSpecific != null) {
            // If the parent also defines this permission.
            if (parentNodeSpecific != null) {
                // If both the group node and the parent node are the same, then use the group
                // as this is a more explicit definition to override the group definition.
                // | OR |
                // If the group node is a super-node of the group node, this means that the node
                // for the group overrides the super-node, and also uses the group definition.
                if (parentNodeSpecific.equals(nodeSpecific) || parentNodeSpecific.isSuperNode(nodeSpecific)) {
                    // Set the group flag as the returned variable.
                    returned = nodeSpecific.getFlag();
                }
                // The group node will only be subjected to the parents definition at this point
                // because the group node is a super-node to the parent, which is more
                // explicitly defined.
                else {
                    returned = parentNodeSpecific.getFlag();
                }
            }
            // If the parent does not define it, but the group defines it.
            else {
                // Assign the value directly.
                returned = nodeSpecific.getFlag();
            }
        }
        // Return the result.
        return returned;
    }

    @Override
    public List<Node> getAllSubPermissionNodes(String superNodeAsString) {
        // Format the node argument.
        superNodeAsString = superNodeAsString.trim();
        // The List to return.
        List<Node> listNodes = new ArrayList<>();
        // If the group has a parent.
        if (hasParent()) {
            // Add all of the nodes form the parent's execution of this method.
            listNodes.addAll(getParent().getAllSubPermissionNodes(superNodeAsString));
        }
        // Go through group all group nodes.
        for (Node nodeNext : getPermissionNodes()) {
            // If the next node is a sub-node
            if (nodeNext.isSubNode(superNodeAsString)) {
                // If the current list (from the parent), already contains the sub-node
                //
                // (Note: Refer to collections using the method 'equals(Object other)' to
                // understand how collections identifies keys, and items in collections)
                if (listNodes.contains(nodeNext)) {
                    // Remove the old Object from the list.
                    listNodes.remove(nodeNext);
                    // Put in the overriding sub-node.
                    listNodes.add(nodeNext);
                }
            }
        }
        // Return the result List.
        return listNodes;
    }

    /**
     * Returns whether or not if the PermissionUser specified is in the
     * PermissionGroup.
     *
     * @param member The PermissionUser being tested.
     * @return Returns true if the PermissionUser given is in the
     * PermissionGroup.
     */
    public boolean hasMember(PermissionUser member) {
        return listPermissionUsers.contains(member);
    }

    /**
     * Adds a PermissionUser to the PermissionGroup.
     *
     * @param permissionUser The PermissionUser to add.
     * @param save           The flag to save the Document.
     */
    public void addMember(PermissionUser permissionUser, boolean save) {
        if (!hasMember(permissionUser)) {
            listPermissionUsers.add(permissionUser);
        }
        permissionUser.setPermissionGroup(this, save);
    }

    /**
     * Removes a PermissionUser from the PermissionGroup.
     *
     * @param permissionUser The PermissionUser to remove.
     * @param save           The flag to save the Document.
     */
    public void removeMember(PermissionUser permissionUser, boolean save) {
        if (hasMember(permissionUser)) {
            listPermissionUsers.remove(permissionUser);
        }
        permissionUser.setPermissionGroup(null, save);
    }

    /**
     * @return Returns the parent PermissionGroup, if one is assigned.
     */
    public PermissionGroup getParent() {
        return this.parent;
    }

    /**
     * Sets the parent PermissionGroup for the PermissionGroup.
     *
     * @param group The PermissionGroup to assign as the parent.
     * @param save  The flag to save the Document.
     */
    public void setParent(PermissionGroup group, boolean save) {
        this.parent = group;
        getMongoDocument().setParentId(group.getUniqueId(), save);
    }

    /**
     * @return Returns true if the PermissionGroup has a parent PermissionGroup.
     */
    public boolean hasParent() {
        return getParent() != null;
    }

    /**
     * @return Returns a List of PermissionUsers of Players who are assigned
     * to the PermissionGroup.
     */
    public List<PermissionUser> getMembers() {
        return this.listPermissionUsers;
    }

    /**
     * @return The UUID identifier for the group.
     */
    public UUID getUniqueId() {
        return getMongoDocument().getUniqueId();
    }

    /**
     * @return Returns the String name of the PermissionGroug.
     */
    public String getGroupName() {
        return getMongoDocument().getGroupName();
    }

    /**
     * Sets the String name of the PermissionGroup.
     *
     * @param name The String name to set.
     * @param save The flag to save the Document.
     */
    public void setGroupName(String name, boolean save) {
        getMongoDocument().setGroupName(name, save);
    }
}