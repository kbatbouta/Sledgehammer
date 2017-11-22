package sledgehammer.lua.permissions;

import java.util.ArrayList;
import java.util.Collection;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.lua.Node;
import sledgehammer.object.LuaTable;

/**
 * Generic Object to handle generic operations for permission objects.
 * 
 * @author Jab
 */
public abstract class PermissionObject extends LuaTable {

	/**
	 * The <Map> containing the context permissions.
	 */
	private Map<String, Node> mapPermissionNodes;

	/**
	 * Main constructor.
	 * 
	 * @param name
	 */
	public PermissionObject(String name) {
		super(name);
		mapPermissionNodes = new HashMap<>();
	}

	/**
	 * @param superNodeAsString
	 *            The <String> node being tested.
	 * @return Returns true if any sub-node of the provided <String> node is
	 *         granted.
	 */
	public boolean hasAnyPermission(String superNodeAsString) {
		// The flag to return.
		boolean returned = false;
		// Grab all sub-nodes from the group, and the parent, if the group has one.
		List<Node> subNodes = getAllSubPermissionNodes(superNodeAsString);
		// If there's any sub-nodes.
		if (subNodes.size() > 0) {
			// Grab all granted sub-nodes from the list.
			List<Node> subNodesGranted = getGrantedNodes(subNodes);
			// If there's any granted sub-nodes, there's no conflicts with them as this is
			// already sorted in the method 'getAllSubPermissionNodes()'.
			if (subNodesGranted.size() > 0) {
				// and so we return true.
				returned = true;
			}
		}
		// Return the result.
		return returned;
	}

	/**
	 * Returns a <List> of any <PermissionNodes> that are a sub-node to the <String>
	 * node given.
	 * 
	 * @param node
	 *            The <String> node used to test the PermissionNodes assigned
	 * @return
	 */
	public List<Node> getSubPermissions(String node) {
		// Make sure the node argument is valid.
		if (node == null || node.isEmpty()) {
			throw new IllegalArgumentException("Node given is null or empty.");
		}
		// Process the node entry.
		node = node.toLowerCase().trim();
		// The List to return.
		List<Node> listPermissionNodes = new ArrayList<>();
		// Go through each assigned permission.
		for (Node permissionNodeNext : getPermissionNodes()) {
			// Check to see if this is a sub-node of the given node to test.
			if (permissionNodeNext.isSubNode(node)) {
				// If so, then add to the list to return.
				listPermissionNodes.add(permissionNodeNext);
			}
		}
		// Return the list with any contents added.
		return listPermissionNodes;
	}

	/**
	 * Tests whether or not the <PermissionObject> contains an assigned
	 * sub-<PermissionNode> for the given <String> node.
	 * 
	 * @param node
	 *            The <String> super-node being tested.
	 * @return Returns true if the <PermissionObject> has an assigned sub-node to
	 *         the <String> super-node tested.
	 */
	public boolean hasSubPermission(String node) {
		// Make sure the node argument is valid.
		if (node == null || node.isEmpty()) {
			throw new IllegalArgumentException("Node given is null or empty.");
		}
		// Process the node entry.
		node = node.toLowerCase().trim();
		// The result to return.
		boolean returned = false;
		// Go through each assigned permission.
		for (Node permissionNodeNext : getPermissionNodes()) {
			// Check to see if this is a sub-node of the given node to test.
			if (permissionNodeNext.isSubNode(node)) {
				// If so, we have one result which is enough. Set the flag and break the loop.
				returned = true;
				break;
			}
		}
		// Return the result.
		return returned;
	}

	/*
	 * Attempts to grab the closest <PermissionNode> that is defined in the
	 * embodying Permission entity, and returns the result flag set for it.
	 * 
	 * @param node The <String> node being tested.
	 * 
	 * @return Returns true if a <PermissionNode> for the <String> node, or a
	 * <PermissionNode> that is a super-node of that node is defined, and permits
	 * the node.
	 */
	/*
	 * public boolean hasPermission(String node) { // Flag to return. boolean flag =
	 * false; // Grab the closest PermissionNode for the one given. Node
	 * permissionNodeClosest = getPermissionNode(node); // If that node exists. if
	 * (permissionNodeClosest != null) { // Grab the flag for it. flag =
	 * permissionNodeClosest.getFlag(); } // Return the result flag if set. return
	 * flag; }
	 */

	/**
	 * Attempts to grab the closest <PermissionNode> that is defined in the
	 * embodying Permission entity, and returns it.
	 * 
	 * @param node
	 *            The <String> node being tested.
	 * @return Returns a <PermissionNode> for the <String> node, or a
	 *         <PermissionNode> that is a super-node of that node is defined.
	 *         Returns null if no <PermissionNode> is found.
	 */
	public Node getPermissionNode(String node) {
		// Node to return.
		// Attempt to grab an explicit node definition, if one exists.
		Node returned = getExplicitPermissionNode(node);
		// If there is no explicit node definition.
		if (returned == null) {
			// Look for a implicit super-node definition.
			returned = getClosestPermissionNode(node);
		}
		// Return the result.
		return returned;
	}

