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

require "Sledgehammer/Gui/Component"
require "Sledgehammer/Utils"

TabPanel = Component:derive("TabPanel");
TabPanel.headerHeight = 24;

function TabPanel:new(x, y, w, h) 

	-- Generic instantiation.
	local object = Component:new(x, y, w, h);
	setmetatable(object, self);
	self.__index = self;
	
	-- The active index to each array.
	object.activeIndex = 0;
	
	-- Panel content for each tab. Requires a 'name' field.
	object.panels = {};
	
	-- String lengths of each tab pre-calculated.
	object.tabLengths = {};

	-- For when a tab is added.
	object.tabsDirty = false;

	-- Beginning x position after the tab space. 
	object.tabEndX = 0;

	-- For when panels need to be resized.
	object.panelsDirty = true;

	-- Amount of tabs. Used as a placemarker for the next added tab.
	object.tabCount = 0;

	-- Dimensions used to calculate tab positions.
	object.tabDimensionHighlightFactors = {};

	-- The font-size of the tabs.
	object.tabFontSize = UIFont.Small;

	-- Height in pixels of the Tab's font.
	object.fontHeight = sHeight(object.tabFontSize);

	-- The active panel / Tab.
	object.activePanel = nil;

	-- Internal panel dimension data.
	object.innerX = 0;
	object.innerY = 0;
	object.innerWidth = 0;
	object.innerHeight = 0;
	
	return object;
end

function TabPanel:initialise()

	-- Invoke super method.
	Component.initialise(self);

end

function TabPanel:createChildren()

	-- Invoke super method.
	Component.createChildren(self);

	-- Setup colors.
	self.colorOutline      = Color(128, 128, 128, 255);
	self.colorText         = Color(164, 164, 164, 255);
	self.colorTextSelected = Color(240, 240, 240, 255);
end

function TabPanel:update()

	-- Update the active panel.
	if self.activePanel ~= nil then
		self.activePanel:update();
	end

	local mouse = self:getLocalMouse();

	if self:containsPoint(mouse.x, mouse.y) then
		-- print(tostring(mouse.x)..", "..tostring(mouse.y));
		mouse = self:getMouse();
		local length = 32;
		
		for index = 0, length, 1 do
			local nextPanel = self.panels[index];
			if nextDim ~= nil then
				local nextDim = nextPanel._dim;
				local factor = self.tabDimensionHighlightFactors[index];
				if self:containsPoint(mouse.x, mouse.y, nextDim) then
					if factor < 2 then
						self.tabDimensionHighlightFactors[index] = factor + 0.2;
					end
				else
					if factor > 1 then
						self.tabDimensionHighlightFactors[index] = factor - 0.1;
					end
				end
			end
		end
	else
		-- Drop Highlights for tabs while the mouse is outside the TabPanel.
		local length = 32;
		for index = 0, length, 1 do
			if self.tabDimensionHighlightFactors[index] ~= nil then
				if self.tabDimensionHighlightFactors[index] > 1 then
					self.tabDimensionHighlightFactors[index] = self.tabDimensionHighlightFactors[index] - 0.1;
				end
			end
		end
	end
end

function TabPanel:prerender()
	if self.tabsDirty then
		local index = 0;
		local length = self.tabCount;
		
		-- Go through each tab.
		for index=0, length, 1 do
			local panel = self.panels[index];
			if panel ~= nil then
				local name = self.panels[index]._name;
				
				-- Set the length of the tab.
				self.tabLengths[index] = sLength(name, self.tabFontSize) + 7;
			end
		end

		-- Reset the font height variable.
		self.fontHeight = sHeight(self.tabFontSize);

		-- Set the tab data clean.
		self.tabsDirty = false;
	end

	self:setInnerDimensions();

	self.activePanel:setX(self:getInnerX());
	self.activePanel:setY(self:getInnerY());
	self.activePanel:setWidth(self:getInnerWidth());
	self.activePanel:setHeight(self:getInnerHeight());

end

function TabPanel:render() end

