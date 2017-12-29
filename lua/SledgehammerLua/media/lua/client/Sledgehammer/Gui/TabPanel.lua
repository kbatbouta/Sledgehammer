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
require "Util"

----------------------------------------------------------------
-- TabPanel.lua
-- UI for generic Tabbed Panels.
-- 
-- @author Jab
-- @license LGPL3
----------------------------------------------------------------
TabPanel = Component:derive("TabPanel");

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:new(x, y, w, h) 
	-- Generic instantiation.
	local o = Component:new(x, y, w, h);
	setmetatable(o, self);
	self.__index = self;
	-- The active index to each array.
	o.active_index = 0;
	-- Panel content for each tab. Requires a 'name' field.
	o.panels = {};
	-- String lengths of each tab pre-calculated.
	o.tab_lengths = {};
	-- For when a tab is added.
	o.tabs_dirty = false;
	-- Beginning x position after the tab space. 
	o.tab_end_x = 0;
	-- For when panels need to be resized.
	o.panels_dirty = true;
	-- Amount of tabs. Used as a placemarker for the next added tab.
	o.tab_count = 0;
	-- Dimensions used to calculate tab positions.
	o.tab_dimension_highlight_factors = {};
	-- The font-size of the tabs.
	o.tab_font_size = UIFont.Small;
	-- Height in pixels of the Tab's font.
	o.font_height = sHeight(o.tab_font_size);
	-- The active panel / Tab.
	o.active_panel = nil;
	-- Internal panel dimension data.
	o.inner_x      = 0;
	o.inner_y      = 0;
	o.inner_width  = 0;
	o.inner_height = 0;
	return o;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:initialise()
	-- Invoke super method.
	Component.initialise(self);

end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:createChildren()
	-- Invoke super method.
	Component.createChildren(self);
	-- Setup colors.
	self.colorOutline      = Color(128, 128, 128, 255);
	self.colorText         = Color(164, 164, 164, 255);
	self.colorTextSelected = Color(240, 240, 240, 255);
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:update()
	-- Update the active panel.
	if self.active_panel ~= nil then
		self.active_panel:update();
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
				local factor = self.tab_dimension_highlight_factors[index];
				if self:containsPoint(mouse.x, mouse.y, nextDim) then
					if factor < 2 then
						self.tab_dimension_highlight_factors[index] = factor + 0.2;
					end
				else
					if factor > 1 then
						self.tab_dimension_highlight_factors[index] = factor - 0.1;
					end
				end
			end
		end
	else
		-- Drop Highlights for tabs while the mouse is outside the TabPanel.
		local length = 32;
		for index = 0, length, 1 do
			if self.tab_dimension_highlight_factors[index] ~= nil then
				if self.tab_dimension_highlight_factors[index] > 1 then
					self.tab_dimension_highlight_factors[index] = self.tab_dimension_highlight_factors[index] - 0.1;
				end
			end
		end
	end
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:prerender()
	if self.tabs_dirty then
		local index  = 0;
		local length = self.tab_count;
		-- Go through each tab.
		for index = 0, length, 1 do
			local panel = self.panels[index];
			if panel ~= nil then
				local name = self.panels[index]._name;
				-- Set the length of the tab.
				self.tab_lengths[index] = sLength(name, self.tab_font_size) + 7;
			end
		end
		-- Reset the font height variable.
		self.font_height = sHeight(self.tab_font_size);
		-- Set the tab data clean.
		self.tabs_dirty = false;
	end
	self:setInnerDimensions();
	if self.active_panel ~= nil then
		self.active_panel:setX(self:getInnerX());
		self.active_panel:setY(self:getInnerY());
		self.active_panel:setWidth(self:getInnerWidth());
		self.active_panel:setHeight(self:getInnerHeight());
	end
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:_render()
	local parent        = self:getParent()           ;
	local sx            = parent:getX() + 4          ;
	local y             = parent:getY() + self:getY();
	local h             = self.font_height           ;
	local panels_length = 32                         ;
	local has_panels    = panels_length > 0 or false ;
	if has_panels then
		for index = 0, panels_length, 1 do
			local panel = self.panels[index];
			if panel ~= nil then
				local tab_length = self.tab_lengths[index];
				local dim = nil;
				-- Draw the tab.
				if panel.active == true then
					dim = self:drawActiveTab(panel._name, sx, y, tab_length, self.font_height, self.colorTextSelected, self.colorOutline, self.font, 2, true);
				else
					dim = self:drawTab(panel._name, sx, y, tab_length, self.font_height, self.colorText, self.colorOutline, self.font, self.tab_dimension_highlight_factors[index], true);
				end
				-- Update panel's tab dimension.
				panel._dim = dim;
				-- Add the tab's length to the next measure.
				sx = sx + tab_length + 2;
			end
		end
		-- Store the x position of the end of the tab space.
		self.tab_end_x = sx + 1;
		-- Draw the line in the remaining space to the right of the tabs.
		self:drawLineH(sx, y + h + 3, self:getWidth() - sx + parent:getX() + self:getX(), 1, self.colorOutline);
	else
		-- No tabs means the entire bar at the top is free to populate.
		self.tab_end_x = 0;
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

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:click()
	local mouse = self:getMouse();
	local length = 32;
	for index = 0, length, 1 do
		local next_panel = self.panels[index];
		if next_panel ~= nil then
			local nextDim = next_panel._dim;
			local factor = self.tab_dimension_highlight_factors[index];
			if self:containsPoint(mouse.x, mouse.y, nextDim) then
				self:setActiveTab(next_panel._name);
				break;
			end
		end
	end
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:setActiveTab(identifier)
	-- Validity check.
	if identifier == nil then
		print("TabPanel:setActiveTab() -> Identifier is null.");
		return;
	end
	if type(identifier) == "number" then
		-- Handle identifier as index.
		-- Validity check.
		if identifier < 0 then
			print("TabPanel:setActiveTab() -> Index must be a valid, non-negative integer.");
			return;
		end
		-- If a panel is currently active, remove it.
		if self.active_panel ~= nil then
			self:removeChild(self.active_panel);
			self.active_panel:setVisible(false);
			self.active_panel.active = false;
		end
		-- Set the new active panel.
		self.active_panel = self.panels[identifier];
		self.active_panel:setVisible(true);
		self.active_panel.active = true;
		-- Add the new active panel as a child of the Tabs UIElement.
		self:addChild(self.active_panel);
	elseif type(identifier) == "string" then
		-- Handle identifier as the name.
		-- Validity check.
		if identifier == "" then
			print("TabPanel:setActiveTab() -> Name given is empty.");
			return;
		end
		-- length of the panels array.
		local length = 32;
		-- Our found panel object.
		local found_panel = nil;
		-- Go through each panel.
		for index = 0, length, 1 do
			-- Grab the next panel.
			local panel = self.panels[index];
			-- Verify the panel is a valid LuaObject.
			if panel ~= nil then
				-- If the name matches, this is the panel.
				if panel._name == identifier then
					-- Set the variable.
					found_panel = panel;
					-- Break for optimization.
					break;
				end
			end
		end
		-- Validity check.
		if found_panel == nil then
			print("TabPanel:setActiveTab() -> No panel found for name: " .. identifier);
			return;
		end
		-- If a panel is currently active, remove it.
		if self.active_panel ~= nil then
			self:removeChild(self.active_panel);
			self.active_panel:setVisible(false);
			self.active_panel.active = false;
		end
		-- Set the new active panel.
		self.active_panel = found_panel;
		self.active_panel:setVisible(true);
		self.active_panel.active = true;
		-- Add the new active panel as a child of the Tabs UIElement.
		self:addChild(self.active_panel);
	end
	self:onTabFocus();
