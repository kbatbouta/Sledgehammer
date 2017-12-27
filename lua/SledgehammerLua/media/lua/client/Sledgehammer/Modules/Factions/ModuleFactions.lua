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

-- Main mod container.
Module_Factions = class(Module, function(o)

	-- Invoke super constructor.
	Module.init(o, "ModuleFactions", "Factions");

	-- Debug flag.
	o.DEBUG = true;
	
end);

-- Events.OnGameStart.Add(FactionsCreate);
-- Events.OnPlayerUpdate.Add(FactionsUpdate);

-- Registers Module to SledgeHammer.
register(Module_Factions());
