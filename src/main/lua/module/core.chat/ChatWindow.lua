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

require "Sledgehammer/Gui/Window"
require "Sledgehammer/Gui/TextPane"
require "ISUI/ISRichTextPanel"
require "Util"

----------------------------------------------------------------
-- ChatWindow.lua
-- UI for the chat module for Sledgehammer.
-- 
-- @plugin Core
-- @module Chat
-- @author Jab
-- @license LGPL3
----------------------------------------------------------------
ChatWindow = Window:derive("ChatWindow");

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function ChatWindow:new(module_chat)
	-- Create the dimensions.
	local core          = getCore()             ;
	local screen_width  = core:getScreenWidth() ;
	local screen_height = core:getScreenHeight();
	local width         = 500                   ;
	local height        = 200                   ;
	local x_offset      = 24                    ;
	local y_offset      = 24                    ;
	-- Generic instantiation.
	local o = Window:new(x_offset, screen_height - height - y_offset, width, height);
	-- Generic assignment.
	setmetatable(o, self);
	self.__index = self;
	-- Load the Textures for the ChatWindow.
	o.icon_discord_enabled  = getTexture("media/ui/Sledgehammer/Chat/discord16.png"         );
	o.icon_discord_disabled = getTexture("media/ui/Sledgehammer/Chat/discord16_disabled.png");
	o.icon_alpha_factor_0   = getTexture("media/ui/Sledgehammer/Chat/AlphaFactor_0.png"     );
	o.icon_alpha_factor_50  = getTexture("media/ui/Sledgehammer/Chat/AlphaFactor_50.png"    );
	o.icon_alpha_factor_75  = getTexture("media/ui/Sledgehammer/Chat/AlphaFactor_75.png"    );
	o.icon_alpha_factor_100 = getTexture("media/ui/Sledgehammer/Chat/AlphaFactor_100.png"   );
	-- Set initial settings.
	o.icon_alpha_button   = o.icon_alpha_factor_75;
	o.module_chat         = module_chat           ;
	o.alpha_factor        = 0.85                  ;
	o.alpha_factor_target = 0.85                  ;
	o.amp                 = 0                     ;
	o.discord_fade        = 1                     ;
	o.discord             = true                  ;
	o.resizable           = true                  ;
	-- Set singleton instance.
	ChatWindow.instance = o;
	-- Return the result instance.
	Events.OnKeyPressed.Add(ChatWindow.onChatKeyPressed);
    -- Register the 'onTick()' method
    Events.OnTickEvenPaused.Add(ChatWindow.onTick);
	return o;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function ChatWindow:unload()
    -- Unregister the 'onTick()' method
    Events.OnTickEvenPaused.Remove(ChatWindow.onTick);
    Events.OnKeyPressed.Remove(ChatWindow.onChatKeyPressed);
    self.tab_panel.onTabFocus    = nil;
    self.input.onCommandEntered  = nil;
    self.input.onPressDown       = nil;
    self.input.onOtherKey        = nil;
    self.input                   = nil;
    self.tab_panel               = nil;
    self:setVisible(false);
    ChatWindow.instance = nil;
end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function ChatWindow:createChildren()
	-- Invoke super method (psuedo-static method).
	Window.createChildren(self);
	-- Create the LuaObjects.
	self.tab_panel = self:createTabPanel();
	self.input     = self:createTextEntryBox();
	self.top_bar   = self:createTopBar();
	-- Add them to the children list of the ChatWindow.
	self:addChild(self.tab_panel);
	self:addChild(self.input    );
	self:addChild(self.top_bar  );
	-- Set the ChatWindow visible.
	self.top_bar:setVisible(true);
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function ChatWindow:createTabPanel()
	-- Create the TabPanel instance inside the ChatWindow.
	local tab_panel = TabPanel:new(4, 4, self:getWidth() - 8, self:getHeight() - 48);
	tab_panel:initialise();
	-- Set method to focus on the text input.
	tab_panel.onTabFocus = function(panel)
		if ChatWindow.instance ~= nil and ChatWindow.instance.input ~= nil then
			ChatWindow.instance.input:focus();
		end
	end
	return tab_panel;
end

