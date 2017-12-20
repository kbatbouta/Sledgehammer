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
require "Sledgehammer/Utils"

Chat = Window:derive("Chat");
Chat.__type = "Chat";
Chat.__extends = "Window";
Chat.instance = nil;

-- TODO: Finalizing theme.
-- TODO: (Future) Add Emoticons.

function Chat:new(module)

	local width  = 500;
	local height = 200;
	local core   = getCore();
	local screenWidth  = core:getScreenWidth();
	local screenHeight = core:getScreenHeight();

	-- Generic instantiation.
	local object = Window:new(24, screenHeight - height - 24, width, height);
	setmetatable(object, self);
	self.__index = self;

	object.amp                 = 0;
	object.alphaFactor         = 0.85;
	object.alphaFactorTarget   = 0.85;
	object.discord             = true;
	object.discordFade         = 1;
	object.iconDiscordEnabled  = getTexture("media/ui/Sledgehammer/Chat/discord16.png");
	object.iconDiscordDisabled = getTexture("media/ui/Sledgehammer/Chat/discord16_disabled.png");
	object.iconAlphaFactor0    = getTexture("media/ui/Sledgehammer/Chat/AlphaFactor_0.png");
	object.iconAlphaFactor50   = getTexture("media/ui/Sledgehammer/Chat/AlphaFactor_50.png");
	object.iconAlphaFactor75   = getTexture("media/ui/Sledgehammer/Chat/AlphaFactor_75.png");
	object.iconAlphaFactor100  = getTexture("media/ui/Sledgehammer/Chat/AlphaFactor_100.png");
	object.iconAlphaButton = object.iconAlphaFactor75;

	object.module = module;
	object.resizable = true;

	-- Set singleton instance.
	Chat.instance = object;

	return object;
end

function Chat:createChildren()
	
	-- Invoke super method.
	Window.createChildren(self);

	local tabPanel = TabPanel:new(4, 4, self:getWidth() - 8, self:getHeight() - 48);

	tabPanel:initialise();
	-- tabPanel:setAnchorTop(true);
	-- tabPanel:setAnchorBottom(false);

	-- Set method to focus on the text input.
	tabPanel.onTabFocus = function(panel)
		if Chat.instance ~= nil and Chat.instance.input ~= nil then
			Chat.instance.input:focus();
		end
	end

	-- Set Chat field.
	self.tabPanel = tabPanel;
	
	-- -- Add tabs.
	self.tabGlobal  = self:createChatTab("Global");
	self.tabLocal   = self:createChatTab("Local");
	self.tabPMS     = self:createChatTab("PMs");
	self.tabPMS.public = false;
	self.tabPMS.showHistory = false;
	self.tabGlobal.public = true;
	self.tabLocal.showHistory = false;

	self.tabIndexGlobal  = self.tabPanel:addTab("Global" , self.tabGlobal);
	self.tabIndexLocal   = self.tabPanel:addTab("Local"  , self.tabLocal);
	self.tabIndexPMS     = self.tabPanel:addTab("PMs"    , self.tabPMS);

	-- Initially set the 'Global' tab as the active tab.
	self.tabPanel:setActiveTab(self.tabIndexGlobal);

	self:addChild(tabPanel);

	local input = ISTextEntryBox:new("", 4, self:getHeight() - 21, self:getWidth() - 7, 18);
	input:initialise();
	input:instantiate();
	input:setAnchorTop(false);
	input:setAnchorBottom(true);
	input.onCommandEntered = Chat.onCommandEntered;
	input.backgroundColor.a = 0;
	input.onPressDown = chat_keypressed;
	input.onOtherKey = chat_keypressed;
	self:addChild(input);
	self.input = input;

	self.topBar = Component:new(0, 1, self:getWidth(), 21);
	self.topBar:initialise();
	self.topBar:instantiate();
	--self.topBar:setAlwaysOnTop(true);
	self.topBar.background = false;
	self.topBar.backgroundColor = {r=1, g=0, b=0, a=0};
	self.topBar.moveWithMouse = true;
	self:addChild(self.topBar);
	--self.topBar:bringToTop();
	self.topBar:setVisible(true);
end

function Chat:renderTopBar()

end

function Chat:update()
	
	if self:isMouseOver() then

		self.amp = self.amp + 4;
		if self.amp > 100 then self.amp = 100; end

		if self.alphaFactor < 1 then
			self.alphaFactor = self.alphaFactor + 0.01;
		end
	else

		self.amp = self.amp - 2;
		if self.amp < 0 then self.amp = 0; end

		if self.alphaFactor > 0.85 then
			self.alphaFactor = self.alphaFactor - 0.01;
		end
	end

	-- local v1 = 100 + (self.amp / 4);
	-- self.colorBackground.r = v1;
	-- self.colorBackground.g = v1;
	-- self.colorBackground.b = v1;
	-- self.colorBackground.a = 108 - (100 - (self.amp));

	-- TODO: set alphaFactor through TabPanel.
	self.tabGlobal.alphaFactor = self.alphaFactor;
	self.tabLocal.alphaFactor = self.alphaFactor;
	self.input:bringToTop();
