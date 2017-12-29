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

require "ISUI/ISRichTextPanel"
require "Sledgehammer/Gui/Component"
require "Sledgehammer/Objects/Color"
require "Util"

----------------------------------------------------------------
-- TextPane.lua
-- UI extnesion for ISRichTextPanel Text Panels.
-- 
-- @author Jab
-- @license LGPL3
----------------------------------------------------------------
TextPane = ISRichTextPanel:derive("TextPane");

----------------------------------------------------------------
-- @override
----------------------------------------------------------------
function TextPane:new(name, x, y, w, h)
	local o = ISRichTextPanel:new(x, y, w, h);
	setmetatable(o, self);
	self.__index = self;
	o.name         = name ;
	o.text         = ""   ;
	o.alpha_factor = 0.85 ;
	o.shadow_text  = true ;
	o.autoscroll   = true ;
	o.active       = false;
	return o;
end

----------------------------------------------------------------
-- @override
----------------------------------------------------------------
function TextPane:update()
	self.autoscroll = self.vscroll.pos == 1;
end

----------------------------------------------------------------
-- Adds a line to the TextPane.
--
-- @string text The String text content to add as a line.
----------------------------------------------------------------
function TextPane:addLine(text)
	local vsv1 = self:isVScrollBarVisible();
	local pos = self.vscroll.pos;
	if text ~= nil then
		if text == "" then
			self.text = text;
		else
			self.text = self.text .. " <LINE> " .. text;
		end
	end
	self:paginate();
	local vsv2 = self:isVScrollBarVisible();
	if not vsv1 and vsv2 then
		self.autoscroll = true;
	end
	if self.autoscroll then
		self:scrollToBottom();
	end
end

----------------------------------------------------------------
-- Renders the TextPane formally.
----------------------------------------------------------------
function TextPane:render() 
	self.vscroll.javaObject:render();
end

----------------------------------------------------------------
-- Renders the text in the TextPane with a stencil for the 
--   TextPane dimensions inside of the scroll view.
----------------------------------------------------------------
function TextPane:_render() 
	self:setStencilRect(0, 0, self.width, self.height);
	-- Invoke super method.
    ISRichTextPanel.render(self);
    self:clearStencilRect();
    self.vscroll.javaObject:render();
end

----------------------------------------------------------------
-- Clears the text in the TextPane.
----------------------------------------------------------------
function TextPane:clear()
	self.text = "";
end