----------------------------------------------------------------
-- Creates the ISTextEntryBox used to enter text in the ChatWindow.
----------------------------------------------------------------
function ChatWindow:createTextEntryBox()
	local input = ISTextEntryBox:new("", 4, self:getHeight() - 21, self:getWidth() - 7, 18);
	input:initialise();
	input:instantiate();
	input:setAnchorTop(false);
	input:setAnchorBottom(true);
	-- Set default field values.
	input.onCommandEntered  = ChatWindow.handleInput ;
	input.onPressDown       = ChatWindow.onKeyPressed;
	input.onOtherKey        = ChatWindow.onKeyPressed;
	input.backgroundColor.a = 0                      ;
	-- Return the result value.
	return input;
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function ChatWindow:createTopBar()
	local top_bar = Component:new(0, 1, self:getWidth(), 21);
	top_bar:initialise();
	top_bar:instantiate();
	top_bar.backgroundColor = self:getDefaultBackgroundColor();
	top_bar.background      = false;
	top_bar.moveWithMouse   = true;
	return top_bar;
end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function ChatWindow:update()
	self:updateAlphaFactor();
	self.input:bringToTop();
end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function ChatWindow:_render()
	self:updateDiscordFade();
    -- Update TopBar position.
	self.top_bar:setX(self.tab_panel.tab_end_x - self:getX());
	self.top_bar:setWidth(self:getWidth() - self.tab_panel.tab_end_x - 20 + self:getX());
	-- Invoke super method.
	Window._render(self);
	-- Draw the Discord Icon.
	self:drawTexture(self.icon_discord_enabled, self:getWidth() - 18, 4, self.discord_fade, 1, 1, 1);
	self:drawTexture(self.icon_discord_disabled, self:getWidth() - 18, 4, 1 - self.discord_fade, 1, 1, 1);
	-- Draw the Alpha-Factor Icon.
	self:drawTexture(self.icon_alpha_button, self:getWidth() - 36, 4, 1, 1, 1, 1);
	-- Render all the children assigned to the ChatWindow.
	self:_renderChildren();
	-- Render the Input TextEntryBox last, so that it is on top.
	self.input.javaObject:render();
end

----------------------------------------------------------------
-- Updates the Alpha-factor so the ChatWindow becomes more visible
--   when the Player is interfacing the window.
----------------------------------------------------------------
function ChatWindow:updateAlphaFactor()
	if self:isMouseOver() then
		self.amp = self.amp + 4;
		if self.amp > 100 then 
			self.amp = 100; 
		end
		if self.alpha_factor < 1 then
			self.alpha_factor = self.alpha_factor + 0.01;
		else
			self.alpha_factor = 1;
		end
	else
		self.amp = self.amp - 2;
		if self.amp < 0 then
			self.amp = 0; 
		end
		if self.alpha_factor > 0.85 then
			self.alpha_factor = self.alpha_factor - 0.01;
		else 
			self.alpha_factor = 0.85;
		end
	end
	for key,panel in pairs(self.tab_panel.panels) do
		panel.alpha_factor = self.alpha_factor;
	end
end

----------------------------------------------------------------
-- Updates the Discord icon's fade factor for animation purposes.
----------------------------------------------------------------
function ChatWindow:updateDiscordFade()
	if not self.discord then
		if self.discord_fade > 0 then
			self.discord_fade = self.discord_fade - 0.04;
		end
	else
		if self.discord_fade < 1 then
			self.discord_fade = self.discord_fade + 0.04;
		end
	end
end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function ChatWindow:onFocus(x, y)
	print("onFocus("..tostring(x)..","..tostring(y)..")");
	-- Grab the Discord button's position.
	local discord_button_position = self:getDiscordButtonPosition();
	-- Grab the Alpha button's position.
	local alpha_button_position = self:getAlphaButtonPosition();
	-- Handle the Discord button if clicked.
	if self:containsPoint(x, y, discord_button_position) then
		self.discord = not self.discord;
	end
	-- Handle the Alpha button if clicked.
	if self:containsPoint(x, y, alpha_button_position) then
		if self.alpha_factor_target == 0.85 then
			self.alpha_factor_target = 1.00;
			self.icon_alpha_button = self.icon_alpha_factor_100;
		elseif self.alpha_factor_target == 1.00 then
			self.alpha_factor_target = 0.25;
			self.icon_alpha_button = self.icon_alpha_factor_0;
		elseif self.alpha_factor_target == 0.25 then
			self.alpha_factor_target = 0.5;
			self.icon_alpha_button = self.icon_alpha_factor_50;
		elseif self.alpha_factor_target == 0.5 then
			self.alpha_factor_target = 0.85;
			self.icon_alpha_button = self.icon_alpha_factor_75;
		end
	end
	self.tab_panel:click();