	/**
	 * @param superNodeAsString
	 *            The <String> node to test.
	 * @return Returns a <List> of <Node>s that are sub-nodes of the <String>
	 *         super-node given.
	 */
	public List<Node> getAllSubPermissionNodes(String superNodeAsString) {
		// The List to return.
		List<Node> listSubNodes = new ArrayList<>();
		// Format the node argument.
		superNodeAsString = superNodeAsString.toLowerCase().trim();
		// Go through each Node in the PermissionObject.
		for (Node permissionNodeNext : getPermissionNodes()) {
			// If the next node is a sub-node of the given String node.
			if (permissionNodeNext.isSubNode(superNodeAsString)) {
				// Add the sub-node to the list.
				listSubNodes.add(permissionNodeNext);
			}
		}
		// Return the result List.
		return listSubNodes;
	}

	/**
	 * Attempts to grab the most specific definition for a given <String> node.
	 * 
	 * (Note: If the node is explicitly defined, that <PermissionNode> will be
	 * returned.)
	 * 
	 * @param node
	 *            The <String> node being tested.
	 * @return Returns the closest <PermissionNode> if one is found.
	 */
	public Node getClosestPermissionNode(String node) {
		// Our assigned variable for any super-node discovered. If there is more than
		// one super-node, we store the most specific one.
		Node permissionNodeClosest = null;
		// Grab the player-specific set of PermissionNodes.
		Collection<Node> nodes = this.getPermissionNodes();
		// Go through each PermissionNode.
		for (Node permissionNodeNext : nodes) {
			// If this is the exact PermissionNode, we use this and that's it.
			if (permissionNodeNext.isNode(node)) {
				permissionNodeClosest = permissionNodeNext;
				break;
			}
			// Is the PermissionNode being checked a super-node to the tested node?
			if (permissionNodeNext.isSuperNode(node)) {
				// If this is true, check and see if the closest-node argument is filled.
				if (permissionNodeClosest != null) {
					// If so, then check and see if the set closest-node is a super-node of the
					// current closest-node being checked.
					if (permissionNodeClosest.isSuperNode(permissionNodeNext)) {
						// If so, set the next closest-node as the most specific super-node.
						permissionNodeClosest = permissionNodeNext;
					}
				}
				// So the current closest-node field isn't set, set it as the current
				// closest-node.
				else {
					permissionNodeClosest = permissionNodeNext;
				}
			}
		}
		// Return the result node, if it exists.
		return permissionNodeClosest;
	}

	/**
	 * Returns a <PermissionNode> with the given node in <String> form.
	 * 
	 * @param node
	 *            The <String> format of the node.
	 * @return Returns a <PermissionNode> if one exists for the <PermissionObject>.
	 */
	public Node getExplicitPermissionNode(String node) {
		// Validate the node argument.
		node = node.toLowerCase().trim();
		// Return the map result.
		return this.mapPermissionNodes.get(node);
	}

	public Map<String, Node> getPermissionMap() {
		return this.mapPermissionNodes;
	}

	public Collection<Node> getPermissionNodes() {
		return getPermissionMap().values();
	}

	/**
	 * @param nodes
	 *            The <List> of <Node>s being tested.
	 * @return Returns true if any of the <Node>s in the <List> given are flagged
	 *         true.
	 */
	public static boolean isAnyNodeGranted(List<Node> nodes) {
		// The result to return.
		boolean returned = false;
		// Go through each Node.
		for (Node node : nodes) {
			// If the Node's flag is set to true.
			if (node.getFlag()) {
				// Set the return value to true.
				returned = true;
				// We do not need to check any other nodes.
				break;
			}
		}
		// Return the result.
		return returned;
	}

	/**
	 * @param nodes
	 *            The <List> of <Node>s being tested.
	 * @return Returns a <List> of <Node>s that are granted. If none in the <List>
	 *         provided are granted, the <List> will return empty.
	 */
	public static List<Node> getGrantedNodes(List<Node> nodes) {
		// The list of granted Nodes to return.
		List<Node> listNodes = new ArrayList<>();
		// Go through each Node in the list.
		for (Node node : nodes) {
			// If the Node is granted.
			if (node.getFlag()) {
				// Add it to the list.
				listNodes.add(node);
			}
		}
		// Return the result list.
		return listNodes;
	}

	/**
	 * @param node
	 *            The <String> node being tested.
	 * @return Returns true if the <PermissionObject> grants the <String> node being
	 *         tested.
	 */
	public boolean hasPermission(String node) {
		// Flag to return.
		boolean returned = false;
		// Grab the closest Node to the one requested.
		Node permissionNodeClosest = getClosestPermissionNode(node);
		// If any node is defined related to this node.
		if (permissionNodeClosest != null) {
			// Set the returned flag to the set flag of that node.
			returned = permissionNodeClosest.getFlag();
		}
		// Return the result flag.
		return returned;
	}
}