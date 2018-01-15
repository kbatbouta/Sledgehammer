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

require "Util"
require "Class"
require "Sledgehammer/Module"

----------------------------------------------------------------
-- Sledgehammer.lua
-- Main Class for the Lua framework for Sledgehammer.
-- 
-- @module Core
-- @author Jab
-- @license LGPL3
----------------------------------------------------------------
SledgeHammer = class(function(o)
	 -- Debug flag for global debugging of SledgeHammer's Lua framework.
	 o.DEBUG = true;
	 -- List of the modules.
	 o.modules = {};
	 -- List of the modules by their Names.
	 o.modulesByName = {};
	 -- List of the modules by their IDs.
	 o.modulesByID = {};
	 -- Load flag.
	 o.loaded = false;
	 -- Start flag.
	 o.started = false;
	 o.delayStartSeconds = 1;
	 o.delayStart = false;
	 o.handshakeAttempt = 1;
	-- List of SledgeHammer Player LuaObjects, identified via ID.
	o.players = {};
	-- Map of SledgeHammer Player LuaObjects, identified via string (username).
	o.playersByName = {};
	 -- Player Object for the player running this engine.
	 o.self = nil;
end);

----------------------------------------------------------------
-- Initializes the Sledgehammer Lua Framework.
----------------------------------------------------------------
function SledgeHammer:init()
	local startTimeStamp = getTimestamp();
	print("Initializing SledgeHammer Lua framework. Version: " .. tostring(SledgeHammer:getVersion()));
	if preloaded_modules_index > 0 then
		-- Grab the length of the preloaded modules table.
		local length = preloaded_modules_index - 1;
		-- Formally register preloaded modules.
		for index = 0, length, 1 do
			local nextModule = preloaded_modules[index];
			self:register(nextModule);
		end 
		-- nullify the preload table.
		preloaded_modules_index = 0;
		preloaded_modules = nil;
	end
	-- Set loaded flag.
	self.loaded = true;
	-- Initialization time.
	self.initTimeStamp = getTimestamp();
	-- Register the update method.
	Events.OnTickEvenPaused.Add(update_sledgehammer);
	print("SledgeHammer initialized. Took " .. tostring(self.initTimeStamp - startTimeStamp) .. " seconds." );
end

----------------------------------------------------------------
-- Starts the Sledgehammer Lua Framework.
----------------------------------------------------------------
function SledgeHammer:start()
	-- Register the command method.
	Events.OnServerCommand.Add(command_sledgehammer);
	self.startTimeStamp = getTimestamp();
	self:doHandshake();
	self.started = true;
end

----------------------------------------------------------------
-- Handles SledgeHammer protocol.
----------------------------------------------------------------
function SledgeHammer:onHandshake()
	-- Start Modules after the initial handshake for Sledgehammer.
	self:startModules();
	-- Also Handshake Modules.
	self:handshakeModules();
end

function SledgeHammer:doHandshake()
	local handshakeSuccess = function(table, request)
		-- Flag the handshake as successful.
		SledgeHammer.instance.handshake = true;
		if SledgeHammer.instance.DEBUG then
			print("Handshake accepted!");
		end
		SledgeHammer.instance:onHandshake();
	end
	local handshakeFailure = function(error, request)
		if SledgeHammer.instance.DEBUG then
			print("Handshake failed. ErrorCode: "..tostring(error));
		end
	end
	local handshake = Request("sledgehammer.module.core", "handshake", nil, handshakeSuccess, handshakeFailure);
	handshake:send();
end

----------------------------------------------------------------
-- Handles the updates for the Sledgehammer Lua Framework. 
----------------------------------------------------------------
function SledgeHammer:update()
	if self.hasUpdated == true then
		-- As of Build 37.14, there is a bug where 'sendClientCommand()' does not send to the server until after
		-- the first update tick. Cycling the update tick once fixes that problem.
		if not self.started then
			self:start();
		end
		self:updateModules();
	end
	self.hasUpdated = true;
end

----------------------------------------------------------------
-- Loads the Modules registered.
----------------------------------------------------------------
function SledgeHammer:loadModules()
	-- Get the length of the modules.
	local length = tLength(self.modules) - 1;
	-- Go through each module.
	for index = 0, length, 1 do
		-- Grab the next module.
		local nextModule = self.modules[index];
		if nextModule ~= nil then
			self:loadModule(nextModule);
		end
	end
end

