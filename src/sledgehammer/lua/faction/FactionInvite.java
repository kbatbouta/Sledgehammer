package sledgehammer.lua.faction;

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

import java.util.UUID;

import sledgehammer.database.module.faction.MongoFactionInvite;

/**
 * Class container for <MongoFactionInvite>.
 * 
 * @author Jab
 */
public class FactionInvite {

	/** The <MongoDocument>. */
	private MongoFactionInvite mongoFactionInvite;

	/**
	 * Main constructor.
	 * 
	 * @param mongoFactionInvite
	 *            The <MongoDocument> container.
	 */
	public FactionInvite(MongoFactionInvite mongoFactionInvite) {
		setMongoDocument(mongoFactionInvite);
	}

	/**
	 *
	 * @return Returns the <Long> UNIX TimeStamp that the Invite was created.
	 */
	public long getTimeInvited() {
		return getMongoDocument().getTimeInvited();
	}

	/**
	 * @param timeToLive
	 *            The <Long> Time in milliseconds that a invite is allowed to live.
	 * @return Returns true if the Invite has lived passed the provided time that it
	 *         is allowed to live.
	 */
	public boolean isExpired(long timeToLive) {
		return System.currentTimeMillis() - getTimeInvited() > timeToLive;
	}

	/**
	 * @return Returns the <UUID> representing the Player inviting.
	 */
	public UUID getInviteeId() {
		return getMongoDocument().getInviteeId();
	}

	/**
	 * @return Returns the <UUID> representing the Player invited.
	 */
	public UUID getInvitedId() {
		return getMongoDocument().getInvitedId();
	}

	/**
	 * @return Returns the <UUID> representing the <Faction> being invited to.
	 */
	public UUID getFactionId() {
		return getMongoDocument().getFactionId();
	}

	/**
	 * @return Returns the <MongoFactionInvite> document.
	 */
	public MongoFactionInvite getMongoDocument() {
		return this.mongoFactionInvite;
	}

	/**
	 * Sets the <MongoFactionInvite> document.
	 * 
	 * @param mongoFactionInvite
	 *            The <MongoFactionInvite> document to set.
	 */
	private void setMongoDocument(MongoFactionInvite mongoFactionInvite) {
		this.mongoFactionInvite = mongoFactionInvite;
	}

	/**
	 * @return Returns the <UUID> identifier for the Invite.
	 */
	public UUID getUniqueId() {
		return getMongoDocument().getUniqueId();
	}

}
