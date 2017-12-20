# Sledgehammer-Lua

This is Sledgehammer's Lua framework, that allows Modules on Sledgehammer to communicate with client-side operations of the game. The framework is organized like Sledgehammer, in that all events are orchestrated and dispatched by modules, or the core itself. This allows a more effective deployment of customizations regarding the ever-abstracting nature of mods.

# Example Module
```lua
require "Sledgehammer"

-- Main mod container.
Module_Example = class(Module, function(o)

    -- Invoke super constructor.
    -- argument 1: The Instance being passed into the constructor. 'this' in Java. (LuaTable)
    -- argument 2: The ID of the Module. (String)
    -- argument 3: The name of the Module. (String)
    Module.init(o, "ModuleExample", "Example");
    
end);

----------------------------------------------------------------
-- Loads the module. Create objects here.
----------------------------------------------------------------
function Module_Example:load() 

end

----------------------------------------------------------------
-- Starts the module. Add listeners here.
----------------------------------------------------------------
function Module_Example:start()

end

----------------------------------------------------------------
-- Stops the module. Remove listeners here.
----------------------------------------------------------------
function Module_Example:stop()

end

----------------------------------------------------------------
-- Unloads the module. Nullify or deconstruct objects here.
----------------------------------------------------------------
function Module_Example:unload()

end

----------------------------------------------------------------
-- Place initial Command requests here.
----------------------------------------------------------------
function Module_Example:handshake()

end

----------------------------------------------------------------
-- On every in-game Tick, this function is executed.
----------------------------------------------------------------
function Module_Example:update()

end

----------------------------------------------------------------
-- Handles a command from the server.
--
-- @string command 	The command given from the server.
-- @table args 		The arguments given with the command.
----------------------------------------------------------------
function Module_Example:command(command, args)

end


-- Registers Module to SledgeHammer.
register(Module_Example());

```