function TabPanel:_render()
	
	local parent = self:getParent();
	local sx = parent:getX() + 4;
	local y = parent:getY() + self:getY();
	local h = self.fontHeight;
	local panelsLength = 32;

	local hasPanels = panelsLength > 0 or false;

	if hasPanels then
		for index = 0, panelsLength, 1 do
			
			local panel = self.panels[index];
			
			if panel ~= nil then

				local tabLength = self.tabLengths[index];

				local dim = nil;

				-- Draw the tab.
				if panel.active == true then
					dim = self:drawActiveTab(panel._name, sx, y, tabLength, self.fontHeight, self.colorTextSelected, self.colorOutline, self.font, 2, true);
				else
					dim = self:drawTab(panel._name, sx, y, tabLength, self.fontHeight, self.colorText, self.colorOutline, self.font, self.tabDimensionHighlightFactors[index], true);
				end

				-- Update panel's tab dimension.
				panel._dim = dim;

				-- Add the tab's length to the next measure.
				sx = sx + tabLength + 2;
			end
		end

		-- Store the x position of the end of the tab space.
		self.tabEndX = sx + 1;

		-- Draw the line in the remaining space to the right of the tabs.
		self:drawLineH(sx, y + h + 3, self:getWidth() - sx + parent:getX() + self:getX(), 1, self.colorOutline);
	else

		-- No tabs means the entire bar at the top is free to populate.
		self.tabEndX = 0;

		-- Draw the line in the remaining space to the right of the tabs.
		self:drawLineH(parent:getX() + self:getX(), y + h + 3, self:getWidth(), 1, self.colorOutline);
	end
	-- Grab the internal dimensions.
	local ix = self:getInnerX();
	local iy = self:getInnerY();
	local iw = self:getInnerWidth();
	local ih = self:getInnerHeight();

	-- Draw the containing border for the internal panel content.
	self:drawRectPartial(parent:getX() + ix + 4, parent:getY() + iy + 4, iw, ih, false, true, true, true, self.colorOutline);

	self:_renderChildren();
end

function TabPanel:click()
	local mouse = self:getMouse();
	
	if SledgeHammer.isDebug() then 
		print("Mouse: "..tostring(mouse.x)..", "..tostring(mouse.y));
	end
	
	local length = 32;
		
	for index = 0, length, 1 do
		local nextPanel = self.panels[index];

		if nextPanel ~= nil then
			local nextDim = nextPanel._dim;
			local factor = self.tabDimensionHighlightFactors[index];

			if SledgeHammer.isDebug() then 
				print("NextDim: "..tostring(nextDim.x1)..", "..tostring(nextDim.y1));
			end

			if self:containsPoint(mouse.x, mouse.y, nextDim) then
				if SledgeHammer.isDebug() then 
					print("Setting active tab: " .. nextPanel._name);
				end
				self:setActiveTab(nextPanel._name);
				break;
			end
		end
	end
end

function TabPanel:setActiveTab(identifier)
	if SledgeHammer.isDebug() then 
		print("setActiveTab("..tostring(identifier)..");");
	end
	-- Validity check.
	if identifier == nil then
		print("TabPanel:setActiveTab() -> Identifier is null.");
		return;
	end

	if(type(identifier) == "number") then
		-- Handle identifier as index.
		
		-- Validity check.
		if identifier < 0 then
			print("TabPanel:setActiveTab() -> Index must be a valid, non-negative integer.");
			return;
		end

		-- If a panel is currently active, remove it.
		if self.activePanel ~= nil then
			self:removeChild(self.activePanel);
			self.activePanel:setVisible(false);
			self.activePanel.active = false;
		end

		-- Set the new active panel.
		self.activePanel = self.panels[identifier];
		self.activePanel:setVisible(true);
		self.activePanel.active = true;

		-- Add the new active panel as a child of the Tabs UIElement.
		self:addChild(self.activePanel);


	elseif(type(identifier) == "string") then
		-- Handle identifier as the name.

		-- Validity check.
		if identifier == "" then
			print("TabPanel:setActiveTab() -> Name given is empty.");
			return;
		end

		-- length of the panels array.
		local length = 32;

		-- Our found panel object.
		local foundPanel = nil;
		
		-- Go through each panel.
		for index=0, length, 1 do

			-- Grab the next panel.
			local panel = self.panels[index];

			-- Verify the panel is a valid LuaObject.
			if panel ~= nil then

				-- If the name matches, this is the panel.
				if panel._name == identifier then

					-- Set the variable.
					foundPanel = panel;

					-- Break for optimization.
					break;
				end
			end
		end

		-- Validity check.
		if foundPanel == nil then
			print("TabPanel:setActiveTab() -> No panel found for name: " .. identifier);
			return;
		end

		-- If a panel is currently active, remove it.
		if self.activePanel ~= nil then
			self:removeChild(self.activePanel);
			self.activePanel:setVisible(false);
			self.activePanel.active = false;
		end

		-- Set the new active panel.
		self.activePanel = foundPanel;
		self.activePanel:setVisible(true);
		self.activePanel.active = true;
		
		-- Add the new active panel as a child of the Tabs UIElement.
		self:addChild(self.activePanel);
	end
	self:onTabFocus();
