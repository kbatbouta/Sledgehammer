package sledgehammer.lua.discord.request;


import sledgehammer.lua.LuaTable;
import sledgehammer.lua.discord.DiscordInformation;

public class RequestDiscordInformation extends LuaTable {

    private DiscordInformation information;

    /**
     * Main constructor.
     */
    public RequestDiscordInformation() {
        super("requestDiscordInformation");
    }

    @Override
    public void onExport() {
        set("info", getInfo());
    }

    public DiscordInformation getInfo() {
        return this.information;
    }

    public void setInfo(DiscordInformation information) {
        this.information = information;
    }



}
