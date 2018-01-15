/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.lua;

import sledgehammer.database.document.MongoDocument;

/**
 * Boilerplate utility Class to handle the generic assignment of
 * MongoDocuments to exported LuaTables.
 *
 * @param <M> The MongoDocument sub-class.
 * @author Jab
 */
public abstract class MongoLuaObject<M extends MongoDocument> extends LuaTable {

    /**
     * The MongoDocument storing the data for the LuaTable.
     */
    private M mongoDocument;

    /**
     * Main constructor.
     *
     * @param mongoDocument The MongoDocument storing the data for the LuaTable.
     * @param name          The String name of the LuaObject.
     */
    public MongoLuaObject(M mongoDocument, String name) {
        super(name);
        setMongoDocument(mongoDocument);
    }

    /**
     * @return Returns the MongoDocument storing the data for the LuaTable.
     */
    public M getMongoDocument() {
        return this.mongoDocument;
    }

    /**
     * Sets the MongoDocument storing the data for the LuaTable.
     *
     * @param mongoDocument The MongoDocument to set.
     */
    protected void setMongoDocument(M mongoDocument) {
        this.mongoDocument = mongoDocument;
    }
}