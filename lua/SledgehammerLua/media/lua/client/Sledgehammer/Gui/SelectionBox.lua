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
--    not affiliated with TheIndieStone, or its immediate affiliates, or contractors. 

require "Sledgehammer/Gui/Component"
require "Util"

SelectionBox = ISPanel:derive("Component");
SelectionBox.__type = "SelectionBox";
SelectionBox.__extends = "Component";
renderer = getRenderer();

----------------------------------------------------------------
-- SelectionBox.lua
--
-- @author Jab
-- @license LGPL
----------------------------------------------------------------
SelectionBox = Component:derive("SelectionBox");

----------------------------------------------------------------
--
----------------------------------------------------------------
function SelectionBox:new(x, y, w)
  -- Generic instantiation.
  local o = Component:new(x, y, w, SelectionBox.height);
  setmetatable(o, self);
  self.__index = self;
  o.options = {};
  o.options_count = 0;
  o.option = "None";
  o.enabled = false;
  o.brightness = 1;
  o.backgroundColor = { r = 32 / 255, g = 32 / 255, b = 32 / 255, a = 1.0 };
  o.dim = { x1 = x, y1 = y, x2 = x + w, y2 = y + SelectionBox.height };
  o._hover = {};
  return o;
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function SelectionBox:initialise()
  -- Invoke super method.
  Component.initialise(self);

end

----------------------------------------------------------------
--
----------------------------------------------------------------
function SelectionBox:createChildren()
  -- Invoke super method.
  Component.createChildren(self);
  -- Setup colors.
  self.colorOutline = Color(180, 180, 180, 255);
  self.colorText = Color(164, 164, 164, 255);
  self.colorTextSelected = Color(240, 240, 240, 255);
  self.color_background_1 = Color(32, 32, 32, 255);
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function SelectionBox:update()
  if self.open then
    self:bringToTop();
  end
  self:updateAlphaFactor();
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function SelectionBox:prerender()
  local ax = self:getAbsoluteX();
  local ay = self:getAbsoluteY();
  local w = self:getWidth();
  local sh = getCore():getScreenHeight();
  local h_box = SelectionBox.height * self.options_count;
  local up = ay + h_box + SelectionBox.height > sh;
  local bax = ax;
  local bay = ay + SelectionBox.height + 1;
  -- If the screen is too big then have the option box on the top.
  if up then
    bay = ay - h_box;
  end
  self._box = { x = bax, y = bay, w = w, h = h_box, up = up };
  self._open_dim = { x1 = bax, y1 = bay, x2 = bax + w, y2 = bay + h_box };
  self._option_dims = {};
  local t_offset = 2;
  if up then
    t_offset = 1;
  end
  local t_y_offset = self:getHeight();
  if up then
    t_y_offset = -h_box;
  end
  local length = self.options_count - 1;
  for index = 0, length, 1 do
    self._option_dims[index] = {
      x1 = bax,
      y1 = t_y_offset + t_offset,
      x2 = bax + w,
      y2 = t_y_offset + t_offset + SelectionBox.height,
      tc = (w / 2),
      option = self.options[index]
    };
    t_offset = t_offset + SelectionBox.height;
  end
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function SelectionBox:_render()
  self:renderBase();
  if self.open then
    self:renderOpen();
  end
end

function SelectionBox:renderBase()
  local ax = self:getAbsoluteX();
  local ay = self:getAbsoluteY();
  local w = self:getWidth();
  local h = self:getHeight();
  self:drawRectPartial(ax, ay, w, h, true, true, true, true, self.colorOutline);
  self:drawTextCentre(self.option, (w / 2), 2, 1, 1, 1, self.alpha_factor);
end

function SelectionBox:renderOpen()
  local box = self._box;
  local up = box.up;
  renderer:render(nil, box.x, box.y, box.w, box.h, 0.1, 0.1, 0.1, 1);
  self:drawRectPartial(box.x, box.y, box.w, box.h, up, not up, true, true, self.colorOutline);
  local length = self.options_count - 1;
  for index = 0, length, 1 do
    local dim = self._option_dims[index];
    local r = 1;
    local g = 1;
    local b = 1;
    local a = 0.5;
    if self._hover[index] then
      a = 1.0;
    end
    self:drawTextCentre(dim.option, dim.tc, dim.y1, r, g, b, a);
  end
end

----------------------------------------------------------------
--
----------------------------------------------------------------
function SelectionBox:onFocus(x, y)
  self.open = not self.open;
  if self.open then
    if SelectionBox.openSelectionBox ~= nil then
      SelectionBox.openSelectionBox.open = false;
    end
    self.just_opened = true;
    SelectionBox.openSelectionBox = self;
    Events.OnTickEvenPaused.Add(SelectionBox.onTick);
    Events.OnKeyPressed.Add(SelectionBox.onKeyPressed);
    Events.OnPostRender.Add(SelectionBox.onPostRender);
    Events.OnMouseDown.Add(SelectionBox.onMouseDown);
    self.is_mouse_down = true;
  else
    SelectionBox.openSelectionBox = nil;
    Events.OnTickEvenPaused.Remove(SelectionBox.onTick);
    Events.OnKeyPressed.Remove(SelectionBox.onKeyPressed);
    Events.OnPostRender.Remove(SelectionBox.onPostRender);
    Events.OnMouseDown.Remove(SelectionBox.onMouseDown);
    self.is_mouse_down = false;
  end
