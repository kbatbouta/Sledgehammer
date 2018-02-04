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

require "ISUI/ISUIElement"
require "ISUI/ISPanel"
require "Sledgehammer/Objects/Color"

Component = ISPanel:derive("Component");
Component.__type = "Component";
Component.__extends = "ISPanel";
renderer = getRenderer();

function Component:new(x, y, w, h)
  local o = ISPanel:new(x, y, w, h)
  setmetatable(o, self);
  self.__index = self;
  o.x = x;
  o.y = y;
  o.w = w;
  o.h = h;
  o.alpha_factor = 1;
  o.brightness = 1;
  o.moveWithMouse = false;
  o._children = {};
  o._children_length = 0;
  return o;
end

function Component:initialise()
  ISPanel.initialise(self);
end

function Component:render()
  if self.parent == nil then
    self:_render();
  end
end

function Component:_renderChildren()
  local length = tLength(self._children) - 1;
  for index = 0, length, 1 do
    local child = self._children[index];
    if child ~= nil and child:isVisible() then
      if child._render ~= nil then
        child:_render();
      else
        child.javaObject:render();
      end
    end
  end
end

function Component:getLocalMouse()
  local m = self:getMouse();
  local ax = self:getAbsoluteX();
  local ay = self:getAbsoluteY();
  return { x = m.x - ax, y = m.y - ay };
end

--function Component:getAbsoluteX()
--  local x = 0;
--  local p = self:getParent();
--  if p ~= nil then
--    x = p:getAbsoluteX() + ISPanel.getAbsoluteX(self);
--  else
--    x = ISPanel.getAbsoluteX(self);
--  end
--  return x;
--end
--
--function Component:getAbsoluteY()
--  local y = 0;
--  local p = self:getParent();
--  if p ~= nil then
--    y = p:getAbsoluteY() + ISPanel.getAbsoluteY(self);
--  else
--    y = ISPanel.getAbsoluteY(self);
--  end
--  return y;
--end

function Component:containsPoint(x, y, dimension)
  if dimension == nil then
    return ISUIElement.containsPoint(self, x, y);
  end
  if x >= dimension.x1 and x <= dimension.x2 and y >= dimension.y1 and y <= dimension.y2 then
    return true;
  else
    return false;
  end
end

-- Injected method for scrolling.
function ISUIElement:scrollToBottom()
  self:setYScroll(-(self:getScrollHeight() - (self:getScrollAreaHeight())));
end

function Component:getMouse()
  return { x = getMouseX(), y = getMouseY() };
end

function Component:drawLineH(x, y, length, thickness, color)
  local af = self.alpha_factor;
  if af == nil then
    af = 1;
  end
  local br = self.brightness;
  if br == nil then
    br = 1;
  end
  local r = (color.r / 255.0) * br;
  local g = (color.g / 255.0) * br;
  local b = (color.b / 255.0) * br;
  local a = (color.a / 255.0) * af;
  renderer:render(nil, x, y, length, thickness, r, g, b, a);
end

function Component:drawLineV(x, y, length, thickness, color)
  local af = self.alpha_factor;
  if af == nil then
    af = 1;
  end
  local br = self.brightness;
  if br == nil then
    br = 1;
  end
  local r = (color.r / 255.0) * br;
  local g = (color.g / 255.0) * br;
  local b = (color.b / 255.0) * br;
  local a = (color.a / 255.0) * af;
  renderer:render(nil, x, y, thickness, length, r, g, b, a);
end

function Component:fillRect(x, y, w, h, color, af)
  if not af then af = self.alpha_factor; end
  if not af then af = 1; end
  local br = self.brightness;
  if br == nil then br = 1; end
  local r = (color.r / 255.0) * br;
  local g = (color.g / 255.0) * br;
  local b = (color.b / 255.0) * br;
  local a = (color.a / 255.0) * af;
  renderer:render(nil, x, y, w, h, r, g, b, a);
end

function Component:alphaGradientH(x, y, w, h, color, alphaStart, alphaStop)
  local as1 = alphaStart / 255.0;
  local as2 = alphaStop / 255.0;
  local step = math.abs(as2 - as1) / w;
  if alphaStart < alphaStop then
    step = -step;
  end
  local n = x + w;
  for nx = x, n, 1 do
    local nextColor = {};
    nextColor.r = color.r;
    nextColor.g = color.g;
    nextColor.b = color.b;
    nextColor.a = (as1 - (math.abs(nx - x) * step)) * 255.0;
    self:drawLineV(nx, y, h, 1, nextColor);
  end
end

function Component:alphaGradientV(x, y, w, h, color, alphaStart, alphaStop)
  local as1 = alphaStart / 255.0;
  local as2 = alphaStop / 255.0;
  local step = math.abs(as2 - as1) / h;
  if alphaStart < alphaStop then
    step = -step;
  end
  local n = y + h;
  for ny = y, n, 1 do
    local nextColor = {};
    nextColor.r = color.r;
    nextColor.g = color.g;
    nextColor.b = color.b;
    nextColor.a = (as1 - (math.abs(ny - y) * step)) * 255.0;
    self:drawLineH(x, ny, w, 1, nextColor);
  end
end

function Component:drawRect(x, y, w, h, color)
  self:drawRectPartial(x, y, w, h, true, true, true, true, color);
end