end

----------------------------------------------------------------
-- 
----------------------------------------------------------------
function ChatWindow:handleCommand(command_message)
	-- Create the Command LuaObject
	local command = {
		__name     = "Command",
		raw        = command_message,
		channel_id = self.tab_panel.active_panel.chat_channel.id
	};
	-- Create the SendCommand LuaObject that contains the Command.
	local args = {
		__name  = "SendCommand",
		command = command
	};
	-- Send the Command to the server.
	sendClientCommand("core", "sendCommand", args);
end

----------------------------------------------------------------
-- Handles ChatMessage creation. Also checks flags for speaking.
--
-- @string message The String message content sent by the player.
----------------------------------------------------------------
function ChatWindow:handleMessage(message)
	-- Make sure that the message given is proper.
	if message == nil then
		print("WARNING: Message given is nil.");
		return;
	end
	-- Make sure that the message is a String type.
	if type(message) ~= "string" then
		print("WARNING: Message given is not a String. It is of the type: "
			..type(message));
		return;
	end 
	-- Grab the active ChatPanel.
	local panel = self:getActiveChatPanel();
	-- Make sure an Active ChatPanel is set.
	if panel == nil then
		print("WARNING: No active ChatChannel set! (Message: \""..message.."\")");
		return;
	end
	-- Grab the Player LuaObject.
	local player = SledgeHammer.instance.self;
	-- Grab the ChatChannel controling the ChatPanel.
	local chat_channel = panel.chat_channel;
	-- Flag for whether or not to send the ChatMessage LuaObject 
	--   to the server.
	local send = true;
	-- Make sure that the Player can speak in the active 
	--   ChatChannel.
	if not chat_channel.flags.can_speak then
		-- Set the flag to false to handle only locally.
		send = false;
		-- Tell the Player he cannot speak in this ChatChannel.
		message = "You are not allowed to talk in this channel.";
	end
	-- This is the LuaObject that will be sent to the server to 
	--   properly initialize. 
	-- (Note: The timestamps cannot be made due
	--   to the language's limitation on Long datatypes. This 
	--   means that the timestamps cannot be created in Lua, 
	--   however they can be made on the server, and sent back
	--   to Lua, expressed as Double values)
	local lua_table = {
		__name             = "ChatMessage",
		id                 = nil,
		channel_id         = chat_channel.id,
		player_id          = player.id,
		editor_id          = nil,
		deleter_id         = nil,
		origin             = "client",
		player_name        = player.nickname,
		message            = message,
		message_original   = message,
		timestamp          = nil,
		timestamp_modified = nil,
		timestamp_printed  = getHourMinute(),
		message_type       = 1,
		edited             = false,
		deleted            = false,
		muted              = false
	};
	if send then
		-- Create a SendChatMessage LuaObject.
		local args = {
			__name  = "SendChatMessage",
			message = lua_table
		};
		-- Send the LuaObject to the server.
		sendClientCommand("core.chat", "sendChatMessage", args);
	else
		-- Apply the System values to the LuaObject. 
		lua_table.origin            = "system";
		lua_table.player_name       = nil     ;
		lua_table.timestamp_printed = nil     ;
		-- Create a local ChatMessage.
		local chat_message = ChatMessage();
		-- Initialize the local ChatMessage.
		chat_message:initialize(lua_table, chat_channel);
		-- Add it to the ChatChannel.
		chat_channel:addChatMessage(chat_message);
	end
end

