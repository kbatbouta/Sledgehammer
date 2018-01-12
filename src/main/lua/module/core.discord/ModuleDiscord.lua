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

require "Sledgehammer"

_G.discord = false;

----------------------------------------------------------------
-- ModuleDiscord.lua
-- Module for the "core.discord" operations for Sledgehammer.
--
-- @plugin Core
-- @module Discord
-- @author Jab
-- @license LGPL3
----------------------------------------------------------------
Module_Discord = class(Module, function(o)
    -- Invoke super constructor.
    Module.init(o, "core.discord", "Discord");
    -- Debug flag.
    o.DEBUG = true;
end);

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function Module_Discord:load()

end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function Module_Chat:start()

end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function Module_Discord:handshake()
    -- The Success Function to handle data.
    local success = function(table, request)
        _G.discord_info = table.info;
        addMainScreenRender(render_discord);
        addMainScreenFocus(focus_discord);
        _G.discord = true;
    end
    -- The Failure Function to handle errors.
    local failure = function(error, request)
        print("WARNING: Failed to retrieve Discord information. (Error:"..tostring(error)..")");
    end
    self:sendRequest("requestInformation", nil, success, failure);
end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function Module_Discord:command(command, args)

end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function Module_Discord:stop()

end

----------------------------------------------------------------
-- @Override
----------------------------------------------------------------
function Module_Discord:unload()

end

render_discord = function(main_screen)
    if not SledgeHammer.instance then return end
    if not SledgeHammer.instance:isStarted() then return end
    local tex = getTexture("media/ui/Sledgehammer/discord.png");
    local sw = getCore():getScreenWidth();
    local sh = getCore():getScreenHeight();
    local w  = tex:getWidth()  / 2;
    local h  = tex:getHeight() / 2;
    local x  = 50;
    local y  = sh - h - 100;
    local a = 1-(warningFade / warningFadeMax);
    local mx = getMouseX();
    local my = getMouseY();
    local inside = contains(x, y, w, h, mx, my);
    local lerp_value = 0;
    if inside then
        if discord_mouse < 1 then
            discord_mouse = discord_mouse + discord_ease_value;
        else
            discord_mouse = 1;
        end
        lerp_value = ease_in_quad(discord_mouse);
    else
        if discord_mouse > 0 then
            discord_mouse = discord_mouse - discord_ease_value;
        else
            discord_mouse = 0;
        end
        lerp_value = ease_out_quad(discord_mouse);
    end
    if lerp_value > 1 then lerp_value = 1; end
    if lerp_value < 0 then lerp_value = 0; end
    local r = lerp(color_white.r, color_blurple.r, lerp_value);
    local g = lerp(color_white.g, color_blurple.g, lerp_value);
    local b = lerp(color_white.b, color_blurple.b, lerp_value);
    if r > 1 then r = 1; end
    if g > 1 then g = 1; end
    if b > 1 then b = 1; end
    if r < 0 then r = 0; end
    if g < 0 then g = 0; end
    if b < 0 then b = 0; end
    main_screen:drawTextureScaled(tex, x, y, w, h, a, r, g, b);
    dx = x;
    dy = y;
    dw = w;
    dh = h;
end

focus_discord = function(main_screen, x, y)
    if contains(dx, dy, dw, dh, x, y) then
        openUrl(discord_info.invite_url);
    end
end

color_white   = {r = 1      , g = 1      , b = 1      };
color_blurple = {r = 114/255, g = 137/255, b = 218/255};
discord_mouse = 0;
discord_ease_value = 0.1;

-- Registers the module to SledgeHammer
register(Module_Discord());