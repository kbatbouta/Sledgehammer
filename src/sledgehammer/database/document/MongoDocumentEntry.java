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
 * Class designed to handle partial <MongoDocument> information in a more
 * organized fashion.
 * 
 * @author Jab
 */
public abstract class MongoDocumentEntry {

	/** The <String> name of the entry. */
	private String name;

	/** The <MongoDocument> using this entry. */
	private MongoDocument mongoDocument;

	/**
	 * Main constructor.
	 * 
	 * @param mongoDocument
	 * @param name
	 */
	public MongoDocumentEntry(MongoDocument mongoDocument, String name) {
		setEntryName(name);
		setMongoDocument(mongoDocument);
	}

	/**
	 * @return Returns the <MongoDocument> storing this entry.
	 */
	public MongoDocument getMongoDocument() {
		return this.mongoDocument;
	}

	/**
	 * Sets the <MongoDocument> storing this entry.
	 * 
	 * @param mongoDocument
	 *            The <MongoDocument> to set.
	 */
	public void setMongoDocument(MongoDocument mongoDocument) {
		this.mongoDocument = mongoDocument;
	}

	/**
	 * @return Returns the <String> name of the entry.
	 */
	public String getEntryName() {
		return this.name;
	}

	/**
	 * Sets the <String> name of the entry.
	 * 
	 * @param name
	 *            The <String> name to set.
	 */
	public void setEntryName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns whether or not the MongoDocumentEntry has an assigned
	 *         <MongoDocument>.
	 */
	public boolean hasMongoDocument() {
		return getMongoDocument() != null;
	}

	/**
	 * Saves the <MongoDocument> that contains the entry.
	 * 
	 * (Note: If the <MongoDocumentEntry> does not have an assigned <MongoDocument>,
	 * an <IllegalStateException> is thrown.)
	 */
	public void save() {
		// Validate that the entry has an assigned MongoDocument.
		if (!hasMongoDocument()) {
			throw new IllegalStateException(
					"Attempting to save a document entry that does not have an assigned document."
							+ " This can be due to a temporary object being called to save."
							+ " use 'hasMongoDocument()' before calling this method.");
		}
		// Save the document with the entry.
		getMongoDocument().save();
	}

	/**
	 * Deletes the <MongoDocumentEntry> from the assigned <MongoDocument>, and saves
	 * the <MongoDocument>.
	 * 
	 * (Note: If the <MongoDocumentEntry> does not have an assigned <MongoDocument>,
	 * an <IllegalStateException> is thrown.)
	 */
	public void delete() {
		// Validate that the entry has an assigned MongoDocument.
		if (!hasMongoDocument()) {
			throw new IllegalStateException(
					"Attempting to delete a document entry that does not have an assigned document."
							+ " This can be due to a temporary object being called to save."
							+ " use 'hasMongoDocument()' before calling this method.");
		}
		// Remove the entry, saving the document in the process.
		getMongoDocument().removeEntry(this);
	}

	/**
	 * Loads the <MongoDocumentEntry>, handing the <DBObject> representation to the
	 * method.
	 * 
	 * @param object
	 *            The <DBObject> representing the <MongoDocumentEntry>.
	 */
	public abstract void onLoad(DBObject object);

	/**
	 * Stores the <MongoDocumentEntry> into a given <DBObject> that is created for
	 * this purpose.
	 * 
	 * @param object
	 *            The <DBObject> to store into.
	 */
	public abstract void onSave(DBObject object);
}