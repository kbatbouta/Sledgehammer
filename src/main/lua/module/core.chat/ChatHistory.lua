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
-- ChatHistory.lua
-- Class for storing and handling common operations for ChatChannel history.
--
-- @module Chat
-- @author Jab
-- @license LGPL3
----------------------------------------------------------------
ChatHistory = class(function(o)
	-- The ChatChannel using the ChatHistory.
	o.chat_channel = nil;
	-- Array for ChatMessages that should be treated as a Stack implementation.
	o.messages = {};
	-- Flag for Debugging.
	o.DEBUG = false;
end);

function ChatHistory:initialize(lua_table, chat_channel)
	-- Set the ChatChannel using the ChatHistory.
	self.chat_channel = chat_channel;
	-- Grab the LuaObject array of the ChatMessages.
	local table_messages = lua_table.messages;
	-- Calculate the Array length.
	local length = tLength(table_messages) - 1;
	-- Go through each LuaObject.
	for index = 0, length, 1 do
		-- Grab the LuaObject for the ChatMessage.
		local table_message = table_messages[index];
		-- Instantiate the ChatMessage.
		local chat_message = ChatMessage();
		-- Initialize the ChatMessage.
		chat_message:initialize(table_message, chat_channel);
		chat_message.history = true;
		-- Add the ChatMessage to the ChatHistory Array.
		self:addChatMessage(chat_message);
	end
end

----------------------------------------------------------------
-- Adds a ChatMessage.
--
-- @table chat_message The ChatMessage being addded.
----------------------------------------------------------------
function ChatHistory:addChatMessage(chat_message)
	-- Grab the length of the messages Array to place the ChatMessage.
	local length = tLength(self.messages);
	-- Add the ChatMessage to the Array.
	self.messages[length] = chat_message;
	-- If the messages Array contains more than the maximum number of lines
	--   allowed, the messages Array needs to be pruned.
	if length > ChatHistory.MAX_LINES then
		-- The new Array to replace the messages Array.
		local messagesNew = {};
		-- This is the starting index for the messages Array.
		local starting_value = tLength(self.messages);
		-- This is the ending index for the messages Array.
		local ending_value = starting_value - ChatHistory.MAX_LINES;
		-- The index to place into the new Array.
		local newIndex = ChatHistory.MAX_LINES;
		-- Go through each of the last MAX_LINES in the message Array.
		for index = starting_value, ending_value, -1 do
			-- Set the position in the new messages Array.
			messagesNew[newIndex] = messages[index];
			newIndex = newIndex - 1; 
		end
		-- Set the pruned Array as the new messages Array.
		self.messages = messagesNew;
		local panel = self.chat_channel.panel;
		panel:clear();
		local length_messages = tLength(self.messages) - 1;
		for index_messages = 0, length_messages, 1 do
			local chat_message_next = self.messages[index_messages];
			local rendered = chat_message_next:render(self.chat_channel);
			panel:addLine(rendered);
		end
	else
		local panel = self.chat_channel.panel;
		local rendered = chat_message:render(self.chat_channel);
		panel:addLine(rendered);
	end
end

function ChatHistory:clear()
	self.messages = {};
	self.chat_channel.panel:clear();
end

-- The Maximum amount of lines that can be stored at one time in the ChatHistory.
ChatHistory.MAX_LINES = 1024;