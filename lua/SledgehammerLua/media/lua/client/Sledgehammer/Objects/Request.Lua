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

require "Sledgehammer"

ASYNC_ERROR_NONE    = 0;
ASYNC_ERROR_TIMEOUT = 1;

----------------------------------------------------------------
-- Request.lua
-- Class designed to promote the ability to asynchronusly provide a callback function to
-- Server-Client commands. This is to help with the scalability of Sledgehammer functionality,
-- as well as preventing unecessery overhead with the Lua engine. When using this method to 
-- request data, data can be requested on a one-time basis. This will prevent multiple listeners
-- constantly consuming computation time while the Lua engine goes through each appended function 
-- per event.
--
-- @author Jab
-- @module Core
-- @license LGPL
-- @copyright Jab, Joshua Edwards 2017
--
-- @string mod 		Module to associate with the command request
-- @string command 	Command to associate with the command request
-- @table args 		Arguments provided with the command request
-- @function callbackSuccess 	Function to call back on a successfull request. {table, request} 
-- @function callbackFailure 	Function to call back on a erronous request. {error, request}
----------------------------------------------------------------
Request = class(function(o, mod, command, args, callbackSuccess, callbackFailure)
	
	-- The module calling for the data.
	o.mod = "request:"..mod;

	-- Command given.
	o.command = command;

	-- The LuaTable arguments for the command.
	o.args = args;

	-- The content requested.
	o.result = nil;

	-- Flag for if the command request is successful. 
	o.success = false;

	-- Timeout of 10 seconds.
	o.timeout = 10;

	-- Error code for result. (0 means success)
	o.error = 0;

	-- Flag for if the request has successfully been attempted.
	o.completed = false;

	-- Flag for if the request has been dispatched or not.
	o.started = false;

	o.callbackSuccess = callbackSuccess;
	o.callbackFailure = callbackFailure;

	-- Internal dispatch function.
	o.__dispatch = function(mod, command, table)
		
		local request = o;

		if mod == request.mod and command == request.command then

			-- Set the request's flag to true.
			request.completed = true;

			-- Set the request to successful.
			request.success = true;

			-- Set the request's result table.
			request.result = table;

			if SledgeHammer.instance.DEBUG then
				print("Request: Result for request '"
					..tostring(mod)..":"..tostring(command).."':");
				if table == nil then
					print("Table is null.");
				else
					rPrint(table, 1024);
				end
				print();
			end

			if request.callbackSuccess ~= nil then
				-- Call callback function.
				request.callbackSuccess(table, request);
			end

			-- Remove from Events list after executing.
			Events.OnServerCommand.Remove(request.__dispatch);

			-- Remove the update function from the OnTick Event.
			Events.OnTickEvenPaused.Remove(request.__update);
		end		
	end

	-- The update function to check for timing out.
	o.__update = function()

		local request = o;
		
		-- Get the current Unix timestamp.
		local current = getTimestamp();

		if (current - request.startTime) > request.timeout then

			-- Set the request to completed.
			request.completed = true;

			-- Set the error code.
			request.error = ASYNC_ERROR_TIMEOUT;

			-- Remove dispatch callback function from Events list.
			Events.OnServerCommand.Remove(request.__dispatch);

			-- Remove this function from the OnTick event.
			Events.OnTickEvenPaused.Remove(request.__update);

			if request.callbackFailure ~= nil then
				-- Dispatch failure Event.
				request.callbackFailure(request.error, request);
			end



		end
	end
end);

----------------------------------------------------------------
-- Sets the timeout time for the request.
-- @int timeout 	The time (in seconds) to cancel the request upon failure to respond.
----------------------------------------------------------------
function Request:setTimeout(timeout)
	
	-- Validity check.
	if timeout == nil then
		print("Timeout is nil!");
		return;
	end

	if type(timeout) == "number" then
		self.timeout = timeout;
	else
		print("timeout is not a number!");
	end
end

----------------------------------------------------------------
-- Starts the request.
----------------------------------------------------------------
function Request:send()

	-- Make sure not to execute this request more than once.
	if not self.started then

		-- Set started flag to true.
		self.started = true;

		-- Set the time started.
		self.startTime = getTimestamp();

		-- Add the dispatch function.
		Events.OnServerCommand.Add(self.__dispatch);
		
		-- Add the ontick function.
		Events.OnTickEvenPaused.Add(self.__update);
		

		if SledgeHammer.isDebug() then 
			print("sendClientCommand("..tostring(self.mod)..", "..tostring(self.command)..", "..tostring(self.args)..");");
		end
		-- Send the request command.
		-- sendClientCommand(self.mod, self.command, {});
	

		-- SledgeHammer.instance:sendCommand(self.mod, self.command, self.args);
		sendClientCommand(self.mod, self.command, self.args);
	end
end

----------------------------------------------------------------
-- Returns whether or not the task has a callback function.
----------------------------------------------------------------
function Request:hasCompleted()
	return self.completed;
end
----------------------------------------------------------------
-- @return Returns the result, if the request is completed. Returns nil if not.
----------------------------------------------------------------
function Request:getResult()
	return self.result;
end

----------------------------------------------------------------
-- @return Returns whether or not the request was successful. Returns nil if not completed.
----------------------------------------------------------------
function Request:isSuccessful()
	if self.completed then
		return self.success;
	else
		return nil;
	end
end

----------------------------------------------------------------
-- @return Returns the error code for the request. Returns nil if not completed.
----------------------------------------------------------------
function Request:getErrorCode()
	if self.completed then
		return self.error;
	else
		return nil;
	end
end