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

import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;

/**
 * A class designed to handle common operations of Mongo DBObjects that act as
 * documents in a <DBCollection>.
 *
 * @author Jab
 */
public abstract class MongoDocument {

    /**
     * The Map that stores entries.
     */
    private Map<String, MongoDocumentElement> mapDocumentEntries;

    /**
     * The DBCollection storing the document.
     */
    private MongoCollection collection;

    /**
     * The String identifier for the document.
     */
    private String fieldId;

    /**
     * Main constructor.
     *
     * @param collection The MongoCollection storing the document.
     * @param fieldId    The String identifier for the document.
     */
    public MongoDocument(MongoCollection collection, String fieldId) {
        setCollection(collection);
        setFieldId(fieldId);
        mapDocumentEntries = new HashMap<>();
    }

    /**
     * Adds a MongoDocumentEntry to the document. This is not automatically
     * handled when the document is loaded because of the concept of sub-classing
     * document entries. This should be handled when loading the document. The
     * entries, however will save if they are added when the document is loaded.
     *
     * @param entry The MongoDocumentEntry being added to the document.
     */
    public void addEntry(MongoDocumentElement entry) {
        mapDocumentEntries.put(entry.getEntryName(), entry);
    }

    /**
     * Removes a given MongoDocumentEntry from the document.
     *
     * @param entry The MongoDocumentEntry to remove.
     * @return Returns true if the document exists and is removed.
     */
    public boolean removeEntry(MongoDocumentElement entry) {
        return removeEntry(entry.getEntryName());
    }

    /**
     * Removes a MongoDocumentEntry from the document with a given String name.
     * This also saves the document.
     *
     * @param entryName The String name of the document being removed.
     * @return Returns true if the document exists and is removed.
     */
    public boolean removeEntry(String entryName) {
        boolean result = this.mapDocumentEntries.remove(entryName) != null;
        // If the entry is contained, then process a save.
        if (result) {
            // Create a new DBObject with the document's identifier.
            DBObject object = new BasicDBObject(getFieldId(), getFieldValue());
            // Populate the main document.
            onSave(object);
            // Go through each entry.
            for (String key : mapDocumentEntries.keySet()) {
                // Grab the next entry with the provided key.
                MongoDocumentElement entry = mapDocumentEntries.get(key);
                // If the entry is the one we are removing.
                if (entry.getEntryName().equals(entryName)) {
                    // Set the field explicitly to null.
                    object.put(entryName, null);
                }
                // Else, Save it as normal.
                else {
                    // Create a new DBObject to populate with the entry data.
                    DBObject objectEntry = new BasicDBObject();
                    // Populate the DBObject.
                    entry.onSave(objectEntry);
                    // Set the DBObject with the key into the main object.
                    object.put(key, objectEntry);
                }
            }
            // Upsert the document.
            getCollection().upsert(object, getFieldId(), this);
        }
        // If the entry is not in the document at the time of attempting to remove it,
        // then this is an illegal situation. Throw the error.
        else {
            throw new IllegalArgumentException(
                    "Entry does not exist for instance of \"" + getClass().getName() + "\": \"" + entryName + "\".");
        }
        return true;
    }

    /**
     * @return Returns the String ID of the document to store. This is assigned in
     * the constructor, and is used for the 'upsert' methodology.
     */
    public String getFieldId() {
        return this.fieldId;
    }

    /**
     * (Private method)
     * <p>
     * Sets the String ID of the document to store. This is called from the
     * constructor, and is used for the 'upsert' methodology.
     *
     * @param fieldId The String ID to set.
     */
    private void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the DBCollection storing the document.
     *
     * @param collection The DBCollection to set.
     */
    private void setCollection(MongoCollection collection) {
        this.collection = collection;
    }

    /**
     * @return Returns the DBCollection that stores the document if saved.
     */
    public MongoCollection getCollection() {
        return this.collection;
    }

    /**
     * Saves the MongoDocument with a given field to identify the document, if it
     * already exists.
     */
    public void save() {
        // Create a new DBObject with the document's identifier.
        DBObject object = new BasicDBObject(getFieldId(), getFieldValue());
        // Populate the main document.
        onSave(object);
        // Save the entries.
        saveEntries(object);
        // Upsert the document.
        getCollection().upsert(object, getFieldId(), this);
    }

    public void saveEntries(DBObject object) {
        // Go through each entry.
        for (String key : mapDocumentEntries.keySet()) {
            // Grab the next entry with the provided key.
            MongoDocumentElement entry = mapDocumentEntries.get(key);
            // Create a new DBObject to populate with the entry data.
            DBObject objectEntry = new BasicDBObject();
            // Populate the DBObject.
            entry.onSave(objectEntry);
            // Set the DBObject with the key into the main object.
            object.put(key, objectEntry);
        }
    }

    /**
     * Deletes the document from the assigned DBCollection.
     */
    public void delete() {
        getCollection().delete(getFieldId(), getFieldValue());
    }

    /**
     * Implemented method that loads the data for the document.
     *
     * @param object The DBObject to pass that contains the data to handle.
     */
    public abstract void onLoad(DBObject object);

    /**
     * Implemented method that saves the data from the document to the given
     * DBObject.
     *
     * @param object The DBObject to save the data to.
     */
    public abstract void onSave(DBObject object);

    /**
     * @return Returns the identification value for the 'upsert' methodology.
     */
    public abstract Object getFieldValue();
}