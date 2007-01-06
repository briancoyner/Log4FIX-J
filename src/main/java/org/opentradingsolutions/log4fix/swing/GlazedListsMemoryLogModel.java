package org.opentradingsolutions.log4fix.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.ThreadSafeList;
import org.opentradingsolutions.log4fix.model.LogEvent;
import org.opentradingsolutions.log4fix.model.LogMessage;
import org.opentradingsolutions.log4fix.model.MemoryLogModel;
import quickfix.SessionID;

/**
 * A thread-safe implementation of the memory model. Thread-safety comes by
 * wrapping the <code>BasicEventList</code> with a <code>ThreadSafeList</code>.
 * All access to the list always executes through the <code>ThreadSafeList</code>.
 *
 * @author Brian M. Coyner
 */
public class GlazedListsMemoryLogModel implements MemoryLogModel {

    private SessionID sessionId;
    private EventList<LogMessage> messages;
    private EventList<LogEvent> events;

    public GlazedListsMemoryLogModel() {
        this (null);
    }

    public GlazedListsMemoryLogModel(SessionID sessionId) {
        messages = new ThreadSafeList<LogMessage>(new BasicEventList<LogMessage>());
        events = new ThreadSafeList<LogEvent>(new BasicEventList<LogEvent>());
        this.sessionId = sessionId;
    }

    public void clear() {
        messages.clear();
        events.clear();
    }

    public SessionID getSessionId() {
        return sessionId;
    }

    public void setSessionId(SessionID sessionId) {
        this.sessionId = sessionId;
    }

    public EventList<LogMessage> getMessages() {
        return messages;
    }

    public EventList<LogEvent> getEvents() {
        return events;
    }

    public void addLogMessage(LogMessage logMessage) {
        messages.add(logMessage);
    }

    public void addLogEvent(LogEvent logEvent) {
        events.add(logEvent);
    }
}
