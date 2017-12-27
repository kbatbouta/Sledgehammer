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

TextPane = ISRichTextPanel:derive("TextPane");

function TextPane:new(name, x, y, w, h)
	local object = ISRichTextPanel:new(x, y, w, h);
	setmetatable(object, self);
	self.__index = self;
	object.name = name;
	object.active = false;
	object.text = "";
	object.shadowText = true;
	object.alphaFactor = 0.85;
	object.autoscroll = true;
	return object;
end

function TextPane:update()
	self.autoscroll = self.vscroll.pos == 1;
end

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

function TextPane:render() 
	self.vscroll.javaObject:render();
end

function TextPane:_render() 
	self:setStencilRect(0, 0, self.width, self.height);
	-- Invoke super method.
    ISRichTextPanel.render(self);
    self:clearStencilRect();
    self.vscroll.javaObject:render();
end