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

import quickfix.ConfigError;
import quickfix.Log;
import quickfix.Message;
import quickfix.field.EncryptMethod;
import quickfix.field.HeartBtInt;
import quickfix.fix42.Logon;

/**
 * @author Brian M. Coyner
 */
public class AbstractMemoryLogTest extends AbstractSessionTestCase {

    private MockMemoryLogModel logModel;
    private Log memoryLog;

    @Override
    public void doSetUp() throws ConfigError {
        logModel = new MockMemoryLogModel(getSessionId());
        memoryLog = new MockMemoryLog(logModel, getSessionId());
    }

    public void testNoEventsAndNoMessages() {
        assertEquals(0, logModel.getEvents().size());
        assertEquals(0, logModel.getMessages().size());
    }

    public void testSingleEvent() {
        memoryLog.onEvent("Single Event");
        assertEquals(1, logModel.getEvents().size());
        assertEquals("Single Event", logModel.getEvents().get(0).getEvent());
    }

    public void testMultipleEvents() {

        final int eventCount = 10;
        for (int index = 0; index < eventCount; index++) {
            memoryLog.onEvent("Event " + index);
        }

        assertEquals(eventCount, logModel.getEvents().size());
        int index = 0;
        for (LogEvent event : logModel.getEvents()) {
            assertEquals("Event " + index++, event.getEvent());
        }

        assertEquals(eventCount, index);
    }

    public void testIncomingMessage() {
        Message message = createMessage();
        memoryLog.onIncoming(message.toString());

        assertEquals(0, logModel.getEvents().size());
        assertEquals(1, logModel.getMessages().size());

        LogMessage logMessage = logModel.getMessages().get(0);
        assertTrue(logMessage.isIncoming());
        assertSame(getSessionId(), logMessage.getSessionId());
    }

    public void testOutgoingMessage() {
        Message message = createMessage();
        memoryLog.onOutgoing(message.toString());

        assertEquals(0, logModel.getEvents().size());
        assertEquals(1, logModel.getMessages().size());

        LogMessage logMessage = logModel.getMessages().get(0);
        assertFalse(logMessage.isIncoming());
        assertSame(getSessionId(), logMessage.getSessionId());
    }

    public void testClearMessages() {
        Message message = createMessage();
        memoryLog.onIncoming(message.toString());
        memoryLog.onOutgoing(message.toString());

        assertEquals(2, logModel.getMessages().size());

        memoryLog.clear();

        assertEquals(0, logModel.getMessages().size());
    }

    // @todo - add more tests.

    private Message createMessage() {
        return new Logon(new EncryptMethod(EncryptMethod.NONE_OTHER), new HeartBtInt(30000));
    }
}