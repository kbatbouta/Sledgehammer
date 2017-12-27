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
require "Sledgehammer/Objects/Color"

Window = Component:derive("Window");

function Window:onMouseUp(x, y)
	if self.resizing == true then
		self.setCapture(false);
	end
	self.resizing = false;
end

function Window:new(x, y, w, h)
	local o = ISUIElement:new(x, y, w, h);
	setmetatable(o, self);
	self.__index = self;
	o.alphaFactorTarget = 0.85;
	o.backgroundColor = {r=0, g=0, b=0, a=0.0};
	return o;
end

function Window:createChildren()
	self.colorBorder      = Color(133, 136, 141, 255);
	self.colorBorderInner = Color(133, 136, 141,  96);
	self.colorBackground1 = Color( 64,  64,  64,  64);
	self.colorBackground2 = Color( 36,  36,  36, 255);
	self.colorGradient    = Color( 10,  10,  10, 255);
	self.colorDebug       = Color(255,   0,   0,   0);
	self.colorBackground  = self.colorBackground1;
end

function Window:applyAlpha()
	if self.alphaFactorTarget == 0.85 then
		self.colorBorder.a      = 255;
		self.colorBorderInner.a =  96;
		self.colorBackground.a  =  64;
		self.colorGradient.a    = 255;
	elseif self.alphaFactorTarget == 1.0 then
		self.colorBorder.a      = 255;
		self.colorBorderInner.a = 255;
		self.colorGradient.a    = 255;
		self.colorBackground = self.colorBackground2;
	elseif self.alphaFactorTarget == 0.25 then
		self.colorBackground = self.colorBackground1;
		self.colorBorder.a      = 255;
		self.colorBorderInner.a =   0;
		self.colorBackground.a  =   0;
		self.colorGradient.a    =   0;
	elseif self.alphaFactorTarget == 0.5 then
		self.colorBorder.a      = 255;
		self.colorBorderInner.a =  48;
		self.colorBackground.a  =  12;
		self.colorGradient.a    = 255;
	end
end

function Window:_render()
	x = self:getX();
	y = self:getY();
	w = self:getWidth();
	h = self:getHeight();

	self:applyAlpha();

	self:alphaGradientH(x - 5, y, 5, h, self.colorGradient, 0, 125);
	self:alphaGradientH(x + w, y, 5, h, self.colorGradient, 125, 0);
	self:alphaGradientV(x, y - 5, w, 5, self.colorGradient, 0, 125);
	self:alphaGradientV(x, y + h, w, 5, self.colorGradient, 125, 0);
	self:fillRoundRectFancy(x + 1, y + 1, w - 1, h - 1, self.colorBackground);
	self:drawRoundRect(x, y, w, h, self.colorBorder);
end