function ISScoreboard:onContext(button)
    local username = self.selectedPlayer;
    username = '"'..username..'"'
    if button.internal == "KICK" then
        handleServerCommand("/kickuser " .. username);
    elseif button.internal == "BAN" then
        handleServerCommand("/banuser " .. username);
    elseif button.internal == "BANIP" then
        handleServerCommand("/banuser " .. username .. " -ip");
    elseif button.internal == "GODMOD" then
        handleServerCommand("/godmod " .. username);
    elseif button.internal == "INVISIBLE" then
        handleServerCommand("/invisible " .. username);
    elseif button.internal == "TELEPORT" then
        handleServerCommand("/teleport " .. username);
    elseif button.internal == "TELEPORTTOYOU" then
        handleServerCommand("/teleport " .. username .. " \"" .. getPlayer():getDisplayName() .. "\"");
    elseif button.internal == "MUTE" then
        -- ISChat.instance:mute(self.selectedPlayer)
		self:doAdminButtons()
    elseif button.internal == "VOIPMUTE" then
        VoiceManager:playerSetMute(self.selectedPlayer)
        self:doAdminButtons()
    end
end

function handleServerCommand(command_message)
    -- Create the Command LuaObject
    local command = {
        __name     = "Command",
        raw        = command_message
    };
    -- Create the SendCommand LuaObject that contains the Command.
    local args = {
        __name  = "SendCommand",
        command = command
    };
    -- Send the Command to the server.
    sendClientCommand("core", "sendCommand", args);
end