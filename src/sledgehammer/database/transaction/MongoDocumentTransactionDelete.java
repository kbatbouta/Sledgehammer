package sledgehammer.database.transaction;

import com.mongodb.BasicDBObject;

import sledgehammer.database.MongoCollection;

public class MongoDocumentTransactionDelete extends MongoDocumentTransaction {

	private String field;
	private Object value;

	public MongoDocumentTransactionDelete(MongoCollection collection, String field, Object value) {
		super(collection);
		setField(field);
		setValue(value);
	}

	@Override
	public void run() {
		String field = getField();
		Object value = getValue();
		getMongoCollection().getDBCollection().remove(new BasicDBObject(field, value));
	}

	public String getField() {
		return this.field;
	}

	private void setField(String field) {
		this.field = field;
	}

	public Object getValue() {
		return this.value;
	}

	private void setValue(Object value) {
		this.value = value;
	}

}
