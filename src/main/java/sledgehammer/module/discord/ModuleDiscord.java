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

import de.btobastian.javacord.entities.Channel;
import sledgehammer.event.core.player.ClientEvent;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.core.send.SendLua;
import sledgehammer.lua.discord.DiscordInformation;
import sledgehammer.lua.discord.request.RequestDiscordInformation;
import sledgehammer.plugin.MongoModule;

/**
 * Module designed to load a Discord bot for SledgeHammer logging, and interfacing purposes.
 *
 * @author Jab
 */
public class ModuleDiscord extends MongoModule {

  /** Static boolean to clarify if the Module is in 'debug mode'. */
  public static boolean DEBUG = true;

  /** The settings instance the Module is using to define and manage its settings. */
  private DiscordSettings settings;

  /** The JavaCord Bot instance. */
  private DiscordBot bot;

  private DiscordEventListener eventListener;
  private DiscordCommandListener commandHandler;
  private String __debugToken;
  private SendLua sendLuaDiscord;
  private DiscordInformation discordInformation;
  private LanguagePackage languagePackage;

  public ModuleDiscord() {
    super(getDefaultDatabase());
  }

  @Override
  public void onLoad() {
    loadLanguagePackage();
    loadLua();
    File directory = getModuleDirectory();
    if (!directory.exists()) {
      directory.mkdirs();
    }
    eventListener = new DiscordEventListener(this);
    commandHandler = new DiscordCommandListener(this);
    register(commandHandler);
  }

  @Override
  public void onStart() {
    // Initialize the Settings Handler.
    settings = new DiscordSettings(this);
    // Load the settings.
    settings.load();
    DEBUG = settings.isDebug();
    // Grab the token from the settings file.
    String token = settings.getBotAccessToken();
    if (token == null || token.isEmpty()) {
      token = __debugToken;
    }
    // Check if token exists.
    if (token == null || token.isEmpty() || token.equals("<TOKEN_HERE>")) {
      println("Token is invalid!");
      stopModule();
      return;
    } else {
      // Initialize the Bot.
      bot = new DiscordBot(this);
      bot.connect(settings.getBotAccessToken());
    }
    register(eventListener);
  }

  @Override
  public void onStop() {
    if (bot != null && bot.isConnected()) {
      bot.disconnect();
    }
    unregister(eventListener);
  }

  @Override
  public void onUnload() {
    unregister(commandHandler);
  }

  @Override
  public void onBuildLua(SendLua send) {
    send.append(sendLuaDiscord);
  }

  @Override
  public void onClientCommand(final ClientEvent event) {
    String clientCommand = event.getCommand();
    if (clientCommand.equalsIgnoreCase("requestInformation")) {
      RequestDiscordInformation request = new RequestDiscordInformation();
      if (discordInformation == null) {
        discordInformation = new DiscordInformation();
        try {
          discordInformation.setDiscordName(bot.getServer().getName());
        } catch (Exception e) {
          discordInformation.setDiscordName("The Discord Server");
        }
        discordInformation.setInviteURL(settings.getInviteURL());
      }
      request.setInfo(discordInformation);
      event.respond(request);
    }
  }

  private void loadLanguagePackage() {
    File langDir = getLanguageDirectory();
    boolean override = !isLangOverriden();
    saveResourceAs("lang/discord_en.yml", new File(langDir, "discord_en.yml"), override);
    languagePackage = new LanguagePackage(getLanguageDirectory(), "discord");
    languagePackage.load();
  }

  private void loadLua() {
    File lua = getLuaDirectory();
    boolean overwrite = !isLuaOverriden();
    File fileDiscordModule = new File(lua, "ModuleDiscord.lua");
    saveResourceAs("lua/module/core.discord/ModuleDiscord.lua", fileDiscordModule, overwrite);
    // Make sure that the core language file(s) are provided.
    sendLuaDiscord = new SendLua(fileDiscordModule);
  }

  public void broadcast(String channelName, String text) {
    // messageGlobal(message);
    ChatChannel channel = getChatModule().getChatChannel(channelName);
    if (channel == null) {
      println("Channel does not exist: " + channelName + ".");
      return;
    }
    if (text == null || text.isEmpty()) {
      println("Text is invalid.");
      return;
    }
    ChatMessage message = createChatMessage(text);
    // TODO: Implement. ?
  }

  public String getPublicChannelName() {
    return "channel_global";
  }

  public String getConsoleChannelName() {
    return "console";
  }

  public DiscordBot getBot() {
    return bot;
  }

  public String getToken() {
    return settings.getBotAccessToken();
  }

  public DiscordSettings getSettings() {
    return settings;
  }

  public void setDebugToken(String string) {
    this.__debugToken = string;
  }

  public Channel getConsoleChannel() {
    return getBot().getConsoleChannel();
  }

  public LanguagePackage getLanguagePackage() {
    return this.languagePackage;
  }
}
