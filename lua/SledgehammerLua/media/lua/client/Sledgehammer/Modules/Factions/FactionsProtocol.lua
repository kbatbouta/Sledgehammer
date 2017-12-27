-- TODO: Figure this out.

-- This file is part of Sledgehammer.
--
--    Sledgehammer is free software: you can redistribute it and/or modify
--    it under the terms of the GNU Lesser General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    (at your option) any later version.
--
--    Sledgehammer is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU Lesser General Public License for more details.
--
--    You should have received a copy of the GNU Lesser General Public License
--    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
--
--	Sledgehammer is free to use and modify, ONLY for non-official third-party servers 
--    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors. 

require "Sledgehammer/Modules/Factions/ModuleFactions"

function onServerCommand(module, in_command, table)
	
	if luautils.stringStarts(in_command, "faction:") then
		out_command = "faction:";

		if luautils.stringStarts(in_command, "faction:receive_info") then
			
			-- Debug message.
			if MOD_Factions.DEBUG == true then
				print("DEBUG: Lua packet being handled: \"faction:receive_info\"...\n");
			end

			-- Handle packet.
			onReceiveInfo(table);
		end
		if luautils.stringStarts(in_command, "faction:relationship_request") then
			
			-- Debug message.
			if MOD_Factions.DEBUG == true then
				print("DEBUG: Lua packet being handled: \"faction:relationship_request\"...\n");
			end

			-- Handle packet.
			onRelationshipRequest(table);
		end
		if luautils.stringStarts(in_command, "faction:kicked") then

			-- Debug message.
			if MOD_Factions.DEBUG == true then
				print("DEBUG: Lua packet being handled: \"faction:kicked\"...\n");
			end

			-- Handle packet.
			onKicked(table);
		end
		if luautils.stringStarts(in_command, "faction:invite_request") then

			-- Debug message.
			if MOD_Factions.DEBUG == true then
				print("DEBUG: Lua packet being handled: \"faction:kicked\"...\n");
			end

			-- Handle packet.
			onKicked(table);

		end


		-- sendClientCommand(MOD_Factions.MOD_ID, out_command, table);
	end
end

function onReceiveInfo(table)

	-- Safety check on valid information.
	if table == nil then
		print("WARNING: Table is null for Sledgehammer->FactionsProtocol->onReceiveInfo()\n");
		return;
	end

	-- If the current info contains Faction data.
	if table.factions ~= nil then

		-- Set the Factions array on the global mod container.
		MOD_Factions.factions      = table.factions     ;

		-- Initially set the user's Faction as null.
		MOD_Factions.self.faction = nil;

		-- If the ID is not -1 (Has a faction)
		if table.factionID ~= -1 then

			-- Loop through all Factions.
			for index = 0, tLength(table.factions) do

				-- Grab the next Faction in the array.
				nextFaction = table.factions[index];

				-- If the ID of this faction is the same, it is the Faction we need.
				if nextFaction:getID() == table.factionID then

					-- Set this as the user's Faction.
					MOD_Factions.self.faction = nextFaction;
					break;
				end
			end
		end
	end

	-- If relationships are sent in this update.
	if table.relationships ~= nil then

	 	-- push the current list to the global object.
		MOD_Factions.relationships = table.relationships;
	end
end

function onRelationshipRequest(table)

end

function onKicked(table)

end

function onInviteCancel(id)

end

function onInviteRejected(table)

end

function onColorChange(table)

end

function onColorChanged(id, color)

end


function send(command, table)
	sendClientCommand(MOD_Factions.MOD_ID, command, table);
end

Events.OnServerCommand.Add(onServerCommand);