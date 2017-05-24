package sledgehammer.objects.chat;

import java.util.ArrayList;
import java.util.List;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaArray;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.Player;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public class Command extends LuaTable {
	private String command;
	private String[] args = new String[0];
	private String raw = null;
	private Player player;
	private String channel;
	
	public Command(String raw) {
		super("Command");
		parse(raw);
	}
	
	public Command(String command, String[] args) {
		super("Command");
		this.command = command;
		if(args != null) {			
			this.args = args;
		}
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Command(KahluaTable table) {
		super("Command", table);
		println("Raw: " + getRaw());
	}
	
	public String getCommand() {
		return command;
	}
	
	public String[] getArguments() {
		return args;
	}
	
	public String getRaw() {
		if(raw == null) {
			raw = "/" + command;
			for(String arg: args) {
				if(arg.contains(" ")) {
					raw += " \"" + arg.trim() + "\"";
				} else {					
					raw += " " + arg.trim();
				}
			}
		}
		return raw;
	}
	
	public void parse(String raw) {

		//FIXME: Args not properly parsing.
//		command = new String(raw).trim().split(" ")[0].toLowerCase();
//		this.args = getArguments(command, raw);
//		
//		raw = "/" + command;
//		for(String arg: args) {
//			if(arg.contains(" ")) {
//				raw += " \"" + arg.trim() + "\"";
//			} else {					
//				raw += " " + arg.trim();
//			}
//		}
	}
	
	public boolean hasArguments() {
		return this.args != null && this.args.length > 0;
	}
	
	public String getArgumentsAsString() {
		if(getArguments().length == 0) return null;
		String raw = getRaw();
		return raw.substring(command.length() + 2, raw.length());
	}
	
	public void debugPrint() {
		println("Command: " + getCommand());
		println("Arguments: " + args.length);
		for(int index = 0; index < args.length; index++) {
			String arg = args[index];
			println("\t[" + index + "]: " + arg);
		}
		println("Raw: " + getRaw());
		println();
	}
	
	public String toString() {
		return getRaw();
	}
	
	@Override
	public void onLoad(KahluaTable table) {
		Object raw = table.rawget("raw");
		println("Object-raw: " + raw);
		
		Object channel = table.rawget("channel");
		if(channel != null) {
			setChannel(channel.toString());
		}
		
		parse(raw.toString());
	}

	@Override
	public void onExport() {
		set("raw", raw);
		set("command", command);
		set("args", new LuaArray<String>(args));
		set("player", getPlayer());
		set("channel", getChannel());
	}
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	

	public static String[] getArguments(String command, String input) {
		List<String> argCache = new ArrayList<>();
		String[] args = null;
		String argCurrent = "";
		boolean inQuotes = false;
		char quoteType = '"';
		char[] chars = input.toCharArray();
		for(int x = 0; x < chars.length; x++) {
			char c = chars[x];
			if(inQuotes) {
				if(c == quoteType) {
					argCache.add(argCurrent);
					argCurrent = "";
					inQuotes = false;
					continue;
				} else argCurrent += c;
			} else {
				if(c == '\"') {
					inQuotes = true;
					continue;
				} else
				if(c == ' ') {
					if(!argCurrent.isEmpty()) {
						argCache.add(argCurrent);
						argCurrent = "";
						continue;						
					}
				} else argCurrent += c;
			}
		}
		if(!argCurrent.isEmpty()) {
			argCache.add(argCurrent);
		}
		if(argCache.size() > 0) {
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
	
	public static String[] subArguments(String[] args, int i) {
		
		if(i < 0 && i < args.length) {
			throw new IllegalArgumentException("I must be 0 or greater, and less than the argument.length.");
		}
		
		if(args == null || args.length < 1) {
			throw new IllegalArgumentException("Args array is invalid!");
		}
		
		String[] newArgs = new String[args.length - i];
		for(int x = i; x < args.length; x++) {
			newArgs[x - i] = args[x];
		}
		return newArgs;
	}
	
	public static void main(String[] args) {
		new Command("/test1 arg1 arg2").debugPrint();
		new Command("/test2 arg1 \"args and stuff\"").debugPrint();
		new Command("test3", null).debugPrint();
		new Command("test4", new String[] {"arg1", "arg2"}).debugPrint();
	}
}
