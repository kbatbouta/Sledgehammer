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
require "Sledgehammer/Objects/Color"
require "Sledgehammer/Modules/Factions/Objects/FactionMember"
require "Sledgehammer/Modules/Factions/Objects/FactionRelationship"

----------------------------------------------------------------
-- Faction.Lua
-- 
-- @module Factions
-- @author Jab
-- @license LGPL
--
-- @string name 	Name of the Faction.
-- @string tag 		The Tag for the Faction in chat.
-- @Color color 	The color of the Faction.
----------------------------------------------------------------
Faction = class(function(o, name, tag, color)
	
	-- Set the variables.
	o.name          = name;
	o.tag           = tag;
	o.color         = color;
	o.members       = {};
	o.relationships = {};
end);

----------------------------------------------------------------
-- @int id 	The internal ID of the Player.
-- @return Returns the Member of the Faction associated with the id.
----------------------------------------------------------------
function Faction:getMember(id)
	member = nil;
	if id ~= -1 then
		local length = tLength(self.members) - 1;
		for index=0, length, 1 do
			nextMember = self.members[index];
			if nextMember.getID() == id then
				member = nextMember;
				break
			end
		end
	end
	return member;
end

----------------------------------------------------------------
-- @number id 	The ID of the other Faction.
-- @return 		Returns the Relationship associated with the Faction ID given.
----------------------------------------------------------------
function Faction:getRelationshipFrom(id)
	relationship = nil;
	if id ~= -1 then
		local length = tLength(self.relationships) - 1;
		for index=0, length, 1 do
			nextRelationship = self.relationships[index];
			if nextRelationship.getIDFrom() == id then
				relationship = nextRelationship;
				break
			end
		end
	end
	return relationship;
end

----------------------------------------------------------------
-- @number id 	The ID of the other Faction.
-- @return 		Returns the Relationship associated with the Faction ID given.
----------------------------------------------------------------
function Faction:getRelationshipTo(ID_to)
	relationship = nil;
	if id ~= -1 then
		local length = tLength(self.relationships) - 1;
		for index=0, length, 1 do
			nextRelationship = self.relationships[index];
			if nextRelationship.getIDTo() == ID_to then
				relationship = nextRelationship;
				break
			end
		end
	end
	return relationship;
end

----------------------------------------------------------------
-- @return 	Returns the ID of the Faction.
----------------------------------------------------------------
function Faction:getID()
	return self.ID;
end

----------------------------------------------------------------
-- @private
-- @number id 	The ID of the Faction.
----------------------------------------------------------------
function Faction:setID(id)
	self.ID = id;
end

----------------------------------------------------------------
-- @return 	Returns the name of the Faction.
----------------------------------------------------------------
function Faction:getName()
	return self.name;
end

----------------------------------------------------------------
-- @return 	Returns the Tag of the Faction for chat.
----------------------------------------------------------------
function Faction:getTag()
	return self.tag;
end

----------------------------------------------------------------
-- @return Returns the Color of the Faction.
----------------------------------------------------------------
function Faction:getColor()
	return self.color;
end

----------------------------------------------------------------
-- @return Returns the list of FactionMembers.
----------------------------------------------------------------
function Faction:getMembers()
	return self.members;
end

----------------------------------------------------------------
-- @protected
-- @table members 	List of Members to be set for the Faction.
----------------------------------------------------------------
function Faction:setMembers(members)
	self.members = members;
end

----------------------------------------------------------------
-- @return 	Returns the list of Relationships associated with this Faction.
----------------------------------------------------------------
function Faction:getRelationships()
	return self.relationships;
end

----------------------------------------------------------------
-- @private
-- @table relationships 	list of Relationships to be set for the Faction.
----------------------------------------------------------------
function Faction:setRelationships(relationships)
	self.relationships = relationships;
end