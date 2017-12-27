package sledgehammer.lua;

import sledgehammer.database.document.MongoDocument;

public abstract class MongoLuaObject<M extends MongoDocument> extends LuaTable {

	private M mongoDocument;
	
	public MongoLuaObject(M mongoDocument, String name) {
		super(name);
		setMongoDocument(mongoDocument);
	}

	public M getMongoDocument() {
		return this.mongoDocument;
	}

	public void setMongoDocument(M mongoDocument) {
		this.mongoDocument = mongoDocument;
	}
}