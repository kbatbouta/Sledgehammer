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
package sledgehammer.database.document;

import com.mongodb.DBObject;

/**
 * Class designed to handle Nodes as entries for the MongoUniqueNodeDocument
 * container.
 *
 * @author Jab
 */
public class MongoNode extends MongoDocumentElement {

    /**
     * The String node being stored.
     */
    private String node;

    /**
     * The Boolean flag to interpret the use of the node.
     */
    private boolean flag;

    /**
     * New constructor.
     *
     * @param mongoDocument The MongoDocument to set. This can be null for
     *                      temporary objects, and set later. Until a valid document is set,
     *                      the object cannot be saved with 'save()' and cannot be deleted
     *                      with 'delete()'.
     * @param node          The String ID of the MongoNode
     * @param flag          The state Boolean flag.
     */
    public MongoNode(MongoDocument mongoDocument, String node, boolean flag) {
        super(mongoDocument, "node");
        // Set the node in String format.
        setNode(node);
        // Set the flag. (Do not save as this is a load assignment)
        setFlag(flag, false);
    }

    /**
     * MongoDB constructor.
     *
     * @param mongoDocument The MongoDocument to set. This can be null for
     *                      temporary objects, and set later. Until a valid document is set,
     *                      the object cannot be saved with 'save()' and cannot be deleted
     *                      with 'delete()'.
     */
    public MongoNode(MongoDocument mongoDocument) {
        super(mongoDocument, "node");
        // Make sure the MongoDocument isn't null. This is explicitly the load
        // constructor.
        if (mongoDocument == null) {
            throw new IllegalArgumentException("MongoDocument given is null. (Load constructor)");
        }
    }

    @Override
    public void onLoad(DBObject object) {
        // Grab the node in String format.
        setNode(object.get("name").toString());
        // Grab the assigned flag.
        setFlag(object.get("flag").toString().equals("1"), false);
    }

    @Override
    public void onSave(DBObject object) {
        // Save the node in String format.
        object.put("name", getNode());
        // Save the assigned flag.
        object.put("flag", getFlag() ? "1" : "0");
    }

    /**
     * @return Returns the state flag of the MongoNode.
     */
    public boolean getFlag() {
        return this.flag;
    }

    /**
     * Sets the state flag of the Node.
     *
     * @param flag The flag being set.
     * @param save Flag to save the document containing this node.
     */
    public void setFlag(boolean flag, boolean save) {
        this.flag = flag;
        // Save the document if the requested.
        if (save) {
            save();
        }
    }

    /**
     * @return Returns the node ID of the MongoNode.
     */
    public String getNode() {
        return this.node;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the node ID of the MongoNode.
     *
     * @param node The node ID to set.
     */
    private void setNode(String node) {
        this.node = node.toLowerCase();
    }
}
