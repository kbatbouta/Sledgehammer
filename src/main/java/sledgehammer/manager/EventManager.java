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

package sledgehammer.manager;

import sledgehammer.Settings;
import sledgehammer.SledgeHammer;
import sledgehammer.annotations.CommandHandler;
import sledgehammer.annotations.EventHandler;
import sledgehammer.event.*;
import sledgehammer.command.CommandEvent;
import sledgehammer.event.ThrowableEvent;
import sledgehammer.command.CommandHandlerComparator;
import sledgehammer.command.CommandHandlerContainer;
import sledgehammer.interfaces.Cancellable;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ClassUtil;
import sledgehammer.util.Command;
import sledgehammer.interfaces.Listener;
import sledgehammer.util.Response;
import zombie.core.raknet.UdpConnection;

import java.lang.reflect.Method;
import java.util.*;

/**
 * This manager handles the Events fired in the Sledgehammer Engine, and Craftboid.
 *
 * @author Jab
 */
public class EventManager extends Manager {

  private EventHandlerComparator comparatorEventHandlers;
  private CommandHandlerComparator comparatorCommandHandlers;

  private Map<Class<? extends Event>, LinkedList<EventHandlerContainer>> mapEventHandlers;
  private Map<String, LinkedList<CommandHandlerContainer>> mapCommandHandlers;

  @Override
  public void onLoad(boolean debug) {
    comparatorEventHandlers = new EventHandlerComparator();
    comparatorCommandHandlers = new CommandHandlerComparator();
    // Create Handler Maps. @formatter:off
    mapEventHandlers = new HashMap<>();
    mapCommandHandlers = new HashMap<>();
    // @formatter:on
  }

  @Override
  public void onStart() {
    if (Settings.getInstance().isDebug()) {
      println(printRegisteredHandlers());
    }
  }

  @Override
  public void onShutDown() {
    reset();
  }

  @Override
  public String getName() {
    return "EventManager";
  }

  /**
   * Handles a Event by sending the Event to all registered EventHandlers for the Event type.
   *
   * <p>If the event given is null, a IllegalArgumentException is thrown.
   *
   * @param event The Event to handle.
   * @param log Flag to issue a LogEvent after handling the Event.
   */
  public Event handleEvent(Event event, boolean log) {
    if (event == null) {
      throw new IllegalArgumentException("Event given is null.");
    }
    LinkedList<EventHandlerContainer> listContainers = getEventHandlers(event);
    if (listContainers != null) {
      // Copy the array to prevent ConcurrentModificationExceptions.
      listContainers = new LinkedList<>(listContainers);
      for (EventHandlerContainer container : listContainers) {
        // Make sure the EventHandler is enabled to handle events.
        if (!container.isEnabled()) {
          continue;
        }
        // If the Event is cancelled and the EventHandler does not handle cancelled
        // events, do
        // not invoke it.
        if (!container.ignoreCancelled() && (event instanceof Cancellable)) {
          if (((Cancellable) event).isCancelled()) {
            continue;
          }
        }
        try {
          // Invoke the EventHandler.
          container.handleEvent(event);
        } catch (Throwable throwable) {
          // Print the information of the handler if debug mode is enabled.
          if (Settings.getInstance().isDebug()) {
            errln("The EventHandler failed to execute: " + container.toString());
            errln("The EventHandler is now disabled.");
          }
          // Disable the container to prevent further issues. Disabling the Event
          // before handling the Throwable can prevent a infinite event loop.
          container.setEnabled(false);
          // Handle the Throwable as a Event.
          handleThrown(throwable);
        }
      }
    }
    // Make sure that we do not try to Log a ThrowableEvent or Log a LogEvent. Make sure that
    // a logged message is present, and that the event is not cancelled.
    if (log
        && !(event instanceof LogEvent)
        && !(event instanceof ThrowableEvent)
        && (!(event instanceof Cancellable) || !((Cancellable) event).isCancelled())
        && event.getLogMessage() != null) {
      // Create a LogEvent, and handle it.
      LogEvent logEvent = new LogEvent(event);
      handleEvent(logEvent, false);
    }
    return event;
  }

