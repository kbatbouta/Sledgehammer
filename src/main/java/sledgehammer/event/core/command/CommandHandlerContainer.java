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

package sledgehammer.event.core.command;

import sledgehammer.Settings;
import sledgehammer.annotations.CommandHandler;
import sledgehammer.event.Event;
import sledgehammer.util.ClassUtil;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * CommandHandlerContainer is a container class for the CommandHandler Annotation. This is to help
 * cache and handle data and operations for command handlers in Sledgehammer. The method of
 * invocation is through the MethodHandle API introduced in the JDK 1.7 version in order to
 * handle methodical invocation that pre-compiles the data required to execute the method using
 * the traditional reflection API, allowing the execution of the command handler to execute with
 * the least amount of computations possible in the JVM.
 * <p>
 * Command handlers must contain one parameter for the method, which is the command being
 * interpreted and handled. If the parameters are not setup to only have the command being
 * handled and the response object, the container will not be enabled. The handler will still be
 * registered for debugging purposes.
 * <p>
 * An example command handler is as follows:
 * <p>
 * <code>
 * CommandHandler(ignoreCancelled = true, priority = 5)
 * private void on(Command command, Response response) {
 * // Handle Command here.
 * }
 * </code>
 *
 * @author Jab
 */
public class CommandHandlerContainer {

    private static MethodHandles.Lookup lookup = MethodHandles.lookup();

    private CommandHandler annotation;
    private Class<?>[] methodParameters;
    private Method method;
    private MethodHandle methodHandle;
    private MethodType methodType;
    private Object container;
    private Object[] methodArgumentsCache;
    private String methodName;
    private boolean isStatic;
    private boolean isEnabled;
    private Class<? extends Event> classEvent;
    private long timeCreated;

