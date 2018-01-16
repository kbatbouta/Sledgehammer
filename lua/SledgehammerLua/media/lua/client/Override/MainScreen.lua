MainScreen.onMenuItemMouseDownMainMenu = function(item, x, y)
    if MainScreen.instance.delay >= 0 then return; end
    if MainScreen.instance.tutorialButton then return end
    if MainScreen.instance.checkSavefileModal then return end

    if (item.internal == "JOINSERVER" or item.internal == "CHALLENGE" or item.internal == "SANDBOX" or item.internal == "BEGINNER" or item.internal == "APOCALYPSE" or item.internal == "SOLO") and not MainScreen.checkTutorial(item) then
        return;
    end

    if item.internal == "LATESTSAVE" then
        MainScreen.continueLatestSave(MainScreen.latestSaveGameMode, MainScreen.latestSaveWorld)
        return
    end

    if item.internal == "EXIT" then
        setGameSpeed(1);
        pauseSoundAndMusic();
        setShowPausedMessage(true);
        getCore():quit();
    end
    if item.internal == "RETURN" then
        ToggleEscapeMenu(getCore():getKey("Main Menu"))
        return
    end
    if item.internal == "JOINSERVER" then
        MainScreen.instance.bottomPanel:setVisible(false);
        MainScreen.instance.joinServer:pingServers(true);
        MainScreen.instance.joinServer:setVisible(true, MainScreen.instance.joyfocus);
    end
    if item.internal == "COOP" then
        MainScreen.instance.bottomPanel:setVisible(false);
        MainScreen.instance.onlineCoopScreen:aboutToShow()
        MainScreen.instance.onlineCoopScreen:setVisible(true, MainScreen.instance.joyfocus);
    end
    if item.internal == "SCOREBOARD" then
        MainScreen.instance.scoreboard:setVisible(true, MainScreen.instance.joyfocus);
        scoreboardUpdate()
    end

    if item.internal == "OPTIONS" then
        MainScreen.instance.mainOptions:toUI()
        MainScreen.instance.mainOptions:setVisible(true, MainScreen.instance.joyfocus);
    end
    if item.internal == "SOLO" then
        MainScreen.instance.soloScreen:setVisible(true, MainScreen.instance.joyfocus);
        MainScreen.instance.soloScreen.onMenuItemMouseDown(MainScreen.instance.soloScreen.tutorial,0,0);
    end
    if item.internal == "LOAD" then
        MainScreen.instance.loadScreen:setSaveGamesList();
        MainScreen.instance.loadScreen:setVisible(true, MainScreen.instance.joyfocus);
    end
    if item.internal == "MODS" then
        MainScreen.instance.modSelect:setVisible(true, MainScreen.instance.joyfocus);
        MainScreen.instance.modSelect:populateListBox(getModDirectoryTable());
    end
    if item.internal == "ADMINPANEL" then
        local modal = ISAdminPanelUI:new(200, 200, 170, 330);
        modal:initialise();
        modal:addToUIManager();
        ToggleEscapeMenu(getCore():getKey("Main Menu"))
        return
    end
    if item.internal == "USERPANEL" then
        local modal = ISUserPanelUI:new(200, 200, 170, 150, getPlayer())
        modal:initialise();
        modal:addToUIManager();
        ToggleEscapeMenu(getCore():getKey("Main Menu"))
        return
    end
    if item.internal == "TUTORIAL" then
        if JoypadState[1] then
            MainScreen.onTutorialControllerWarn()
            return
        end
        MainScreen.startTutorial();
    end
    if item.internal == "APOCALYPSE" then
        MainScreen.instance:setDefaultSandboxVars()
            getWorld():setGameMode("Sandbox");
            getWorld():setMap("DEFAULT")
            getWorld():setWorld("demo");
            deleteSave("Sandbox/demo");
            createWorld("demo");
            -- menu activated via joypad, we disable the joypads and will re-set them automatically when the game is started
            if MainScreen.instance.joyfocus then
                local joypadData = MainScreen.instance.joyfocus
                joypadData.focus = nil;
                updateJoypadFocus(joypadData)
                JoypadState.count = 0
                JoypadState.players = {};
                JoypadState.joypads = {};
                JoypadState.forceActivate = joypadData.id;
            end
            GameWindow.doRenderEvent(false);
            forceChangeState(GameLoadingState.new());
    end

    if item.internal == "INVITE" then
        InviteFriends.instance:fillList();
