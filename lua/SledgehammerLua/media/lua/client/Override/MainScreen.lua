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
--        if MainScreen.instance.inGame then
--            saveGame();
--            MainScreen.instance:getLatestSave();
--        end
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