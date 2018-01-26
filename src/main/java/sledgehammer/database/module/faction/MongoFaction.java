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

package sledgehammer.database.module.faction;

import java.util.UUID;

import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoDocument;
import sledgehammer.util.ChatTags;
import zombie.sledgehammer.util.MD5;

/**
 * TODO: Document
 *
 * @author Jab
 */
public class MongoFaction extends MongoDocument {

  private UUID uniqueId;
  private String name;
  private String tag;
  private String color = "white";
  private String colorCache = ChatTags.getColor(color);
  private String password;
  private UUID ownerId;

  /**
   * Loading constructor.
   *
   * @param collection The MongoCollection storing the MongoDocument.
   * @param object The DBObject storing the faction data.
   */
  public MongoFaction(MongoCollection collection, DBObject object) {
    super(collection, "id");
    onLoad(object);
  }

  /**
   * Constructor for new factions.
   *
   * @param collection The MongoCollection storing the MongoDocument.
   * @param name The name of the Faction.
   * @param tag The tag for Faction members.
   * @param color The color of the Faction. (The un-rasterize form. a.k.a: light_blue
   * @param ownerId The Unique ID of the Player owning the faction.
   * @param password The un-encrypted password of the faction.
   */
  public MongoFaction(
      MongoCollection collection,
      String name,
      String tag,
      String color,
      UUID ownerId,
      String password) {
    super(collection, "id");
    setUniqueId(UUID.randomUUID(), false);
    setFactionName(name, false);
    setFactionTag(tag, false);
    setFactionColorString(color, false);
    setPassword(password, false);
    setOwnerId(ownerId, false);
  }

  @Override
  public void onLoad(DBObject object) {
    setUniqueId(object.get("id").toString(), false);
    setFactionName(object.get("name").toString(), false);
    setFactionTag(object.get("tag").toString(), false);
    setOwnerId(object.get("ownerId").toString(), false);

    Object oColor = object.get("color");
    if (oColor != null) {
      setFactionColorString(oColor.toString(), true);
    }

    Object oPassword = object.get("password");
    if (oPassword != null) {
      setEncryptedPassword(oPassword.toString(), false);
    }
  }

  @Override
  public void onSave(DBObject object) {
    // @formatter:off
    object.put("id", getUniqueId().toString());
    object.put("name", getFactionName());
    object.put("tag", getFactionTag());
    object.put("color", getFactionRawColor());
    object.put("ownerId", getOwnerId().toString());
    object.put("password", getEncryptedPassword());
    // @formatter:on
  }

  @Override
  public Object getFieldValue() {
    return getUniqueId().toString();
  }

  public void setPassword(String password, boolean save) {
    setEncryptedPassword(MD5.encrypt(password), save);
  }

  public String getEncryptedPassword() {
    return this.password;
  }

  private void setEncryptedPassword(String password, boolean save) {
    this.password = password;
    if (save) save();
  }

  public String getFactionColor() {
    return this.colorCache;
  }

  public void setFactionColorString(String color, boolean save) {
    this.color = color;
    this.colorCache = ChatTags.getColor(color);
    if (save) save();
  }

  public String getFactionTag() {
    return this.tag;
  }

  public void setFactionTag(String tag, boolean save) {
    this.tag = tag.toUpperCase();
    if (save) save();
  }

  public String getFactionName() {
    return this.name;
  }

  public void setFactionName(String name, boolean save) {
    this.name = name;
    if (save) save();
  }

  public UUID getOwnerId() {
    return this.ownerId;
  }

  public void setOwnerId(UUID uniqueId, boolean save) {
    this.ownerId = uniqueId;
    if (save) save();
  }

  private void setOwnerId(String uniqueId, boolean save) {
    setOwnerId(UUID.fromString(uniqueId), save);
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public void setUniqueId(UUID uniqueId, boolean save) {
    this.uniqueId = uniqueId;
    if (save) save();
  }

  public void setUniqueId(String uniqueId, boolean save) {
    setUniqueId(UUID.fromString(uniqueId), save);
  }

  public String getFactionRawColor() {
    return this.color;
  }

  public String getFactionColorTag() {
    return this.colorCache;
  }
}