--        InviteFriends.instance:setVisible(true);
        MainScreen.instance.inviteFriends:setVisible(true, MainScreen.instance.joyfocus);
    end

    if item.internal == "WORKSHOP" then
        MainScreen.instance.workshopSubmit:fillList()
        MainScreen.instance.workshopSubmit:setVisible(true, MainScreen.instance.joyfocus)
    end

    MainScreen.instance.bottomPanel:setVisible(false);
end

function MainScreen:prerender()
    ISPanel.prerender(self);
    if(self.inGame) then
        self:drawRect(0, 0, self.width, self.height, 0.5, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b);
    end
    self.delay = self.delay - 1;
    local textManager = getTextManager();
    self.time = self.time + (1.0 / 60);
    local lastIsSameTitle = false;
    local nextIsSameTitle = false;
    if self.time > 11.8 then
        if self.credits:size() > self.creditsIndex then
            if self.creditsIndex > 0 then
                if self.credits:get(self.creditsIndex-1).title == self.credits:get(self.creditsIndex).title then
                    lastIsSameTitle = true;
                end
            end
            if self.credits:size()-1 > self.creditsIndex then
                if self.credits:get(self.creditsIndex+1).title == self.credits:get(self.creditsIndex).title then
                    nextIsSameTitle = true;
                end
            end
        end
        local del = self.creditTime / self.creditTimeMax;
        local credAlpha = self.creditTime / self.creditTimeMax;
        if(credAlpha <= 0.5) then
            credAlpha = credAlpha * 2;
        elseif (credAlpha >= 0.8) then
            credAlpha = 1.0 - ((credAlpha - 0.8) * 5);
        else
            credAlpha = 1;
        end
        local credAlpha2 = self.creditTime / self.creditTimeMax;
        if(credAlpha2 <= 0.1) then
            credAlpha2 = credAlpha2 * 10;
            if lastIsSameTitle then credAlpha2 = 1; end
        elseif (credAlpha2 >= 0.9) then
            credAlpha2 = 1.0 - ((credAlpha2 - 0.9) * 10);
            if nextIsSameTitle then credAlpha2 = 1; end
        else
            credAlpha2 = 1;
        end
        self.creditTime = self.creditTime + (1 / 60.0);
        if self.creditTime > self.creditTimeMax then
            self.creditTime = 0;
            self.creditsIndex = self.creditsIndex + 1;
        end
        if self.credits:size() > self.creditsIndex and not self.inGame and ISDemoPopup.instance == nil then
            textManager:DrawString(UIFont.Cred1, (getCore():getScreenWidth()*0.75)+50 , getCore():getScreenHeight()*0.1, self.credits:get(self.creditsIndex).title, 1, 1, 1, credAlpha2);
            local x = (getCore():getScreenWidth()*0.75);
            local xwid = textManager:MeasureStringX(UIFont.Cred2, self.credits:get(self.creditsIndex).name);
            if(x + xwid > getCore():getScreenWidth()) then
               x = x - ((x + xwid) - getCore():getScreenWidth()) - 10;
            end
            textManager:DrawString(UIFont.Cred2, x, (getCore():getScreenHeight()*0.1) + 26, self.credits:get(self.creditsIndex).name, 1, 1, 1, credAlpha);
        end
    end
    local mainScreen = MainScreenState.getInstance();
    if mainScreen ~= nil and (ISDemoPopup.instance == nil) and (ISScoreboard.instance and not ISScoreboard.instance:isVisible()) then
        local length = tLength(MainScreen.render_functions) - 1;
        for index = 0, length, 1 do
            MainScreen.render_functions[index](self);
        end
    end
end

function MainScreen:onFocus(x, y)
    if ISScoreboard.instance and ISScoreboard.instance:isVisible() then return; end
    local length = tLength(MainScreen.focus_functions) - 1;
    for index = 0, length, 1 do
        MainScreen.focus_functions[index](self, x, y);
    end
end

-- Functions to register and call.
MainScreen.render_functions = {};
MainScreen.focus_functions  = {};

function addMainScreenRender(func)
    local length = tLength(MainScreen.render_functions);
    MainScreen.render_functions[length] = func;
    return length;
end

function addMainScreenFocus(func)
    local length = tLength(MainScreen.focus_functions);
    MainScreen.focus_functions[length] = func;
    return length;
end