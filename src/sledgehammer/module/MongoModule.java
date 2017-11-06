package sledgehammer.module;

import com.mongodb.DB;

import sledgehammer.SledgeHammer;
import sledgehammer.database.SledgehammerDatabase;

/**
 * Module class designed to package simple MongoDB operations.
 * 
 * @author Jab
 *
 */
public abstract class MongoModule extends Module {

	/** The database for the <MongoModule>. */
	private DB database;

	/**
	 * Main constructor. Use 'getDatabase(String databaseName)' to grab the
	 * database.
	 * 
	 * @param database
	 *            The <DB> database associated with the <MongoModule>
	 */
	public MongoModule(DB database) {
		setDatabase(database);
	}

	/**
	 * @return Returns the <DB> database associated with the <MongoModule>
	 */
	public DB getDatabase() {
		return this.database;
	}

	/**
	 * Sets the <DB> database associated with the <MongoModule>
	 * 
	 * @param database
	 *            The <DB> database.
	 */
	private void setDatabase(DB database) {
		this.database = database;
	}

	/**
	 * @return Returns the <SledgehammerDatabase> instance for <SledgeHammer>
	 */
	public SledgehammerDatabase getSledgehammerDatabase() {
		return SledgeHammer.instance.getDatabase();
	}

	/**
	 * Returns a <DB> database with a given <String> name.
	 * 
	 * @param databaseName
	 *            The <String> name of the <DB> database.
	 * @return The <DB> database.
	 */
	@SuppressWarnings("deprecation")
	public final DB getDatabase(String databaseName) {
		return SledgeHammer.instance.getDatabase().getClient().getDB(databaseName);
	}
}
