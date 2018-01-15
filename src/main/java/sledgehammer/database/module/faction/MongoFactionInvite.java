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

/**
 * TODO: Document
 *
 * @author Jab
 */
public class MongoFactionInvite extends MongoDocument {

    private UUID inviteeId;
    private UUID invitedId;
    private UUID factionId;
    private UUID uniqueId;
    private long timeInvited;

    /**
     * Loading constructor.
     *
     * @param collection
     *            The MongoCollection storing the invite.
     * @param object
     *            The DBObject storing the invite data.
     */
    public MongoFactionInvite(MongoCollection collection, DBObject object) {
        super(collection, "id");
        onLoad(object);
    }

    public MongoFactionInvite(MongoCollection collection, UUID inviteeId, UUID invitedId, UUID factionId) {
        super(collection, "id");
        setUniqueId(UUID.randomUUID());
        setInviteeId(inviteeId);
        setInvitedId(invitedId);
        setFactionId(factionId);
        setTimeInvited(System.currentTimeMillis());
    }

    @Override
    public void onLoad(DBObject object) {
        setUniqueId(UUID.fromString(object.get("id").toString()));
        setInviteeId(UUID.fromString(object.get("inviteeId").toString()));
        setInvitedId(UUID.fromString(object.get("invitedId").toString()));
        setFactionId(UUID.fromString(object.get("factionId").toString()));
        setTimeInvited(Long.parseLong(object.get("timeInvited").toString()));
    }

    @Override
    public void onSave(DBObject object) {
        // @formatter:off
		object.put("inviteeId", getInviteeId().toString());
		object.put("invitedId", getInvitedId().toString());
		object.put("factionId", getFactionId().toString());
		object.put("timeInvited", getTimeInvited() + "");
		// @formatter:on
    }

    @Override
    public Object getFieldValue() {
        return getUniqueId();
    }

    /**
     * @return Returns the Long UNIX TimeStamp representing the time the
     * Invitation was created.
     */
    public long getTimeInvited() {
        return this.timeInvited;
    }

    /**
     * Sets the time that the Invitation was created.
     *
     * @param timeInvited The Long UNIX TimeStamp representing the time the Invitation was
     *                    created.
     */
    private void setTimeInvited(long timeInvited) {
        this.timeInvited = timeInvited;
    }

    public UUID getFactionId() {
        return this.factionId;
    }

    private void setFactionId(UUID factionId) {
        this.factionId = factionId;
    }

    public UUID getInviteeId() {
        return this.inviteeId;
    }

    private void setInviteeId(UUID playerInviteeId) {
        this.inviteeId = playerInviteeId;
    }

    public UUID getInvitedId() {
        return this.invitedId;
    }

    private void setInvitedId(UUID playerInvitedId) {
        this.invitedId = playerInvitedId;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    private void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
}