  /**
   * Handles a Command by packaging it as a CommandEvent and handling the Event, then logging it.
   *
   * <p>All Command listeners must use a EventHandler method for the event.
   *
   * @param command The Command to handle as a CommandEvent.
   * @return Returns the CommandEvent result.
   */
  public CommandEvent handleCommand(Command command) {
    return handleCommand(command, true);
  }

  /**
   * Handles a Command by packaging it as a CommandEvent and handling the Event.
   *
   * <p>All Command listeners must use a EventHandler method for the event.
   *
   * @param command The Command to handle as a CommandEvent.
   * @param log Set to true to log the event.
   * @return Returns the CommandEvent result.
   */
  public CommandEvent handleCommand(Command command, boolean log) {
    if (command == null) {
      throw new IllegalArgumentException("Command given is null.");
    }
    boolean isHelp = command.getCommand().equalsIgnoreCase("help");
    CommandEvent event = new CommandEvent(command);
    Player commander = command.getPlayer();
    if (commander == null) {
      if (Settings.getInstance().isDebug()) {
        println("Command is given but does not have an assigned Player: " + command.getRaw());
        println("Assign 'Sledgehammer.getAdministrator()' if this is a Plug-in command.");
        println("Command not executed.");
      }
      return event;
    }
    Response response = event.getResponse();
    if (isHelp) {
      response.appendLine();
    }
    boolean deniedOnce = false;
    boolean handledOnce = false;
    String raw = command.getRaw().toLowerCase().trim();
    String[] split = raw.split(" ");
    for (int index = split.length - 1; index >= 0; index--) {
      StringBuilder stringBuilder = new StringBuilder();
      int subIndex = 0;
      do {
        stringBuilder.append(split[subIndex++]);
      } while (subIndex <= index);
      String commandSearch = stringBuilder.toString().replace("\"", "");
      LinkedList<CommandHandlerContainer> listContainers = mapCommandHandlers.get(commandSearch);
      if (listContainers != null) {
        // Copy the array to prevent ConcurrentModificationExceptions.
        listContainers = new LinkedList<>(listContainers);
        for (CommandHandlerContainer container : listContainers) {
          if (!container.isEnabled()) {
            continue;
          }
          String[] permissionNodes = container.getPermissionNodes();
          // If the commanding player has permission to the handler, invoke it.
          if (commander.hasPermission(permissionNodes)) {
            try {
              handledOnce = true;
              container.handleCommand(command, response);
            } catch (Throwable throwable) {
              if (Settings.getInstance().isDebug()) {
                errln("The CommandHandler failed to execute: " + container.toString());
                errln("The CommandHandler is now disabled.");
              }
              container.setEnabled(false);
              handleThrown(throwable);
            }
          } else {
            deniedOnce = true;
          }
          // If the command is denied and not handled, then this is due to permission nodes not
          // being granted. Set the response message to the permission denied message.
          if (deniedOnce && !handledOnce) {
            response.deny();
          }
        }
      }
    }
    if (isHelp) {
      String responseText = response.getResponse();
      String[] lines = response.getResponse().split("<LINE>");
      ArrayList<String> listLines = new ArrayList<>(Arrays.asList(lines));
      Collections.sort(
          listLines,
          new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
              return o1.compareTo(o2);
            }
          });
      response.setResponse("Commands: <LINE> ");
      for (String line : listLines) {
        line = line.trim();
        if (!line.isEmpty()) {
          response.appendLine(line);
        }
      }
      response.setHandled(true);
    }
    return event;
  }

  /**
   * Handles a command, and logs it.
   *
   * @param connection The native UdpConnection representing the origin of the sender.
   * @param input The String input given. This is a pre-Command as a raw input.
   * @return Returns the result CommandEvent.
   */
  public CommandEvent handleCommand(UdpConnection connection, String input) {
    return handleCommand(connection, input, true);
  }

  /**
   * Handles a command.
   *
   * @param connection The native UdpConnection representing the origin of the sender.
   * @param input The String input given. This is a pre-Command as a raw input.
   * @param logEvent Flag to log the CommandEvent.
   * @return Returns the result CommandEvent.
   */
  public CommandEvent handleCommand(UdpConnection connection, String input, boolean logEvent) {
    Player p =
        connection == null
            ? SledgeHammer.getAdministrator()
            : SledgeHammer.instance.getPlayer(connection.username);
    // Create a CommandEvent.
    Command command = new Command(input);
    command.setPlayer(p);
    // Fire the CommandEvent handle method, and return its result.
    return handleCommand(command, logEvent);
  }

  /**
   * Handles a thrown Throwable by packaging it as a ThrowableEvent and handling the Event.
   *
   * <p>All Throwable listeners must use a EventHandler method for the event.
   *
   * @param throwable The Throwable to handle as a ThrowableEvent.
   * @return Returns the ThrowableEvent result.
   */
  public ThrowableEvent handleThrown(Throwable throwable) {
    ThrowableEvent event = new ThrowableEvent(throwable);
    handleEvent(event, false);
    return event;
  }

  /**
   * Registers a given Listener. All EventHandlers declared in the Listener will be registered into
   * the EventManager's EventHandler registry.
   *
   * <p>If a null is given for the Listener parameter, an IllegalArgumentException is thrown.
   *
   * <p>If the Listener instance is already registered, none of the EventHandlers will be
   * registered. This allows for multiple instances of the same class to be registered, although
   * this is not recommended.
   *
   * @param listener The Listener to register.
   */
  public void register(Listener listener) {
    // Grab the methods for the Listener.
    Method[] methods = ClassUtil.getAllDeclaredMethods(listener.getClass());
    if (Settings.getInstance().isDebug()) {
      println("Registering listener: " + listener.getClass().getSimpleName());
    }
    // We will go through each one to see if it is an EventHandler.
    for (Method method : methods) {
      EventHandler eventHandler = method.getAnnotation(EventHandler.class);
      if (eventHandler != null) {
        EventHandlerContainer container = new EventHandlerContainer(listener, method, eventHandler);
        register(container);
        continue;
      }
      CommandHandler commandHandler = method.getAnnotation(CommandHandler.class);
      if (commandHandler != null) {
        CommandHandlerContainer container =
            new CommandHandlerContainer(listener, method, commandHandler);
        register(container);
      }
    }
  }

  /**
   * (Private Method)
   *
   * <p>Registers a given CommandHandlerContainer to the EventManager registry.
   *
   * <p>If the container is already registered, then no further actions occur.
   *
   * @param container The CommandHandlerContainer to register.
   */
  private void register(EventHandlerContainer container) {
    // Make sure that the event class is fully registered.
    if (!container.isEnabled()) {
      println("EventHandler is not enabled: " + container.toString());
      return;
    }
    // Grab the class for the event being handled. This is how we identify the event handler.
    Class<? extends Event> classEvent = container.getEventClass();
    LinkedList<EventHandlerContainer> listContainers = mapEventHandlers.get(classEvent);
    // If the event has no list of handlers, then create one.
    if (listContainers == null) {
      listContainers = new LinkedList<>();
      mapEventHandlers.put(classEvent, listContainers);
    }
    // The list already exists for the event, so a check is needed to make sure that the handler
    // does not register more than once in the list.
    else if (listContainers.contains(container)) {
      if (Settings.getInstance().isDebug()) {
        errln("EventHandler is already registered: " + container.toString());
      }
      return;
    }
    if (Settings.getInstance().isDebug()) {
      println("Registered EventHandler: " + container.toString());
    }
    // At this point we know that the handler is valid, and is not already in the list for the
    // event, so we add it to the list.
    listContainers.add(container);
    // In order to maintain priority of event handlers, each addition must follow with a sort.
    Collections.sort(listContainers, comparatorEventHandlers);
  }

  private void register(CommandHandlerContainer container) {
    // Make sure that the event class is fully registered.
    if (!container.isEnabled()) {
      println("CommandHandler is not enabled: " + container.toString());
      return;
    }
    // Grab the command for the container.
    String[] commands = container.getCommands();
    for (String command : commands) {
      String commandLower = command.toLowerCase();
      LinkedList<CommandHandlerContainer> listContainers = mapCommandHandlers.get(commandLower);
      // If the event has no list of handlers, then create one.
      if (listContainers == null) {
        listContainers = new LinkedList<>();
        mapCommandHandlers.put(commandLower, listContainers);
      }
      // The list already exists for the event, so a check is needed to make sure that the handler
      // does not register more than once in the list.
      else if (listContainers.contains(container)) {
        if (Settings.getInstance().isDebug()) {
          errln("CommandHandler is already registered: " + container.toString());
        }
        return;
      }
      if (Settings.getInstance().isDebug()) {
        println("Registered CommandHandler: " + container.toString());
      }
      // At this point we know that the handler is valid, and is not already in the list for the
      // event, so we add it to the list.
      listContainers.add(container);
      // In order to maintain priority of event handlers, each addition must follow with a sort.
      Collections.sort(listContainers, comparatorCommandHandlers);
    }
  }

  /**
   * Unregisters all handlers registered with the given listener instance.
   *
   * @param Listener The listener instance to unregister all handlers registered that identify with
   *     the listener instance.
   */
  public void unregister(Listener Listener) {
    // Go through all registered Event Classes.
    for (Class<? extends Event> classEvent : new ArrayList<>(mapEventHandlers.keySet())) {
      // Grab the EventHandlers that handle the current Event.
      List<EventHandlerContainer> listContainers = getEventHandlers(classEvent);
      // Make sure that the list is defined to check EventHandlers.
      if (listContainers == null) {
        continue;
      }
      // Iterate through all entries for the Event.
      Iterator<EventHandlerContainer> iterator = listContainers.iterator();
      while (iterator.hasNext()) {
        EventHandlerContainer container = iterator.next();
        // If the container identifies with the Listener, then we remove it. It will
        // be unregistered from the EventManager.
        if (container.getContainer().equals(Listener)) {
          iterator.remove();
        }
      }
      // If the list no longer contains any EventHandlers, then unregister the list.
      if (listContainers.size() == 0) {
        mapEventHandlers.remove(classEvent);
      }
    }
  }

  /**
   * This returns a event handler container that represents a event handler registered to the event
   * manager. If no container is identified with the ID given, null is returned.
   *
   * <p>If a null or empty ID is passed, a IllegalArgumentException is thrown.
   *
   * @param id The string ID of the event handler.
   * @return Returns a event handler container identified with the id given. If no event handler is
   *     registered with the ID given, null is returned.
   */
  public EventHandlerContainer getContainer(String id) {
    EventHandlerContainer returned = null;
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("EventHandler ID given is null or empty.");
    }
    for (Class<? extends Event> classEvent : mapEventHandlers.keySet()) {
      EventHandlerContainer container = getContainer(classEvent, id);
      if (container != null) {
        returned = container;
        break;
      }
    }
    return returned;
  }

  /**
   * This returns a CommandHandlerContainer that represents a EventHandler registered to the
   * EventManager for the Event given. If no container is identified with the ID given, null is
   * returned.
   *
   * <p>If a null or empty ID is given, a IllegalArgumentException is thrown. If a null Event is
   * given, a IllegalArgumentException is thrown.
   *
   * @param event The Event that the EventHandler handles.
   * @param id The String ID of the EventHandler.
   * @return Returns a CommandHandlerContainer with the ID given. If no EventHandler is registered
   *     with the ID given, null is returned.
   */
  public EventHandlerContainer getContainer(Event event, String id) {
    if (event == null) {
      throw new IllegalArgumentException("Event given is null.");
    }
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("EventHandler ID given is null or empty.");
    }
    return getContainer(event.getClass(), id);
  }

  /**
   * This returns a CommandHandlerContainer that represents a EventHandler registered to the
   * EventManager for the class of the Event given. If no container is identified with the ID given,
   * null is returned.
   *
   * <p>If a null or empty ID is given, a IllegalArgumentException is thrown. If a null class is
   * given, a IllegalArgumentException is thrown.
   *
   * @param classEvent The Class of the Event that the EventHandler handles.
   * @param id The String ID of te EventHandler.
   * @return Returns a CommandHandlerContainer with the ID given. If no EventHandler is registered
   *     with the ID given, null is returned.
   */
  public EventHandlerContainer getContainer(Class<? extends Event> classEvent, String id) {
    if (classEvent == null) {
      throw new IllegalArgumentException("The Event Class given is null.");
    }
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("EventHandler ID given is null or empty.");
    }
    EventHandlerContainer returned = null;
    LinkedList<EventHandlerContainer> listContainers = mapEventHandlers.get(classEvent);
    if (listContainers != null && listContainers.size() > 0) {
      for (EventHandlerContainer container : listContainers) {
        if (container.getId().equals(id)) {
          returned = container;
          break;
        }
      }
    }
    return returned;
  }

  /** Resets the EventManager by removing all EventHandlers from the registry. */
  public void reset() {
    for (Class<? extends Event> classEvent : mapEventHandlers.keySet()) {
      LinkedList<EventHandlerContainer> listContainers = mapEventHandlers.get(classEvent);
      if (listContainers != null) {
        Iterator<EventHandlerContainer> iterator = listContainers.iterator();
        while (iterator.hasNext()) {
          EventHandlerContainer container = iterator.next();
          container.setEnabled(false);
          iterator.remove();
        }
      }
    }
    mapEventHandlers.clear();
  }

  public String printRegisteredHandlers() {
    return "EventManager diagnostics:\n" + printEventHandlers("") + printCommandHandlers("") + "\n";
  }

  public String printEventHandlers(String prefix) {
    String title = "# Events registered";
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(prefix).append(createBar('#', 169)).append('\n');
    stringBuilder.append(prefix).append("#").append(createBar(' ', 168)).append("#\n");
    stringBuilder
        .append(prefix)
        .append(title)
        .append(createBar(' ', 169 - title.length()))
        .append("#\n");
    stringBuilder.append(prefix).append("#").append(createBar(' ', 168)).append("#\n");
    stringBuilder.append(prefix).append(createBar('#', 170)).append('\n');
    List<Class<? extends Event>> listEvents = new ArrayList<>(mapEventHandlers.keySet());
    Collections.sort(
        listEvents,
        new Comparator<Class<? extends Event>>() {
          @Override
          public int compare(Class<? extends Event> o1, Class<? extends Event> o2) {
            String className1 = ClassUtil.getClassName(o1);
            String className2 = ClassUtil.getClassName(o2);
            return className1.compareTo(className2);
          }
        });
    String sPriority = "Priority";
    String sListener = "Listener";
    String sMethod = "Method";
    String sId = "ID";
    for (Class<? extends Event> classEvent : listEvents) {
      String eventName = ClassUtil.getClassName(classEvent);
      StringBuilder stringBuilderEvent = new StringBuilder();
      LinkedList<EventHandlerContainer> listContainers = mapEventHandlers.get(classEvent);
      stringBuilderEvent.append(prefix).append("[").append(eventName).append("]");
      stringBuilderEvent.append(createBar('=', 165 - eventName.length())).append("[");
      stringBuilderEvent.append(listContainers.size()).append("]\n");
      stringBuilderEvent.append(prefix).append("| ").append("\n");
      stringBuilderEvent.append(prefix).append("| ");
      stringBuilderEvent.append(sPriority).append(space(sPriority, 16));
      stringBuilderEvent.append(sListener).append(space(sListener, 32));
      stringBuilderEvent.append(sMethod).append(space(sMethod, 40));
      stringBuilderEvent.append(sId).append(space(sId, 32)).append("\n");
      ;
      for (EventHandlerContainer container : listContainers) {
        stringBuilderEvent.append(printEventHandler(container, prefix));
      }
      stringBuilder.append(stringBuilderEvent);
    }
    return stringBuilder.toString();
  }

  public String printEventHandler(EventHandlerContainer container, String prefix) {
    int tabsContainer = 4;
    String sPriority = "" + container.getPriority();
    String sContainer = ClassUtil.getClassName(container.getContainer());
    String sMethod = container.getMethod().getName();
    String sId = "\"" + container.getId() + "\"";
    return (prefix + "| ")
        + (sPriority + space(sPriority, 16))
        + (sContainer + space(sContainer, 32))
        + (sMethod + space(sMethod, 40))
        + (sId + space(sId, 32) + "\n");
  }

  public String printCommandHandlers(String prefix) {
    String title = "# Commands registered";
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(prefix).append(createBar('#', 169)).append('\n');
    stringBuilder.append(prefix).append("#").append(createBar(' ', 168)).append("#\n");
    stringBuilder
        .append(prefix)
        .append(title)
        .append(createBar(' ', 169 - title.length()))
        .append("#\n");
    stringBuilder.append(prefix).append("#").append(createBar(' ', 168)).append("#\n");
    stringBuilder.append(prefix).append(createBar('#', 170)).append('\n');
    // Go through each Event and print the diagnostic for it.
    List<String> listCommands = new ArrayList<>(mapCommandHandlers.keySet());
    Collections.sort(
        listCommands,
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        });
    String sPriority = "Priority";
    String sListener = "Listener";
    String sMethod = "Method";
    String sId = "ID";
    String sPermission = "Permission";
    for (String command : listCommands) {
      StringBuilder stringBuilderEvent = new StringBuilder();
      LinkedList<CommandHandlerContainer> listContainers = mapCommandHandlers.get(command);
      stringBuilderEvent.append(prefix).append("[").append(command).append("]");
      stringBuilderEvent.append(createBar('=', 165 - command.length())).append("[");
      stringBuilderEvent.append(listContainers.size()).append("]\n");
      stringBuilderEvent.append(prefix).append("| ").append("\n");
      stringBuilderEvent.append(prefix).append("| ");
      stringBuilderEvent.append(sPriority).append(space(sPriority, 16));
      stringBuilderEvent.append(sListener).append(space(sListener, 32));
      stringBuilderEvent.append(sMethod).append(space(sMethod, 40));
      stringBuilderEvent.append(sId).append(space(sId, 32));
      stringBuilderEvent.append(sPermission).append("\n");
      for (CommandHandlerContainer container : listContainers) {
        stringBuilderEvent.append(printCommandHandler(container, prefix));
      }
      stringBuilder.append(stringBuilderEvent);
    }
    return stringBuilder.toString();
  }

  public String printCommandHandler(CommandHandlerContainer container, String prefix) {
    int tabsContainer = 4;
    String sPriority = "" + container.getPriority();
    String sContainer = ClassUtil.getClassName(container.getContainer());
    String sMethod = container.getMethod().getName();
    String sId = "\"" + container.getId() + "\"";
    int permissionNodeOffset = 120;
    String result =
        (prefix + "| ")
            + (sPriority + space(sPriority, 16))
            + (sContainer + space(sContainer, 32))
            + (sMethod + space(sMethod, 40))
            + (sId + space(sId, 32));
    String[] sPermissions = container.getPermissionNodes();
    if (sPermissions.length == 1) {
      return result + "\"" + sPermissions[0] + "\"\n";
    } else if (sPermissions.length > 1) {
      StringBuilder permissionAppend = new StringBuilder("\"" + sPermissions[0] + "\"\n");
      for (int index = 1; index < sPermissions.length; index++) {
        String sPermission = "\"" + sPermissions[index] + "\"\n";
        permissionAppend.append(prefix).append("| ");
        permissionAppend.append(createBar(' ', permissionNodeOffset)).append(sPermission);
      }
      return result + permissionAppend.toString();
    } else {
      return result + "\"" + "NULL" + "\"\n";
    }
  }

  private static String space(String string, int spaces) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int index = 0; index < spaces - string.length(); index++) {
      stringBuilder.append(" ");
    }
    return stringBuilder.toString();
  }

  private static String createBar(char c, int size) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int index = 0; index < size; index++) {
      stringBuilder.append(c);
    }
    return stringBuilder.toString();
  }

  /**
   * This returns the List of registered EventHandlers for a given Event to handle.
   *
   * @param event The Event that is handled.
   * @return Returns a LinkedList of ordered EventHandlers that handle the Event given. If no
   *     listeners are assigned to the event, null is returned.
   */
  public LinkedList<EventHandlerContainer> getEventHandlers(Event event) {
    return getEventHandlers(event.getClass());
  }

  /**
   * This returns the List of registered EventHandlers for a given Event to handle.
   *
   * @param classEvent The Class of the Event that is handled.
   * @return Returns a LinkedList of ordered EventHandlers that handle the Event given. If no
   *     listeners are assigned to the event, null is returned.
   */
  public LinkedList<EventHandlerContainer> getEventHandlers(Class<? extends Event> classEvent) {
    return mapEventHandlers.get(classEvent);
  }
}
