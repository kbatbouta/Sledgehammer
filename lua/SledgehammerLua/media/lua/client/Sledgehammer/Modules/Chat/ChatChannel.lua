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

require "Class"
require "Sledgehammer/Modules/Chat/ChatHistory"
require "Sledgehammer/Modules/Chat/ChatMessage"

----------------------------------------------------------------
-- ChatChannel.lua
-- Class for storing and handling common operations for chat channels.
--
-- @module Chat
-- @author Jab
-- @license LGPL3
----------------------------------------------------------------
ChatChannel = class(function(o)
	-- The UUID of the ChatChannel as a String.
	o.id = nil;
	-- The String name of the ChatChannel.
	o.name = nil;
	-- The flags for the ChatChannel.
	o.flags = {
		-- Whether or not the ChatChannel is Global. This means that if global is set 
		--   to false, the nature of the ChatChannel will be like the 'Local' 
		--   ChatChannel, where only Players by the Player authoring the ChatMessage 
		--   will see it.
		global = true,
		-- To force the explicit tag on this ChatChannel regardless of origin.
		explicit = true,
		-- Whether or not the ChatChannel is available to all Players.
		public = true,
		-- Whether or not the ChatChannel is custom.
		custom = false,
		-- Whether or not Players can speak in the ChatChannel. (Read-Only)
		can_speak = true,
		-- Whether or not to Save ChatMessages to a ChatHistory LuaObject.
		history = false
	};
	-- Debug flag.
	o.DEBUG = true;
	-- The ChatHistory storing ChatMessages for the ChatChannel.
	o.chat_history = nil;
	-- The ChatPanel controlled by the ChatChannel.
	o.panel = nil;
end);

----------------------------------------------------------------
-- Initializes the ChatMessage with the provided LuaTable.
--
-- @table lua_table The LuaTable storing the ChatMessage data.
----------------------------------------------------------------
function ChatChannel:initialize(lua_table)
	-- Set the fields from the given LuaTable.
	self.id    = lua_table.id   ;
	self.name  = lua_table.name ;
	self.flags = lua_table.flags;
end

----------------------------------------------------------------
-- Initializes the ChatMessage with the provided LuaTable.
--
-- @table chat_message The ChatMessage to add to the ChatHistory.
----------------------------------------------------------------
function ChatChannel:addChatMessage(chat_message)
	self.chat_history:addChatMessage(chat_message);
	self.panel:update();
end

function ChatChannel:rename(name)
	self.name        = name;
	self.panel.name  = name;
	self.panel._name = name;
end