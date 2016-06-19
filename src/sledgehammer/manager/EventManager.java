package sledgehammer.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ChatEvent;
import sledgehammer.event.CommandEvent;
import sledgehammer.event.Event;
import sledgehammer.event.LogEvent;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.interfaces.ExceptionListener;
import sledgehammer.interfaces.LogListener;
import sledgehammer.modules.core.CoreCommandListener;
import sledgehammer.util.ChatTags;
import sledgehammer.util.Printable;
import sledgehammer.util.Result;
import sledgehammer.wrapper.Player;
import zombie.core.logger.LoggerManager;
import zombie.core.raknet.UdpConnection;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

/**
 * Manager class designed to organize EventListeners and execution of Events. 
 * 
 * @author Jab
 */
public class EventManager extends Printable {

	/**
	 * Instance of SledgeHammer. While this is statically accessible through the
	 * singleton, maintaining an OOP hierarchy is a good practice.
	 */
	private SledgeHammer sledgeHammer = null;

	/**
	 * Map for registered EventListener interfaces.
	 */
	private Map<String, List<EventListener>> mapEventListeners;

	/**
	 * Map for registered CommandListener interfaces.
	 */
	private Map<String, List<CommandListener>> mapCommandListeners;

	/**
	 * List for registered LogListener interfaces.
	 */
	private List<LogListener> listLogListeners;
	
	/**
	 * List for registered ExceptionListneer interfaces.
	 */
	private List<ExceptionListener> listExceptionListeners;

	/**
	 * Main constructor.
	 * 
	 * @param instance
	 */
	public EventManager(SledgeHammer instance) {
		
		// Set the SledgeHammer instance given.
		sledgeHammer = instance;
		
		// Initialize Maps.
		mapEventListeners = new HashMap<>();
		mapCommandListeners = new HashMap<>();

		// Initialize Lists.
		listLogListeners = new ArrayList<>();
		listExceptionListeners = new ArrayList<>();

		// Put a wild-card List for the CommandListener interface Map.
		mapCommandListeners.put("*", new ArrayList<CommandListener>());
	}
	
	// TODO: Permission Integration.
	// TODO: Clean up.
	/**
	 * Method executing the '/help' command.
	 * 
	 * @param command
	 */
	private void help(CommandEvent command) {

		Player player = command.getPlayer();

		String response = "Commands: " + NEW_LINE + " " + COLOR_WHITE + " ";

		for (List<CommandListener> listListeners : mapCommandListeners.values()) {
			if (listListeners != null) {
				for (CommandListener listener : listListeners) {
					if (listener != null) {
						String[] commands = listener.getCommands();
						if (commands != null) {
							for (String com : listener.getCommands()) {
								if (com != null) {
									String tip = listener.onTooltip(player, com.toLowerCase());
									if (tip != null) {
										response += COLOR_LIGHT_GREEN + " " + com + ": " + COLOR_WHITE + " "
												+ listener.onTooltip(player, com.toLowerCase()) + COLOR_WHITE + " "
												+ NEW_LINE + " " + NEW_LINE + " ";
									}
								}
							}
						}
					}
				}
			}
		}

		CoreCommandListener coreCommandListener = sledgeHammer.getModuleManager().getCoreModule().getCommandListener();

		if (coreCommandListener != null) {
			String[] commands = coreCommandListener.getCommands();
			if (commands != null) {
				for (String com : coreCommandListener.getCommands()) {
					if (com != null) {
						String tip = coreCommandListener.onTooltip(player, com.toLowerCase());
						if (tip != null) {
							response += COLOR_LIGHT_GREEN + " " + com + ": " + COLOR_WHITE + " "
									+ coreCommandListener.onTooltip(player, com.toLowerCase()) + COLOR_WHITE + " "
									+ NEW_LINE + " " + NEW_LINE + " ";
						}
					}
				}
			}
		}

		CommandListener vanillaListener = sledgeHammer.getModuleManager().getVanillaModule().getCommandListener();
		if (vanillaListener != null) {
			String[] commands = vanillaListener.getCommands();
			if (commands != null) {
				for (String com : vanillaListener.getCommands()) {
					if (com != null) {
						String tip = vanillaListener.onTooltip(player, com.toLowerCase());
						if (tip != null) {
							response += COLOR_LIGHT_GREEN + " " + com + ": " + COLOR_WHITE + " "
									+ vanillaListener.onTooltip(player, com.toLowerCase()) + COLOR_WHITE + " "
									+ NEW_LINE + " " + NEW_LINE + " ";
						}
					}
				}
			}
		}

		command.setResponse(Result.SUCCESS, response);
	}

