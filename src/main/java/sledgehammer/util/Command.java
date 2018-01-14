/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.lua.LuaArray;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.core.Player;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class Command extends LuaTable {
    private String command;
    private String[] args = new String[0];
    private String raw = null;
    private Player player;
    private UUID channelId;

    public Command(String raw) {
        super("Command");
        parse(raw);
    }

    public Command(String command, String[] args) {
        super("Command");
        this.command = command;
        if (args != null) {
            this.args = args;
        }
    }

    /**
     * Lua load constructor.
     *
     * @param table The KahluaTable to set the data.
     */
    public Command(KahluaTable table) {
        super("Command", table);
    }

    @Override
    public void onLoad(KahluaTable table) {
        Object raw = table.rawget("raw");
        Object oChannelId = table.rawget("channel_id");
        if (oChannelId != null) {
            setChannelId(UUID.fromString(oChannelId.toString()));
        }
        parse(raw.toString());
    }

    @Override
    public void onExport() {
        LuaArray<String> args = new LuaArray<>(getArguments());
        // @formatter:off
	    set("raw"       , getRaw()      );
		set("command"   , getCommand()  );
		set("args"      , args          );
		set("player"    , getPlayer()   );
		set("channel_id", getChannelId());
		// @formatter:on
    }

    @Override
    public String toString() {
        return "(" + getPlayer() + ") " + getRaw();
    }

    public String getRaw() {
        if (raw == null) {
            StringBuilder rawBuilder = new StringBuilder("/");
            rawBuilder.append(command);
            for (String arg : args) {
                if (arg.contains(" ")) {
                    rawBuilder.append(" \"");
                    rawBuilder.append(arg.trim());
                    rawBuilder.append("\"");
                } else {
                    rawBuilder.append(" ");
                    rawBuilder.append(arg.trim());
                }
            }
            raw = rawBuilder.toString();
        }
        return raw;
    }

    public void parse(String raw) {
        command = raw.replace("/", "").replace("!", "").trim().split(" ")[0].toLowerCase();
        args = tryAgain(raw);
        this.raw = raw;
    }

    public String getArgumentsAsString() {
        if (getArguments().length == 0)
            return null;
        String raw = getRaw();
        return raw.substring(command.length() + 2, raw.length());
    }

    public void debugPrint() {
        println("Command: " + getCommand());
        println("Arguments: " + args.length);
        for (int index = 0; index < args.length; index++) {
            String arg = args[index];
            println("\t[" + index + "]: " + arg);
        }
        println("Raw: " + getRaw());
        println();
    }

    public boolean hasArguments() {
        return this.args != null && this.args.length > 0;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArguments() {
        return args;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public UUID getChannelId() {
        return this.channelId;
    }

    public void setChannelId(UUID channelId) {
        this.channelId = channelId;
    }

    public static String[] tryAgain(String input) {
        char[] chars = input.toCharArray();
        List<String> args = new LinkedList<>();
        StringBuilder arg = new StringBuilder();
        boolean in = false;
        for (char c : chars) {
            switch (c) {
                case ' ':
                    if (!in) {
                        args.add(arg.toString());
                        arg = new StringBuilder();
                    } else {
                        arg.append(c);
                    }
                    break;
                case '\"':
                    in = !in;
                    break;
                default:
                    arg.append(c);
                    break;
            }
        }
        if (arg.length() > 0) {
            args.add(arg.toString());
        }
        String[] ret = new String[args.size() - 1];
        for (int index = 1; index < args.size(); index++) {
            ret[index - 1] = args.get(index);
        }
        return ret;
    }

    public static String[] getArguments(String command, String input) {
        List<String> argCache = new ArrayList<>();
        String[] args;
        StringBuilder argCurrent = new StringBuilder();
        boolean inQuotes = false;
        char quoteType = '"';
        char[] chars = input.toCharArray();
        for (char c : chars) {
            if (inQuotes) {
                if (c == quoteType) {
                    argCache.add(argCurrent.toString());
                    argCurrent = new StringBuilder();
                    inQuotes = false;
                } else
                    argCurrent.append(c);
            } else {
                switch (c) {
                    case '\"':
                        inQuotes = true;
                        break;
                    case ' ':
                        if (argCurrent.length() > 0) {
                            argCache.add(argCurrent.toString());
                            argCurrent = new StringBuilder();
                        }
                        break;
                    default:
                        argCurrent.append(c);
                        break;
                }
            }
        }
        if (argCurrent.length() > 0) {
            argCache.add(argCurrent.toString());
        }
        if (argCache.size() > 0) {
            String firstArg = argCache.get(0).toLowerCase();
            if (firstArg.contains(command.toLowerCase())) {
                args = new String[argCache.size() - 1];
                for (int x = 1; x < argCache.size(); x++) {
                    args[x - 1] = argCache.get(x);
                }
            } else {
                args = new String[argCache.size()];
                for (int x = 0; x < argCache.size(); x++) {
                    args[x] = argCache.get(x);
                }
            }
        } else {
            args = new String[0];
        }
        return args;
    }

    public static String[] getSubArgs(String[] args, int index) {
        if (args == null) {
            throw new IllegalArgumentException("Arguments Array provided is null.");
        }
        if (args.length == 0) {
            throw new IllegalArgumentException("Arguments Array provided is empty.");
        }
        if (args.length - index < 0) {
            throw new IllegalArgumentException("index given to start is beyond the last index of the arguments Array provided.");
        }
        String[] ret = new String[args.length - index];
        System.arraycopy(args, index, ret, 0, args.length - index);
        return ret;
    }

    public static void main(String[] args) {
        new Command("/test1 arg1 arg2").debugPrint();
        new Command("/test2 arg1 \"args and stuff\"").debugPrint();
        new Command("test3", null).debugPrint();
        new Command("test4", new String[]{"arg1", "arg2"}).debugPrint();
    }

    public static String combineArguments(String[] args, int index) {
        if (args == null) {
            throw new IllegalArgumentException("Arguments array given is null.");
        }
        if (args.length == 0) {
            System.err.println("WARNING: Arguments given is empty for argument combination. Returning as an empty string.");
            SledgeHammer.instance.stackTrace();
            return "";
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index cannot be a negative value.");
        }
        if (args.length <= index) {
            throw new IndexOutOfBoundsException("Index provided is larger or equal to the length of the arguments array given.");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = index; i < args.length; i++) {
            builder.append(args[i]);
        }
        return builder.toString().trim();
    }
}