end

function Chat:createChatTab(name)
	local pane = TextPane:new(name, 0, self.tabPanel:getHeaderHeight() + 1, self:getWidth(), self:getHeight() - self.tabPanel:getHeaderHeight());
    pane:initialise();
    pane.background = false;
    pane:setAnchorLeft(true);
    -- pane.render = ISChat.render_chatText;
    pane.maxLines = 500;
    pane.autosetheight = false;
    pane:addScrollBars();
    pane.vscroll.background = false;
    pane:ignoreHeightChange();
    pane._name = name;
    pane.panel = pane;
    pane.public = false;
    pane.speak = true;
   	return pane;
end

function Chat:removeChatTab(name)
	self.tabPanel:removeTab(name);
	self.tabPanel:setActiveTab(0);
end

function Chat:hasChannel(name)
	local length = tLength(self.tabPanel.panels) - 1;
	
	for index = 0, length, 1 do
		local nextPanel = self.tabPanel.panels[index];
		if nextPanel ~= nil and nextPanel.name == name then
			return true;
		end
	end

	return false;
end

function Chat:onCommandEntered()
	
	local text = Chat.instance.input:getText();
	Chat.instance.input:clear();

	if text ~= "" then
		if luautils.stringStarts(text, "/") or luautils.stringStarts(text, "!") then
			local command = string.sub(text, 2, string.len(text));
			Chat:handleCommand(command);
		else
			Chat:handleMessage(text);
		end
	end

	Chat.instance.input:unfocus();

	-- Legacy code
    -- sendWorldMessage(command);
end

function Chat:handleCommand(text)

	local command = {
		__name  = "Command",
		raw     = text,
		channel = Chat.instance.tabPanel.activePanel.name,
	};

	local args = {
		command = command
	};

	if SledgeHammer.isDebug() then 
		rPrint(args);
	end

	sendClientCommand("core", "sendCommand", args);
end

function Chat:getActiveChannel() 
	return self.tabPanel.activePanel;
end

function Chat:handleMessage(text)

	local panel = Chat.instance:getActiveChannel();
	local channelName = panel.name;
	local channel = Chat.instance.module.channels[string.lower(channelName)];
	
	if channel and channel.properties.speak == true then
		local message = {
			__name            = "ChatMessagePlayer",
			channel           = channelName,
			playerName        = SledgeHammer.instance.self.username,
			playerID          = SledgeHammer.instance.self.id,
			origin            = "client",
			message           = text,
			messageOriginal   = text,
			edited            = false,
			deleted           = false,
			editorID          = -1,
			messageID         = -1,
			deleterID         = -1,
			modifiedTimestamp = -1,
			time              = getHourMinute()
		};

		local args = { message = message };

		if SledgeHammer.isDebug() then 
			rPrint(args);
		end
		sendClientCommand("core.chat", "sendChatMessagePlayer", args);
	
	else
		panel:addLine("You are not allowed to talk in this channel.");
	end

end

-- Legacy stencil render method from ISChat.
function Chat:renderTab()
	-- Crop to the Panel's dimensions.
	self:setStencilRect(0, 0, self:getWidth(), self:getHeight());
    
    -- Perform the native render method.
    ISRichTextPanel.render(self);

    -- Finalize the stencil draw.
    self:clearStencilRect();
end

function Chat:_render()
	
	-- Update top bar position.
	self.topBar:setX(self.tabPanel.tabEndX - self:getX());
	self.topBar:setWidth(self:getWidth() - self.tabPanel.tabEndX - 20 + self:getX());

	-- Invoke super method.
	Window._render(self);

	if not self.discord then
		if self.discordFade > 0 then
			self.discordFade = self.discordFade - 0.04;
		end
	else
		if self.discordFade < 1 then
			self.discordFade = self.discordFade + 0.04;
		end
	end

	self:drawTexture(self.iconDiscordEnabled, self:getWidth() - 18, 4, self.discordFade, 1, 1, 1);
	self:drawTexture(self.iconDiscordDisabled, self:getWidth() - 18, 4, 1 - self.discordFade, 1, 1, 1);

	self:drawTexture(self.iconAlphaButton, self:getWidth() - 36, 4, 1, 1, 1, 1);
	self:_renderChildren();
	self.input.javaObject:render();
end

function Chat:renameChatChannel(nameNew, nameOld)
	local panel = self.tabPanel:getTab(nameOld);
	panel.name = nameNew;
	panel._name = nameNew;
