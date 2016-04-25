require "ISUI/ISChat"
local MOD_ID      = "ZEMOD"              ;
local MOD_NAME    = "ZIRC Extension Mod" ;
local MOD_VERSION = "1.00"               ;
local MOD_AUTHOR  = "Jab"                ;

local function info()
	-- print(ISChat.instance);
	-- ISChat.instance.addLineInChat = addLineInChat;
	-- ISChat.instance.prerender = prerender;
end

ISChat.addLineInChat = function(user, line, addLocal, addTimestamp)
     if luautils.stringStarts(line, "[B]") then
        local broadcast = line.sub(line, 4);

        local messageLength = string.len(broadcast);
        local msgStart = 0;

        for x=0,messageLength - 1 do
            local c = string.sub(broadcast,x,x+1);
            if c == "> " then
                msgStart = x + 1;
                break;
            end
        end
        local msg = string.sub(broadcast, msgStart, messageLength);
        local color = string.sub(broadcast, 0, msgStart - 1);
        broadcast = msg;
        
        print("Broadcast: ");
        print("Message: " .. broadcast);
        print("Color: " .. color);

        color = string.sub(color,2,-2);
        local colors = luautils.split(luautils.split(color,":")[2],",");

        ISChat.instance.servermsg = broadcast;
        ISChat.instance.servermsgR = tonumber(colors[1]);
        ISChat.instance.servermsgG = tonumber(colors[2]);
        ISChat.instance.servermsgB = tonumber(colors[3]);

        print("Red  : " .. colors[1]);
        print("Green: " .. colors[2]);
        print("Blue : " .. colors[3]);

        ISChat.instance.servermsgTimer = 300;
        return;
     end

    if luautils.stringStarts(line, "[SERVERMSG]") then
        ISChat.instance.servermsg = line.sub(line, 12);
        ISChat.instance.servermsgTimer = 300;
        return;
     end

     -- If someone is muted, ignore msg.
     if user and ISChat.instance.mutedUsers[user] then return end

     local isRaw = false;


     if luautils.stringStarts(line, "[T]") then
     	line = line.sub(line,4);
     	addTimestamp = true;
     end

     -- if luautils.stringStarts(line, "[R]") then
     -- 	line = line.sub(line,4);
     -- 	addTimestamp = false;
     -- end
     
     ISChat.instance.moreinfo:setVisible(true);
     ISChat.instance.chatText:setVisible(true);

     ISChat.invisibleTimer = ISChat.toggleInvisibleTimer;
     
     local chatText = ISChat.instance.chatText
     local vscroll = chatText.vscroll
     local scrolledToBottom = (chatText:getScrollHeight() <= chatText:getHeight()) or (vscroll and vscroll.pos == 1)
    if user and not isRaw then
        line = user.." : "..line
    end
     if addTimestamp and not isRaw then
        line =  "[" .. getHourMinute() .. "] " .. line;
     end
    if addLocal then
        line = getText("UI_chat_local") .. line
    end
    if #ISChat.lines > ISChat.maxLine then
       local newLines = {};
       for i,v in ipairs(ISChat.lines) do
           if i ~= 1 then
               table.insert(newLines, v);
           end
       end
       table.insert(newLines, line .. " <LINE> ");
       ISChat.lines = newLines;
    else
        table.insert(ISChat.lines, line .. " <LINE> ");
    end
     ISChat.instance.chatText.text = "";
    local newText = "";
    for i,v in ipairs(ISChat.lines) do
        if i == #ISChat.lines then v = string.gsub(v, " <LINE> $", "") end
        newText = newText .. v;
    end
     ISChat.instance.chatText.text = newText;
     ISChat.instance.chatText:paginate();
    if scrolledToBottom then
        ISChat.instance.chatText:setYScroll(-10000);
    end
end

