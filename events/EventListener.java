package events;

import java.util.HashMap;

/**
 * Any class implementing this interface is able to change its state when
 * notified from outside.
 */
public interface EventListener {
    default void update(Event event, HashMap<String, Object> data) {
        throwUnimplementedError(event);
    }

    default int getEventPriority(Event event) {
        return 1;
    }

    /**
     * Method to run inside the @overrited update method in case the EventListener
     * didn't implemented the given eventType associated with the given Object data.
     * 
     * @param event
     * @param data
     * @throws Error
     */
    default void throwUnsupportedError(Event event, Object data) throws Error {
        throw new Error(this + " was listening for " + event + " with " + data
                + " as data, but it does not support this event!");
    }

    /**
     * Method to run inside the @overrited update method in case the EventListener
     * didn't implemented the given eventType.
     * 
     * @param event
     * @param data
     * @throws Error
     */
    default void throwUnimplementedError(Event event) throws Error {
        throw new Error(this + " was notified about " + event + ", but it does not support this event!");
    }

    default int compareTo(Event event, EventListener otherListener) throws Error {
        return -Integer.compare(getEventPriority(event), otherListener.getEventPriority(event));
    }
}