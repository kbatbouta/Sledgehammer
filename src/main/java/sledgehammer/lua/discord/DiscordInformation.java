package sledgehammer.lua.discord;

import sledgehammer.lua.LuaTable;

public class DiscordInformation extends LuaTable {

    private String inviteURL = "www.discordapp.com";
    private String discordName = "A Discord Server";

    public DiscordInformation() {
        super("DiscordInformation");
    }

    @Override
    public void onExport() {
        // @formatter:off
        set("discord_name", getDiscordName());
        set("invite_url"  , getInviteURL()  );
        // @formatter:on
    }

    public String getInviteURL() {
        return this.inviteURL;
    }

    public void setInviteURL(String inviteURL) {
        this.inviteURL = inviteURL;
    }

    public String getDiscordName() {
        return this.discordName;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

}
