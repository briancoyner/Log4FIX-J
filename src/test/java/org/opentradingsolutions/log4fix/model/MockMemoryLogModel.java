package org.opentradingsolutions.log4fix.model;

import quickfix.SessionID;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian M. Coyner
 */
public class MockMemoryLogModel implements MemoryLogModel {

    private List<LogMessage> actualLogMessages;
    private List<LogEvent> actualLogEvents;
    private SessionID sessionId;

    public MockMemoryLogModel() {
        this (null);
    }

    public MockMemoryLogModel(SessionID sessionId) {
        this.sessionId = sessionId;
        actualLogMessages = new ArrayList<LogMessage>();
        actualLogEvents = new ArrayList<LogEvent>();
    }

    public void clear() {
        actualLogEvents.clear();
        actualLogMessages.clear();
    }

    public SessionID getSessionId() {
        return sessionId;
    }

    public void setSessionId(SessionID sessionId) {
        this.sessionId = sessionId;

    }

    public List<LogMessage> getMessages() {
        return actualLogMessages;
    }

    public List<LogEvent> getEvents() {
        return actualLogEvents;
    }

    public void addLogMessage(LogMessage logMessage) {
        actualLogMessages.add(logMessage);
    }

    public void addLogEvent(LogEvent logEvent) {
        actualLogEvents.add(logEvent);
    }
}
