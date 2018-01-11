function ISUserPanelUI:create()
    local btnWid = 150
    local btnHgt = 25
    local padBottom = 10
    local y = 50;
    self.factionBtn = ISButton:new(10, y, btnWid, btnHgt, getText("UI_userpanel_factionpanel"), self, ISUserPanelUI.onOptionMouseDown);
    self.factionBtn.internal = "FACTIONPANEL";
    self.factionBtn:initialise();
    self.factionBtn:instantiate();
    self.factionBtn.borderColor = self.buttonBorderColor;
    self:addChild(self.factionBtn);
    y = y + 30;
    self.ticketsBtn = ISButton:new(10, y, btnWid, btnHgt, getText("UI_userpanel_tickets"), self, ISUserPanelUI.onOptionMouseDown);
    self.ticketsBtn.internal = "TICKETS";
    self.ticketsBtn:initialise();
    self.ticketsBtn:instantiate();
    self.ticketsBtn.borderColor = self.buttonBorderColor;
    self:addChild(self.ticketsBtn);
    y = y + 30;
    if not Faction.isAlreadyInFaction(self.player) then
        self.factionBtn.title = getText("IGUI_FactionUI_CreateFaction");
        if not Faction.canCreateFaction(self.player) then
            self.factionBtn.enable = false;
            self.factionBtn.tooltip = getText("IGUI_FactionUI_FactionSurvivalDay", getServerOptions():getInteger("FactionDaySurvivedToCreate"));
        end
    end
    self.cancel = ISButton:new(10, self:getHeight() - padBottom - btnHgt, btnWid, btnHgt, getText("IGUI_Exit"), self, ISUserPanelUI.onOptionMouseDown);
    self.cancel.internal = "CANCEL";
    self.cancel:initialise();
    self.cancel:instantiate();
    self.cancel.borderColor = self.buttonBorderColor;
    self:addChild(self.cancel);
end

function ISUserPanelUI:updateButtons()
    if not Faction.isAlreadyInFaction(self.player) then
        self.factionBtn.title = getText("IGUI_FactionUI_CreateFaction");
        if not Faction.canCreateFaction(self.player) then
            self.factionBtn.enable = false;
            self.factionBtn.tooltip = getText("IGUI_FactionUI_FactionSurvivalDay", getServerOptions():getInteger("FactionDaySurvivedToCreate"));
        end
    else
        self.factionBtn.title = getText("UI_userpanel_factionpanel");
    end
end