end

function TabPanel:addTab(name, UIObject) 

	-- Validity check.
	if UIObject == nil then
		print("TabPanel:addTab() -> UIObject is nil for name: " .. name);
		return;
	end

	UIObject._dim = {x1 = -9999, x2 = -9999, y1 = -9999, y2 = -9999};
	
	-- Set an internal name field.
	UIObject._name = name;
	

	-- Grab the index before incrementing the length.
	local toReturn = nil;

	local index = 0;
	while toReturn == nil do
		
		if SledgeHammer.isDebug() then 
			print(tostring(self.panels[index]));
		end
		
		if self.panels[index] == nil then
			toReturn = index;
			break;
		end
		
		index = index + 1;
	end
	
	-- Insert the object into the panel array.
	self.panels[toReturn] = UIObject;
	self.tabDimensionHighlightFactors[toReturn] = 1;

	-- Increment tabs length.
	self.tabCount = self.tabCount + 1;

	-- Set dirty flags to recalculate.
	self.tabsDirty = true;
	self.panelsDirty = true;

	-- Start as not visible.
	UIObject:setVisible(false);

	-- Return the index position of the tab.
	return toReturn;
end

function TabPanel:getActiveTab()
	return self.activePanel;
end


function TabPanel:onTabFocus(tab) end

function TabPanel:removeTab(name)
	
	-- TO lowercase to match.
	name = string.lower(name);

	local length = 32;
	for index = 0, length, 1 do 
		local panel = self.panels[index];
		if panel ~= nil then
			if string.lower(panel._name) == name then
				
				-- Set panel to null.
				self.panels[index] = nil;
				self.tabCount = self.tabCount - 1;		
				-- Compress the table-array.
				-- self.panels = compress(self.panels);
				
				-- If the removed tab is the active tab, set the first tab as active.
				if self.activeIndex == index then
					self.activeIndex = 0;
					self.activePanel = self.panels[0];
				
				end

				break;
			end
		end
	end
end

function TabPanel:getTab(identifier)
	
	if SledgeHammer.isDebug() then 
		print("TabPanel:getTab("..tostring(identifier)..");");
	end

	if type(identifier) == "number" then
	-- Handle identifier as index.
		
		-- Validity check.
		if identifier < 0 then
			print("Index must be a valid, non-negative integer.");
			return;
		end

		return self.panels[identifier];

	elseif type(identifier) == "string" then
		-- Handle identifier as the name.

		-- Validity check.
		if identifier == "" then
			print("Name given is empty.");
			return;
		end

		identifier = string.lower(identifier);

		-- length of the panels array.
		local length = 32;
		-- print("length: -> " .. tostring(length));

		-- Go through each panel.
		for index=0, length, 1 do

			-- Grab the next panel.
			local panel = self.panels[index];

			-- print("self.panels["..tostring(index).."] = "..tostring(panel));

			-- Verify the panel is a valid LuaObject.
			if panel ~= nil then

				-- print("panel._name = "..panel._name);

				-- If the name matches, this is the panel.
				if string.lower(panel._name) == identifier then
					-- print("MATCH! Returning panel: "..tostring(panel));
					-- Set the variable.
					return panel;
				end
			end
		end
	end
end

function TabPanel:setInnerDimensions()
	-- Grab the parent.
	local p  = self:getParent();
	
	-- Parent coordinates.
	local px = p:getX();
	local py = p:getY();

	-- Inner dimensions.
	local x = self:getX() - 4;
	local y = self:getY() + 15;
	local w = self:getWidth();
	local h = self:getHeight();

	-- Set inner dimensions.
	self.innerX = x;
	self.innerY = y;
	self.innerWidth = w;
	self.innerHeight = h;
end

function TabPanel:getInnerX()
	return self.innerX;
end

function TabPanel:getInnerY()
	return self.innerY;
end

function TabPanel:getInnerWidth()
	return self.innerWidth;
end

function TabPanel:getInnerHeight()
	return self.innerHeight;
end

function TabPanel:getHeaderHeight()
	return 24;
end