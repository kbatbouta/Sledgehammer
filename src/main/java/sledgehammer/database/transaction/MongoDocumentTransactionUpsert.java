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

package sledgehammer.database.transaction;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import sledgehammer.Settings;
import sledgehammer.database.MongoCollection;

public class MongoDocumentTransactionUpsert extends MongoDocumentTransaction {

  private DBObject object;
  private String field;
  private Object lock;

  public MongoDocumentTransactionUpsert(
      MongoCollection collection, DBObject object, String field, Object lock) {
    super(collection);
    setObject(object);
    setField(field);
    setLock(lock);
  }

  @Override
  public void run() {
    MongoCollection collection = getMongoCollection();
    DBCollection dbCollection = collection.getDBCollection();
    String field = getField();
    DBObject object = getObject();
    BasicDBObject append = new BasicDBObject();
    append.append("$set", object);
    Object id = object.get(field);
    if (Settings.getInstance().isDebug()) {
      System.out.println(
          "("
              + dbCollection.getName()
              + "): Upserting document: (field:"
              + field
              + " id:"
              + id
              + ")");
    }
    dbCollection.update(new BasicDBObject(field, object.get(field)), append, true, false);
  }

  public Object getLock() {
    return this.lock;
  }

  private void setLock(Object lock) {
    this.lock = lock;
  }

  public DBObject getObject() {
    return this.object;
  }

  private void setObject(DBObject object) {
    this.object = object;
  }

  public String getField() {
    return this.field;
  }

  private void setField(String field) {
    this.field = field;
  }
}
