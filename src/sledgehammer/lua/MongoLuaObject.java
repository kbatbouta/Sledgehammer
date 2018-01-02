package sledgehammer.lua;

import sledgehammer.database.document.MongoDocument;

/**
 * Boilerplate utility Class to handle the generic assignment of
 * <MongoDocument>'s to exported <LuaTable>'s.
 * 
 * @author Jab
 * @param <M>
 *            The <MongoDocument> sub-class.
 */
public abstract class MongoLuaObject<M extends MongoDocument> extends LuaTable {

	/** The <MongoDocument> storing the data for the <LuaTable>. */
	private M mongoDocument;

	/**
	 * Main constructor.
	 * 
	 * @param mongoDocument
	 *            The <MongoDocument> storing the data for the <LuaTable>.
	 * @param name
	 *            The <String> name of the <LuaObject>.
	 */
	public MongoLuaObject(M mongoDocument, String name) {
		super(name);
		setMongoDocument(mongoDocument);
	}

	/**
	 * @return Returns the <MongoDocument> storing the data for the <LuaTable>.
	 */
	public M getMongoDocument() {
		return this.mongoDocument;
	}

	/**
	 * Sets the <MongoDocument> storing the data for the <LuaTable>.
	 * 
	 * @param mongoDocument
	 *            The <MongoDocument. to set.
	 */
	protected void setMongoDocument(M mongoDocument) {
		this.mongoDocument = mongoDocument;
	}
}