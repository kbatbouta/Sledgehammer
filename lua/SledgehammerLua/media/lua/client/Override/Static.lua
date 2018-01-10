require "ISUI/ISChat"

function static()
	-- Disables legacy chat.
	ISChat.createChat = function() end
end

function staticStart()
	disableLegacyChat();
end

function disableLegacyChat()
	-- Removes legacy chat from UI update.
	ISChat.chat:removeFromUIManager();
	-- Hides legacy chat.
	ISChat.chat:setVisible(false);
	ISChat.instance.moreinfo:setVisible(false);
    ISChat.instance.chatText:setVisible(false);
	-- Removes legacy chat event hooks.
	Events.OnWorldMessage.Remove(ISChat.addLineInChat);
    Events.OnMouseDown.Remove(ISChat.unfocus);
    Events.OnKeyPressed.Remove(ISChat.onToggleChatBox);
    Events.OnKeyKeepPressed.Remove(ISChat.onKeyKeepPressed);
end

Events.OnInitWorld.Add(static);
Events.OnGameStart.Add(staticStart);
