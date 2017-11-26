package sledgehammer.lua;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.document.node.MongoNode;
import sledgehammer.object.LuaTable;

/**
 * Class designed to load and store node data and operations.
 * 
 * @author Jab
 */
public class Node extends LuaTable {

	/** The <MongoDocumentEntry> to store the data. */
	private MongoNode mongoNode;

	/**
	 * MongoDB constructor.
	 * 
	 * @param mongoNode
	 *            The <MongoDocumentEntry> storing the data.
	 */
	public Node(MongoNode mongoNode) {
		super("Node");
		setMongoDocument(mongoNode);
	}

	/**
	 * Lua constructor.
	 * 
	 * (Note: The <MongoDocumentEntry> created from this constructor will not be
	 * assigned to a <MongoDocumentEntry>, and cannot be implicitly saved)
	 * 
	 * @param table
	 *            The <KahluaTable> storing the data.
	 */
	public Node(KahluaTable table) {
		super("Node", table);
	}

	@Override
	public void onExport() {
		// Set the name to be the node value in String format.
		set("name", getNode());
		// Set the flag to be the boolean flag.
		set("flag", getFlag());
	}

	@Override
	public void onLoad(KahluaTable table) {
		// Grab the string format of the node in object form.
		Object oNode = table.rawget("name");
		// Check to make sure the node is valid.
		if (oNode == null) {
			throw new IllegalArgumentException("The KahluaTable is invalid for a node. (Field 'name' is null)");
		}
		// Check to make sure the node is proper.
		if (!(oNode instanceof String)) {
			throw new IllegalArgumentException(
					"The KahluaTable is invalid for a node. (Field 'name' is not a String value)");
		}
		// Grab the node in String format.
		String node = (String) table.rawget("name");
		// Nodes without flags on import are implictly true.
		boolean flag = true;
		// Grab the Object form of the flag.
		Object oFlag = table.rawget("flag");

		// If it does not exist, attempt to load it.
		if (oFlag != null) {
			// Grab the string format of the flag.
			String sFlag = oFlag.toString();
			// If the flag is not equal to these, it will be set to false.
			flag = sFlag.equals("1") || sFlag.equalsIgnoreCase("true");
		}
		// Create the MongoDocument for the data.
		MongoNode mongoNode = new MongoNode(null, node, flag);
		// set the MongoDocument.
		setMongoDocument(mongoNode);
	}

	@Override
	public boolean equals(Object other) {
		// Our flag to return.
		boolean returned = false;
		// If the other object is a Node container object.
		if (other instanceof Node) {
			// Check the node and the flag representing it.
			returned = ((Node) other).getNode().equals(getNode()) && ((Node) other).getFlag() == getFlag();
		}
		// Return the result.
		return returned;
	}

	@Override
	public String toString() {
		// Stores as "node:flag".
		return getNode() + ":" + (getFlag() ? "1" : "0");
	}

	/**
	 * Checks if a given <Node> matches this <Node>
	 * 
	 * @param node
	 *            The <Node> being tested.
	 * @return Returns true if the <String> node matches the node in this <Node>
	 */
	public boolean isNode(Node node) {
		return isNode(node.getNode());
	}

	/**
	 * Checks if a given <String> node is this <Node>.
	 * 
	 * @param node
	 *            The <String> node being tested.
	 * @return Returns true if the <String> node matches the node in this <Node>
	 */
	public boolean isNode(String node) {
		boolean returned = false;
		node = node.toLowerCase();
		if (node.equals(getNode())) {
			returned = true;
		}
		return returned;
	}

	/**
	 * Checks if a given <Node> is a super-node of this <Node>.
	 * 
	 * @param node
	 *            The <Node> being tested.
	 * @return Returns true if the <Node> is a super-node of this <Node>.
	 */
	public boolean isSuperNode(Node node) {
		return isNode(node.getNode());
	}

	/**
	 * Checks if a given <String> node is a super-node of this <Node>.
	 * 
	 * @param node
	 *            The <String> node being tested.
	 * @return Returns true if the <String> node is a super-node of this <Node>
	 */
	public boolean isSuperNode(String node) {
		// Our flag to return.
		boolean returned = false;
		// Format the argument.
		node = node.toLowerCase();
		// Check if the node contains our given node string.
		// Make sure that the node does not equal the given node.
		if (!getNode().equals(node) && getNode().contains(node)) {
			// If this is true, then it is a super-node.
			returned = true;
		}
		// Return the result.
		return returned;
	}

	/**
	 * Checks if a given <Node> is a sub-node of this <Node>.
	 * 
	 * @param node
	 *            The <Node> being tested.
	 * @return Returns true if the given <Node> is a sub-node of this <Node>.
	 */
	public boolean isSubNode(Node node) {
		return isSubNode(node.getNode());
	}

	/**
	 * Checks if a given <String> node is a sub-node of the <Node>.
	 * 
	 * @param node
	 *            The <String> node being tested.
	 * @return Returns true if the given <String> node is a sub-node of the <Node>.
	 */
	public boolean isSubNode(String node) {
		// Our flag to return.
		boolean returned = false;
		// Format the argument.
		node = node.toLowerCase();
		// Check if our given node string contains the node.
		// Make sure that the node does not equal the given node.
		if (!getNode().equals(node) && node.contains(getNode())) {
			// If this is true, then it is a sub-node.
			returned = true;
		}
		// Return the result.
		return returned;
	}

	public String getNode() {
		return getMongoDocument().getNode();
	}

	public boolean getFlag() {
		return getMongoDocument().getFlag();
	}

	public void setFlag(boolean flag, boolean save) {
		getMongoDocument().setFlag(flag, save);
	}

	public MongoNode getMongoDocument() {
		return this.mongoNode;
	}

	private void setMongoDocument(MongoNode mongoNode) {
		this.mongoNode = mongoNode;
	}

	/**
	 * Creates a <Node> from a given <String> node. The node should be formatted as
	 * such:
	 * 
	 * 1) "sledgehammer.node.example" -> this node will automatically be set to true
	 * for the flag.
	 * 
	 * 2) "sledgehammer.node.example:1 -> this node will be set to true.
	 * 
	 * 3) "sledgehammer.node.example:true -> this also works. (false, and other
	 * entries will be set as false)
	 * 
	 * @param node
	 *            The <String> node being packaged into a <Node> object.
	 * @return Returns a <Node> container for the given <String> node. (Note: The
	 *         <MongoDocument> for the <Node> will not be assigned to a
	 *         <MongoDocument> and cannot be implicitly saved or deleted. You may
	 *         however use 'onSave(DBObject)' explicitly to the <MongoDocument>.
	 */
	public static Node fromString(String node) {
		// Our object to return.
		Node returned = null;
		// If the node provided is null, return null.
		if (node == null) {
			return returned;
		}
		// Loaded nodes without arguments are defaulted to true.
		boolean flag = true;
		// If an argument is provided, then we grab the result. It must either be 'true'
		// or '1'.
		if (node.contains(":")) {
			// Split the node into arguments.
			String[] argSplit = node.split(":");
			// Re-assign the node variable to only the node itself.
			node = argSplit[0];
			// Set the flag if it equals 1 or true. All other entries will be flagged false.
			flag = argSplit[1].equals("1") || argSplit[1].equalsIgnoreCase("true");
		}
		// Create a temporary MongoDocument for the node.
		MongoNode mongoNode = new MongoNode(null, node, flag);
		// Create the Node object.
		returned = new Node(mongoNode);
		// Return the result.
		return returned;
	}
}