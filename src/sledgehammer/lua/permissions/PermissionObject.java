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

import sledgehammer.database.document.MongoNode;
import sledgehammer.database.document.MongoUniqueNodeDocument;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.Node;

/**
 * Generic Object to handle generic operations for permission objects.
 * 
 * @author Jab
 */
public abstract class PermissionObject<M extends MongoUniqueNodeDocument> extends LuaTable {

	/** The <Map> containing the context permissions. */
	private Map<String, Node> mapPermissionNodes;

	/** The <MongoDocument> storing the data. */
	private M mongoDocument;

	/**
	 * Main constructor.
	 * 
	 * @param name
	 */
	public PermissionObject(M mongoDocument, String name) {
		super(name);
		mapPermissionNodes = new HashMap<>();
		setMongoDocument(mongoDocument);
	}

	/**
	 * Sets a permission <Node> if one exists, or creates a permission <Node> if one
	 * does not exists.
	 * 
	 * @param nodeAsString
	 *            The node in <String> format.
	 * @param flag
	 *            The flag to set for the <Node>.
	 * @param save
	 *            Flag to save the <MongoDocument> after setting the node.
	 * @return Returns the result <Node> with the flag set.
	 */
	public Node setPermission(String nodeAsString, boolean flag, boolean save) {
		if(nodeAsString == null) {
			throw new IllegalArgumentException("String node given is null.");
		}
		Node returned = this.getExplicitPermissionNode(nodeAsString);
		if (returned == null) {
			MongoNode mongoNode = new MongoNode(getMongoDocument(), nodeAsString, flag);
			returned = new Node(mongoNode);
			addNode(returned, save);
		} else {
			returned.setFlag(flag, save);
		}
		return returned;
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

	/**
	 * TODO: Document
	 * 
	 * @param node
	 * @param save
	 */
	public void addNode(Node node, boolean save) {
		// Validate the node argument.
		if (node == null) {
			throw new IllegalArgumentException("Node given is null.");
		}
		if (mapPermissionNodes.containsKey(node.getNode())) {
			Node nodePrevious = mapPermissionNodes.get(node.getNode());
			nodePrevious.getMongoDocument().setMongoDocument(null);
			nodePrevious.setFlag(node.getFlag(), false);
		}
		node.getMongoDocument().setMongoDocument(getMongoDocument());
		getMongoDocument().addNode(node.getMongoDocument(), false);
		mapPermissionNodes.put(node.getNode(), node);
		if (save) {
			getMongoDocument().save();
		}
	}

	/**
	 * Removes a <Node> if it is assigned to the <PermissionObject>.
	 * 
	 * @param node
	 *            The <Node> to remove.
	 * @param save
	 *            Flag for saving the document after removing the node.
	 * @return
	 */
	public void removeNode(Node node, boolean save) {
		// Validate the node argument.
		if (node == null) {
			throw new IllegalArgumentException("Node given is null.");
		}
		// Get the node in String format.
		String nodeAsString = node.getNode();
		// Validate that the permission given is assigned to the object.
		if (!mapPermissionNodes.containsKey(nodeAsString)) {
			throw new IllegalArgumentException("Node given is not assigned to PermissionObject.");
		}
		// If so, remove it from the map first.
		mapPermissionNodes.remove(nodeAsString);
		// Remove the node formally on the document layer, and save it if the parameter
		// flag to save is passed as true.
		getMongoDocument().removeNode(node.getMongoDocument(), save);
	}

	/**
	 * (Internal Method)
	 * 
	 * Constructs the <Node> containers from the <MongoUniqueNodeDocument>.
	 * 
	 * @param mongoDocument
	 */
	public void loadNodes(M mongoDocument) {
		// Empty the map for the Nodes.
		mapPermissionNodes.clear();
		// Go through each node document.
		for (MongoNode mongoNode : mongoDocument.getMongoNodes()) {
			// Create a node container.
			Node node = new Node(mongoNode);
			// Add it to the node map.
			mapPermissionNodes.put(node.getNode(), node);
		}
	}


	/**
	 * (Internal Method)
	 * 
	 * Sets the <MOngoUniqueNodeDocument> strong the data for the
	 * <PermissionObject>, and loads the <Node> containers for the <MongoNodes>
	 * registered to the document.
	 * 
	 * @param mongoDocument
	 *            The <MongoUniqueNodeDocument> to set.
	 */
	private void setMongoDocument(M mongoDocument) {
		// Validate MongoDocument argument.
		if (mongoDocument == null) {
			throw new IllegalArgumentException("MongoDocument given is null.");
		}
		// Make sure the document is not the exact same already set.
		if (this.mongoDocument == null || !this.mongoDocument.equals(mongoDocument)) {
			// Set the new document.
			this.mongoDocument = mongoDocument;
			// Load the nodes from the document.
			loadNodes(this.mongoDocument);
		}
	}

	/**
	 * 
	 */
	public void save() {
		M mongoDocument = getMongoDocument();
		if (mongoDocument == null) {
			throw new IllegalStateException("MongoDocument is not set, and cannot be saved.");
		}
		mongoDocument.save();
	}

	public Map<String, Node> getPermissionMap() {
		return this.mapPermissionNodes;
	}
	
	public Collection<Node> getPermissionNodes() {
		return getPermissionMap().values();
	}

	/**
	 * @return Returns the <MongoUniqueNodeDocument> storing the data for the
	 *         <PermissionObject>.
	 */
	public M getMongoDocument() {
		return this.mongoDocument;
	}
}