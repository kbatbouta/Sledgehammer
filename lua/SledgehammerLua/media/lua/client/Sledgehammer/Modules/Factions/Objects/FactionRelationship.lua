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
-- FactionRelationship.lua
-- Class designed to store Faction Relationship data.
--
-- @module Factions
-- @author Jab
-- @license LGPL
--
-- @number id1 		ID of the affecting Faction.
-- @number id2	 	ID of the affected Faction.
-- @number status 	Status flag of the relationship.
-- @string message 	Message provided for the relationship.
----------------------------------------------------------------
FactionRelationship = class(function(o, id1, id2, status, message)
	
	-- Set the variables.
	o.id1     = id1;
	o.id2     = id2;
	o.status  = status;
	o.message = message;
end);

----------------------------------------------------------------
-- @return Returns the ID of the affecting Faction.
----------------------------------------------------------------
function FactionRelationship:getIDTo()
	return self.id1;
end

----------------------------------------------------------------
-- @return Returns the ID of the affected Faction.
----------------------------------------------------------------
function FactionRelationship:getIDFrom()
	return self.id2;
end

----------------------------------------------------------------
-- @return Returns the status of the relationship.
----------------------------------------------------------------
function FactionRelationship:getStatus()
	return self.status;
end

----------------------------------------------------------------
-- Sets the status of the relationship.
--
-- @number status  	The status flag of the relationship.
----------------------------------------------------------------
function FactionRelationship:setStatus(status)
	self.status = status;
end

----------------------------------------------------------------
-- @return Returns the message provided for the relationship.
----------------------------------------------------------------
function FactionRelationship:getMessage()
	return self.message;
end

----------------------------------------------------------------
-- Sets the message provided for the relationship.
--
-- @string message 	The message provided for the relationship.
----------------------------------------------------------------
function FactionRelationship:setMessage(message)
	self.message = message;
end