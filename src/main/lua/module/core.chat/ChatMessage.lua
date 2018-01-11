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

----------------------------------------------------------------
-- ChatMessage.lua
-- Class for storing and handling common operations for chat messages.
--
-- @module Chat
-- @author Jab
-- @license LGPL3
----------------------------------------------------------------
ChatMessage = class(function(o)
	-- The UUID of the ChatMessage as a String.
	o.id = nil;
	-- The UUID of the ChatChannel as a String. 
	o.channel_id = nil;
	-- The UUID of the Player as a String.
	o.player_id = nil;
	-- The UUID of the editor as a String.
	o.editor_id = nil;
	-- The UUID of the deleter as a String.
	o.deleter_id = nil;
	-- The origin of the ChatMessage. This helps dictate how to handle the ChatMessage.
	o.origin = "client";
	-- The String formatted text of the origin.
	o.origin_text = "";
	-- The String name of the Player.
	o.player_name = nil;
	-- The String message content of the ChatMessage.
	o.message = "";
	-- The String original message content of the ChatMessage.
	o.message_original = "";
	-- The Long timestamp of the ChatMessage, packaged as a Double. 
	o.timestamp = nil;
	-- The Long modified timestamp of the ChatMessage, packaged as a Double.
	o.timestamp_modified = nil;
	-- The String timestamp of the ChatMessage. This is used in-front of the ChatMessage.
	o.timestamp_printed = nil;
	-- The compiled String timestamp of the ChatMessage.
	o.timestamp_header = "";
	-- The Integer type of the ChatMessage.
	o.message_type = 0;
	-- Flag for whether or not the ChatMessage is muted and not displayed.
	o.muted = false;
	-- Flag for whether or not the ChatMessage is edited.
	o.edited = false;
	-- Flag for whether or not the ChatMessage is deleted.
	o.deleted = false;
	-- Debug flag.
	o.DEBUG = true;
end);

----------------------------------------------------------------
-- Initializes the ChatMessage with the provided LuaTable.
--
-- @table lua_table The LuaTable storing the ChatMessage data.
----------------------------------------------------------------
function ChatMessage:initialize(lua_table, chat_channel)
	-- Set the ChatChannel that the ChatMessage is in.
	self.chat_channel = chat_channel or "*";
	-- Set the fields from the given LuaTable.
	self.id                 = lua_table.id                ;
	self.channel_id         = lua_table.channel_id        ;
	self.player_id          = lua_table.player_id         ;
	self.editor_id          = lua_table.editor_id         ;
	self.deleter_id         = lua_table.deleter_id        ;
	self.origin             = lua_table.origin            ;
	self.player_name        = lua_table.player_name       ;
	self.message            = lua_table.message           ;
	self.message_original   = lua_table.message_original  ;
	self.timestamp          = lua_table.timestamp         ;
	self.timestamp_modified = lua_table.timestamp_modified;
	self.timestamp_printed  = lua_table.timestamp_printed ;
	self.message_type       = lua_table.message_type      ;
	self.edited             = lua_table.edited            ;
	self.deleted            = lua_table.deleted           ;

	local origin = self.origin;
	-- Format the origin for the ChatMessage.
	if origin ~= nil and origin ~= "" and origin ~= "client" and origin ~= "server" then
		-- If the origin is defined, then head the message with the
		-- origin name.
		self.origin_text = "("..firstToUpper(origin)..") ";
	end
	-- Next, the timestamp needs to be formatted and pre-compiled.
	local timestamp_header = "";
	local timestamp_printed = self.timestamp_printed;
	-- If the time is not null and the time is not empty, Head the
	-- message with it.
	if timestamp_printed ~= nil and timestamp_printed ~= "" then
		-- Set the pre-compiled Timestamp header.
		self.timestamp_header = "["..timestamp_printed.."] : ";
	end
end

----------------------------------------------------------------
-- Resets the rendered Strings so that they are re-rendered when called.
----------------------------------------------------------------
function ChatMessage:unrender()
	self.rendered          = nil;
	self.rendered_explicit = nil;
end

----------------------------------------------------------------
-- Renders the ChatMessage into a String if not defined.
--
-- @table chat_channel (Optional) The ChatChannel requesting the
--   rendered String. 
-- @return Returns the rendered String.
----------------------------------------------------------------
function ChatMessage:render(chat_channel)
	-- Flag for whether or not the ChatChannel name should be 
	--   in-front of the rendered ChatMessage.
	local explicit = false;
	-- If the chat_channel argument is passed and the ChatChannel
	--   is not the one the ChatMessage is listed under, then the
	--   explicit flag is set, returning the ChatChannel origin
	--   in-front of the ChatMessage.
	if chat_channel ~= nil and (chat_channel.flags.explicit or chat_channel.id ~= self.chat_channel.id) then
		explicit = true;
	end
	-- Check if the ChatMessage is already rendered.
	if self.rendered == nil then
		local history_header = "";
		if self.history then 
			history_header = ChatMessage.HISTORY_HEADER.."  ";
		end
		local player_header = "";
		if self.player_name ~= nil then
			player_header = self.player_name.." : ";
		end
		self.rendered = history_header..self.timestamp_header..self.origin_text..player_header..self.message;
	end
	-- Check if the explicit ChatMessage is already rendered.
	if self.rendered_explicit == nil then
		self.rendered_explicit = "("..self.chat_channel.name..") "..self.rendered;
	end
	-- If we need the explicit form of the ChatMessage, return it.
	if explicit then
		return self.rendered_explicit;
	-- Else, return the simple form of the ChatMessage.
	else
		return self.rendered;
	end
end

function ChatMessage:resetRenders()
	self.rendered          = nil;
	self.rendered_explicit = nil;
end

----------------------------------------------------------------
-- Sets the ChatMessage's timestamp header.
--
-- @boolean history Flag to set whether or not the ChatMessage 
--   is a history message.
----------------------------------------------------------------
function ChatMessage:setHistory(history)
	if history ~= self.history then
		self.history = history;
		self:resetRenders();
	end
end

ChatMessage.HISTORY_COLOR  = {r = 0.78, g = 0.78, b = 0.78};
ChatMessage.HISTORY_HEADER = colorToTag(ChatMessage.HISTORY_COLOR);