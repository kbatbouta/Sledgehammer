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

-- Main mod container.
Module_Core = class(Module, function(o)
	-- Invoke super constructor.
	Module.init(o, "core", "Core");
	-- Debug flag.
	o.DEBUG = true;
	o.broadcastCurrent = nil;
end);

function Module_Core:load()
	self.sounds = {};
end

function Module_Core:handshake()
	local success = function(table, request)
        SledgeHammer.instance.self = table.self;
        if SledgeHammer.instance.DEBUG then
            print("Player's ID is " .. tostring(SledgeHammer.instance.self.id));
        end
        addMainScreenRender(render_sledgehammer);
        addMainScreenFocus(focus_sledgehammer);
    end
    local failure = function(error, request)
        print("SledgeHammer: Failed to request post-login information. ErrorCode: ".. tostring(error));
    end
    self:sendRequest("requestInfo", nil, success, failure);
end

function Module_Core:updatePlayer(player)
	if SledgeHammer.instance.self.id == player.id then
		SledgeHammer.instance.self = player;
	end
	SledgeHammer.instance:addPlayer(player);

end

function Module_Core:command(command, args)
	rPrint(args);
	if command == "updatePlayer" then
		self:updatePlayer(args.player);
	elseif command == "sendFile" then
		self:writeFile(args);
	end
end


render_sledgehammer = function(main_screen)
	if not SledgeHammer.instance then return end
	if not SledgeHammer.instance:isStarted() then return end
	local tex  = getTexture("media/ui/Sledgehammer/logo.png");
	local sw = getCore():getScreenWidth();
	local sh = getCore():getScreenHeight();
	local w  = tex:getWidth()  / 2;
	local h  = tex:getHeight() / 2;
	local x  = sw - w - 46;
	local y  = sh - h - 100;
	local a = 1-(warningFade / warningFadeMax);
	main_screen:drawTextureScaled(tex , x , y , w , h , a     , 1  , 1  , 1  );
	lx = x;
	ly = y;
	lw = w;
	lh = h;
	warningFade = warningFade - (1.5 / 60.0);
	if warningFade < 0 then warningFade = 0; end
end

function focus_sledgehammer(main_screen, x, y)
    if contains(lx, ly, lw, lh, x, y) then
        openUrl("https://github.com/JabJabJab/Sledgehammer/");
    end
end

register(Module_Core());