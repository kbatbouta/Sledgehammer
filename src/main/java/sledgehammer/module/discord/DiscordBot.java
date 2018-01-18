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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.plugin.Module;
import sledgehammer.util.ChatTags;
import sledgehammer.util.Printable;
import sledgehammer.util.ZUtil;
import zombie.network.GameServer;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

/**
 * Class designed to handle JavaCord API executions for the ModuleDiscord
 * module.
 *
 * @author Jab
 */
public class DiscordBot extends Printable implements FutureCallback<DiscordAPI>, MessageCreateListener {

    public static final String MONOSPACE_START = "`\n";
    public static final String MONOSPACE_END = "\n`";

    /**
     * The Module instance using this DiscordBot instance.
     */
    private ModuleDiscord module;
    /**
     * API instance for JavaCord.
     */
    private DiscordAPI api;
    private DiscordMessageDispatcher dispatcher;
    private Map<Channel, List<String>> mapMessageQueue;
    private List<Channel> listPublicChannels;
    private Channel channelConsole;
    /**
     * Flag for noting whether or not the bot connected using a stored token.
     */
    private boolean connectedUsingToken = false;
    private boolean connected = false;
    private boolean inCommand;
    private Server server;

    public DiscordBot(ModuleDiscord instance) {
        module = instance;
        listPublicChannels = new ArrayList<>();
    }

    /**
     * Attempts to establish a connection to the Discord Services, using a given
     * email address and a password.
     */
    public void connect(String emailAddress, String password, String token) {
        // Grab the API.
        api = Javacord.getApi();
        connectedUsingToken = true;
        api.setToken(token, true);
        // Point to self as the Callback handler.
        api.connect(this);
    }

    /**
     * Connects the bot to Discord using a generated token from a previous
     * connection.
     */
    public void connect(String token) {
        // Flag the bot for connecting with a stored token.
        connectedUsingToken = true;
        // Grab the API.
        api = Javacord.getApi(token, true);
        // Point to self as the Callback handler.
        api.connect(this);
    }

