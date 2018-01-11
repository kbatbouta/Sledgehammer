function ISAdminPanelUI:create()
    local btnWid = 150
    local btnHgt = 25
    local padBottom = 10
    local y = 20;
    if isCoopHost() then
        self.beAdmin = ISButton:new(10, y, btnWid, btnHgt, getText("IGUI_AdminPanel_EnableAdminPower"), self, ISAdminPanelUI.onOptionMouseDown);
        self.beAdmin.internal = "BEADMIN";
        self.beAdmin:initialise();
        self.beAdmin:instantiate();
        self.beAdmin.borderColor = self.buttonBorderColor;
        if getAccessLevel() == "admin" then
            self.beAdmin.title = getText("IGUI_AdminPanel_DisableAdminPower");
        end
        self:addChild(self.beAdmin);
        self.beAdmin.tooltip = getText("IGUI_AdminPanel_TooltipAdminPower");
        y = y + 30;
    end
    if getAccessLevel() == "observer" then
        self.dbBtn.enable = false;
    end
    y = y + 30;
    self.checkStatsBtn = ISButton:new(10, y, btnWid, btnHgt, getText("IGUI_AdminPanel_CheckYourStats"), self, ISAdminPanelUI.onOptionMouseDown);
    self.checkStatsBtn.internal = "CHECKSTATS";
    self.checkStatsBtn:initialise();
    self.checkStatsBtn:instantiate();
    self.checkStatsBtn.borderColor = self.buttonBorderColor;
    self:addChild(self.checkStatsBtn);
    y = y + 30;
    self.buildCheatBtn = ISButton:new(10, y, btnWid, btnHgt, "", self, ISAdminPanelUI.onOptionMouseDown);
    if ISBuildMenu.cheat then
        self.buildCheatBtn.title = getText("IGUI_AdminPanel_DisableBuildCheat");
    else
        self.buildCheatBtn.title = getText("IGUI_AdminPanel_EnableBuildCheat");
    end
    self.buildCheatBtn.internal = "BUILDCHEAT";
    self.buildCheatBtn:initialise();
    self.buildCheatBtn:instantiate();
    self.buildCheatBtn.borderColor = self.buttonBorderColor;
    self.buildCheatBtn.tooltip = getText("IGUI_AdminPanel_TooltipBuildCheat");
    self:addChild(self.buildCheatBtn);
    y = y + 30;
    self.seeOptionsBtn = ISButton:new(10, y, btnWid, btnHgt, getText("IGUI_AdminPanel_SeeServerOptions"), self, ISAdminPanelUI.onOptionMouseDown);
    self.seeOptionsBtn.internal = "SEEOPTIONS";
    self.seeOptionsBtn:initialise();
    self.seeOptionsBtn:instantiate();
    self.seeOptionsBtn.borderColor = self.buttonBorderColor;
    self:addChild(self.seeOptionsBtn);
    y = y + 30;
    if getAccessLevel() == "admin" then
        self.nonpvpzoneBtn = ISButton:new(10, y, btnWid, btnHgt, getText("IGUI_AdminPanel_NonPvpZone"), self, ISAdminPanelUI.onOptionMouseDown);
        self.nonpvpzoneBtn.internal = "NONPVPZONE";
        self.nonpvpzoneBtn:initialise();
        self.nonpvpzoneBtn:instantiate();
        self.nonpvpzoneBtn.borderColor = self.buttonBorderColor;
        self:addChild(self.nonpvpzoneBtn);
        y = y + 30;
    end
    self.seeFactionBtn = ISButton:new(10, y, btnWid, btnHgt, getText("IGUI_AdminPanel_SeeFaction") .. " (" .. Faction.getFactions():size() ..")", self, ISAdminPanelUI.onOptionMouseDown);
    self.seeFactionBtn.internal = "SEEFACTIONS";
    self.seeFactionBtn:initialise();
    self.seeFactionBtn:instantiate();
    self.seeFactionBtn.borderColor = self.buttonBorderColor;
    self:addChild(self.seeFactionBtn);
    y = y + 30;
    self.seeSafehousesBtn = ISButton:new(10, y, btnWid, btnHgt, getText("IGUI_AdminPanel_SeeSafehouses") .. " (".. SafeHouse.getSafehouseList():size() .. ")", self, ISAdminPanelUI.onOptionMouseDown);
    self.seeSafehousesBtn.internal = "SEESAFEHOUSES";
    self.seeSafehousesBtn:initialise();
    self.seeSafehousesBtn:instantiate();
    self.seeSafehousesBtn.borderColor = self.buttonBorderColor;
    self:addChild(self.seeSafehousesBtn);
    y = y + 30;
    self.seeTicketsBtn = ISButton:new(10, y, btnWid, btnHgt, getText("IGUI_AdminPanel_SeeTickets"), self, ISAdminPanelUI.onOptionMouseDown);
    self.seeTicketsBtn.internal = "SEETICKETS";
    self.seeTicketsBtn:initialise();
    self.seeTicketsBtn:instantiate();
    self.seeTicketsBtn.borderColor = self.buttonBorderColor;
    self:addChild(self.seeTicketsBtn);
    y = y + 30;
    self.miniScoreboardBtn = ISButton:new(10, y, btnWid, btnHgt, getText("IGUI_AdminPanel_MiniScoreboard"), self, ISAdminPanelUI.onOptionMouseDown);
    self.miniScoreboardBtn.internal = "MINISCOREBOARD";
    self.miniScoreboardBtn:initialise();
    self.miniScoreboardBtn:instantiate();
    self.miniScoreboardBtn.borderColor = self.buttonBorderColor;
    self:addChild(self.miniScoreboardBtn);
    self.miniScoreboardBtn.tooltip = getText("IGUI_AdminPanel_TooltipMiniScoreboard");
    y = y + 30;
    self.cancel = ISButton:new(10, self:getHeight() - padBottom - btnHgt, btnWid, btnHgt, getText("IGUI_Exit"), self, ISAdminPanelUI.onOptionMouseDown);
    self.cancel.internal = "CANCEL";
    self.cancel:initialise();
    self.cancel:instantiate();
    self.cancel.borderColor = self.buttonBorderColor;
    self:addChild(self.cancel);
    self:updateButtons();
end

function ISAdminPanelUI:updateButtons()
    local enabled = false;
    if getAccessLevel() ~= "" then
        enabled = true;
    end
    self.checkStatsBtn.enable = enabled;
    self.seeOptionsBtn.enable = enabled;
    self.seeFactionBtn.enable = enabled;
    self.seeSafehousesBtn.enable = enabled;
    self.seeTicketsBtn.enable = enabled;
    self.miniScoreboardBtn.enable = enabled;
    self.buildCheatBtn.enable = enabled;
end