end

----------------------------------------------------------------
-- Updates the Alpha-factor so the SelectionBox becomes more
-- visible when the Player is interfacing it.
----------------------------------------------------------------
function SelectionBox:updateAlphaFactor()
  if self.open then
    self.alpha_factor = 1; return;
  end
  if self:isMouseOver() then
    self.alpha_factor = 1;
  else
    if self.alpha_factor > 0.6 then
      self.alpha_factor = self.alpha_factor - 0.05;
    else
      self.alpha_factor = 0.6;
    end
  end
end

function SelectionBox:addOption(option)
  local length = tLength(self.options);
  self.options[length] = option;
  self.options_count = length + 1;
  self._hover[length] = false;
  if length == 1 and self.option == "None" then
    self:setOption(option);
  end
  self.enabled = true;
end

function SelectionBox:removeOption(option)
  local table_new = {};
  local _index = 0;
  local length = tLength(self.options) - 1;
  for index = 0, length, 1 do
    local o = self.options[index];
    if option ~= o then
      table_new[_index] = o;
      _index = _index + 1;
    end
  end
  self.options = table_new;
  self.options_count = _index;
  if self.option == option then
    if self.options_count > 0 then
      self.option = self.options[0];
    else
      self.option = "None";
      self.enabled = false;
    end
  end
  self._hover = {};
  length = self.options_count - 1;
  for index = 0, length, 1 do
    self._hover[index] = false;
  end
end

function SelectionBox:setOption(option)
  if option == nil then
    self.option = "None";
  else
    -- TODO: Integrity check.
    self.option = option;
  end
  self.alpha_factor = 1;
  self:onSelection(option);
end

function SelectionBox:onSelection(option) end

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

-- The static definition for header heights for the SelectionBox.
SelectionBox.height = 17;

SelectionBox.openSelectionBox = nil;

SelectionBox.onTick = function()
  local sbox = SelectionBox.openSelectionBox;
  local mouse = sbox:getMouse();
  local mx = mouse.x;
  local my = mouse.y;
  local length = tLength(sbox._option_dims) - 1;
  local ay = sbox:getAbsoluteY();
  for index = 0, length, 1 do
    local option_dim = sbox._option_dims[index];
    local x1 = option_dim.x1;
    local y1 = option_dim.y1 + ay;
    local x2 = option_dim.x2;
    local y2 = option_dim.y2 + ay;
    if mx >= x1 and mx <= x2 and my >= y1 and my <= y2 then
      sbox._hover[index] = true;
    else
      sbox._hover[index] = false;
    end
  end
  if not sbox.is_mouse_down then
    if isMouseButtonDown(0) then
      SelectionBox.onMouseDown(mx, my);
    end
  else
    if not isMouseButtonDown(0) then
      sbox.is_mouse_down = false;
    end
  end
end

SelectionBox.onMouseDown = function(x, y)
  print("mouse: " .. tostring(x) .. ", " .. tostring(y));
  local sbox = SelectionBox.openSelectionBox;
  if sbox ~= nil then
    if sbox.just_opened then
      sbox.just_opened = false;
      return;
    end
    local mouse = sbox:getMouse();
    local mx = mouse.x;
    local my = mouse.y;
    local length = tLength(sbox._option_dims) - 1;
    local ay = sbox:getAbsoluteY();
    for index = 0, length, 1 do
      local option_dim = sbox._option_dims[index];
      local x1 = option_dim.x1;
      local y1 = option_dim.y1 + ay;
      local x2 = option_dim.x2;
      local y2 = option_dim.y2 + ay;
      if mx >= x1 and mx <= x2 and my >= y1 and my <= y2 then
        sbox:setOption(option_dim.option);
        break;
      else
      end
    end
  end
  SelectionBox.closeActiveSelectionBox();
end

SelectionBox.onPostRender = function()
end

SelectionBox.onKeyPressed = function(key)
  if SelectionBox.openSelectionBox ~= nil then
    if key == Keyboard.KEY_ESCAPE then
      SelectionBox.closeActiveSelectionBox();
    end
  end
end

SelectionBox.closeActiveSelectionBox = function()
  if SelectionBox.openSelectionBox ~= nil then
    SelectionBox.openSelectionBox.open = false;
    SelectionBox.openSelectionBox.is_mouse_down = false;
    SelectionBox.openSelectionBox = nil;
  end
  Events.OnTickEvenPaused.Remove(SelectionBox.onTick);
  Events.OnKeyPressed.Remove(SelectionBox.onKeyPressed);
  Events.OnPostRender.Remove(SelectionBox.onPostRender);
  Events.OnMouseDown.Remove(SelectionBox.onMouseDown);
end


