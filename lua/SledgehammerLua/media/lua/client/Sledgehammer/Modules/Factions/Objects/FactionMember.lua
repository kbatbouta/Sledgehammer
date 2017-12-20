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

require "Sledgehammer"

----------------------------------------------------------------
-- FactionMember.lua
-- 
-- @module Factions
-- @author Jab
-- @license LGPL
--
-- @number id 		The internal ID of the Player.
-- @string name 	The name of the Player.
-- @number rank 	The rank flag of the Player. 
----------------------------------------------------------------
FactionMember = class(function(o, id, name, rank)
	
	-- Set the variables.
	o.id      = id;
	o.name    = name;
	o.rank    = rank;
	o.faction = nil;
end);

----------------------------------------------------------------
-- @return 	Returns the internal ID of the Player.
----------------------------------------------------------------
function FactionMember:getID()
	return self.id;
end

----------------------------------------------------------------
-- Sets the internal ID of the Player.
--
-- @private
-- @string ID 	The internal ID of the Player.
----------------------------------------------------------------
function FactionMember:setID(ID)
	self.ID = ID;
end

----------------------------------------------------------------
-- @return 	Returns the name of the Player.
----------------------------------------------------------------
function FactionMember:getName()
	return self.name;
end

----------------------------------------------------------------
-- @return Returns the Rank flag of the Player.
----------------------------------------------------------------
function FactionMember:getRank()
	return self.rank;
end

----------------------------------------------------------------
-- @return Returns the Faction the Player is a member of.
----------------------------------------------------------------
function FactionMember:getFaction()
	return self.faction;
end

----------------------------------------------------------------
-- Sets the Faction the Player is a member of.
--
-- @private
-- @Faction faction 	The Faction the Player is a member of.
----------------------------------------------------------------
function FactionMember:setFaction(faction)
	self.faction = faction;
end