function Component:drawRectPartial(x, y, width, height, n, s, e, w, color)
  if n == true then
    self:drawLineH(x, y, width, 1, color);
  end
  if s == true then
    self:drawLineH(x, y + height, width, 1, color);
  end
  if w == true then
    self:drawLineV(x, y, height, 1, color);
  end
  if e == true then
    self:drawLineV(x + width, y, height, 1, color);
  end
end

function Component:drawRoundRect(x, y, w, h, color)
  self:fillRect(x + 3, y, w - 6, 1, color); -- top
  self:fillRect(x + 3, y + h, w - 6, 1, color); -- bottom
  self:fillRect(x, y + 3, 1, h - 6, color); -- left
  self:fillRect(x + w, y + 3, 1, h - 6, color); -- right
  self:fillRect(x + 1, y + 1, 2, 1, color); -- top-left
  self:fillRect(x + 1, y + 1, 1, 2, color);
  self:fillRect(x + w - 3, y + 1, 2, 1, color); -- top-right
  self:fillRect(x + w - 1, y + 2, 1, 2, color);
  self:fillRect(x + 2, y + h - 1, 2, 1, color); -- bottom-left
  self:fillRect(x + 1, y + h - 3, 1, 2, color);
  self:fillRect(x + w - 3, y + h - 1, 2, 1, color); -- bottom-right
  self:fillRect(x + w - 1, y + h - 3, 1, 2, color);
end

function Component:drawActiveTab(text, x, y, length, height, textColor, borderColor, font, highlightFactor, shadowText)
  return self:drawTab_(text, x, y, length, height, textColor, borderColor, font, true, highlightFactor, shadowText);
end

function Component:drawTab(text, x, y, length, height, textColor, borderColor, font, highlightFactor, shadowText)
  return self:drawTab_(text, x, y, length, height, textColor, borderColor, font, false, highlightFactor, shadowText);
end

function Component:drawTab_(text, x, y, length, height, textColor, borderColor, font, active, highlightFactor, shadowText)
  local p = self:getParent();
  local l = length;
  local h = height;
  local af = self.alpha_factor;
  if af == nil then
    af = 1;
  end
  local br = self.brightness;
  if br == nil then
    br = 1;
  end
  self:fillRect(x + 3, y, l - 6, 1, borderColor); -- top
  self:fillRect(x + 1, y + 1, 2, 1, borderColor); -- top-left
  self:fillRect(x + 1, y + 1, 1, 2, borderColor);
  self:fillRect(x + l - 3, y + 1, 2, 1, borderColor); -- top-right
  self:fillRect(x + l - 1, y + 2, 1, 2, borderColor);
  self:fillRect(x, y + 3, 1, h, borderColor);
  self:fillRect(x + l, y + 3, 1, h, borderColor);
  self:drawLineH(x + l, y + 3 + h, 2, 1, borderColor);
  if not active then
    self:fillRect(x, y + h + 3, l + 1, 1, borderColor);
  end
  local oldbr = self.br;
  local br = highlightFactor;
  if shadowText then
    self:drawText(text, x - p.x + 2, y - p.y - 2 + 2, 0, 0, 0, limit((textColor.a / 255.0) * af * 0.5, 0, 1), font);
    self:drawText(text, x - p.x + 1, y - p.y - 2 + 1, limit((textColor.r / 255.0) * br * 0.1, 0, 1), limit((textColor.g / 255.0) * br * 0.1, 0, 1), limit((textColor.b / 255.0) * br * 0.1, 0, 1), limit((textColor.a / 255.0) * af * 0.8, 0, 1), font);
  end
  self:drawText(text, x - p.x, y - p.y - 2, limit((textColor.r / 255.0) * br, 0, 1), limit((textColor.g / 255.0) * br, 0, 1), limit((textColor.b / 255.0) * br, 0, 1), limit((textColor.a / 255.0) * af, 0, 1), font);
  return { x1 = x, x2 = x + l, y1 = y, y2 = y + h };
end

function Component:fillRoundRect(x, y, w, h, color)
  self:fillRect(x + 2, y + 2, w - 4, h - 4, color); -- body
end

function Component:fillRoundRectFancy(x, y, w, h, color)
  self:fillRect(x + 2, y + 2, w - 4, h - 4, color); -- body
  self:fillRect(x + 4, y + 1, w - 8, h - 2, color); -- vertical
  self:fillRect(x + 1, y + 4, w - 2, h - 8, color); -- horizontal
  self:drawRoundRect(x, y, w, h, color);
end

function Component:isChild(obj)
  local returned = false;
  if self._children == nil then
    self._children = {};
    self._children_length = 0;
  end
  local length = tLength(self._children) - 1;
  for index = 0, length, 1 do
    local child = self._children[index];
    if child == obj then
      returned = true;
      break;
    end
  end
  return returned;
end

function Component:addChild(obj)
  -- Make sure not to add the child more than once.
  if self:isChild(obj) then return; end

  ISUIElement.addChild(self, obj);
  if self._children == nil then
    self._children = {};
    self._children_length = 0;
  end
  local length = tLength(self._children);
  self._children[length] = obj;
  self._children_length = length + 1;
end

function Component:removeChild(obj)
  -- Make sure not to remove the child more than once.
  if not self:isChild(obj) then return; end
  ISUIElement.removeChild(self, obj);
  local table_new = {};
  local _index = 0;
  local length = tLength(self._children) - 1;
  for index = 0, length, 1 do
    local child = self._children[index];
    if child ~= obj then
      table_new[_index] = child;
      _index = _index + 1;
    end
  end
  self._children = table_new;
  self._children_length = _index;
end

function Component:_render()
end
