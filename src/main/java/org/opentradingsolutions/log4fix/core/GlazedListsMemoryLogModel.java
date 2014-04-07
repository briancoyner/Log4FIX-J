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
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.impl.ThreadSafeList;
import quickfix.SessionID;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Executor;

import org.opentradingsolutions.log4fix.ui.importer.ThreadPerTaskExecutor;

/**
 * A thread-safe implementation of the memory core. Thread-safety comes by
 * wrapping the <code>BasicEventList</code> with a <code>ThreadSafeList</code>.
 * All access to the list always executes through the <code>ThreadSafeList</code>.
 *
 * @author Brian M. Coyner
 */
public class GlazedListsMemoryLogModel implements MemoryLogModel {

    private final FilterList<LogMessage> messages;
    private final EventList<LogEvent> events;
    private final PropertyChangeSupport support;
    private final ThreadSafeList<LogMessage> underlying;
    private SessionID sessionId;

    public GlazedListsMemoryLogModel() {
        this(null);
    }

    public GlazedListsMemoryLogModel(SessionID sessionId) {
        underlying = new ThreadSafeList<LogMessage>(new BasicEventList<LogMessage>());
        messages = new FilterList<LogMessage>(underlying);
        events = new ThreadSafeList<LogEvent>(new BasicEventList<LogEvent>());
        this.sessionId = sessionId;
        support = new PropertyChangeSupport(this);
    }

    public void clear() {

        Runnable r = new Runnable() {
            public void run() {
                // must clear the underlying list... clearing the filter list only clears the
                // values in the filter
                underlying.clear();
                events.clear();
                setSessionId(null);
            }
        };
        Executor perTaskExecutor = new ThreadPerTaskExecutor();
        perTaskExecutor.execute(r);
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }

    public SessionID getSessionId() {
        return sessionId;
    }

    public void setSessionId(SessionID sessionId) {
        this.sessionId = sessionId;
        support.firePropertyChange("sessionId", null, sessionId);
    }

    public FilterList<LogMessage> getMessages() {
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