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

require "Class"

----------------------------------------------------------------
-- Module.lua
--
-- @module Core
-- @author Jab
-- @license LGPL
--
-- @string id 		The internal ID of the Module.
-- @string name 	The name of the Module.
----------------------------------------------------------------
Module = class(function(o, id, name)
	
	-- Set the variables.
	o.id         =     id;
	o.name       =   name;
	o.version    = "1.00";
	o.loaded     =  false;
	o.started    =  false;
	o.handshaked =  false;
end);

----------------------------------------------------------------
-- @return 	Returns the version of the Module.
----------------------------------------------------------------
function Module:getVersion()
	return self.version;
end

----------------------------------------------------------------
-- Loads the module. Create objects here.
----------------------------------------------------------------
function Module:load()

end

----------------------------------------------------------------
-- Starts the module. Add listeners here.
----------------------------------------------------------------
function Module:start()

end

----------------------------------------------------------------
-- Place initial Command requests here.
----------------------------------------------------------------
function Module:handshake()

end

----------------------------------------------------------------
-- On every in-game Tick, this function is executed.
----------------------------------------------------------------
function Module:update()

end

----------------------------------------------------------------
-- Handles a command from the server.
--
-- @string command 	The command given from the server.
-- @table args 		The arguments given with the command.
----------------------------------------------------------------
function Module:command(command, args)

end

function Module:sendCommand(command, args)
	-- Sends the command to SledgeHammer.
	SledgeHammer.instance:sendCommand("sledgehammer.module."..self:getID(), command, args);
end

function Module:sendRequest(command, args, success, failure)
	local request = Request("sledgehammer.module."..self:getID(), command, args, success, failure);
	request:send();
end

----------------------------------------------------------------
-- Stops the module. Remove listeners here.
----------------------------------------------------------------
function Module:stop()

end

----------------------------------------------------------------
-- Unloads the module. Nullify or deconstruct objects here.
----------------------------------------------------------------
function Module:unload()

end

----------------------------------------------------------------
-- @return 	Returns the name of the Module.
----------------------------------------------------------------
function Module:getName()
	return self.name;
end

----------------------------------------------------------------
-- @return 	Returns the ID of the Module.
----------------------------------------------------------------
function Module:getID()
	return self.id;
end

----------------------------------------------------------------
-- @return 	Returns if the Module has started.
----------------------------------------------------------------
function Module:isStarted()
	return self.started;
end

----------------------------------------------------------------
-- @return 	Returns if the Module has handshaked.
----------------------------------------------------------------
function Module:isHandshaked()
	return self.handshaked;
end

----------------------------------------------------------------
-- @return 	Returns if the Module is currently stopped.
----------------------------------------------------------------
function Module:isStopped()
	return not self.started;
end

----------------------------------------------------------------
-- @return 	Returns if the Module has been loaded.
----------------------------------------------------------------
function Module:isLoaded()
	return self.loaded;
end

----------------------------------------------------------------
-- @return 	Returns if the Module is not loaded.
----------------------------------------------------------------
function Module:isUnloaded()
	return not self.loaded;
end