----------------------------------------------------------------
-- Create a TextPane for the ChatWindow. 
--
-- @table chat_channel The ChatChannel used to create the TextPane.
----------------------------------------------------------------
function ChatWindow:createChatTab(chat_channel)
	-- Create the arguments for the TextPane.
	local name   = chat_channel.name;
	local x      = 0;
	local y      = self.tab_panel:getHeaderHeight() + 1;
	local width  = self:getWidth();
	local height = self:getHeight() - self.tab_panel:getHeaderHeight();
	-- Create the TextPane.
	local pane = TextPane:new(name, x, y, width, height);
    pane:initialise();
    pane:setAnchorLeft(true);
    -- Set the initial field values.
    pane.chat_channel       = chat_channel;
    pane._name              = name        ;
    pane.maxLines           = 500         ;
    pane.background         = false       ;
    pane.autosetheight      = false       ;
    pane:addScrollBars();
    pane.vscroll.background = false       ;
    pane.public             = false       ;
    pane.speak              = true        ;
    pane:ignoreHeightChange();
    -- Return the result TextPane.
   	return pane;
end

----------------------------------------------------------------
-- Removes a ChatTab.
-- @table chat_channel The ChatChannel representing the ChatTab.
----------------------------------------------------------------
function ChatWindow:removeChatTab(chat_channel)
	local name = chat_channel.name;
	self.tab_panel:removeTab(name);
	self.tab_panel:setActiveTab(0);
end

----------------------------------------------------------------
-- @table chat_channel The ChatChannel representing the ChatTab
-- @return Returns a ChatTab if one is registered with the ChatChannel given.
----------------------------------------------------------------
function ChatWindow:getChatTabByChatChannel(chat_channel)
	return self:getChatTabByName(chat_channel.name);
end

----------------------------------------------------------------
-- @string name The name of the ChatTab.
-- @return Returns a ChatTab if one is registered with the name given.
----------------------------------------------------------------
function ChatWindow:getChatTabByName(name)
	-- The tab to return.
	local returned = false;
	-- Grab the size of panels Array.
	local length = tLength(self.tab_panel.panels) - 1;
	-- Go through each panel.
	for index = 0, length, 1 do
		-- Grab the next panel in the Array.
		local next_panel = self.tab_panel.panels[index];
		-- If the panel is the one we are looking for.
		if next_panel ~= nil and next_panel.name == name then
			-- Set the flag to true and break to save computation.
			returned = next_panel;
			break;
		end
	end
	-- Return the result tab.
	return returned;
end

----------------------------------------------------------------
-- @string name The name of the ChatTab.
-- @return Returns true if a ChatTab is registered with the name given.
----------------------------------------------------------------
function ChatWindow:hasChatTab(name)
	return self:getChatTabByName(name) ~= nil;
end

----------------------------------------------------------------
-- @return Returns the Active ChatPanel.
----------------------------------------------------------------
function ChatWindow:getActiveChatPanel()
	return self.tab_panel.active_panel;
end

function ChatWindow:setActiveChatPanel(chat_channel)
	self.tab_panel:setActiveTab(chat_channel.name);
end

----------------------------------------------------------------
----------------------------------------------------------------
--      ######  ########    ###    ######## ####  ######      --
--     ##    ##    ##      ## ##      ##     ##  ##    ##     --
--     ##          ##     ##   ##     ##     ##  ##           --
--      ######     ##    ##     ##    ##     ##  ##           --
--           ##    ##    #########    ##     ##  ##           --
--     ##    ##    ##    ##     ##    ##     ##  ##    ##     --
--      ######     ##    ##     ##    ##    ####  ######      --
----------------------------------------------------------------
----------------------------------------------------------------

-- The Static instance of the ChatWindow.
ChatWindow.instance = nil;
-- The String 'Type' of Element.
ChatWindow.__type = "Chat";
-- The String 'Type' the Element extends.
ChatWindow.__extends = "Window";

----------------------------------------------------------------
-- Handles The ChatWindow's updates every update tick.
--
-- @static
----------------------------------------------------------------
function ChatWindow:onTick() end

