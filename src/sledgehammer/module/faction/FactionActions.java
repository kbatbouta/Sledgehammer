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
package sledgehammer.module.faction;

import java.util.List;

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

import sledgehammer.lua.core.Player;
import sledgehammer.lua.faction.Faction;
import sledgehammer.lua.faction.FactionInvite;
import sledgehammer.lua.faction.FactionMember;
import sledgehammer.util.ChatTags;
import sledgehammer.util.Response;
import sledgehammer.util.Result;

/**
 * TODO: Document
 * 
 * @author Jab
 */
public class FactionActions {

	/** The <ModuleFactions> instance using this. */
	private ModuleFactions module;

	/**
	 * Main constructor.
	 * 
	 * @param module
	 *            The <ModuleFactions> instance using this.
	 */
	public FactionActions(ModuleFactions module) {
		setModule(module);
	}

	/**
	 * Attempts to create a <Faction> with given parameters.
	 * 
	 * @param factionName
	 *            The <String> name of the <Faction>.
	 * @param tag
	 *            The <String> tag of the <Faction>.
	 * @param password
	 *            The administrator <String> password of the <Faction>.
	 * @param player
	 *            The <Player> creating the <Faction.
	 * @return The <Response> result.
	 */
	public Response createFaction(String factionName, String tag, String password, Player player) {
		Response response = null;
		if (factionName == null || factionName.isEmpty()) {
			return new Response("Faction name is invalid.", "", Result.FAILURE);
		}
		factionName = factionName.trim();
		if (module.factionExists(factionName)) {
			return new Response("Faction already exists: " + factionName, "", Result.FAILURE);
		}
		response = module.validateFactionTag(tag);
		if (response.getResult() == Result.FAILURE) {
			return response;
		}
		tag = tag.toUpperCase();
		if (password == null || password.isEmpty()) {
			return new Response("Faction Password is invalid. A password MUST be provided and cannot be empty.", "",
					Result.FAILURE);
		}
		password = password.trim();
		response = module.validatePlayerFactionCreate(player);
		if (response.getResult() == Result.FAILURE) {
			return response;
		}
		try {
			module.createFaction(factionName, tag, password, player.getUniqueId());
			return new Response("Faction created. Be sure to write down your password!",
					player.getUsername() + " created faction \"" + factionName + "\".", Result.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Response("Failed to create faction. (Internal Error)", "", Result.FAILURE);
	}

	/**
	 * Attempts to disband the given <Faction>.
	 * 
	 * @param player
	 *            The <Player> attempting to disband the <Faction>
	 * @return The <Response> result.
	 */
	public Response disbandFaction(Player player) {
		FactionMember factionMember = module.getFactionMember(player);
		if (factionMember == null) {
			return new Response("You are not in a faction.", "", Result.FAILURE);
		}
		Faction faction = factionMember.getFaction();
		if (!faction.isOwner(player)) {
			return new Response("You don't own this faction.", "", Result.FAILURE);
		}
		try {
			module.deleteFaction(faction);
			return new Response("Faction disbanded.",
					player.getUsername() + " disbanded the faction \"" + faction.getFactionName() + "\".",
					Result.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Response("Failed to disband faction. (Internal Error)", "", Result.FAILURE);
	}

	/**
	 * TODO: Document.
	 * 
	 * @param playerOwner
	 * @param playerInvited
	 * @return
	 */
	public Response inviteToFaction(Player playerOwner, Player playerInvited) {
		// Grab the unique ID of the Player.
		UUID playerOwnerId = playerOwner.getUniqueId();
		UUID playerInvitedId = playerInvited.getUniqueId();
		if (playerOwner.equals(playerInvited)) {
			return new Response("You cannot invite yourself.", "", Result.FAILURE);
		}
		// Grab the FactionMember.
		FactionMember member = module.getFactionMember(playerOwnerId);
		// If the member object is null, the Owner provided is not in a Faction.
		if (member == null) {
			return new Response("Sorry! You are not currently in a faction.", "", Result.FAILURE);
		}
		// Grab the Faction of the Member.
		Faction factionOwner = member.getFaction();
		// Data consistency check.
		if (factionOwner == null) {
			throw new IllegalStateException("FactionMember exists for player ID: \"" + playerOwnerId.toString()
					+ "\", yet the Faction representing the member is null.");
		}
		// Check to see if the FactionMember is the owner of the Faction.
		if (!factionOwner.isOwner(playerOwner)) {
			return new Response("Sorry! You do not own the faction \"" + factionOwner.getFactionName() + "\".", "",
					Result.FAILURE);
		}

		// Grab the FactionMember of the invited player.
		FactionMember memberInvited = module.getFactionMember(playerInvitedId);
		// If the invited player is a member of a Faction, check to see if he is valid
		// to receive invites.
		if (memberInvited != null) {
			// Grab the Faction.
			Faction factionInvited = memberInvited.getFaction();
			// If the invited player's faction is the same as the owner.
			if (factionInvited.equals(factionOwner)) {
				return new Response("Player \"" + playerInvited.getUsername() + "\" is in the same faction.", "",
						Result.FAILURE);
			}
			// If the invited player is the owner of his faction.
			if (factionInvited.isOwner(playerInvited)) {
				return new Response(
						"Player \"" + playerInvited.getUsername()
								+ "\" is the owner of another Faction, and cannot be invited to your faction.",
						"", Result.FAILURE);
			}
		}
		// Process the invite.
		module.createFactionInvite(factionOwner, playerOwner.getUniqueId(), playerInvited.getUniqueId());
		return null;
	}

	/**
	 * TODO: Document
	 * 
	 * @param faction
	 * @param player
	 * @param password
	 * @return
	 */
	public Response joinFaction(Faction faction, Player player, String password) {
		// Grab the FactionMember container if it exists.
		FactionMember factionMember = module.getFactionMember(player);
		Faction factionCurrent = null;
		// If the player is currently in another faction.
		if (factionMember != null) {
			// Grab the current faction the player is in.
			factionCurrent = factionMember.getFaction();
			// Check to make sure the player joining isn't the owner of a faction.
			if (factionCurrent.isOwner(factionMember)) {
				return new Response("You are currently the owner of a faction and cannot join another faction.", "",
						Result.FAILURE);
			}
			// Check to make sure the faction to join isn't the same faction.
			if (factionCurrent.equals(faction)) {
				return new Response("You are already in this faction.", "", Result.FAILURE);
			}
		}
		// Check to make sure the password given is valid.
		if (!faction.isPassword(password)) {
			return new Response("Incorrect password.", "", Result.FAILURE);
		}
		// If the player is currently a member in a faction, remove.
		if (factionCurrent != null) {
			factionCurrent.removeMember(factionMember);
		}
		// If the player is not in a faction, create the member and save.
		if (factionMember == null) {
			factionMember = module.createFactionMember(player, faction);
		} else {
			factionMember.setFaction(factionCurrent, true);
		}
		// Return success message.
		return new Response("Joined faction \"" + faction.getFactionName() + "\".", "", Result.SUCCESS);
	}

	/**
	 * TODO: Document.
	 * 
	 * @param player
	 * @return
	 */
	public Response leaveFaction(Player player) {
		if (player == null) {
			throw new IllegalArgumentException("Player given is null!");
		}
		// Grab the FactionMember container for the player.
		FactionMember factionMember = module.getFactionMember(player);
		// Check to make sure the Player is a member of a faction.
		if (factionMember == null) {
			return new Response("You are not in a faction.", "", Result.FAILURE);
		}
		// Grab the Faction
		Faction faction = factionMember.getFaction();
		// Check to make sure the member is not the owner of the faction.
		if (faction.isOwner(factionMember)) {
			return new Response("You are the owner of your faction."
					+ "In order to leave it, you must first disband it, or transfer ownership to another member.", "",
					Result.FAILURE);
		}
		// Process leaving the faction.
		factionMember.leaveFaction();
		// Return success message.
		return new Response("Left the faction.", "", Result.SUCCESS);
	}

	/**
	 * TODO: Document
	 * 
	 * @param player
	 * @param faction
	 * @return
	 */
	public Response acceptInvite(Player player, Faction faction) {
		// Check to see if the invite exists.
		FactionInvite factionInvite = module.getFactionInvite(player, faction);
		// Check if there's no invite for the player.
		if (factionInvite == null) {
			return new Response("You do not have an invite from the faction \"" + faction.getFactionName() + "\".", "",
					Result.FAILURE);
		}
		// Process and return the Response from the module.
		return module.acceptInvite(factionInvite);
	}

	/**
	 * Rejects & deletes <FactionInvite> invites for the Player. If null is passed
	 * on the <Faction> argument, then all invites that exist (if any), will be
	 * rejects and deleted.
	 * 
	 * @param player
	 *            The <Player> being affected.
	 * @param faction
	 *            The <Faction> being specified. If null is passed, all <Faction>
	 *            invites will be processed.
	 * @return A <Response> based on the outcome. If any <FactionInvite> invites are
	 *         rejected and removed, Result.SUCCESS will be passed.
	 */
	public Response rejectInvites(Player player, Faction faction) {
		// Parameter check.
		if (player == null) {
			throw new IllegalArgumentException("Player given is null!");
		}
		// If the faction is specified, grab only that faction's invite, if it exists.
		if (faction != null) {
			// Grab the faction's invite, if it exists.
			FactionInvite factionInvite = module.getFactionInvite(player, faction);
			// If the invite does not exist, then let the player know.
			if (factionInvite == null) {
				return new Response("You do not have an invite from the faction \"" + faction.getFactionName() + "\".",
						"", Result.FAILURE);
			}
			// It exists, so delete it.
			module.deleteInvite(factionInvite);
			// Let the player know that the invite is rejected.
			return new Response("Rejected invite from the faction \"" + faction.getFactionName() + "\".", "",
					Result.SUCCESS);
		}
		// If the faction is not specified, we grab every faction invite for the player.
		else {
			// Grab all the invites for the Player.
			List<FactionInvite> listFactionInvites = module.getInvitesForPlayer(player);
			// Store the amount.
			int countInvites = listFactionInvites.size();
			// if the amount of invites is 0, let the player know that he doesn't have any
			// invites.
			if (countInvites == 0) {
				return new Response("There are no invites for you to reject.", "", Result.FAILURE);
			}
			// Go through each Invite.
			for (FactionInvite factionInvite : listFactionInvites) {
				// Delete the invite.
				module.deleteInvite(factionInvite);
			}
			// Properly display the amount of invites rejected tot he player.
			String plural = listFactionInvites.size() != 1 ? "s" : "";
			return new Response("Removed " + countInvites + " Invite" + plural + ".", "", Result.SUCCESS);
		}
	}

	/**
	 * TODO: Document
	 * 
	 * @param playerOwner
	 * @param playerKick
	 * @return
	 */
	public Response kickFromFaction(Player playerOwner, Player playerKick) {
		FactionMember factionMemberOwner = module.getFactionMember(playerOwner);
		if (factionMemberOwner == null) {
			return new Response("You are not on a faction.", "", Result.FAILURE);
		}
		Faction faction = factionMemberOwner.getFaction();
		if (!faction.isOwner(factionMemberOwner)) {
			return new Response("You do not own your faction.", "", Result.FAILURE);
		}
		FactionMember factionMemberKick = module.getFactionMember(playerKick);
		if (factionMemberKick == null) {
			return new Response("Player is not in a faction: \"" + playerKick.getUsername() + "\".", "",
					Result.FAILURE);
		}
		Faction factionKick = factionMemberKick.getFaction();
		if (!factionKick.equals(faction)) {
			return new Response("Player is not in the same faction.", "", Result.FAILURE);
		}
		module.removeFactionMember(factionMemberKick);
		// Return the success message.
		return new Response("Player kicked from the faction.", "", Result.SUCCESS);
	}

	/**
	 * Validates & changes the password for a <Faction>.
	 * 
	 * @param player
	 *            The <Player>. Will return as a failure if not the owner of a
	 *            <Faction>.
	 * @param passwordOriginal
	 *            The original <String> password to validate the change.
	 * @param passwordNew
	 *            The new <String> password to set for the <Faction>.
	 * @return Returns a <Response> result. If all validations pass, Result.SUCCESS
	 *         is passed.
	 */
	public Response changeFactionPassword(Player player, String passwordOriginal, String passwordNew) {
		// Grab the FactionMember for the Player.
		FactionMember factionMember = module.getFactionMember(player);
		// Make sure the Player is in a Faction.
		if (factionMember == null) {
			return new Response("You do not own a faction.", "", Result.FAILURE);
		}
		// Grab the Faction.
		Faction faction = factionMember.getFaction();
		// Check to make sure the Player is the owner of the faction.
		if (!faction.isOwner(factionMember)) {
			return new Response("You do not own your faction.", "", Result.FAILURE);
		}
		// Check to make sure the original password matches the one currently stored.
		if (!faction.isPassword(passwordOriginal)) {
			return new Response("The given password is invalid.", "", Result.FAILURE);
		}
		// Set the password, and save.
		faction.setPassword(passwordNew, true);
		// Return the success message.
		return new Response("Faction password changed..", "", Result.SUCCESS);
	}

	/**
	 * Validates & changes the tag for a <Faction>.
	 * 
	 * @param player
	 *            The <Player>. Will return as a failure if not the owner of a
	 *            <Faction>.
	 * @param tagNew
	 *            The <String> tag to set for the <Faction>.
	 * @return Returns a <Response> result. If all validations pass, Result.SUCCESS
	 *         is passed.
	 */
	public Response changeFactionTag(Player player, String tagNew) {
		// Grab the FactionMember for the Player.
		FactionMember factionMember = module.getFactionMember(player);
		// Make sure the Player is in a Faction.
		if (factionMember == null) {
			return new Response("You do not own a faction.", "", Result.FAILURE);
		}
		// Grab the Faction.
		Faction faction = factionMember.getFaction();
		// Check to make sure the Player is the owner of the faction.
		if (!faction.isOwner(factionMember)) {
			return new Response("You do not own your faction.", "", Result.FAILURE);
		}
		// Check to make sure the tag isn't already being used by the faction.
		if (faction.isTag(tagNew)) {
			return new Response("Your faction already uses this tag.", "", Result.FAILURE);
		}
		// Check to make sure no other faction is using the tag.
		if (module.tagExists(tagNew)) {
			return new Response("Tag is already being used by another faction.", "", Result.FAILURE);
		}
		// Validate that the tag fits the current requirements set by the module.
		Response responseValidateTag = module.validateFactionTag(tagNew);
		if (responseValidateTag.getResult() == Result.FAILURE) {
			return responseValidateTag;
		}
		// Set the tag, and save.
		module.setFactionTag(faction, tagNew);
		// Return the success message.
		return new Response("Faction tag changed to \"" + tagNew + "\".", "", Result.SUCCESS);
	}

	/**
	 * 
	 * @param player
	 *            The <Player>. Will return as a failure if not the owner of a
	 *            <Faction>.
	 * @param nameNew
	 *            The <String> name to set for the <Faction>.
	 * @return Returns a <Response> result. If all validations pass, Result.SUCCESS
	 *         is passed.
	 */
	public Response changeFactionName(Player player, String nameNew) {
		// Grab the FactionMember for the Player.
		FactionMember factionMember = module.getFactionMember(player);
		// Make sure the Player is in a Faction.
		if (factionMember == null) {
			return new Response("You do not own a faction.", "", Result.FAILURE);
		}
		// Grab the Faction.
		Faction faction = factionMember.getFaction();
		// Check to make sure the Player is the owner of the faction.
		if (!faction.isOwner(factionMember)) {
			return new Response("You do not own your faction.", "", Result.FAILURE);
		}
		nameNew = nameNew.trim();
		Response responseValidateName = module.validateFactionName(nameNew);
		if (responseValidateName.getResult() == Result.FAILURE) {
			return responseValidateName;
		}
		// Set the new name, and save the document.
		module.setFactionName(faction, nameNew);
		// Return the success message.
		return new Response("Faction name changed.", "", Result.SUCCESS);
	}

	/**
	 * 
	 * @param player
	 *            The <Player>. Will return as a failure if not the owner of a
	 *            <Faction>.
	 * @param colorNew
	 *            The <String> color to set for the <Faction>.
	 * @return Returns a <Response> result. If all validations pass, Result.SUCCESS
	 *         is passed.
	 */
	public Response changeFactionColor(Player player, String colorNew) {
		// Grab the FactionMember for the Player.
		FactionMember factionMember = module.getFactionMember(player);
		// Make sure the Player is in a Faction.
		if (factionMember == null) {
			return new Response("You do not own a faction.", "", Result.FAILURE);
		}
		// Grab the Faction.
		Faction faction = factionMember.getFaction();
		// Check to make sure the Player is the owner of the faction.
		if (!faction.isOwner(factionMember)) {
			return new Response("You do not own your faction.", "", Result.FAILURE);
		}
		// Make sure the color is a valid color.
		if (!ChatTags.isValidColor(colorNew)) {
			return new Response("Invalid color: \"" + colorNew
					+ "\". Use /colors to see list of available colors. (Only light or normal colors are allowed)", "",
					Result.FAILURE);
		}
		// Make sure the color is not a dark color.
		if (ChatTags.isDarkColor(colorNew)) {
			return new Response(
					"Factions can only use light or normal colors. Dark colors (including black and brown), are not allowed.",
					"", Result.FAILURE);
		}
		// Grab the coded version of this color.
		String colorCode = ChatTags.getColor(colorNew);
		// Make sure the color is not already the one being used.
		if (faction.getFactionColor().equalsIgnoreCase(colorCode)) {
			return new Response("The faction already uses this color.", "", Result.FAILURE);
		}
		// Change the faction color, and save.
		faction.setFactionColor(colorNew, true);
		// Return the success message.
		return new Response("Changed faction color to \"" + colorNew + "\".", "", Result.SUCCESS);
	}

	/**
	 * 
	 * @param player
	 *            The <Player>. Will return as a failure if not the owner of a
	 *            <Faction>.
	 * @param ownerNewUsername
	 *            The <String> username of the owner to set for the <Faction>.
	 * @return Returns a <Response> result. If all validations pass, Result.SUCCESS
	 *         is passed.
	 */
	public Response changeFactionOwner(Player playerOwner, Player playerOwnerNew) {
		// Grab the FactionMember for the Player.
		FactionMember factionMemberOwner = module.getFactionMember(playerOwner);
		// Make sure the Player is in a Faction.
		if (factionMemberOwner == null) {
			return new Response("You do not own a faction.", "", Result.FAILURE);
		}
		// Grab the Faction.
		Faction faction = factionMemberOwner.getFaction();
		// Check to make sure the Player is the owner of the faction.
		if (!faction.isOwner(factionMemberOwner)) {
			return new Response("You do not own your faction.", "", Result.FAILURE);
		}
		FactionMember factionMemberOwnerNew = module.getFactionMember(playerOwnerNew);
		if (factionMemberOwnerNew == null || !factionMemberOwnerNew.getFaction().equals(faction)) {
			return new Response("Player is not in your faction.", "", Result.FAILURE);
		}
		// Set the new owner.
		faction.setOwner(factionMemberOwnerNew, true);
		// Return the success message.
		return new Response("Changed faction owner to \"" + playerOwnerNew.getUsername() + "\".", "", Result.SUCCESS);
	}

	public ModuleFactions getModule() {
		return this.module;
	}

	private void setModule(ModuleFactions module) {
		this.module = module;
	}

}