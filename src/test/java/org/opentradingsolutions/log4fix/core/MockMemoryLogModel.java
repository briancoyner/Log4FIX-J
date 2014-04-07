/*
 * The Log4FIX Software License
 * Copyright (c) 2006 - 2011 Brian M. Coyner All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 3. Neither the name of the product (Log4FIX), nor Brian M. Coyner,
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL BRIAN M. COYNER OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package org.opentradingsolutions.log4fix.core;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.impl.ThreadSafeList;
import quickfix.SessionID;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * @author Brian M. Coyner
 */
public class MockMemoryLogModel implements MemoryLogModel {

    private List<LogMessage> actualLogMessages;
    private List<LogEvent> actualLogEvents;
    private SessionID sessionId;

    public MockMemoryLogModel() {
        this(null);
    }

    public MockMemoryLogModel(SessionID sessionId) {
        this.sessionId = sessionId;
        actualLogMessages = new ThreadSafeList<LogMessage>(new BasicEventList<LogMessage>());
        actualLogEvents = new BasicEventList<LogEvent>();
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

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {

    }
}
