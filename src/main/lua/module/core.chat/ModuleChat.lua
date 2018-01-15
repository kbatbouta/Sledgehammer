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
-- @Override
----------------------------------------------------------------
function Module_Chat:load()
    -- Inidialize Chat GUI
    self.gui = ChatWindow:new(self);
    self.gui:initialise();
    self.gui.resizable = false;
    self:disableLegacyChat();
    self.gui:addToUIManager();
    self.channels = {};
end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function Module_Chat:start()
end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function Module_Chat:handshake()
    self:requestChannels();
end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function Module_Chat:command(command, args)
    if command == "sendChatMessages" then
        local messages = args.messages;
        local length = tLength(messages) - 1;
        for index = 0, length, 1 do
            local message = messages[index];
            local channel_id = message.channel_id;
            local chat_channel = self:getChannelWithId(channel_id);
            if chat_channel == nil then
                chat_channel = self:getChannelWithName("Global");
            end
            local chat_message = ChatMessage();
            chat_message:initialize(message, chat_channel);
            self:assignChatMessage(chat_message, false);
        end
    elseif command == "sendChatChannel" then
        local chat_channel = ChatChannel();
        chat_channel:initialize(args.channel);
        self:addChatChannel(chat_channel);
        local chat_history = ChatHistory();
        chat_history:initialize(args.history, chat_channel);
        chat_channel.chat_history = chat_history;
    elseif command == "sendChatChannelRemove" then
        self:removeChatChannel(args.channel_id);
    elseif command == "sendChatChannelRename" then
        local channel_id = args.channel_id;
        local chat_channel = self:getChannelWithId(channel_id);
        if chat_channel ~= nil then
            chat_channel:rename(args.name_new);
        end
    end
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:requestChannels()
    local moduleChat = self;
    local success = function(table, request)
        if SledgeHammer.instance.DEBUG then
            rPrint(table, 1024);
        end
        local length = tLength(table.channels) - 1;
        for index = 0, length, 1 do
            local table_channel = table.channels[index];
            local chat_channel = ChatChannel();
            chat_channel:initialize(table_channel);
            moduleChat:addChatChannel(chat_channel);
        end
        self.gui.tab_panel:setActiveTab("Global");
        -- After initializing the data, set the chat to visible.
        self.gui:setVisible(true);
        self:requestChannelsHistory();
    end
    local failure = function(error, request)
        print("WARNING: Failed to start chat.");
        print(error);
    end
    self:sendRequest("requestChatChannels", nil, success, failure);
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:requestChannelsHistory()
    local moduleChat = self;
    local success = function(table, request)
        if SledgeHammer.instance.DEBUG then
            rPrint(table, 1024);
        end
        local histories = table.histories;
        local length = tLength(histories) - 1;
        for index = 0, length, 1 do
            local table_history = histories[index];
            local channel_id = table_history.channel_id;
            local chat_channel = moduleChat:getChannelWithId(channel_id);
            if chat_channel ~= nil then
                local chat_history = ChatHistory();
                chat_history:initialize(table_history, chat_channel);
                chat_channel.chat_history = chat_history;
            else
                print("WARNING: Received ChatHistory for unknown ChatChannel: " .. tostring(table_history.channel_id));
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
    self.channels[chat_channel.id] = chat_channel;
    local panel = ChatWindow.instance:createChatTab(chat_channel);
    ChatWindow.instance.tab_panel:addTab(chat_channel.name, panel);
    chat_channel.panel = panel;
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:removeChatChannel(channel_id)
    local chat_channel = self:getChannelWithId(channel_id);
    if chat_channel == nil then
        print("WARNING: ChatChannel not found to remove with ID: \"" .. tostring(channel_id) .. "\".");
        return;
    end
    self.channels[channel_id] = nil;
    self.gui:removeChatTab(chat_channel);
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function Module_Chat:assignChatMessage(chat_message)
    -- Pre-Render the ChatMessage to avoid wasted calculations.
    chat_message:render();
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
        local chat_channel_global = self:getChannelWithName("global");
        local channel_id = chat_message.channel_id;
        local channel = self:getChannelWithId(channel_id);
        if channel == nil then
            channel = chat_channel_global;
        end
        channel:addChatMessage(chat_message);
        if chat_channel_global.id ~= channel.id then
            chat_channel_global:addChatMessage(chat_message);
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
    for channel_id, chat_channel in pairs(self.channels) do
        if string.lower(chat_channel.name) == channel_name then
            return chat_channel;
        end
    end
    return nil;
end

function Module_Chat:disableLegacyChat()
    ISChat.createChat = function()
    end
    if ISChat.chat ~= nil then
        -- Removes legacy chat from UI update.
        ISChat.chat:removeFromUIManager();
        -- Hides legacy chat.
        ISChat.chat:setVisible(false);
        ISChat.instance.moreinfo:setVisible(false);
        ISChat.instance.chatText:setVisible(false);
        -- Removes legacy chat event hooks.
        Events.OnWorldMessage.Remove(ISChat.addLineInChat);
        Events.OnMouseDown.Remove(ISChat.unfocus);
        Events.OnKeyPressed.Remove(ISChat.onToggleChatBox);
        Events.OnKeyKeepPressed.Remove(ISChat.onKeyKeepPressed);
        ISChat.chat = nil;
    end
end

-- Registers the module to SledgeHammer
register(Module_Chat());