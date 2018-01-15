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
--  Sledgehammer is free to use and modify, ONLY for non-official third-party servers 
--    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors. 

require "OptionScreens/MainScreen"

pixel_phase = 0;
pixel_phase_deg = math.pi / 360;

pixels = nil;
pixels_amount = 0;

warningFadeMax = 10;
warningFade = warningFadeMax;

lx = 0;
ly = 0;
lw = 0;
lh = 0;

dx = 0;
dy = 0;
dw = 0;
dh = 0;

render_pixels = function(main_screen)
    if not SledgeHammer.instance then return end
    if not SledgeHammer.instance:isStarted() then return end
    local core = getCore();
    local sw = core:getScreenWidth();
    local sh = core:getScreenHeight();
    if pixels == nil then
        create_pixels(sw, sh);
    end
    -- Go through each pixel.
    for index = 0, pixels_amount, 1 do
        local pixel = pixels[index];
        local x = pixel.x;
        local y = pixel.y;
        local l = pixel.length;
        local r = 0.6;
        local g = 0.6;
        local b = 0.6;
        local a = 30 / l;
        if l > 23 then
            local v = ((l - 23) / 7);
            r = 0.6  + (v / 2.5);
            g = 0.6  + (v / 2.5);
            b = 0.75 + (v / 2.6);
            a = math.sin(pixel.phase);
        end
        if r > 1   then r = 1  ; end
        if g > 1   then g = 1  ; end
        if b > 1   then b = 1  ; end
        if r < 0   then r = 0  ; end
        if g < 0   then g = 0  ; end
        if b < 0   then b = 0  ; end
        if a < 0.2 then a = 0.2; end
        if a > 1   then a = 1  ; end
        main_screen:drawLine2(x, y, x - l, y - l, a, r, g, b);
        if x >= sw + l or y >= sh + l then
            create_pixel(index, false, sw, sh);
        else
            if l < 5 then l = 5; end
            local vx = 1.2 * (l / 2.5);
            local vy = 1.2 * (l / 2.5);
            if vx > 9.5 then vx = 9.5; end
            if vy > 9.5 then vy = 9.5; end
            pixel.x = pixel.x + vx;
            pixel.y = pixel.y + vy;
            pixel.phase = pixel.phase + 0.1;
        end
    end
end

create_pixels = function(w, h)
    local sw = w or getCore():getScreenWidth();
    local sh = h or getCore():getScreenHeight();
    pixels = {};
    pixels_amount = (sw + sh) / 2;
    for index = 0, pixels_amount, 1 do
        create_pixel(index, true, sw, sh);
    end
end

create_pixel = function(index, random, w, h) 
    local sw = w or getCore():getScreenWidth();
    local sh = h or getCore():getScreenHeight();
    local flip = ZombRand(2);
    local x = 0;
    local y = 0;
    local length = ZombRand(30);
    if length < 4 then length = 4; end
    if random then
        x = ZombRand(sw);
        y = ZombRand(sh);
    else
        if flip == 1 then
            x = ZombRand(sw);
            y = -length;
        else
            x = -length;
            y = ZombRand(sh);
        end
    end
    pixels[index] = {
        x      = x, 
        y      = y, 
        length = length,
        phase  = 0
    };
end

onResolutionChange = function(ow, oh, nw, nh)
    if not SledgeHammer.instance then return end
    if not SledgeHammer.instance:isStarted() then return end
    create_pixels(nw, nh);
end

Events.OnResolutionChange.Add(onResolutionChange);

addMainScreenRender(render_pixels);