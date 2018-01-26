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

package sledgehammer.lua.faction;

import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.module.faction.MongoFactionInvite;
import sledgehammer.lua.MongoLuaObject;

/**
 * Class container for MongoFactionInvite.
 *
 * @author Jab
 */
public class FactionInvite extends MongoLuaObject<MongoFactionInvite> {

  /**
   * Main constructor.
   *
   * @param mongoDocument The MongoDocument container.
   */
  public FactionInvite(MongoFactionInvite mongoDocument) {
    super(mongoDocument, "FactionInvite");
  }

  /**
   * Lua load constructor.
   *
   * @param mongoDocument The MongoDocument container.
   * @param table The KahluaTable to load.
   */
  public FactionInvite(MongoFactionInvite mongoDocument, KahluaTable table) {
    super(mongoDocument, "FactionInvite");
    onLoad(table);
  }

  @Override
  public void onLoad(KahluaTable table) {
    // TODO: Implement.
  }

  @Override
  public void onExport() {
    // TODO: Implement.
  }

  /** @return Returns the Long UNIX TimeStamp that the Invite was created. */
  public long getTimeInvited() {
    return getMongoDocument().getTimeInvited();
  }

  /**
   * @param timeToLive The Long Time in milliseconds that a invite is allowed to live.
   * @return Returns true if the Invite has lived passed the provided time that it is allowed to
   *     live.
   */
  public boolean isExpired(long timeToLive) {
    return System.currentTimeMillis() - getTimeInvited() > timeToLive;
  }

  /** @return Returns the Unique ID representing the Player inviting. */
  public UUID getInviteeId() {
    return getMongoDocument().getInviteeId();
  }

  /** @return Returns the Unique ID representing the Player invited. */
  public UUID getInvitedId() {
    return getMongoDocument().getInvitedId();
  }

  /** @return Returns the Unique ID representing the Faction being invited to. */
  public UUID getFactionId() {
    return getMongoDocument().getFactionId();
  }

  /** @return Returns the Unique ID identifier for the Invite. */
  public UUID getUniqueId() {
    return getMongoDocument().getUniqueId();
  }

  public void save() {
    getMongoDocument().save();
  }
}