function ISChat:prerender()
	ISChat.instance = self
	
    --if (self.timer == nil or self.timer < 0) and self.servermsg == nil then return end

	local mouseOver =  false              ;
	local font      =  UIFont.Cred1       ;
	
	local xCenter   =  0                  ;
	local yCenter   =  0                  ;
	local xOffset   =  0                  ;
	local yOffset   = -200                ;
	local message   = self.servermsg      ;
	local time      = self.servermsgTimer ;
    local fontRed   = self.servermsgR     ;
    local fontGreen = self.servermsgG     ;
    local fontBlue  = self.servermsgB     ;
    local fontAlpha = 0                   ;

    if time ~= nil and time >= 20 then
        fontAlpha = 1.0;
    end
    if time ~= nil and time < 20 and time >= 0 then
        fontAlpha = time / 20.0;
    end

    if fontRed == nil then
        fontRed   = 1;
        fontGreen = 0;
        fontBlue  = 0;
    end

    mouseOver = self.moreinfo.mouseOver or self.moreinfo.moving or self.moreinfo.resizeWidget.resizing or self.moreinfo.resizeWidget2.resizing or false;
    self.moreinfo:setDrawFrame(mouseOver);
    
    if mouseOver then
        ISChat.invisibleTimer = 100
    end

    if ISChat.invisibleTimer > 0 then
        ISChat.invisibleTimer = ISChat.invisibleTimer - 1;
    elseif self.textEntry:getIsVisible() == false then
        self.moreinfo:setVisible(false);
    end
    if self.servermsg then
    	-- Grab the Screen's center coordinates.
    	xCenter = getCore():getScreenWidth()  / 2;
    	yCenter = getCore():getScreenHeight() / 2;

        self:drawTextCentre(message, xCenter + xOffset, yCenter + yOffset, fontRed, fontGreen, fontBlue, fontAlpha, UIFont.Cred1);
        
        if self.servermsgTimer < 0 then
            self.servermsg      = nil ;
    		self.servermsgTimer = 0   ;
        else
        	self.servermsgTimer = self.servermsgTimer - 1;
        end
    end
end

function ISChat:onCommandEntered()
    local command = ISChat.instance.textEntry:getText();

    ISChat.instance.logIndex = 0;
    if command and command ~= "" then
        local newLog = {};
        table.insert(newLog, command);
        for i,v in ipairs(ISChat.instance.log) do
            table.insert(newLog, v);
            if i > 20 then
                break;
            end
        end
        ISChat.instance.log = newLog;
    end

    ISChat.instance.textEntry:clear();
    ISChat.instance.textEntry:unfocus();
    ISChat.instance.textEntry:setText("");
    ISChat.instance.textEntry:setVisible(false);
    if not command or command == "" then
        return;
    end
    local commandProcessed, isShout, isWhisper = false, false, false;
    if luautils.stringStarts(command, "/all") and ServerOptions.getBoolean("GlobalChat") then
        local message = luautils.trim(string.gsub(command, "/all", ""));
        if message ~= "" then
            sendWorldMessage(message);
        end
        commandProcessed = true;
    elseif luautils.stringStarts(command, "/s ") or luautils.stringStarts(command, "/shout ") then
        isShout = true;
        if luautils.stringStarts(command, "/s ") then
            command = string.sub(command, #"/s ");
        elseif luautils.stringStarts(command, "/shout ") then
            command = string.sub(command, #"/shout ");
        end
    elseif luautils.stringStarts(command, "/w ") or luautils.stringStarts(command, "/whisper ") then
        isWhisper = true;
        if luautils.stringStarts(command, "/w ") then
            command = string.sub(command, #"/w ");
        elseif luautils.stringStarts(command, "/whisper ") then
            command = string.sub(command, #"/whisper ");
        end
    elseif luautils.stringStarts(command, "/") then
        SendCommandToServer(command);
        commandProcessed = true;
    end
    if not commandProcessed then
        local suffix = "";
        if isShout then
            getPlayer():SayShout(command);
            suffix = " (shout)";
        elseif isWhisper then
            getPlayer():SayWhisper(command);
            suffix = " (whisper)";
        else
            getPlayer():Say(command);
        end

        if ServerOptions.getBoolean("LogLocalChat") then
            command = string.gsub(command, "<", "&lt;")
            command = string.gsub(command, ">", "&gt;")
            ISChat.addLineInChat(getOnlineUsername() .. suffix, command, true, true);
        end
    end
    doKeyPress(false);
    ISChat.instance.timerTextEntry = 20;
end

-- Event hooks.
Events.OnGameStart.Add(info);