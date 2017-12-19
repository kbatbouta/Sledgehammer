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

require 'Sledgehammer'

----------------------------------------------------------------
-- Player.lua
-- 
-- @module Core
-- @author Jab
-- @license LGPL

-- @number id 			Internal ID of the Player.
-- @string name 		The name of the Player.
-- @string nickname 	The nickname of the Player.
----------------------------------------------------------------
Player = class(function(o, id, name, nickname)
	
	-- The server ID of the player.
	o.id = id;

	-- The server name of the player.
	o.name = name;

	-- The server nickname of the player.
	o.nickname = nickname;
end);