end

function Chat:getChannel(name)
	return self.tabPanel:getTab(name);
end

-- Legacy protocol.
function Chat:addLine(user, line, addLocal, addTimestamp)
	if Chat.instance ~= nil then
		local tabGlobal = Chat.instance.tabPanel:getTab(Chat.instance.tabIndexGlobal);
		tabGlobal:addLine(user);
	end
end

function Chat:getDiscordButtonPosition()
	return {x1 = self:getWidth() - 18, x2 = self:getWidth() - 2, y1 = 4, y2 = 20};
end

function Chat:getAlphaFactorTargetButtonPosition()
	return {x1 = self:getWidth() - 36, x2 = self:getWidth() - 18, y1 = 4, y2 = 20};
end

function Chat:onFocus(x, y) 

	if SledgeHammer.isDebug() then 
		print("Chat:onFocus("..tostring(x)..", "..tostring(y)..");");
	end

	-- Discord button.
	local discordButton = self:getDiscordButtonPosition();

	if self:containsPoint(x, y, discordButton) then
		self.discord = not self.discord;
	end

	local alphaButton = self:getAlphaFactorTargetButtonPosition();

	if self:containsPoint(x, y, alphaButton) then
		if self.alphaFactorTarget == 0.85 then
			self.alphaFactorTarget = 1.00;
			self.iconAlphaButton = self.iconAlphaFactor100;
		elseif self.alphaFactorTarget == 1.00 then
			self.alphaFactorTarget = 0.25;
			self.iconAlphaButton = self.iconAlphaFactor0;
		elseif self.alphaFactorTarget == 0.25 then
			self.alphaFactorTarget = 0.5;
			self.iconAlphaButton = self.iconAlphaFactor50;
		elseif self.alphaFactorTarget == 0.5 then
			self.alphaFactorTarget = 0.85;
			self.iconAlphaButton = self.iconAlphaFactor75;
		end
	end

	self.tabPanel:click();
end

lastIndexPressed = -1;

function chat_keypressed(key)
	if SledgeHammer.isDebug() then 
		print("chat_keypressed("..tostring(key)..")");
	end
end

t_pressed = false;
y_pressed = false;
esc_pressed = false;

function chat_onTick()

	if Chat.instance          == nil then return; end
	if Chat.instance.tabPanel == nil then return; end

	if Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) then

		local index = -1;
		local secondKeypressed = false;

		if Keyboard.isKeyDown(Keyboard.KEY_1) then index = 1; secondKeypressed = true; end
		if Keyboard.isKeyDown(Keyboard.KEY_2) then index = 2; secondKeypressed = true; end
		if Keyboard.isKeyDown(Keyboard.KEY_3) then index = 3; secondKeypressed = true; end
		if Keyboard.isKeyDown(Keyboard.KEY_4) then index = 4; secondKeypressed = true; end
		if Keyboard.isKeyDown(Keyboard.KEY_5) then index = 5; secondKeypressed = true; end
		if Keyboard.isKeyDown(Keyboard.KEY_6) then index = 6; secondKeypressed = true; end
		if Keyboard.isKeyDown(Keyboard.KEY_7) then index = 7; secondKeypressed = true; end
		if Keyboard.isKeyDown(Keyboard.KEY_8) then index = 8; secondKeypressed = true; end
		if Keyboard.isKeyDown(Keyboard.KEY_9) then index = 9; secondKeypressed = true; end

		if index ~= -1 then
			local panel = Chat.instance.tabPanel:getTab(index - 1);
			if panel ~= nil then
				Chat.instance.tabPanel:setActiveTab(index - 1);
			end
		end
	end

	if Keyboard.isKeyDown(20) and t_pressed ~= true then -- 'T'
		if not Chat.instance.input.javaObject:isFocused() then
			Chat.instance:setVisible(true);
			Chat.instance.input:focus();
		end
		t_pressed = true;
	else
		t_pressed = false;
	end

	if Keyboard.isKeyDown(21) and y_pressed ~= true then -- 'Y'
		if not Chat.instance.input.javaObject:isFocused() then
			Chat.instance:setVisible(true);
			Chat.instance.tabPanel:setActiveTab(1);
			y_pressed = true;
		end
	else
		y_pressed = false;
	end
	
	if Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) and esc_pressed ~= true then
		if Chat.instance.input.javaObject:isFocused() then
			Chat.instance.input:clear();
			Chat.instance.input:unfocus();
		else
			Chat.instance:setVisible(false);
		end
		esc_pressed = true;
	else
		esc_pressed = false;
	end
end

Events.OnTickEvenPaused.Add(chat_onTick);

-- Legacy protocol.
Events.OnWorldMessage.Add(Chat.addLine);