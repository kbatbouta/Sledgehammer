/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.module.discord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import sledgehammer.util.YamlUtil;

/**
 * TODO: Document.
 *
 * @author Jab
 */
@SuppressWarnings({"rawtypes"})
public class DiscordSettings {

    private ModuleDiscord module;
    private Map map;
    private File file;
    private boolean debug = false;
    private String token;
    private String inviteURL;
    private String channelPublic;
    private String channelModerator;

    /**
     * Main constructor.
     *
     * @param module The <ModuleDiscord> instance using the settings.
     */
    DiscordSettings(ModuleDiscord module) {
        setModule(module);
    }

    public void load() {
        file = new File(module.getModuleDirectory(), "config.yml");
        if (!file.exists()) {
            module.println("The Discord module must be configured to run. The config file is located here: ");
            module.println(file.getAbsolutePath());
            module.saveResourceAs("discord-config.yml", "config.yml", false);
            module.unload();
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            map = YamlUtil.getYaml().load(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Boolean debug = (Boolean) map.get("debug");
        if (debug != null) {
            this.debug = debug;
        }
        String token = (String) map.get("token");
        if (token != null) {
            this.token = token;
        }
        String inviteURL = (String) map.get("invite_url");
        if (inviteURL != null) {
            if(!inviteURL.equals("www.discordapp.com") && !inviteURL.toLowerCase().startsWith("https://discord.gg/")) {
                module.errln("ERROR: The Invite URL link provided is not a valid Discord link. "
                        + "Make sure the link is valid, and is a discord invite link. E.G: https://discord.gg/#####");
                this.inviteURL = "www.discordapp.com";
            } else {
                this.inviteURL = inviteURL;
            }
        }
        String channelPublic = (String) map.get("public_channel");
        if (channelPublic != null) {
            this.channelPublic = channelPublic;
        }
        String channelModerator = (String) map.get("moderator_channel");
        if (channelModerator != null) {
            this.channelModerator = channelModerator;
        }
    }

    public boolean isDebug() {
        return this.debug;
    }

    public String getBotAccessToken() {
        return this.token;
    }

    public String getInviteURL() {
        return this.inviteURL;
    }

    public String getPublicChannel() {
        return this.channelPublic;
    }

    public String getModeratorChannel() {
        return this.channelModerator;
    }

    public ModuleDiscord getModule() {
        return this.module;
    }

    private void setModule(ModuleDiscord module) {
        this.module = module;
    }
}