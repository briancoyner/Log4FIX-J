package org.opentradingsolutions.log4fix.model;

import quickfix.SessionID;

import java.util.List;

/**
 * Implementations must provide thread-safe "add" operations if more than one
 * thread add messages or events. This is true for the "live" update mode because
 * the incoming and outgoing messages are processed by separate threads.
 *
 * @author Brian M. Coyner
 */
public interface MemoryLogModel {

    /**
     * @return the session Id corresponding to the FIX messages. May return
     * <code>null</code>.
     */
    SessionID getSessionId();

    void setSessionId(SessionID sessionId);

    /**
     * @return non-null list of zero or more {@link LogMessage}s.
     */
    List<LogMessage> getMessages();

    /**
     * @return non-null list of zero or more {@link LogEvent}s.
     */
    List<LogEvent> getEvents();

    /**
     * Use this method to add a new log message to the model. Implementations
     * <strong>must</strong> ensure that the list is modified in a thread-safe manner
     * because multiple threads may attempt to add a message at the same time.
     *
     * @param logMessage the new message.
     */
    void addLogMessage(LogMessage logMessage);

    /**
     * Use this method to add a new log event to the model. Implementations
     * <strong>must</strong> ensure that the list is modified in a thread-safe manner
     * because multiple threads may attempt to add an event at the same time.
     *
     * @param logEvent the new event
     */
    void addLogEvent(LogEvent logEvent);

    /**
     * Clears the messages and the events.
     */
    void clear();
}