    /**
     * Main constructor.
     *
     * @param container  The Object to identify with the command handler.
     *                   This is typically the declaring class.
     * @param method     The Method that is the command handler to invoke.
     * @param annotation The command handler annotation that contains
     *                   the information important to the functions
     *                   of the command handler.
     */
    public CommandHandlerContainer(Object container, Method method, CommandHandler annotation) {
        setTimeCreated(System.currentTimeMillis());
        setContainer(container);
        setMethod(method);
        setAnnotation(annotation);
        // Setup the MethodHandle.
        createMethodHandler();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Method: \"").append(methodName)
                .append("\" container = \"").append(getContainer().getClass().getSimpleName())
                .append("\" static = \"").append(isStatic)
                .append("\" enabled = \"").append(isEnabled())
                .append("\" Arguments:");
        Object[] parameters = getMethodParameters();
        if (parameters != null && parameters.length > 0) {
            for (int index = 0; index < parameters.length; index++) {
                Object param = parameters[index];
                if (param != null) {
                    stringBuilder.append(" (").append(index).append("): (").append(param
                            .getClass().getSimpleName())
                            .append(") = ").append(param.toString());
                } else {
                    stringBuilder.append(" (").append(index).append("): (NULL)");
                }
            }
        } else {
            stringBuilder.append("None.");
        }
        return stringBuilder.toString();
    }

    /**
     * Handles setting up the method handle cache to invoke when executing a command handler.
     */
    private void createMethodHandler() {
        // In order to invoke the Method, the Method must be accessible, otherwise an
        // IllegalAccessException is thrown.
        method.setAccessible(true);
        // We need to make sure that we can identify the Method for any debugging purposes.
        methodName = method.getName();
        // Grab the parameters of the Method to make sure that the handler is valid.
        methodParameters = method.getParameterTypes();
        // If there's no parameters or excess parameters, we cannot enable the handler.
        if (methodParameters.length != 2) {
            if(Settings.getInstance().isDebug()) {
                System.err.println("Invalid parameter count for CommandHandler:\n" + toString());
            }
            return;
        }
        // Make sure that the first parameter given is a Command Object.
        if (!methodParameters[0].equals(Command.class)) {
            if (Settings.getInstance().isDebug()) {
                System.err.println("Invalid parameter 0 for CommandHandler:\n" + toString());
            }
            return;
        }
        // Make sure that the second parameter given is a Response Object.
        if (!methodParameters[1].equals(Response.class)) {
            if (Settings.getInstance().isDebug()) {
                System.err.println("Invalid parameter 1 for CommandHandler:\n" + toString());
            }
            return;
        }
        // Make note if the handler is static.
        isStatic = Modifier.isStatic(method.getModifiers());
        // Make sure the container identifier is valid.
        setContainer(container != null ? container.getClass() : method.getDeclaringClass());
        // This will expressly note the parameters to pass to the handle, and the type of
        // Class expected to return.
        methodType = MethodType.methodType(method.getReturnType(), methodParameters);
        try {
            // We will use the lookup factory to un-package and assemble the method into a
            // MethodHandle reference. This is similar to pointers in C / C++.
            methodHandle = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // If the handler method is static, the parameter cache does not require an instance
        // of the declaring class or anonymous class to invoke.
        if (isStatic) {
            // In this case, the handler will require 2 parameter:
            // [0] -> Command
            // [1] -> Response
            methodArgumentsCache = new Object[2];
        }
        // We will setup the parameter cache for a non-static (invokeVirtual) method. These
        // methods are what is expected to be used as handlers.
        else {
            // In this case, the handler will require 3 parameters:
            // [0] -> self
            // [1] -> Command
            // [2] -> Response
            methodArgumentsCache = new Object[3];
            // Set the self instance as the Container.
            methodArgumentsCache[0] = getContainer();
        }
        // We are now good to start invoking the handler. Let the EventManager know this.
        setEnabled(true);
    }

    /**
     * Handles a command. If the command handler is not enabled, then we return without
     * attempting to handle the command.
     *
     * @param command  The command to handle.
     * @param response The response to set.
     * @throws Throwable Thrown if the command handler fails to handle the command, or
     *                   the method handle fails to invoke the method.
     */
    public void handleCommand(Command command, Response response) throws Throwable {
        // Make sure that the EventHandler is enabled before invoking the EventHandler.
        if (!isEnabled()) {
            return;
        }
        // If the Event is cancelled, and the EventHandler does not ignore this, do not handle
        // the Event.
        if (response.isHandled() && !this.ignoreHandled()) {
            return;
        }
        // Grab the parameter cache for the EventHandler.
        Object[] parameters = getMethodParameters();
        // If the method is static, we do not need to reference the instance of the declaring class.
        if (isStatic()) {
            // The method is static, so we need to only pass the Event instance being handled.
            methodArgumentsCache[0] = command;
            methodArgumentsCache[1] = response;
            // Finally, invoke the method.
            methodHandle.invokeWithArguments(methodArgumentsCache);
            // Clear the cache of references to the arguments given.
            methodArgumentsCache[0] = null;
            methodArgumentsCache[1] = null;
        }
        // The method is dynamic, so we need to pass the instance of the declaring class.
        else {
            // The declaring class instance is already defined in the first index of the
            // parameter array. All we need to do is assign the Event instance being handled.
            methodArgumentsCache[1] = command;
            methodArgumentsCache[2] = response;
            // Finally, invoke the method.
            methodHandle.invokeWithArguments(methodArgumentsCache);
            // Clear the cache of references to the arguments given.
            methodArgumentsCache[1] = null;
            methodArgumentsCache[2] = null;
        }
    }

    /**
     * @return Returns the method that is the command handler being invoked when handling commands.
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the method that is the command handler being invoked when handling commands.
     *
     * @param method The method to set.
     */
    private void setMethod(Method method) {
        this.method = method;
    }

    /**
     * @return Returns the annotation instance storing the metadata for the command handler.
     */
    public CommandHandler getAnnotation() {
        return this.annotation;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the annotation instance for the command handler.
     *
     * @param annotation The annotation instance to set.
     */
    private void setAnnotation(CommandHandler annotation) {
        this.annotation = annotation;
    }

    /**
     * @return Returns the container for the method to invoke.
     */
    public Object getContainer() {
        return this.container;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the container for the method to invoke.
     *
     * @param container The declaring class instance to set.
     */
    private void setContainer(Object container) {
        this.container = container;
    }

    /**
     * @return Returns the priority index for the command handler. The higher the number, the more
     * priority the command handler has over other command handler, and will be invoked first.
     */
    public int getPriority() {
        return getAnnotation().priority();
    }

    /**
     * @return Returns true if the command handler handles the command, even if the command is
     * cancelled by a prior command handler.
     */
    public boolean ignoreHandled() {
        return getAnnotation().ignoreHandled();
    }

    /**
     * @return Returns the string ID (if defined), for the command handler.
     */
    public String getId() {
        return getAnnotation().id();
    }

    /**
     * @return Returns true if the command handler is declared as a static method.
     */
    public boolean isStatic() {
        return this.isStatic;
    }

    /**
     * @return Returns the parameters for the method.
     */
    public Object[] getMethodParameters() {
        return this.methodParameters;
    }

    /**
     * @return Returns true if the command handler is enabled. When this is disabled, commands will
     * not be passed and handled.
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Sets the command handler as enabled. When this is disabled, commands will not be passed and
     * handled.
     *
     * @param flag The status to set.
     */
    public void setEnabled(boolean flag) {
        this.isEnabled = flag;
    }

    /**
     * @return Returns the UNIX timestamp for the container to identify when the container is
     * created.
     */
    public long getTimeCreated() {
        return this.timeCreated;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the UNIX timestamp for the container to identify when the container is created.
     *
     * @param timeCreated The UNIX timestamp to set.
     */
    private void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getCommand() {
        return getAnnotation().command().toLowerCase();
    }
}