end

----------------------------------------------------------------
--
----------------------------------------------------------------
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
		if self.panels[index] == nil then
			toReturn = index;
			break;
		end
		index = index + 1;
	end
	-- Insert the object into the panel array.
	self.panels[toReturn] = UIObject;
	self.tab_dimension_highlight_factors[toReturn] = 1;
	-- Increment tabs length.
	self.tab_count = self.tab_count + 1;
	-- Set dirty flags to recalculate.
	self.tabs_dirty = true;
	self.panels_dirty = true;
	-- Start as not visible.
	UIObject:setVisible(false);
	-- Return the index position of the tab.
	return toReturn;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
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
				self.tab_count = self.tab_count - 1;		
				-- If the removed tab is the active tab, set the first tab as active.
				if self.active_index == index then
					self.active_index = 0;
					self.active_panel = self.panels[0];
				end
				break;
			end
		end
	end
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:getTab(identifier)
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
		-- Go through each panel.
		for index = 0, length, 1 do
			-- Grab the next panel.
			local panel = self.panels[index];
			-- Verify the panel is a valid LuaObject.
			if panel ~= nil then
				-- If the name matches, this is the panel.
				if string.lower(panel._name) == identifier then
					-- Set the variable.
					return panel;
				end
			end
		end
	end
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:setInnerDimensions()
	-- Grab the parent.
	local p  = self:getParent();
	-- Parent coordinates.
	local px = p:getX();
	local py = p:getY();
	-- Inner dimensions.
	local x = self:getX() -  4;
	local y = self:getY() + 15;
	local w = self:getWidth() ;
	local h = self:getHeight();
	-- Set inner dimensions.
	self.inner_x      = x;
	self.inner_y      = y;
	self.inner_width  = w;
	self.inner_height = h;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:getActiveTab()
	return self.active_panel;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:getInnerX()
	return self.inner_x;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:getInnerY()
	return self.inner_y;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:getInnerWidth()
	return self.inner_width;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:getInnerHeight()
	return self.inner_height;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:onTabFocus(tab) end

----------------------------------------------------------------
--
----------------------------------------------------------------
function TabPanel:render() end

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

-- The static definition for header heights for the TabPanel.
TabPanel.header_height = 24;

----------------------------------------------------------------
-- @static
-- @return Returns the HeaderHieght defined for the TabPanel.
----------------------------------------------------------------
function TabPanel:getHeaderHeight()
	return TabPanel.header_height;
end