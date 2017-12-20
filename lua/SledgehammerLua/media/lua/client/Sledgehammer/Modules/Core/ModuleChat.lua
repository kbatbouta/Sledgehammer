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

-- Main mod container.
Module_Chat = class(Module, function(o)

	-- Invoke super constructor.
	Module.init(o, "core.chat", "Chat");

	-- Debug flag.
	o.DEBUG = true;
end);

function Module_Chat:load()

	-- Initialize Chat GUI.
	self.gui = Chat:new(self);
	self.gui:initialise();
	self.gui.resizable = false;
	self.gui:addToUIManager();
	self.channels = {};
end

function Module_Chat:start()
	
end

function Module_Chat:handshake()
	self:requestChannels();
end

function Module_Chat:command(command, args)

	if command == "sendChatMessagePlayer" then		
		self:addChatMessagePlayer(args.message, false);
	elseif command == "sendChatMessage" then
		self:addChatMessage(args.message, false);
	elseif command == "sendChatChannel" then
		self:addChatChannel(args.channel);
	elseif command == "removeChatChannel" then
		self:removeChatChannel(args.channel);
	elseif command == "sendRenameChatChannel" then
		self:renameChatChannel(args.channel, args.nameNew, args.nameOld);
	end
end

function Module_Chat:renameChatChannel(channel, nameNew, nameOld)
	self.channels[string.lower(nameNew)] = channel;
	self.channels[string.lower(nameOld)] = nil;
	self.gui:renameChatChannel(nameNew, nameOld);
end

function Module_Chat:addChatMessage(message, history)
	
	local channelName = message.channel;
	local _channel    = self.channels[string.lower(channelName)];
	local origin = message.origin or "";
	local time = message.time or "";

	local channelGlobal = self.gui:getChannel("Global");
	
	if origin ~= nil and origin ~= "client" and origin ~= server then
		-- If discord is disabled, then return the message.
		if self.gui.discord == false and origin == "discord" then return; end
		if origin ~= "" then
			origin = "("..firstToUpper(origin)..") ";
		end
	end

	if time ~= nil and time ~= "" then
		time = "[" .. time .. "] : ";
	end

	local compiled = time .. origin .. message.message;
	
	if channelName == "*" then
		local channels = self.gui.tabPanel.panels;
		local length = tLength(channels) - 1;

		for index=0, length, 1 do
			local channel = channels[index];
			channel:addLine(compiled);
		end
	else
		local channel = self.gui:getChannel(channelName);

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

function Module_Chat:addChatMessagePlayer(message, history)
	local player      = message.player;
	local channelName = message.channel;
	local _channel    = self.channels[string.lower(channelName)];
	if SledgeHammer.isDebug() then 
		print("ChannelName: "..tostring(channelName));
	end
	
	if channelName == "*" then
		local channels = self.gui.tabPanel.panels;
		local length = tLength(channels) - 1;

		for index=0, length, 1 do
			local channel = channels[index];
			
			local compiled = "[" .. message.time .. "] :  " .. self:colorToTag(player.color)
			.. player.nickname .. " <RGB:1,1,1> : " .. message.message;
			
			channel:addLine(compiled);			
		end
	else
		local channel     = self.gui:getChannel(channelName);
		local channelGlobal  = self.gui:getChannel("Global");

		local compiled = "[" .. message.time .. "] :  " .. self:colorToTag(player.color)
			.. player.nickname .. " <RGB:1,1,1> : " .. message.message;
		
		if history then
			compiled = "[" .. message.time .. "] : " .. player.nickname .. " : " .. message.message;
			compiled = self:colorToTag({r = 0.8, g = 0.8, b = 0.8}) .. " " .. compiled .. " <RGB:1,1,1>"; 
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

function Module_Chat:colorToTag(color)
	if color == nil then return ""; end
	return '<RGB:'..color.r..','..color.g..','..color.b..'>';
end

function Module_Chat:addChatChannel(channel)

	self.channels[string.lower(channel.channelName)] = channel;

	local panel = nil;

	-- If the channel is not yet registered.
	if not self.gui:hasChannel(channel.channelName) then

		if SledgeHammer.instance.DEBUG then	
			print("Adding Channel: "..tostring(channel.channelName));
		end

		-- Create the TextPane instance.
		panel = self.gui:createChatTab(channel.channelName);

		-- Add to the TabPanel inside the chat GUI.
		self.gui.tabPanel:addTab(channel.channelName, panel);

	else
		if SledgeHammer.instance.DEBUG then
			print("Channel already exists: " ..tostring(channel.channelName));
		end

		panel = self.gui:getChannel(channel.channelName);
	end

	-- Set the public flag.
	panel.public = channel.properties.public;

	-- Set the speak flag.
	panel.speak = channel.properties.speak;

	-- Set context string.
	panel.context = channel.properties.context;

	-- Set description string.
	panel.description = channel.properties.description;

	-- Set showHistory flag.
	panel.showHistory = channel.properties.showHistory;

	if channel.properties.history then
		self:loadChannelHistory(channel);
	end
end

function Module_Chat:removeChatChannel(channel)
	self.gui:removeChatTab(channel.channelName);
end

function Module_Chat:requestChannels()
	local success = function(table, request)
		
		self.channels = table.channels;
		local channels = self.channels;

		local length = table.length - 1;

		for index = 0, length, 1 do

			self:addChatChannel(channels[index]);
		end
		
		if SledgeHammer.instance.DEBUG then
			print("Chat initialization complete. Setting chat visible.");
		end
		-- After initializing the data, set the chat to visible.
		self.gui:setVisible(true);
	end
	
	local failure = function(error, request)
		print("WARNING: Failed to start chat.");
	end

	self:sendRequest("getChatChannels", nil, success, failure);
end

function Module_Chat:loadChannelHistory(channel)
	local history = channel.history;
	if history ~= nil then

		self:addChatMessage({message = "Channel history:", channel = channel.channelName}, true);

		local length = tLength(history) - 1;
		for index = 0, length, 1 do
			local nextHistory = history[index];
			if nextHistory.origin == "client" and nextHistory.player ~= nil then
				self:addChatMessagePlayer(nextHistory, true);
			else
				self:addChatMessage(nextHistory, true);
			end
		end

		self:addChatMessage({message = "<CENTER> __________________________________________________________________________ <LINE> <LEFT>", channel = channel.channelName}, true);
	end
end

function Module_Chat:stop()
	self.gui:removeFromUIManager();
	self.gui:setVisible(false);
end

function Module_Chat:unload()
	self.gui = nil;
end

-- Registers the module to SledgeHammer
register(Module_Chat());