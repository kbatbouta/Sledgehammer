package sledgehammer.objects.chat;

import java.util.ArrayList;
import java.util.List;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaArray;
import sledgehammer.object.LuaTable;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public class Command extends LuaTable {
	private String command;
	private String[] args = new String[0];
	private String raw = null;
	
	public Command(String raw) {
		super("Command");
		parse(raw);
	}
	
	public Command(String command, String[] args) {
		super("Command");
		this.command = command;
		this.args = args;
	}
	
	public Command(KahluaTable table) {
		super("Command", table);
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
		this.raw = raw;
		command = raw.split(" ")[0];
		if(command.startsWith("/")) {			
			command = command.substring(1, command.length());
		}
		this.args = getArguments(command, raw);
	}
	
	public boolean hasArguments() {
		return this.args != null && this.args.length > 0;
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
		Object command = table.rawget("command");
		Object args = table.rawget("args");
		
		if(raw != null && command == null) {			
			parse(raw.toString());
		} else {
			command = table.rawget("command").toString();
			if(args != null) {				
				LuaArray<String> args2 = new LuaArray<>((KahluaTable) args);
				this.args = args2.toArray();
			} else {
				this.args = new String[0];
			}
		}
	}

	@Override
	public void onExport() {
		set("raw", raw);
		set("command", command);
		set("args", new LuaArray<String>(args));
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
	
	public static void main(String[] args) {
		new Command("/test1 arg1 arg2").debugPrint();
		new Command("/test2 arg1 \"args and stuff\"").debugPrint();
		new Command("/test3").debugPrint();
		new Command("test4").debugPrint();
		new Command("test5", new String[] {"arg1", "arg2"}).debugPrint();
	}
}
