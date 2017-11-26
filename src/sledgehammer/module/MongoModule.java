package sledgehammer.module;

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

import com.mongodb.DB;

import sledgehammer.SledgeHammer;
import sledgehammer.database.module.core.SledgehammerDatabase;

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
	public final static DB getDatabase(String databaseName) {
		return SledgeHammer.instance.getDatabase().getClient().getDB(databaseName);
	}
	
	/**
	 * @return Returns the default <DB> database.
	 */
	public final static DB getDefaultDatabase() {
		return SledgeHammer.instance.getDatabase().getDatabase();
	}
}
