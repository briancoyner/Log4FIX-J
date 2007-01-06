package org.opentradingsolutions.log4fix.model;

import org.opentradingsolutions.log4fix.datadictionary.ClassPathDataDictionaryLoader;
import quickfix.ConfigError;
import quickfix.Message;
import quickfix.field.EncryptMethod;
import quickfix.field.HeartBtInt;
import quickfix.fix42.Logon;

/**
 * @author Brian M. Coyner
 */
public class MemoryLogTest extends AbstractSessionTestCase {

    private MockMemoryLogModel logModel;
    private LiveMemoryLog memoryLog;

    public void doSetUp() throws ConfigError {
        logModel = new MockMemoryLogModel(getSessionId());
        memoryLog = new LiveMemoryLog(logModel, getSessionId(),
                new ClassPathDataDictionaryLoader());
    }

    public void testMemoryLogWithNoEventsAndNoMessages() {
        assertEquals(0, logModel.getEvents().size());
        assertEquals(0, logModel.getMessages().size());
    }

    public void testMemoryLogWithSingleEvent() {
        memoryLog.onEvent("Single Event");
        assertEquals(1, logModel.getEvents().size());
        assertEquals("Single Event", logModel.getEvents().get(0).getEvent());
    }

    public void testMemoryLogWithMultipleEvents() {

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
        Message message = new Logon(new EncryptMethod(EncryptMethod.NONE_OTHER),
                new HeartBtInt(30000));
        memoryLog.onIncoming(message.toString());

        assertEquals(0, logModel.getEvents().size());
        assertEquals(1, logModel.getMessages().size());

        LogMessage logMessage = logModel.getMessages().get(0);
        assertTrue(logMessage.isIncoming());
        assertSame(getSessionId(), logMessage.getSessionId());
    }

    // @todo - add more tests.
}
