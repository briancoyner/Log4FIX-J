package org.opentradingsolutions.log4fix.model;

/**
 * A simple wrapper for a QuickFIX log event.
 * This class may, in the future, tack on a date and time.
 *
 * @author Brian M. Coyner
 */
public class LogEvent {

    public static final String NULL_EVENT_MESSAGE = "Missing Message";

    private final String event;

    public LogEvent(final String event) {
        if (event == null || "".equals(event)) {
            this.event = NULL_EVENT_MESSAGE;
        } else {
            this.event = event;
        }
    }

    public String getEvent() {
        return event;
    }

    /**
     * This is useful for various reasons. For example, it allows us to use a Swing
     * <code>List</code> without a special renderer.  
     *
     * @return the raw string of the event.
     */
    public String toString() {
        return event;
    }
}