----------------------------------------------------------------
-- Handles 
--
-- @static
----------------------------------------------------------------
function ChatWindow:handleInput()
	local text = ChatWindow.instance.input:getText();
	ChatWindow.instance.input:clear();
	if text ~= "" then
		if luautils.stringStarts(text, "/") or luautils.stringStarts(text, "!") then
			-- Grab the Command
			local command = string.sub(text, 2, string.len(text));
			-- Handle the Command.
			ChatWindow.instance:handleCommand(command);
		else
			ChatWindow.instance:handleMessage(text);
		end
	end
	ChatWindow.instance.input:unfocus();
end

----------------------------------------------------------------
-- Handles a Key being pressed in the TextEntryBox for ChatWindow.
--
-- @static
----------------------------------------------------------------
ChatWindow.onKeyPressed = function(key)
	print("input key pressed: "..tostring(key));
	local chat = ChatWindow.instance;
	if key == Keyboard.KEY_ESCAPE then
		chat.input:clear();
		chat.input:unfocus();
		chat:setVisible(false);
	end
end

ChatWindow.onChatKeyPressed = function(key)
	print("key pressed: "..tostring(key));
	local chat = ChatWindow.instance;
	-- This handles the ChatWindow setting itself visible.
	if key == 20 then -- 'T'
		if not chat.input.javaObject:isFocused() then
			chat:setVisible(true);
		end
        chat.input:focus();
    end
	-- This handles the ChatWindow setting itself visible, and switching
	--   the active ChatPanel to the Global ChatPanel. 
	if key == 21 and ChatWindow.y_pressed ~= true then -- 'Y'
		if not chat.input.javaObject:isFocused() then
			chat:setVisible(true);
			chat.tab_panel:setActiveTab("Global");
		end
	end
	-- If the CTRL key and a NUMBER key is pressed, this is a short-cut
	--   for setting which tab in the ChatWindow is active.
	if Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) and chat:getIsVisible() then
		-- The index to set for the ChatWindow.
		local index = -1;
		-- Flag for if a second key is pressed.
		local second_key_pressed = false;
		-- Check each key combination.
		if key == Keyboard.KEY_1 then index = 1; second_key_pressed = true; end
		if key == Keyboard.KEY_2 then index = 2; second_key_pressed = true; end
		if key == Keyboard.KEY_3 then index = 3; second_key_pressed = true; end
		if key == Keyboard.KEY_4 then index = 4; second_key_pressed = true; end
		if key == Keyboard.KEY_5 then index = 5; second_key_pressed = true; end
		if key == Keyboard.KEY_6 then index = 6; second_key_pressed = true; end
		if key == Keyboard.KEY_7 then index = 7; second_key_pressed = true; end
		if key == Keyboard.KEY_8 then index = 8; second_key_pressed = true; end
		if key == Keyboard.KEY_9 then index = 9; second_key_pressed = true; end
		-- If the second key pressed is a NUMBER key, then process the 
		--   active ChatTab function.
		if index ~= -1 then
			-- Grab the ChatPanel.
			local panel = ChatWindow.instance.tab_panel:getTab(index - 1);
			-- Make sure that a ChatPanel is at this index.
			if panel ~= nil then
				-- Set the ChatPanel as the active ChatPanel.
				ChatWindow.instance.tab_panel:setActiveTab(index - 1);
			end
		end
	end
	if key == Keyboard.KEY_ESCAPE then
		chat.input:clear();
		chat.input:unfocus();
		chat:setVisible(false);
	end
end

----------------------------------------------------------------
-- @static
-- @return Returns the position of the Discord button.
----------------------------------------------------------------
function ChatWindow:getDiscordButtonPosition()
	return {
		x1 = self:getWidth() - 18,
		x2 = self:getWidth() - 2,
		y1 = 4,
		y2 = 20
	};
end

----------------------------------------------------------------
-- @static
-- @return Returns the position of the Alpha button.
----------------------------------------------------------------
function ChatWindow:getAlphaButtonPosition()
	return {
		x1 = self:getWidth() - 36,
		x2 = self:getWidth() - 18,
		y1 = 4,
		y2 = 20
	};
end

----------------------------------------------------------------
-- @static
-- @return Returns the default Color of the background for ChatWindow.
----------------------------------------------------------------
function ChatWindow:getDefaultBackgroundColor()
	return {
		r = 1,
		g = 0,
		b = 0,
		a = 0
	};
end