----------------------------------------------------------------
-- Starts the Modules registered.
----------------------------------------------------------------
function SledgeHammer:startModules()
	-- Get the length of the modules.
	local length = tLength(self.modules) - 1;
	-- Go through each module.
	for index = 0, length, 1 do
		-- Grab the next module.
		local nextModule = self.modules[index];
		if nextModule ~= nil then
			self:startModule(nextModule);
		end
	end
end

----------------------------------------------------------------
-- Handshakes all registered Modules.
----------------------------------------------------------------
function SledgeHammer:handshakeModules()
	-- Get the length of the modules.
	local length = tLength(self.modules) - 1;
	-- Go through each module.
	for index = 0, length, 1 do
		-- Grab the next module.
		local nextModule = self.modules[index];
		if nextModule ~= nil then
			self:handshakeModule(nextModule);
		end
	end
end

----------------------------------------------------------------
-- Updates the Modules registered.
----------------------------------------------------------------
function SledgeHammer:updateModules()
	-- Get the length of the modules.
	local length = tLength(self.modules) - 1;
	-- Go through each module.
	for index = 0, length, 1 do
		-- Grab the next module.
		local nextModule = self.modules[index];
		-- If the Module is valid.
		if nextModule ~= nil and nextModule:isLoaded() and nextModule:isStarted() then
			-- Update the module.
			nextModule:update();
		end
	end
end

----------------------------------------------------------------
-- Stops the Modules registered.
----------------------------------------------------------------
function SledgeHammer:stopModules()
	-- Get the length of the modules.
	local length = tLength(self.modules) - 1;
	-- Go through each module.
	for index = 0, length, 1 do
		-- Grab the next module.
		local nextModule = self.modules[index];
	end
end


----------------------------------------------------------------
-- Unloads the Modules registered.
----------------------------------------------------------------
function SledgeHammer:unloadModules()
	-- Get the length of the modules.
	local length = tLength(self.modules) - 1;
	-- Go through each module.
	for index = 0, length, 1 do
		-- Grab the next module.
		local nextModule = self.modules[index];
		if nextModule ~= nil then
			self:unloadModule(nextModule);
		end
	end
	self.modules = {};
end

----------------------------------------------------------------
-- Loads a Module.
----------------------------------------------------------------
function SledgeHammer:loadModule(mod)
	if mod == nil then return; end
	if mod:isLoaded() then return; end
	print("Sledgehammer: Loading Module: '"..tostring(mod:getName()).."'.");
	mod:load();
	mod.loaded = true;
end

----------------------------------------------------------------
-- Starts a Module. (Loads the Module if not done already)
----------------------------------------------------------------
function SledgeHammer:startModule(mod)
	if mod == nil then return; end
	if mod:isStarted() then return; end
	if mod:isUnloaded() then
		self:loadModule(mod);
	end
	print("Sledgehammer: Starting Module: '"..tostring(mod:getName()).."'.");
	mod:start();
	mod.started = true;
end

----------------------------------------------------------------
-- Handshakes a Module. 
----------------------------------------------------------------
function SledgeHammer:handshakeModule(mod)
	if mod == nil then return; end
	if mod:isStopped() then return; end
	if mod:isHandshaked() then return; end
	mod:handshake();
	mod.handshaked = true;
end

----------------------------------------------------------------
-- Stops a Module.
----------------------------------------------------------------
function SledgeHammer:stopModule(mod)
	if mod == nil then return; end
	if mod:isStopped() then return; end
	print("Sledgehammer: Stopping Module: '"..tostring(mod:getName()).."'.");
	mod:stop();
	mod.started = false;
end

----------------------------------------------------------------
-- Unloads a Module. (Stops the Module if not stopped already)
----------------------------------------------------------------
function SledgeHammer:unloadModule(mod)
	if mod == nil then return; end
	if mod:isUnloaded() then return; end
	if mod:isStarted() then
		self:stopModule(mod);
	end
	print("Sledgehammer: Unloading Module: '"..tostring(mod:getName()).."'.");
	mod:unload();
	mod.loaded = false;
end

----------------------------------------------------------------
-- Sends commands to the Server-Side SledgeHammer module.
--
-- @string mod 		Module name.
-- @string command 	Command being sent to the Module.
-- @table args 		Arguments passed to the Module. 
----------------------------------------------------------------
function SledgeHammer:sendCommand(mod, command, args)
	-- Validity check.
	if mod == nil then
		print("Module given is null!");
		return;
	end
	-- Validity check.
	if command == nil then
		print("Module Command given is null!");
		return;
	elseif command == "" then
		print("Module Command given is empty!");
		return;
	end
	-- Send to the Server. (zombie.Lua.LuaManager)
	sendClientCommand(mod, command, args);