	/**
	 * Executes EventListeners from a given Event instance. 
	 * 
	 * This method is a simplified version of:
	 * <code> handleEvent(event, true); </code>, 
	 * 
	 * The Event is logged.
	 * 
	 * @param event
	 * 
	 * @return
	 */
	public Event handleEvent(Event event) {
		return handleEvent(event, true);
	}

	/**
	 * Executes EventListeners from a given Event instance. Logging is optional.
	 * 
	 * @param event
	 * 
	 * @param logEvent
	 * 
	 * @return
	 */
	public Event handleEvent(Event event, boolean logEvent) {
		try {
			if (event == null)
				throw new IllegalArgumentException("Event is null!");

			if (event.getID() == CommandEvent.ID) {
				return (handleCommand((CommandEvent) event, logEvent));
			}

			List<EventListener> listEventListeners = mapEventListeners.get(event.getID());
			if (listEventListeners != null) {
				for (EventListener listener : listEventListeners) {
					listener.handleEvent(event);
					if (event.canceled())
						return event;
					if (event.handled())
						break;
				}
			}

			// If the Event is set to canceled, return.
			if (event.canceled())
				return event;

			// Force Core Event-handling to be last, for modification potential.
			if (!event.handled()) {
				sledgeHammer.getModuleManager().getCoreModule().getEventListener().handleEvent(event);
			}

			// If the Event is set to canceled, return before logging it.
			if (event.canceled())
				return event;

			// Log the Event.
			if (logEvent)
				logEvent(event);

		} catch (Exception e) {
			println("Error handling event " + event + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
		return event;
	}
	
	/**
	 * Handles an Exception, passing it to all registered ExceptionListener's, with a reason String given.
	 * 
	 * @param reason
	 * 
	 * @param throwable
	 */
	public void handleException(String reason, Throwable throwable) {
		for (ExceptionListener listener : listExceptionListeners) {
			if (listener != null) {
				listener.onError(reason, throwable);
			}
		}
	}

	/**
	 * Logs a Event, running through each LogListener interface.
	 * 
	 * @param event
	 */
	public void logEvent(Event event) {
		try {
			// Create a new LogEvent instance for the Event.
			LogEvent logEvent = new LogEvent(event);

			// Go through each LogListener interface and fire it.
			for (LogListener listener : listLogListeners) {
				if (listener != null) {					
					listener.onLogEntry(logEvent);
				}
			}

			String log = "SledgeHammer";

			// Grab the ID of the event.
			String eName = event.getID();

			// For organization purposes.
			if (eName.equalsIgnoreCase(ChatEvent.ID)) {
				log += "-CHAT";
			} else if (eName.equalsIgnoreCase(CommandEvent.ID)) {
				log += "-COMMAND";
			}

			// If important, log as such. Else log normally.
			if (logEvent.isImportant()) {
				LoggerManager.getLogger(log).write(logEvent.getLogMessage(), "IMPORTANT");
			} else {
				LoggerManager.getLogger(log).write(logEvent.getLogMessage());
			}

		} catch (Exception e) {
			println("Error logging event " + event + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
	}

	/**
	 * Handles a command.
	 * 
	 * @param connection
	 * @param input
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input) {
		return handleCommand(connection, input, true);
	}

	/**
	 * Handles a command.
	 * 
	 * @param connection
	 * @param input
	 * @param logEvent
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input, boolean logEvent) {
		Player player = null;

		// Create a Player instance.
		if (connection == null)
			player = new Player();
		else
			player = new Player(connection);

		// Create a CommandEvent.
		CommandEvent c = new CommandEvent(player, input);

		// Fire the CommandEvent handle method, and return its result.
		return handleCommand(c, logEvent);
	}

	/**
	 * Handles a CommandEvent.
	 * 
	 * @param c
	 * @param logEvent
	 * @return
	 */
	private CommandEvent handleCommand(CommandEvent c, boolean logEvent) {
		synchronized (this) {
			try {
				String command = c.getCommand();

				Player player = c.getPlayer();
				if (player.getUsername().equalsIgnoreCase("admin")) {
					if (command.equalsIgnoreCase("emulate")) {
						String[] args = c.getArguments();
						if (args.length > 1) {
							String name = args[0];

							String commandString = c.getRaw();
							commandString = commandString.split(name)[1].trim();

							println("CommandString: " + commandString);

							Player playerEmulated = new Player(name);

							CommandEvent event = new CommandEvent(playerEmulated, commandString);
							handleCommand(event, logEvent);
							event.setResponse(event.getResult(), ChatTags.stripTags(event.getResponse(), true));
							return event;
						} else {
							return c;
						}
					}
				}

				// If '/help' is fired.
				if (command.equalsIgnoreCase("help")) {
					help(c);
					return c;
				}

				// Run through selected command listeners first (Optimization).
				List<CommandListener> listListeners = mapCommandListeners.get(c.getCommand());

				if (listListeners != null) {
					for (CommandListener listener : listListeners) {
						// If the listener is not null, fire the
						// CommandListener.
						if (listener != null)
							listener.onCommand(c);

						// If the listener set the command as handled, break the
						// loop.
						if (c.handled())
							break;
					}
				}

				// Force Vanilla CommandListener to last for modification
				// potential.
				CommandListener vanillaListener = sledgeHammer.getModuleManager().getVanillaModule().getCommandListener();
				if (!c.handled() && vanillaListener != null)
					vanillaListener.onCommand(c);

				CoreCommandListener coreCommandListener = sledgeHammer.getModuleManager().getCoreModule().getCommandListener();

				if (!c.handled() && coreCommandListener != null) {
					coreCommandListener.onCommand(c);
				}

				if (logEvent) {
					// Iterate the log listeners after the command.
					if (c.getLoggedMessage() != null) {
						LogEvent entry = new LogEvent(c);
						for (LogListener listener : listLogListeners) {
							if (listener != null)
								listener.onLogEntry(entry);
						}
					}
				}

				// For console commands, or other methods outside of the game,
				// this strips the color codes, and replaces '<LINE>' with \n.
				if (c.getPlayer().getConnection() == null) {
					c.setResponse(c.getResult(), ChatTags.stripTags(c.getResponse(), true));
				}
			} catch (Exception e) {
				println("Error handling command " + c + ": " + e.getMessage());
				for (StackTraceElement o : e.getStackTrace()) {
					println(o);
				}
			}
		}
		return c;
	}

	/**
	 * Registers an EventListener interface, with a Event ID, given as a String.
	 * 
	 * @param type
	 * 
	 * @param listener
	 */
	public void registerEventListener(String type, EventListener listener) {

		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		List<EventListener> listListeners = mapEventListeners.get(type);
		if (listListeners == null) {
			listListeners = new ArrayList<>();
			mapEventListeners.put(type, listListeners);
			listListeners.add(listener);
		} else {
			listListeners.add(listener);
		}
	}

	/**
	 * Registers an EventListener interface, with all Event IDs listed in the
	 * interface as String[] getTypes().
	 * 
	 * @param listener
	 */
	public void registerEventListener(EventListener listener) {

		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		String[] types = listener.getTypes();
		if (types == null)
			throw new IllegalArgumentException("listener.getTypes() array is null!");
		for (String type : types) {
			registerEventListener(type, listener);
		}

	}

	/**
	 * Registers a CommandListener interface, with a command, given as a String.
	 * 
	 * @param command
	 * 
	 * @param listener
	 */
	public void registerCommandListener(String command, CommandListener listener) {

		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");

		command = command.toLowerCase();

		List<CommandListener> listListeners = mapCommandListeners.get(command);
		if (listListeners == null) {
			listListeners = new ArrayList<>();
			mapCommandListeners.put(command, listListeners);
			listListeners.add(listener);
		} else {
			listListeners.add(listener);
		}
	}
	
	/**
	 * Unregisters a given CommandListener.
	 * 
	 * @param command
	 * 
	 * @param listener
	 */
	public void unregisterCommandListener(String command, CommandListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		// Grab the list associated with the command.
		List<CommandListener> list = mapCommandListeners.get(command);
		
		// If the list exists, tell the list to remove the CommandListener.
		if(list != null) list.remove(listener);

	}

	/**
	 * Registers a LogListener interface.
	 * 
	 * @param listener
	 */
	public void registerLogListener(LogListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		// Make sure that the listener isn't registered twice.
		if(!listLogListeners.contains(listener)) {			
			listLogListeners.add(listener);
		}
	}
	
	/**
	 * Registers a ExceptionListener interface.
	 * 
	 * @param listener
	 */
	public void registerExceptionListener(ExceptionListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		// Make sure that the listener isn't registered twice.
		if(!listExceptionListeners.contains(listener)) {			
			listExceptionListeners.add(listener);
		}
	}
	
	/**
	 * Unregisters a given EventListener.
	 * 
	 * @param listener
	 */
	public void unregister(EventListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		String[] types = listener.getTypes();
		
		// Make sure the Listener 
		if(types == null || types.length == 0) throw new IllegalArgumentException("Listener does not have defined types!");
		
		// Go through each type registered with the listener.
		for (String type : listener.getTypes()) {			
			
			// Grab the list holding the listener.
			List<EventListener> list = mapEventListeners.get(type);
			
			// If the list is valid, remove the listener from the List.
			if(list != null) {				
				list.remove(listener);
			}
			
		}
	}

	/**
	 * Unregisters a given CommandListener.
	 * 
	 * @param listener
	 */
	public void unregister(CommandListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		for(String command : listener.getCommands()) {
			mapCommandListeners.get(command).remove(listener);
		}
	}

	/**
	 * 
	 * @param listener
	 */
	public void unregister(LogListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		listLogListeners.remove(listener);
	}

	/**
	 * 
	 * @param listener
	 */
	public void unregister(ExceptionListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		listExceptionListeners.remove(listener);
	}

	/**
	 * Returns the map of EventListener interfaces registered.
	 * 
	 * @return
	 */
	public Map<String, List<EventListener>> getEventListeners() {
		return this.mapEventListeners;
	}

	/**
	 * Returns the List of registered LogListeners.
	 * 
	 * @return
	 */
	public List<LogListener> getLogListeners() {
		return this.listLogListeners;
	}
	
	/**
	 * Returns the List of registered ExceptionListeners.
	 * 
	 * @return
	 */
	public List<ExceptionListener> getExceptionListeners() {
		return this.listExceptionListeners;
	}

	/**
	 * Returns the Map of registered CommandListeners, organized as Lists with the given command as a key, in lowercase.
	 * 
	 * @return
	 */
	public Map<String, List<CommandListener>> getCommandListeners() {
		return this.mapCommandListeners;
	}

	@Override
	public String getName() { return "Sledgehammer"; }

}
