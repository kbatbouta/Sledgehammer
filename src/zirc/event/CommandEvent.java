package zirc.event;

import java.util.ArrayList;
import java.util.List;

import zirc.wrapper.Player;

public class CommandEvent extends PlayerEvent {
	
	public static final String ID = "CommandEvent";
	
	private String input;
	private String[] arguments;
	private String command;
	private String loggedMessage;
	private LogEvent.LogType loggedID = LogEvent.LogType.INFO;
	
	private Result result = Result.FAILURE;
	private String response = null;
	private boolean logImportant = false;
	
	public CommandEvent(Player player, String input) {
		super(player);
		this.input = input;
		command = input.replace("/", "").replace("!", "").trim().split(" ")[0].toLowerCase();
	}
	
	public String[] getArguments() {
		if (arguments == null) arguments = getArguments(command, input);
		return arguments;
	}
	
	public String getRaw() {
		return this.input;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public String getResponse() {
		return response;
	}
	
	public Result getResult() {
		return result;
	}
	
	public void setResponse(Result result, String response) {
		this.result = result;
		this.response = response;
		this.setHandled(true);
	}
	
	public LogEvent.LogType getLoggedID() {
		return loggedID;
	}
	
	public String getLoggedMessage() {
		return loggedMessage;
	}
	
	public void setLoggedMessage(LogEvent.LogType loggedID, String logMessage) {
		this.loggedMessage = logMessage;
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
	
	public static enum Result {
		SUCCESS,
		FAILURE;
	}

	public void setLoggedImportant(boolean b) {
		this.logImportant = b;
	}
	
	public boolean getLogImportance() {
		return this.logImportant;
	}

	@Override
	public String getLogMessage() {
		return getLoggedMessage();
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

	@Override
	public String getName() {
		return ID;
	}
	
}
