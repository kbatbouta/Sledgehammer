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
import sledgehammer.event.core.command.CommandEvent;
import sledgehammer.event.core.ThrowableEvent;
import sledgehammer.event.core.command.CommandHandlerComparator;
import sledgehammer.event.core.command.CommandHandlerContainer;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ClassUtil;
import sledgehammer.util.Command;
import sledgehammer.interfaces.Listener;
import zombie.core.raknet.UdpConnection;

import java.lang.annotation.Annotation;
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
        mapEventHandlers   = new HashMap<>();
        mapCommandHandlers = new HashMap<>();
        // @formatter:on
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
     * <p>
     * If the event given is null, a IllegalArgumentException is thrown.
     *
     * @param event The Event to handle.
     * @param log   Flag to issue a LogEvent after handling the Event.
     */
    public Event handleEvent(Event event, boolean log) {
        if (event == null) {
            throw new IllegalArgumentException("Event given is null.");
        }
        LinkedList<EventHandlerContainer> listContainers = getEventHandlers(event);
        if (listContainers != null) {
            for (EventHandlerContainer container : listContainers) {
                // Make sure the EventHandler is enabled to handle events.
                if (container.isEnabled()) {
                    continue;
                }
                // If the Event is cancelled and the EventHandler does not handle cancelled
                // events, do
                // not invoke it.
                if (event.canceled() && !container.ignoreCancelled()) {
                    continue;
                }
                try {
                    // Invoke the EventHandler.
                    container.handleEvent(event);
                } catch (Throwable throwable) {
                    // Print the information of the handler if debug mode is enabled.
                    if (Settings.getInstance().isDebug()) {
                        errln("The EventHandler failed to execute: ");
                        errln(container.toString());
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
        if (log && !(event instanceof LogEvent) && !(event instanceof ThrowableEvent)
                && !event.canceled()
                && event.getLogMessage() != null) {
            // Create a LogEvent, and handle it.
            LogEvent logEvent = new LogEvent(event);
            handleEvent(logEvent, false);
        }
        return event;
    }

    /**
     * Handles a Command by packaging it as a CommandEvent and handling the Event, then logging it.
     * <p>
     * All Command listeners must use a EventHandler method for the event.
     *
     * @param command The Command to handle as a CommandEvent.
     * @return Returns the CommandEvent result.
     */
    public CommandEvent handleCommand(Command command) {
        return handleCommand(command, true);
    }

    /**
     * Handles a Command by packaging it as a CommandEvent and handling the Event.
     * <p>
     * All Command listeners must use a EventHandler method for the event.
     *
     * @param command The Command to handle as a CommandEvent.
     * @param log     Set to true to log the event.
     * @return Returns the CommandEvent result.
     */
    public CommandEvent handleCommand(Command command, boolean log) {
        CommandEvent event = new CommandEvent(command);
        handleEvent(event, log);
        return event;
    }

    /**
     * Handles a command, and logs it.
     *
     * @param connection The native UdpConnection representing the origin of the sender.
     * @param input      The String input given. This is a pre-Command as a raw input.
     * @return Returns the result CommandEvent.
     */
    public CommandEvent handleCommand(UdpConnection connection, String input) {
        return handleCommand(connection, input, true);
    }

    /**
     * Handles a command.
     *
     * @param connection The native UdpConnection representing the origin of the sender.
     * @param input      The String input given. This is a pre-Command as a raw input.
     * @param logEvent   Flag to log the CommandEvent.
     * @return Returns the result CommandEvent.
     */
    public CommandEvent handleCommand(UdpConnection connection, String input, boolean logEvent) {
        Player p = connection == null ? SledgeHammer.getAdministrator()
                : SledgeHammer.instance.getPlayer(connection.username);
        // Create a CommandEvent.
        Command command = new Command(input);
        command.setPlayer(p);
        // Fire the CommandEvent handle method, and return its result.
        return handleCommand(command, logEvent);
    }

    /**
     * Handles a thrown Throwable by packaging it as a ThrowableEvent and handling the Event.
     * <p>
     * All Throwable listeners must use a EventHandler method for the event.
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
     * Registers a given Listener. All EventHandlers declared in the Listener will be
     * registered into the EventManager's EventHandler registry.
     * <p>
     * If a null is given for the Listener parameter, an IllegalArgumentException is thrown.
     * <p>
     * If the Listener instance is already registered, none of the EventHandlers will be
     * registered. This allows for multiple instances of the same class to be registered,
     * although this is not recommended.
     *
     * @param listener The Listener to register.
     */
    public void register(Listener listener) {
        // Grab the methods for the Listener.
        Method[] methods = ClassUtil.getAllDeclaredMethods(listener.getClass());
        if (Settings.getInstance().isDebug()) {
            println("Registering listener: " + listener.getClass().getSimpleName());
            println("Methods: " + methods.length);
        }
        // We will go through each one to see if it is an EventHandler.
        for (Method method : methods) {
            EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if (eventHandler != null) {
                EventHandlerContainer container = new EventHandlerContainer(listener, method,
                        eventHandler);
                register(container);
                continue;
            }
            CommandHandler commandHandler = method.getAnnotation(CommandHandler.class);
            if (commandHandler != null) {
                CommandHandlerContainer container = new CommandHandlerContainer(listener, method,
                        commandHandler);
                register(container);
                continue;
            }
        }
    }

    /**
     * (Private Method)
     * <p>
     * Registers a given CommandHandlerContainer to the EventManager registry.
     * <p>
     * If the container is already registered, then no further actions occur.
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
                errln("EventHandler is already registered:\n" + container.toString());
            }
            return;
        }
        if (Settings.getInstance().isDebug()) {
            println("Registered EventHandler:\n" + container.toString());
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
        String command = container.getCommand();
        LinkedList<CommandHandlerContainer> listContainers = mapCommandHandlers.get(command);
        // If the event has no list of handlers, then create one.
        if (listContainers == null) {
            listContainers = new LinkedList<>();
            mapCommandHandlers.put(command, listContainers);
        }
        // The list already exists for the event, so a check is needed to make sure that the handler
        // does not register more than once in the list.
        else if (listContainers.contains(container)) {
            if (Settings.getInstance().isDebug()) {
                errln("CommandHandler is already registered:\n" + container.toString());
            }
            return;
        }
        if (Settings.getInstance().isDebug()) {
            println("Registered CommandHandler:\n" + container.toString());
        }
        // At this point we know that the handler is valid, and is not already in the list for the
        // event, so we add it to the list.
        listContainers.add(container);
        // In order to maintain priority of event handlers, each addition must follow with a sort.
        Collections.sort(listContainers, comparatorCommandHandlers);
    }

    /**
     * Unregisters all handlers registered with the given listener instance.
     *
     * @param Listener The listener instance to unregister all handlers
     *                 registered that identify with the listener instance.
     */
    public void unregister(Listener Listener) {
        // Go through all registered Event Classes.
        for (Class<? extends Event> classEvent : mapEventHandlers.keySet()) {
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
     * This returns a event handler container that represents a event handler registered to the
     * event manager. If no container is identified with the ID given, null is returned.
     * <p>
     * If a null or empty ID is passed, a IllegalArgumentException is thrown.
     *
     * @param id The string ID of the event handler.
     * @return Returns a event handler container identified with the id given. If no event
     * handler is registered with the ID given, null is returned.
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
     * EventManager for the Event given. If no container is identified with the ID given, null
     * is returned.
     * <p>
     * If a null or empty ID is given, a IllegalArgumentException is thrown.
     * If a null Event is given, a IllegalArgumentException is thrown.
     *
     * @param event The Event that the EventHandler handles.
     * @param id    The String ID of the EventHandler.
     * @return Returns a CommandHandlerContainer with the ID given. If no EventHandler is registered
     * with the ID given, null is returned.
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
     * EventManager for the class of the Event given. If no container is identified with the ID
     * given, null is returned.
     * <p>
     * If a null or empty ID is given, a IllegalArgumentException is thrown.
     * If a null class is given, a IllegalArgumentException is thrown.
     *
     * @param classEvent The Class of the Event that the EventHandler handles.
     * @param id         The String ID of te EventHandler.
     * @return Returns a CommandHandlerContainer with the ID given. If no EventHandler is registered
     * with the ID given, null is returned.
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

    /**
     * Resets the EventManager by removing all EventHandlers from the registry.
     */
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

    /**
     * This returns the List of registered EventHandlers for a given Event to handle.
     *
     * @param event The Event that is handled.
     * @return Returns a LinkedList of ordered EventHandlers that handle the Event given. If no
     * listeners are assigned to the event, null is returned.
     */
    public LinkedList<EventHandlerContainer> getEventHandlers(Event event) {
        return getEventHandlers(event.getClass());
    }

    /**
     * This returns the List of registered EventHandlers for a given Event to handle.
     *
     * @param classEvent The Class of the Event that is handled.
     * @return Returns a LinkedList of ordered EventHandlers that handle the Event given. If no
     * listeners are assigned to the event, null is returned.
     */
    public LinkedList<EventHandlerContainer> getEventHandlers(Class<? extends Event> classEvent) {
        return mapEventHandlers.get(classEvent);
    }
}
