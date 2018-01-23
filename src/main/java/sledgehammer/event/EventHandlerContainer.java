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

package sledgehammer.event;

import sledgehammer.SledgeHammer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * EventHandlerContainer is a container class for the EventHandler Annotation. This is to help cache and handle
 * data and operations for EventHandlers in Sledgehammer. The method of invocation is through the MethodHandle
 * API introduced in the JDK 1.7 version in order to handle methodical invocation that pre-compiles the data
 * required to execute the method using the traditional reflection API, allowing the execution of the event
 * handler to execute with the least amount of computations possible in the JVM.
 * <p>
 * EventHandlers must contain one parameter for the method, which is the Event being interpreted and handled. If
 * the parameters are not setup to only have the event being handled, the container will not be enabled. The
 * handler will still be registered for debugging purposes.
 * <p>
 * An example EventHandler is as follows:
 * <p>
 * <code>
 * EventHandler(ignoreCancelled = true, eventPriority = 5)
 * private void on(ChatMessageEvent event) {
 * // Handle Event here.
 * }
 * </code>
 *
 * @author Jab
 */
public class EventHandlerContainer {

    private static MethodHandles.Lookup lookup = MethodHandles.lookup();

    private EventHandler annotation;
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

    /**
     * Main constructor.
     *
     * @param container  The Object to identify with the EventHandler.
     *                   This is typically the declaring class.
     * @param method     The Method that is the EventHandler to invoke.
     * @param annotation The EventHandler Annotation that contains
     *                   the information important to the functions
     *                   of the EventHandler.
     */
    public EventHandlerContainer(Object container, Method method, EventHandler annotation) {
        setContainer(container);
        setMethod(method);
        setAnnotation(annotation);
        // Setup the MethodHandle.
        createMethodHandler();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Method: ").append(methodName)
                .append(" container = ").append(getContainer().getClass().getSimpleName())
                .append(" static = ").append(isStatic)
                .append(" enabled = ").append(isEnabled())
                .append("Arguments: ");
        Object[] parameters = getMethodParameters();
        if (parameters != null && parameters.length > 0) {
            for (int index = 0; index < parameters.length; index++) {
                Object param = parameters[index];
                if (param != null) {
                    stringBuilder.append("\t(").append(index).append("): (").append(param.getClass().getSimpleName())
                            .append(") = ").append(param.toString());
                } else {
                    stringBuilder.append("\t(").append(index).append("): (NULL)");
                }
            }
        } else {
            stringBuilder.append("None.");
        }
        return stringBuilder.toString();
    }