    public Channel createChannelIfNotExists(String name) {
        Channel channel = getChannel(name);
        if (channel == null) {
            try {
                println(name);
                channel = getServer().createChannel(name).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return channel;
    }

    public void disconnect() {
        // Stops the dispatcher.
        if (dispatcher != null && dispatcher.isAlive()) {
            dispatcher.setInactive();
        }
        api.disconnect();
    }

    public static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static String toAsciiString(String string) {
        return deAccent(string).replaceAll("[^\\x00-\\x7F]", "");
    }

    public void registerChannels() {
        Server server = getServer();
        Collection<ChatChannel> channels = module.getChatModule().getChatChannels();
        for (ChatChannel channel : channels) {
            String name = channel.getChannelName();
            // TODO: Find a way to work around the JDK8 limitation with JavaCord 3.0 not being able to support categories.
            if (name.toLowerCase().contains("faction")) {
                continue;
            }
            // Only register public channels.
            if (!channel.isPublicChannel()) {
                continue;
            }
            String ascii = toAsciiString("channel_" + name);
            Channel dChannel = getChannel(ascii);
            if (dChannel == null) {
                dChannel = createChannelIfNotExists(ascii);
            }
            listPublicChannels.add(dChannel);
        }
    }

    public Channel getChannel(String channelName) {
        for (Channel channel : getServer().getChannels()) {
            if (channel.getName().equalsIgnoreCase(channelName)) {
                return channel;
            }
        }
        return null;
    }

    public Server getServer() {
        if (server == null) {
            server = api.getServers().iterator().next();
        }
        return server;
    }

    /**
     * Executes when successfully connecting to Discord services.
     */
    public void onSuccess(DiscordAPI api) {

        ModuleDiscord module = getModule();
        DiscordSettings settings = module.getSettings();
        String inviteURL = settings.getInviteURL();
        if (inviteURL == null || inviteURL.isEmpty()) {
            // Create a URL to open, based on the ID.
            String url = "https://discordapp.com/oauth2/authorize?client_id=<APPLICATION_ID_HERE>&scope=bot&permissions=0";
            println("In order to add this bot to your server, place your application's ID in this URL: ");
            println(url);
            if (api.getServers().isEmpty()) {
                return;
            }
            this.server = api.getServers().iterator().next();
            registerChannels();
        }
        if (ModuleDiscord.DEBUG) {
            println("Dispatcher started.");
        }
        // Initialize the Message queue map.
        mapMessageQueue = new HashMap<>();
        discoverChannels();
        for (ChatChannel channel : module.getChatModule().getChatChannels()) {
            if (channel.isPublicChannel() && channel.isGlobalChannel() && channel.canSpeak()) {
                String name = toAsciiString("channel_" + channel.getChannelName());
                createChannelIfNotExists(name);
            }
        }
        if (ModuleDiscord.DEBUG) {
            println("MessageQueue created.");
        }
        // Set connection flag to true.
        connected = true;
        // Initialize and run the Dispatcher.
        dispatcher = new DiscordMessageDispatcher(this);
        dispatcher.start();
        api.registerListener(this);
    }

    public void discoverChannels() {
        String channelConsole = module.getConsoleChannelName();
        for (Server server : api.getServers()) {
            for (Channel channel : server.getChannels()) {
                if (channel.getName().equalsIgnoreCase(channelConsole)) {
                    this.channelConsole = channel;
                    return;
                }
            }
        }
    }

    /**
     * Executes when failing to connect to Discord services.
     */
    public void onFailure(Throwable throwable) {
        // TODO: SLAM-14: Handle failure.
        connected = false;
        stackTrace(throwable);
    }

    /**
     * @return Returns whether or not the DiscordBot instance connected using a stored
     * token.
     */
    boolean connectedUsingToken() {
        return connectedUsingToken;
    }

    public synchronized void say(String channelName, boolean alert, String... messages) {
        if (server == null) {
            return;
        }
        if (mapMessageQueue == null) {
            mapMessageQueue = new HashMap<>();
        }
        Channel channel = getChannel(channelName);
        if (channel == null) {
            if (ModuleDiscord.DEBUG) {
                println("Channel given is null!");
                return;
            }
        }
        List<String> channelQueue = mapMessageQueue.get(channel);
        if (channelQueue == null) {
            channelQueue = new ArrayList<>();
            mapMessageQueue.put(channel, channelQueue);
        }
        String timeInternal = "[" + ZUtil.getHourMinuteSeconds() + "]";
        StringBuilder builtString = new StringBuilder();
        for (String message : messages) {
            if (message == null) {
                continue;
            }
            if (builtString.length() + timeInternal.length() + message.length() > 2000) {
                channelQueue.add(timeInternal + " " + builtString);
                builtString = new StringBuilder(message);
            } else {
                if (builtString.length() == 0) {
                    builtString.append(message);
                } else {
                    builtString.append("\n").append(message);
                }
            }
        }
        if (builtString.length() > 0) {
            String s = MONOSPACE_START + timeInternal + " " + builtString + MONOSPACE_END
                    + (alert ? " @here" : "");
            channelQueue.add(s);
        }
        // Compact the list to prevent local chat flooding.
        if (channelQueue.size() > 5) {
            ZUtil.compactList(channelQueue);
        }
    }

    public void warn(boolean alert, String string) {
        if (string == null || string.isEmpty())
            return;
        say(module.getSettings().getModeratorChannel(), alert, "[WARNING]: " + string);
    }

    public void info(boolean alert, String string) {
        if (string == null || string.isEmpty())
            return;
        say(module.getSettings().getModeratorChannel(), alert, "[INFO]: " + string);
    }

    public void staff(String staffMember, String string) {
        if (string == null || string.isEmpty())
            return;
        say(module.getSettings().getModeratorChannel(), false, "[STAFF][" + staffMember + "]: " + string);
    }

    /**
     * @return Returns the ModuleDiscord instance using this DiscordBot instance.
     */
    ModuleDiscord getModule() {
        return module;
    }

    /**
     * Executed every time the bot receives a message.
     */
    public void onMessageCreate(DiscordAPI api, final Message message) {
        // Grab the user who sent the message.
        User user = message.getAuthor();
        // Avoid Bot feedback.
        if (api.getYourself() == user) {
            return;
        }
        // Grab the username of the user.
        String username = user.getName();
        // Grab the nickname of the user.
        // POSSIBLE DEPRECATION: user.getNick(server);
        String nick = user.getName();
        // If the nickname is valid, set the username to the nickname.
        if (nick != null && !nick.isEmpty()) {
            username = nick;
        }
        // Grab the Channel the message was sent to.
        Channel channel = message.getChannelReceiver();
        // Grab the Channel's name.
        String channelName = channel.getName();
        // Grab the content of the message.
        String content = message.getContent();
        // Grab any mentions from the message.
        List<User> listMentions = message.getMentions();
        for (User mUser : listMentions) {
            String mUsername = mUser.getName();
            // POSSIBLE DEPRECATION: user.getNick(server);
            String mNick = mUser.getName();
            if (mNick != null && !mNick.isEmpty()) {
                mUsername = mNick;
            }
            String s = " " + ChatTags.COLOR_LIGHT_BLUE + " @" + mUsername + " " + ChatTags.COLOR_WHITE;
            content = content.replace("<@" + mUser.getId() + ">", s);
            content = content.replace("<@!" + mUser.getId() + ">", s);
        }
        String output = content;
        if (!output.isEmpty()) {
            Module module = getModule();
            // Debug print this message.
            if (ModuleDiscord.DEBUG) {
                println("[DISCORD] -> #" + channelName + ": " + username + ": " + COLOR_WHITE + " " + output);
            }
            if (channelName.startsWith("channel_")) {
                channelName = channelName.split("channel_")[1];
            }
            ChatChannel c = module.getChatChannel(channelName);
            if (c != null) {
                ChatMessage chatMessage = module.createChatMessage(username + " : " + output);
                chatMessage.setOrigin(ChatMessage.ORIGIN_DISCORD, false);
                chatMessage.setChannelId(c.getUniqueId(), false);
                chatMessage.save();
                c.addChatMessage(chatMessage);
            }
        }
        if (channelName.equals(getConsoleChannel().getName())) {
            if (content.startsWith("!")) {
                if (content.startsWith("!z")) {
                    try {
                        onZCommand(username, content);
                    } catch (Exception e) {
                        //
                    }
                } else {
                    content = content.substring(1, content.length());
                    String[] args_ = content.split(" ");
                    String[] args = new String[args_.length - 1];
                    try {
                        System.arraycopy(args, 1, args, 0, args_.length - 1);
                    } catch (Exception e) {
                        args = new String[0];
                    }
                    onCommand(username, args_[0].toLowerCase(), args);
                }
            }
        }
    }

    private void onCommand(String sender, String command, String[] args) {
        if (command.equalsIgnoreCase("about")) {
            info(false, sender + ": (Discord-Bot V" + module.getVersion() + ")");
            info(false, "This bot is hosted by " + module.getPublicServerName() + ".");
        } else if (command.equalsIgnoreCase("online")) {
            info(false, sender + ": " + GameServer.getPlayerCount() + " players are online.");
        }
    }

    private void onZCommand(String sender, String message) {
        String command = message.split("!z")[1].trim();
        inCommand = true;
        String response = GameServer.handleServerCommand(command, null);
        String[] split = response.split("\n");
        say(module.getSettings().getModeratorChannel(), false, split);
        inCommand = false;
    }

    /**
     * @param user The Discord User.
     * @return Returns the in-chat mention tag for a User.
     */
    public static String getMentionTag(User user) {
        return "@" + user.getName() + "#" + user.getDiscriminator();
    }

    public boolean isConnected() {
        return connected;
    }

    public Map<Channel, List<String>> getQueue() {
        return mapMessageQueue;
    }

    public Channel getConsoleChannel() {
        return channelConsole;
    }

    public DiscordAPI getAPI() {
        return api;
    }

    public boolean inCommand() {
        return inCommand;
    }

    public DiscordMessageDispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public String getName() {
        return "DiscordBot";
    }
}