end

----------------------------------------------------------------
-- Handles Client-side Sledgehammer Module commands.
--
-- @string mod 		Module name.
-- @string command 	Command being sent to the Module.
-- @table args 		Arguments passed to the Module.
----------------------------------------------------------------
function SledgeHammer:onClientCommand(mod, command, args)
	-- Checks to see if this is a module command.
	if luautils.stringStarts(mod, "sledgehammer.module.") then
		-- Converts to simple module name.
		local modName = toSimpleModuleName(mod);
		if self.DEBUG then
			print("SledgeHammer: Received command: module = '"..tostring(modName).."' command = '"..tostring(command).."'.");
			rPrint(args, 1024);
		end
		-- If this is the core, route directly and return.
		if modName == "core" then
			if command == "reload" then
				self:stopModules();
				self:unloadModules();
				self.modules     = {}   ;
				self.modulesByID = {}   ;
				self.handshake   = false;
				self:doHandshake();
			elseif command == "sendLua" then
				local func = load_function(args.lua);
				func();
				return;
			elseif command == "sendSelf" then
				-- Grab the Player and the information.
				local player = args.player    ;
				local id     = player.id      ;
				local name   = player.username;
				-- Set the Player object in the maps.
				self.players[id]         = player;
				self.playersByName[name] = player;
				self.self                = player;
			elseif command == "sendPlayer" then
				-- Grab the Player and the information.
				local player = args.player    ;
				local id     = player.id      ;
				local name   = player.username;
				-- Set the Player object in the maps.
				self.players[id]         = player;
				self.playersByName[name] = player;
			end
		end
		-- Grab the module being commanded.
		local modu = self.modulesByID[modName];
		-- Validity check.
		if modu == nil then
			-- print("SledgeHammer: Module is null: '" .. tostring(modName) .. "', for command: '" .. command .. "'.");
			return;
		end
		-- Handle the command.
		modu:command(command, args);
	end
end

----------------------------------------------------------------
-- Registers a Module.
----------------------------------------------------------------
function SledgeHammer:register(mod)
	-- Validity check.
	if mod == nil then
		print("Sledgehammer:register() -> Module given is null!");
		return;
	end
	-- Validity check.
	if tContainsValue(self.modules, mod) then
		print("Sledgehammer:register() -> Module already registered: '"..tostring(mod).."'.");
		return;
	end
	-- Grab the next index.
	local length = tLength(self.modules);
	-- Add the module.
	self.modules[length]              = mod;
	self.modulesByID[mod:getID()]     = mod;
	self.modulesByName[mod:getName()] = mod;
	-- If Sledgehammer is not initialized yet, return.
	if not self:isLoaded() then return; end
	-- Check to see if the Module needs to load.
	if not mod:isLoaded() then
		self:loadModule(mod);
	end
	-- If Sledgehammer is not started, return.
	if not self:isStarted() then return; end
	-- Check to see if the Module needs to handshake.
	if not mod:isHandshaked() then
		self:handshakeModule(mod);
	end
	-- Check to see if the Module needs to start;
	if not mod:isStarted() then
		self:startModule(mod);
	end
end

function SledgeHammer:addPlayer(player)
	-- Validity check.
	if player == nil then
		print("Player given is null!");
		return;
	end
	-- Get the size of the players LuaTable.
	local length = tLength(self.players) - 1;
	-- Go through each index of the players LuaTable.
	for index=0, length, 1 do
		-- Grab the next player.
		local nextPlayer = self.players[index];
		-- If the player is already in the list.
		if player.id == nextPlayer.id then
			self.players[index] = player;
		end
	end
	self.playersByName[player.nickname] = player;
	self.playersByName[player.username] = player;
end

-- Returns a Player LuaObject, with a given ID, or name. Returns nil if player isn't found.
--
-- TODO: Associate an async request with modules.
function SledgeHammer:getPlayer(identifier)
	if identifier == nil then
		print("Sledgehammer:getPlayer() -> Given Identifier is null!");
		return nil;
	end
	if(type(identifier) == "number") then
		return self.players[identifier];
	elseif type(identifier == "string") then
		return self.playersByName[identifier];
	end
end

function SledgeHammer:removePlayer(player)
	print("'Sledgehammer:removePlayer()' is not implemented.");
end

