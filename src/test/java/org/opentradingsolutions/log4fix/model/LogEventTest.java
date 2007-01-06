package org.opentradingsolutions.log4fix.model;

import junit.framework.TestCase;

/**
 * @author Brian M. Coyner
 */
public class LogEventTest extends TestCase {

    public void nullValueNull() {
        LogEvent event = new LogEvent(null);
        assertEquals(LogEvent.NULL_EVENT_MESSAGE, event.getEvent());
    }

    public void testEmptyStringValue() {
        LogEvent event = new LogEvent("");
        assertEquals(LogEvent.NULL_EVENT_MESSAGE, event.getEvent());
    }

    public void testOneCharacterStringValue() {
        LogEvent event = new LogEvent("A");
        assertEquals("A", event.getEvent());
    }

    public void testToStringIsSameAsGetEvent() {
        LogEvent event = new LogEvent("Brian Was Here");
        assertEquals(event.getEvent(), event.toString());
        assertSame(event.getEvent(), event.toString());
    }
}