    /**
     * Handles setting up the MethodHandle cache to invoke when executing an EventHandler.
     */
    private void createMethodHandler() {
        // In order to invoke the Method, the Method must be accessible, otherwise an IllegalAccessException
        // is thrown.
        method.setAccessible(true);
        // We need to make sure that we can identify the Method for any debugging purposes.
        methodName = method.getName();
        // Grab the parameters of the Method to make sure that the EventHandler is valid.
        methodParameters = method.getParameterTypes();
        // If there's no parameters or excess parameters, we cannot enable the EventHandler.
        if (methodParameters.length != 1) {
            return;
        }
        // Make sure that the first parameter given is an Event Object.
        if (!Event.class.isInstance(methodParameters[0])) {
            return;
        }
        // Grab the identifying Event to pass to the EventHandler.
        classEvent = (Class<? extends Event>) methodParameters[0];
        // Make note if the EventHandler is static.
        isStatic = Modifier.isStatic(method.getModifiers());
        // Make sure the container identifier is valid.
        setContainer(container != null ? container.getClass() : method.getDeclaringClass());
        // This will expressly note the parameters to pass to the EventHandle, and the type of Class expected to return.
        methodType = MethodType.methodType(method.getReturnType(), methodParameters);
        try {
            // We will use the lookup factory to un-package and assemble the method into a MethodHandle reference. This
            // is similar to pointers in C / C++.
            methodHandle = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // If the EventHandler method is static, the parameter cache does not require
        // an instance of the declaring class or anonymous class to invoke.
        if (isStatic) {
            // In this case, the EventHandler will require only 1 parameter:
            // [1] -> Event (The Event Object to pass)
            methodArgumentsCache = new Object[1];
        }
        // We will setup the parameter cache for a non-static (invokeVirtual) method.
        // These methods are what is expected to be used as EventHandlers.
        else {
            // In this case, the EventHandler will require 2 parameters:
            // [0] -> self (This is much like Python or Lua, where the self is passed to a class|table-assigned function)
            // [1] -> Event (The Event Object to pass)
            methodArgumentsCache = new Object[2];
            // Set the self instance as the Container.
            methodArgumentsCache[0] = getContainer();
        }
        // We are now good to start invoking the EventHandler. Let the EventManager know this.
        setEnabled(true);
    }

    /**
     * Handles an Event. If the EventHandler is not enabled,
     * then we return without attempting to handle the Event.
     *
     * @param event The Event passed to handle.
     */
    public void handleEvent(Event event) {
        // Make sure that the EventHandler is enabled before invoking the EventHandler.
        if (!isEnabled()) {
            return;
        }
        // If the Event is cancelled, and the EventHandler does not ignore this, do not
        // handle the Event.
        if (event.canceled() && !this.ignoreCancelled()) {
            return;
        }
        // Grab the parameter cache for the EventHandler.
        Object[] parameters = getMethodParameters();
        try {
            // If the method is static, we do not need to reference the instance of the
            // declaring class.
            if (isStatic()) {
                // The method is static, so we need to only pass the Event instance being
                // handled.
                methodArgumentsCache[0] = event;
                // Finally, invoke the method.
                methodHandle.invokeWithArguments(methodArgumentsCache);
            }
            // The method is dynamic, so we need to pass the instance of the declaring
            // class.
            else {
                // The declaring class instance is already defined in the first index
                // of the parameter array. All we need to do is assign the Event
                // instance being handled.
                methodArgumentsCache[1] = event;
                // Finally, invoke the method.
                methodHandle.invokeWithArguments(methodArgumentsCache);
            }
        } catch (Throwable throwable) {
            String error = "Failed to Invoke EventManager.\n" + toString();
            SledgeHammer.instance.getEventManager().handleException(error, throwable);
        }
    }

    /**
     * @return Returns the Method that is the EventHandler being invocated when
     * handling Events.
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Method that is the EventHandler being invocated when handling Events.
     *
     * @param method The Method to set.
     */
    private void setMethod(Method method) {
        this.method = method;
    }

    /**
     * @return Returns the Annotation instance storing the meta-data for the EventHandler.
     */
    public EventHandler getAnnotation() {
        return this.annotation;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Annotation instance for the EventHandler.
     *
     * @param annotation The Annotation instance to set.
     */
    private void setAnnotation(EventHandler annotation) {
        this.annotation = annotation;
    }

    /**
     * @return Returns the container for the Method to invoke.
     */
    public Object getContainer() {
        return this.container;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the container for the Method to invoke.
     *
     * @param container The declaring class instance to set.
     */
    private void setContainer(Object container) {
        this.container = container;
    }

    /**
     * @return Returns the priority index for the EventHandler.
     * The higher the number, the more priority the EventHandler
     * has over other EventHandlers, and will be invoked first.
     */
    public int getPriority() {
        return getAnnotation().eventPriority();
    }

    /**
     * @return Returns true if the EventHandler handles the Event,
     * even if the event is cancelled by a prior EventHandler.
     */
    public boolean ignoreCancelled() {
        return getAnnotation().ignoreCancelled();
    }

    /**
     * @return Returns a Module-ID if defined for the EventHandler.
     */
    public String getModuleId() {
        return getAnnotation().moduleId();
    }

    /**
     * @return Returns true if the EventHandler is declared as a static method.
     */
    public boolean isStatic() {
        return this.isStatic;
    }

    /**
     * @return Returns the Parameters for the Method.
     */
    public Object[] getMethodParameters() {
        return this.methodParameters;
    }

    /**
     * @return Returns true if the EventHandler is enabled.
     * When this is disabled, Events will not be passed and handled.
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Sets the EventHandler as enabled. When this is disabled,
     * Events will not be passed and handled.
     *
     * @param flag The status to set.
     */
    public void setEnabled(boolean flag) {
        this.isEnabled = flag;
    }

    /**
     * @return Returns the Class of the Event to handle.
     */
    public Class<? extends Event> getEventClass() {
        return this.classEvent;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Class of the Event to handle.
     *
     * @param classEvent The Class to set.
     */
    private void setEventClass(Class<? extends Event> classEvent) {
        this.classEvent = classEvent;
    }
}