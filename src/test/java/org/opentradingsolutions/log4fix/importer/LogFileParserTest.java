package org.opentradingsolutions.log4fix.importer;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * These tests assert that a FIX message can be extracted from an input stream.
 * The {@link LogFileParser} extracts messages starting with '8=' through the last
 * SOH found on the line. 
 *
 * @author Brian M. Coyner
 */
public class LogFileParserTest extends TestCase {

    public void testNullInputStreamFailsFast() {
        try {
            new LogFileParser(null, new LinkedBlockingQueue<String>());
            fail("A null InputStream should fail fast.");

        } catch (IllegalArgumentException expected) {
        }
    }

    public void testNullBlockingQueueFailsFast() {
        try {
            new LogFileParser(getClass().getResourceAsStream("/"), null);
            fail("A null BlockingQueue should fail fast.");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testNonEmptyBlockingQueueFailsFast() {
        try {
            BlockingQueue<String> fixMessages =
                    new LinkedBlockingQueue<String>();
            assertTrue(fixMessages.add("Brian"));
            new LogFileParser(getClass().getResourceAsStream("/"), fixMessages);
            fail("A non-empty BlockingQueue should fail fast.");
        } catch (IllegalStateException expected) {
        }
    }

    public void testInputStreamWithNoFIXMessages() throws Exception {

        String noFIXMessages = "No\nMessages Are\nIn This\nStream";
        InputStream is = new ByteArrayInputStream(noFIXMessages.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        Thread t = new Thread(new LogFileParser(is, queue));
        t.start();
        t.join(1000);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
        assertEquals("DONE", queue.take());
    }

    public void testInputStreamWithOneFIXMessage() throws Exception {
        String message = "8=10=123\u0001";
        InputStream is = new ByteArrayInputStream(message.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        Thread t = new Thread(new LogFileParser(is, queue));
        t.start();
        t.join(1000);
        assertFalse(queue.isEmpty());
        assertEquals(2, queue.size());
        assertEquals(message, queue.take());
        assertEquals("DONE", queue.take());
    }

    public void testInputStreamWithTwoFIXMessages() throws Exception {
        String message = "8=10=123\u0001";

        String line = message + "\n" + message;

        // put a new line on the end
        InputStream is = new ByteArrayInputStream(line.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        Thread t = new Thread(new LogFileParser(is, queue));
        t.start();
        t.join(1000);
        assertFalse(queue.isEmpty());
        assertEquals(3, queue.size());
        assertEquals(message, queue.take());
        assertEquals(message, queue.take());
        assertEquals("DONE", queue.take());
    }

    /**
     * A message that does not end with SOH.
     */
    public void testFIXMessageThatDoesNotEndWithSOH() throws Exception {
        String oneMessage = "8=FIX.4.210=123";
        InputStream is = new ByteArrayInputStream(oneMessage.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        Thread t = new Thread(new LogFileParser(is, queue));
        t.start();
        t.join(1000);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
        assertEquals("DONE", queue.take());
    }
}