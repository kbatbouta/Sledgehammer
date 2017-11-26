package sledgehammer.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public abstract class MongoNodeDocument extends MongoDocument {

	/** The Map storing <MongoNode>s by their node value as the key. */
	private Map<String, MongoNode> mapNodes;
	
	/**
	 * Main constructor.
	 * 
	 * @param collection
	 *            The <DBCollection> storing the document.
	 * @param fieldId
	 *            The <String> identifier for the document.
	 */
	public MongoNodeDocument(DBCollection collection, String fieldId) {
		super(collection, fieldId);
		mapNodes = new HashMap<>();
	}
	
	/**
	 * Handles loading nodes.
	 * 
	 * @param object
	 *            The <DBObject> storing the node data.
	 */
	public void loadNodes(DBObject object) {
		mapNodes.clear();
		@SuppressWarnings({ "rawtypes" })
		List objectList = (List) object.get("nodes");
		for (Object nextObject : objectList) {
			DBObject nextDBObject = (DBObject) nextObject;
			MongoNode mongoNode = new MongoNode(this);
			mongoNode.onLoad(nextDBObject);
			addNode(mongoNode, false);
		}
	}

	/**
	 * Handles saving nodes.
	 * 
	 * @param object
	 *            The <DBObject> that stores the nodes.
	 */
	public void saveNodes(DBObject object) {
		// Create a list of objects for the export.
		List<DBObject> listNodes = new ArrayList<>();
		// Go through each node in the document.
		for (MongoNode mongoNode : getNodes()) {
			// Create the object to contain the saved data.
			DBObject objectMongoNode = new BasicDBObject();
			// Save the MongoNode to the object.
			mongoNode.onSave(objectMongoNode);
			// Add the result object to the list.
			listNodes.add(objectMongoNode);
		}
		// Place the nodes into the main document object provided.
		object.put("nodes", listNodes);
	}

	/**
	 * Adds a <MongoNode> to the document, if the entry does not exist.
	 * 
	 * (All nodes are stored and compared in lower-case automatically)
	 * 
	 * @param mongoNode
	 *            The <MongoNode> being added to the document.
	 * @param save
	 *            Flag to save the document after adding the <MongoNode>.
	 * @return Returns true if the <MongoNode> is added to the document.
	 */
	public boolean addNode(MongoNode mongoNode, boolean save) {
		boolean returned = false;
		if (!hasNode(mongoNode)) {
			mapNodes.put(mongoNode.getNode(), mongoNode);
			returned = true;
			if (save) save();
		}
		return returned;
	}

	/**
	 * Removes a node from the document, if the entry exists.
	 * 
	 * (All nodes are stored and compared in lower-case automatically)
	 * 
	 * @param node
	 *            The <String> node being removed from the document.
	 * @param save
	 *            Flag to save the document after removing the node.
	 * @return
	 */
	public boolean removeNode(MongoNode mongoNode, boolean save) {
		boolean returned = false;
		if (hasNode(mongoNode)) {
			mapNodes.remove(mongoNode.getNode());
			returned = true;
			if (save) save();
		}
		return returned;
	}

	/**
	 * Checks if a <MongoNode> is assigned to the document.
	 * 
	 * @param node
	 *            The <MongoNode> being tested.
	 * @return Returns true if the document contains the node.
	 */
	public boolean hasNode(MongoNode mongoNode) {
		return mapNodes.containsKey(mongoNode.getNode());
	}

	/**
	 * @return Returns a <List> of the <String> nodes assigned to the document.
	 */
	public Collection<MongoNode> getNodes() {
		return mapNodes.values();
	}
}
