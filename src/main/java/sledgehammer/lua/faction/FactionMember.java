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
import sledgehammer.SledgeHammer;
import sledgehammer.database.module.faction.MongoFactionMember;
import sledgehammer.lua.MongoLuaObject;
import sledgehammer.lua.core.Color;
import sledgehammer.lua.core.Player;
import sledgehammer.module.faction.ModuleFactions;

/**
 * MongoLuaObject to handle faction-member data and operations for the Factions Module.
 *
 * @author Jab
 */
public class FactionMember extends MongoLuaObject<MongoFactionMember> {

  /** The Faction the FactionMember is in. */
  private Faction faction;

  /**
   * Main constructor.
   *
   * @param mongoDocument The MongoDocument container.
   */
  public FactionMember(MongoFactionMember mongoDocument) {
    super(mongoDocument, "FactionMember");
    setMongoDocument(mongoDocument);
  }

  @Override
  public void onLoad(KahluaTable table) {
    // TODO: implement.
  }

  @Override
  public void onExport() {
    // TODO: implement.
  }

  /**
   * Sets the new Faction.
   *
   * @param faction The Faction to set.
   * @param save The flag to save the Document.
   */
  public void setFaction(Faction faction, boolean save) {
    Faction factionOld = getFaction();
    if (factionOld != null) {
      factionOld.removeMember(this);
    }
    if (faction != null) {
      boolean success = faction.addMember(this);
      if (success) {
        this.faction = faction;
        getMongoDocument().setFactionId(faction.getUniqueId(), save);
      }
    } else {
      this.faction = null;
    }
    Player player = SledgeHammer.instance.getPlayer(getPlayerId());
    setTag(player);
  }

  public void setTag(Player player) {
    if (player != null) {
      if (faction != null) {
        player.setNickname("[" + faction.getFactionTag() + "] " + player.getUsername());
        player.setColor(Color.getColor(faction.getFactionRawColor()));
      } else {
        player.setNickname(null);
        player.setColor(Color.WHITE);
      }
    }
  }

  /** Removes the FactionMember from the Faction. */
  public void leaveFaction() {
    ModuleFactions moduleFactions = SledgeHammer.instance.getModule(ModuleFactions.class);
    moduleFactions.removeFactionMember(this);
  }

  /** @return Returns the Unique ID of the Player as the FactionMember. */
  public UUID getPlayerId() {
    return getMongoDocument().getPlayerId();
  }

  /** @return Returns the Unique ID of the Faction the FactionMember is in. */
  public UUID getFactionId() {
    return getMongoDocument().getFactionId();
  }

  /** @return Returns the Faction the FactionMember is in. */
  public Faction getFaction() {
    return this.faction;
  }
}
