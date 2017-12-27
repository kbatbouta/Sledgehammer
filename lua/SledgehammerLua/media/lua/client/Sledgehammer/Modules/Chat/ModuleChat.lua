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
require "Sledgehammer/Gui/Implemented/Chat"
require "Sledgehammer/Modules/Chat/ChatChannel"
require "Sledgehammer/Modules/Chat/ChatHistory"
require "Sledgehammer/Modules/Chat/ChatMessage"

----------------------------------------------------------------
-- ModuleChat.lua
-- Module for the "core.chat" operations for Sledgehammer.
-- 
-- @plugin Core
-- @module Chat
-- @author Jab
-- @license LGPL3
----------------------------------------------------------------
Module_Chat = class(Module, function(o)
	-- Invoke super constructor.
	Module.init(o, "core.chat", "Chat");
	-- Debug flag.
	o.DEBUG = true;
end);

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:load()
	-- Inidialize Chat GUI
	self.gui = Chat:new(self);
	self.gui:initialise();
	self.gui.resizable = false;
	self.gui:addToUIManager();
	self.channels = {};
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:start()

end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:handshake()
	self:requestChannels();
	self:requestChannelsHistory();
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:command(command, args)
	if command == "sendChatMessage" then
		local channel_id = args.message.channel_id;
		local chat_channel = self:getChannelWithId(channel_id);
		local chat_message = ChatMessage();
		chat_message:initialize(args.message, chat_channel);
		self:addChatMessage(chat_message, false);
	elseif command == "sendChatChannel" then
		local chat_channel = ChatChannel();
		chat_channel:initialize(args.channel);
		self:addChatChannel(chat_channel);
	elseif command == "sendChatChannelRemove" then
		self:removeChatChannel(args);
	-- elseif command == "sendChatChannelRename" then
	end
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:requestChannels()
	
	local moduleChat = self;
	local success = function(table, request)

		local length = tLength(table.channels) - 1;
		for index = 0, length, 1 do
			local table_channel = table.channels[index];
			rPrint(table_channel);
			local chat_channel = ChatChannel();
			chat_channel:initialize(table_channel);
			moduleChat:addChatChannel(chat_channel);
		end
		-- After initializing the data, set the chat to visible.
		self.gui:setVisible(true);
	end

	local failure = function(error, request)
		print("WARNING: Failed to start chat.");
		print(error);
	end

	self:sendRequest("requestChatChannels", nil, success, failure);
end

function Module_Chat:requestChannelsHistory()
	local moduleChat = self;
	local success = function(table, request)
		local histories = table.histories;
		local length = tLength(histories) - 1;
		for index = 0, length, 1 do
			local table_history = histories[index];
			local channel_id = table_history.channel_id;
			local chat_channel = self:getChannelWithId(channel_id);
			if chat_channel ~= nil then
				local chat_history = ChatHistory();
				chat_history:initialize(table_history);
				chat_channel.chat_history = chat_history;
				print("Set ChatHistory for ChatChannel: "..chat_channel.name);
			else
				print("WARNING: Received ChatHistory for unknown ChatChannel: "..table_history.channel_id);
			end
		end
	end
	local failure = function(error, request)
		print("WARNING: Failed to retrieve chat history.");
		print(error);
	end

	self:sendRequest("requestChatHistories", nil, success, failure);
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:addChatChannel(chat_channel)
	print("Adding ChatChannel: "..chat_channel.name);
	local length = tLength(self.channels);
	self.channels[length] = chat_channel;
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:addChatMessage(chat_message, history)
	local channel_id = chat_message.channel_id;
	local channel = self:getChannelWithId(channel_id);
	local origin = message.origin or "";
	local timestamp = message.timestamp or "";
	local channelGlobal = self.gui:getChannelWithName("Global");
	-- Format the ChatMessage to make sure it is ready to be added.
	self:formatChatMessage(chat_message);
	-- Pre-Render the ChatMessage to avoid wasted calculations.
	chat_message:render();
	self:assignChatMessage(chat_message);
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:formatChatMessage(chat_message)
	-- local origin = chat_message.origin;
	-- -- Format the origin for the ChatMessage.
	-- if origin ~= nil and origin ~= "client" and origin ~= server then
	-- 	-- If discord is disabled, then mute the ChatMessage.
	-- 	if self.gui.discord == false and origin == "discord" then 
	-- 		chat_message.muted = true;
	-- 	else
	-- 		-- If the origin is defined, then head the message with the
	-- 		-- origin name.
	-- 		if origin ~= "" then
	-- 			origin_text = "("..firstToUpper(origin)..") ";
	-- 		end
	-- 	end
	-- end
	-- -- Set the formatted origin text.
	-- chat_message.origin_text = origin;
	-- -- Next, the timestamp needs to be formatted and pre-compiled.
	-- local timestamp_header = "";
	-- local timestamp_printed = chat_message.timestamp_printed;
	-- -- If the time is not null and the time is not empty, Head the
	-- -- message with it.
	-- if timestamp_printed ~= nil and timestamp_printed ~= "" then
	-- 	timestamp_header = "["..timestamp_printed.."] : ";
	-- end
	-- -- Set the pre-compiled Timestamp header.
	-- chat_message.timestamp_header = timestamp_header;
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:assignChatMessage(chat_message)
	local channel_id = chat_message.channel_id;
	if channel_id == nil or channel_id == "*" then
		local length = tLength(channels) - 1;
		for index = 0, length, 1 do
			local channel = self.channels[index];
			if channel.can_speak then
				channel:addChatMessage(chat_message);
			end
		end
	else
		local channel_id = chat_message.channel_id;
		local channel = self:getChannelWithId(channel_id);
		if history then
			compiled = self:colorToTag({r = 0.8, g = 0.8, b = 0.8}) .. " " .. compiled; 
		end
		if channel ~= nil then
			channel:addLine(compiled .. " <RGB:1,1,1>");
			if not history and channel ~= channelGlobal and _channel ~= nil and _channel.properties.global == true then
				channelGlobal:addLine("("..tostring(channel._name)..") "..compiled .. " <RGB:1,1,1>");
			end
		else
			channelGlobal:addLine(compiled .. " <RGB:1,1,1>");
		end
	end
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:getChannelWithId(channel_id)
	-- The associative array for ChatChannels are stored with 
	--   the channel_id as the key, so we attemp to retrieve 
	--   it directly as the key to the array.
	return self.channels[channel_id];
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:getChannelWithName(channel_name)
	-- Format the argument.
	channel_name = string.lower(channel_name);
	-- The ChatChannel to return.
	local returned = nil;
	-- Grab the amount of channels.
	local length = tLength(channels) - 1;
	-- Go through each channel.
	for index = 0, length, 1 do
		-- Grab the next ChatChannel in the associative array.
		local channel = self.channels[index];
		-- If the names match in a non-case-sensitive test, then
		--   this is the ChatChannel we are looking for. Set the
		--   returned object and break out of the loop to save
		--   computation time.
		if string.lower(channel.name) == channel_name then
			returned = channel;
			break;
		end
	end
	-- Return the result.
	return returned;
end

-- Registers the module to SledgeHammer
register(Module_Chat());