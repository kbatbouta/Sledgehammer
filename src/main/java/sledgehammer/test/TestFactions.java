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

package sledgehammer.test;

public class TestFactions {
	//
	// private static String user1 = "Jab";
	// private static String user2 = "Ja";
	// private static String user3 = "admin";
	// private static String user4 = "NotARealUser";
	//
	// private static String fact1 = "SuperBandits";
	// private static String fact2 = "NotBandits";
	//
	// private static String pass1 = "ImLegend";
	// private static String pass2 = "AmEpic";
	//
	// private static String abbv1 = "SB";
	// private static String abbv2 = "NB";
	//
	// public static void println(String... messages) {
	// for (String message : messages)
	// System.out.println("[TEST-CASE] FACTIONS: " + message);
	// }
	//
	// public static void main(String[] args) {
	// try {
	// ServerWorldDatabase.instance.create();
	// Core.GameSaveWorld = "servertest";
	// Factions.instance = new Factions();
	//
	// testCreateFaction();
	// testRemoveFaction();
	// testJoinFaction();
	// testLeaveFaction();
	// testTransferOwnership();
	// testChangeAbbreviation();
	// testChangePassword();
	// testChangeName();
	// testInviteToFaction();
	// testKickFromFaction();
	// testUserCache();
	//
	// } catch (SQLException var47) {
	// var47.printStackTrace();
	// } catch (ClassNotFoundException var48) {
	// var48.printStackTrace();
	// }
	// }
	//
	// static void testCreateFaction() {
	// String response;
	// println("###############################################################################");
	// println("Test: Creating Factions", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Creating Duplicate Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Creating Duplicate Faction Abbreviation...");
	// response = Factions.instance.createFaction(fact2, abbv1, pass2, user1);
	// println("Response: " + response, "");
	//
	// println("Creating Faction with invalid user...");
	// response = Factions.instance.createFaction(fact2, abbv2, pass2, user4);
	// println("Response: " + response, "");
	//
	// println("Creating Faction with Empty Password...");
	// response = Factions.instance.createFaction(fact2, abbv2, "", user2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	// }
	//
	// static void testRemoveFaction() {
	// String response;
	// println("###############################################################################");
	// println("Test: Removing factions", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Removing Faction Non-existent Faction...");
	// response = Factions.instance.disbandFaction(fact2, user1);
	// println("Response: " + response, "");
	//
	// println("Removing Faction Wrong password...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	//
	// println("Removing Faction Wrong user...");
	// response = Factions.instance.disbandFaction(fact1, user2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	// }
	//
	// static void testJoinFaction() {
	// String response;
	// println("###############################################################################");
	// println("Test: Joining Faction", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Joining Faction that doesn't exist...");
	// response = Factions.instance.joinFaction(fact2, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("Joining Faction with invalid password...");
	// response = Factions.instance.joinFaction(fact1, pass2, user2);
	// println("Response: " + response, "");
	//
	// println("Creating 2nd Faction...");
	// response = Factions.instance.createFaction(fact2, abbv2, pass2, user2);
	// println("Response: " + response, "");
	//
	// println("Joining User 3 to 2nd Faction...");
	// response = Factions.instance.joinFaction(fact2, pass2, user3);
	// println("Response: " + response, "");
	//
	// println("Joining Faction, being an owner in another faction...");
	// response = Factions.instance.joinFaction(fact1, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("Joining User 3 Faction with already being in another faction...");
	// response = Factions.instance.joinFaction(fact1, pass1, user3);
	// println("Response: " + response, "");
	//
	// println("Disbanding 2nd faction...");
	// response = Factions.instance.disbandFaction(fact2, user2);
	// println("Response: " + response, "");
	//
	// println("Joining Faction...");
	// response = Factions.instance.joinFaction(fact1, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	// }
	//
	// static void testLeaveFaction() {
	// String response;
	// println("###############################################################################");
	// println("Test: Leaving Faction", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("User 2 Joining Faction...");
	// response = Factions.instance.joinFaction(fact1, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("Leaving Faction as null username...");
	// response = Factions.instance.leaveFaction(null);
	// println("Response: " + response, "");
	//
	// println("Leaving Faction as empty username...");
	// response = Factions.instance.leaveFaction("");
	// println("Response: " + response, "");
	//
	// println("Leaving Faction as owner...");
	// response = Factions.instance.leaveFaction(user1);
	// println("Response: " + response, "");
	//
	// println("Leaving Faction while not being in a faction...");
	// response = Factions.instance.leaveFaction(user3);
	// println("Response: " + response, "");
	//
	// println("Leaving Faction...");
	// response = Factions.instance.leaveFaction(user2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	//
	// }
	//
	// static void testInviteToFaction() {
	// String response;
	// println("###############################################################################");
	// println("Test: Invitations", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Inviting User 2 with Non-Existent Faction...");
	// response = Factions.instance.inviteToFaction(fact2, user1, user2);
	// println("Response: " + response, "");
	//
	// println("Inviting User 2 with Invalid Owner...");
	// response = Factions.instance.inviteToFaction(fact1, user3, user2);
	// println("Response: " + response, "");
	//
	// println("Inviting User 2...");
	// response = Factions.instance.inviteToFaction(fact1, user1, user2);
	// println("Response: " + response, "");
	//
	// println("Accepting Invite with Non-Existent Faction...");
	// response = Factions.instance.acceptInvite(fact2, user2);
	// println("Response: " + response, "");
	//
	// println("Accepting Invite with Invalid User...");
	// response = Factions.instance.acceptInvite(fact1, user3);
	// println("Response: " + response, "");
	//
	// println("Accepting Invite...");
	// response = Factions.instance.acceptInvite(fact1, user2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	// }
	//
	// static void testKickFromFaction() {
	// String response;
	// println("###############################################################################");
	// println("Test: Kicking from Faction", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Inviting User 2...");
	// response = Factions.instance.inviteToFaction(fact1, user1, user2);
	// println("Response: " + response, "");
	//
	// println("Accepting Invite...");
	// response = Factions.instance.acceptInvite(fact1, user2);
	// println("Response: " + response, "");
	//
	// println("Kicking from Faction with Non-Existent Faction...");
	// response = Factions.instance.kickFromFaction(fact2, user1, user2);
	// println("Response: " + response, "");
	//
	// println("Kicking from Faction with Invalid Owner...");
	// response = Factions.instance.kickFromFaction(fact1, user3, user2);
	// println("Response: " + response, "");
	//
	// println("Kicking from Faction...");
	// response = Factions.instance.kickFromFaction(fact1, user1, user2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	//
	// }
	//
	// static void testTransferOwnership() {
	// String response;
	// println("###############################################################################");
	// println("Test: Transferring Ownership", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Joining 2nd user...");
	// response = Factions.instance.joinFaction(fact1, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("Transferring ownership with invalid owner...");
	// response = Factions.instance.transferOwnership(fact1, user3, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("Transferring ownership with invalid password...");
	// response = Factions.instance.transferOwnership(fact1, user1, pass2, user2);
	// println("Response: " + response, "");
	//
	// println("Transferring ownership with user not in faction...");
	// response = Factions.instance.transferOwnership(fact1, user1, pass1, user3);
	// println("Response: " + response, "");
	//
	// println("Transferring ownership with invalid faction name");
	// response = Factions.instance.transferOwnership(fact2, user1, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("Transferring ownership...");
	// response = Factions.instance.transferOwnership(fact1, user1, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user2);
	// println("Response: " + response, "");
	// }
	//
	// static void testChangePassword() {
	// String response;
	// println("###############################################################################");
	// println("Test: Change Password", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Password with Non-Existent Faction...");
	// response = Factions.instance.changePassword(fact2, user1, pass1, pass2);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Password with Non-Owner...");
	// response = Factions.instance.changePassword(fact1, user2, pass1, pass2);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Password with Invalid Password...");
	// response = Factions.instance.changePassword(fact1, user1, pass2, pass1);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Password with Same Password...");
	// response = Factions.instance.changePassword(fact1, user1, pass1, pass1);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Password...");
	// response = Factions.instance.changePassword(fact1, user1, pass1, pass2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	// }
	//
	// static void testChangeAbbreviation() {
	// String response;
	// println("###############################################################################");
	// println("Test: Change Abbreviation", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Abbreviation with null...");
	// response = Factions.instance.changeAbbreviation(fact1, user1, null);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Abbreviation with empty...");
	// response = Factions.instance.changeAbbreviation(fact1, user1, "");
	// println("Response: " + response, "");
	//
	// println("Changing Faction Abbreviation with Non-Existent Faction...");
	// response = Factions.instance.changeAbbreviation(fact2, user1, abbv2);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Abbreviation with Invalid User...");
	// response = Factions.instance.changeAbbreviation(fact1, user2, abbv2);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Abbreviation with Invalid Password...");
	// response = Factions.instance.changeAbbreviation(fact1, user1, abbv2);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Abbreviation...");
	// response = Factions.instance.changeAbbreviation(fact1, user1, abbv2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	// }
	//
	// static void testChangeName() {
	// String response;
	// println("###############################################################################");
	// println("Test: Change Name", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Joining 2nd user...");
	// response = Factions.instance.joinFaction(fact1, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Name with Non-Existent Faction Name...");
	// response = Factions.instance.changeName(fact2, user1, fact2);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Name with Non-Owner...");
	// response = Factions.instance.changeName(fact1, user3, fact2);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Name with Invalid Password...");
	// response = Factions.instance.changeName(fact1, user1, fact2);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Name with Matching Names");
	// response = Factions.instance.changeName(fact1, user1, fact1);
	// println("Response: " + response, "");
	//
	// println("Changing Faction Name...");
	// response = Factions.instance.changeName(fact1, user1, fact2);
	// println("Response: " + response, "");
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact2, user1);
	// println("Response: " + response, "");
	// }
	//
	// static void testUserCache() {
	// String response;
	// println("###############################################################################");
	// println("Test: User Cache", "", "Creating Faction...");
	// response = Factions.instance.createFaction(fact1, abbv1, pass1, user1);
	// println("Response: " + response, "");
	//
	// println("Joining 2nd user...");
	// response = Factions.instance.joinFaction(fact1, pass1, user2);
	// println("Response: " + response, "");
	//
	// println("User Cache: " + user1);
	// println("User tag: " + Factions.instance.getUserTag(user1));
	//
	// println("User Cache: " + user2);
	// println("User tag: " + Factions.instance.getUserTag(user2));
	//
	// println("Removing Faction...");
	// response = Factions.instance.disbandFaction(fact1, user1);
	// println("Response: " + response, "");
	//
	// println("User Cache: " + user1);
	// println("User tag: " + Factions.instance.getUserTag(user1));
	//
	// println("User Cache: " + user2);
	// println("User tag: " + Factions.instance.getUserTag(user2));
	// }
}