----------------------------------------------------------------
-- @return 	Returns whether or not SledgeHammer has fully loaded.
----------------------------------------------------------------
function SledgeHammer:isLoaded() return self.loaded; end

----------------------------------------------------------------
-- @return 	Returns whether or not SledgeHammer is unloaded.
----------------------------------------------------------------
function SledgeHammer:isUnloaded() return not self.loaded; end

----------------------------------------------------------------
-- @return 	Returns whether or not SledgeHammer has started.
----------------------------------------------------------------
function SledgeHammer:isStarted() return self.started; end

----------------------------------------------------------------
-- @return 	Returns whether or not SledgeHammer is currently stopped.
----------------------------------------------------------------
function SledgeHammer:isStopped() return not self.started; end

----------------------------------------------------------------
-- @return Returns the version of this instance of the Sledgehammer Lua Framework.
----------------------------------------------------------------
function SledgeHammer:getVersion() return "4.00"; end

----------------------------------------------------------------
-- @return Returns the TimeStamp for the initialization of the SledgeHammer Lua Framework.
----------------------------------------------------------------
function SledgeHammer:getInitializedTimeStamp() return SledgeHammer.instance.initTimeStamp; end

----------------------------------------------------------------
-- @return If Sledgehammer is being ran in debug mode.
----------------------------------------------------------------
function SledgeHammer:isDebug() return SledgeHammer.instance.DEBUG; end

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

-- List of modules to be loaded. (static for preloading)
preloaded_modules       = {};
-- Index of pre-loaded modules.
preloaded_modules_index =  0;

----------------------------------------------------------------
-- Static method to execute the instantiation of the Sledgehammer
-- Lua Framwork.
--
-- @static
----------------------------------------------------------------
function load_sledgehammer()
	-- Initialize core and store as Singleton.
	SledgeHammer.instance = SledgeHammer();
end

----------------------------------------------------------------
-- Static method to execute loading of the Sledgehammer Lua 
-- Framework.
--
-- @static
----------------------------------------------------------------
function init_sledgehammer()
	SledgeHammer.instance:init();
end

----------------------------------------------------------------
-- Static method to pipe updates on every tick.
--
-- @static
----------------------------------------------------------------
function update_sledgehammer()
	SledgeHammer.instance:update();
end

----------------------------------------------------------------
-- @static
----------------------------------------------------------------
function command_sledgehammer(mod, command, args)
	SledgeHammer.instance:onClientCommand(mod, command, args);
end

----------------------------------------------------------------
-- Static method for preloading modules.
-- 
-- @static
----------------------------------------------------------------
function register(mod)
	-- Validity check.
	if mod == nil then
		print("register() -> Module given is null!");
		return;
	end
	local duplicateRegistry = false;
	local length = 0;
	local nextModule = nil;
	-- If Sledgehammer is initialized, use internal tables.
	if SledgeHammer.instance ~= nil then
		-- Grab the length of the list.
		length = tLength(SledgeHammer.instance.modules) - 1;
		-- Go through all registered Modules.
		for index = 0, length, 1 do
			-- Grab the next Module in the list.
			nextModule = SledgeHammer.instance.modules[index];
			-- If the ID's match, then it is a duplicate register.
			if nextModule:getID() == mod:getID() then
				duplicateRegistry = true;
				break;
			end
		end
		-- Check to see if the Module is already loaded.
		if duplicateRegistry then
			print("register() -> Module is already registered: "..tostring(nextModule:getID()));
			return;
		end
		-- Formally register the Module.
		SledgeHammer.instance:register(mod);
	-- If Sledghammer is not initialized, use static preloaded tables.
	else
		-- Grab the length of the list.
		length = preloaded_modules_index - 1;
		-- Go through all preoaded Modules.
		for index = 0, length, 1 do
			-- Grab the next Module in the list.
			nextModule = preloaded_modules[index];
			-- If the ID's match, then it is a duplicate register.
			if nextModule:getID() == mod:getID() then
				duplicateRegistry = true;
				break;
			end
		end
		-- Check to see if the Module is already preloaded.
		if duplicateRegistry then
			print("register() -> Module is already registered: "..tostring(nextModule:getID()));
			return;
		end
		-- Set the module.
		preloaded_modules[preloaded_modules_index] = mod;
		-- Increment the index.
		preloaded_modules_index = preloaded_modules_index + 1;
	end
end

-- Add the creation function to the Event dispatcher.
Events.OnInitWorld.Add(load_sledgehammer);
-- Add the initialization function to the Event dispatcher.
Events.OnGameStart.Add(init_sledgehammer);