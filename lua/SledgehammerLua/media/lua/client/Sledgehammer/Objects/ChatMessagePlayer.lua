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

require 'Class'
require 'Sledgehammer/Objects/ChatMessage'

----------------------------------------------------------------
-- ChatMessagePlayer.lua
--
-- @module Core
-- @author Jab
-- @license LGPL
--
-- @Channel channel 	The Channel being broadcasted.
-- @string message 		The content of the message.
-- @number playerID 	The internal ID of the Player sending the message.
----------------------------------------------------------------
ChatMessagePlayer = class(ChatMessage, function(o, channel, message, playerID)
	
	-- Invoke super constructor.
	ChatMessage.init(o, channel, message);

	-- The ID of the player broadcasting the message.
	o.playerID = playerID;

	-- The internal reference to the Sledgehammer Player LuaObject.
	-- TODO: Implement.
	o._player = nil;

end);

----------------------------------------------------------------
-- @return Returns the Player's internal ID.
----------------------------------------------------------------
function ChatMessagePlayer:getPlayerID()
	return self.playerID;
end