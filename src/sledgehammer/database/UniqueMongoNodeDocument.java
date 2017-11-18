package sledgehammer.database;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Class to handle <String> node operations for <UniqueMongoDocument> documents.
 * 
 * (All nodes are stored and compared in lower-case automatically)
 * 
 * @author Jab
 */
public abstract class UniqueMongoNodeDocument extends UniqueMongoDocument {

	/** The List of <String> nodes. */
	private List<String> listNodes;

	/**
	 * MongoDB constructor.
	 * 
	 * @param collection
	 *            The <DBCollection> storing the document.
	 * @param object
	 *            The <DBObject> storing the data.
	 */
	public UniqueMongoNodeDocument(DBCollection collection, DBObject object) {
		super(collection, object);
		listNodes = new ArrayList<>();
	}

	/**
	 * New constructor.
	 * 
	 * @param collection
	 *            The <DBCOllection> storing the document.
	 */
	public UniqueMongoNodeDocument(DBCollection collection) {
		super(collection);
		listNodes = new ArrayList<>();
	}

	/**
	 * Handles loading nodes.
	 * 
	 * @param object
	 *            The <DBObject> storing the node data.
	 */
	public void loadNodes(DBObject object) {
		listNodes.clear();
		@SuppressWarnings({ "rawtypes" })
		List objectList = (List) object.get("nodes");
		for (Object next : objectList) {
			String node = (String) next;
			listNodes.add(node);
		}
	}

	/**
	 * Handles saving nodes.
	 * 
	 * @param object
	 *            The <DBObject> that stores the nodes.
	 */
	public void saveNodes(DBObject object) {
		object.put("nodes", getNodes());
	}

	/**
	 * Adds a node to the document, if the entry does not exist.
	 * 
	 * (All nodes are stored and compared in lower-case automatically)
	 * 
	 * @param node
	 *            The <String> node being added to the document.
	 * @param save
	 *            Flag to save the document after adding the node.
	 * @return
	 */
	public boolean addNode(String node, boolean save) {
		boolean returned = false;
		if (!hasNode(node)) {
			node = node.toLowerCase();
			listNodes.add(node);
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
	public boolean removeNode(String node, boolean save) {
		boolean returned = false;
		if (hasNode(node)) {
			node = node.toLowerCase();
			listNodes.remove(node);
			returned = true;
			if (save) save();
		}
		return returned;
	}

	/**
	 * @param node
	 *            The <String> node being tested.
	 * @return Returns true if the document contains the node.
	 */
	public boolean hasNode(String node) {
		node = node.toLowerCase();
		return listNodes.contains(node);
	}

	/**
	 * @return Returns a <List> of the <String> nodes assigned to the document.
	 */
	public List<String> getNodes() {
		return listNodes;
	}
}