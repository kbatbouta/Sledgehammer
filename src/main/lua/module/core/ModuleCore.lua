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
end);

function Module_Core:handshake()
    print("Handshake core");
	local success = function(table, request)
		SledgeHammer.instance.self = table.self;
		if SledgeHammer.instance.DEBUG then
			print("Player's ID is " .. tostring(SledgeHammer.instance.self.id));
		end
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
	if command == "updatePlayer" then
		self:updatePlayer(args.player);
	end
end

register(Module_Core());