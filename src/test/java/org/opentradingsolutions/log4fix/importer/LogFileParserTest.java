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

package org.opentradingsolutions.log4fix.importer;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * These tests assert that a FIX message can be extracted from an input stream.
 * The {@link LogMessageParser} extracts messages starting with '8=' through the last
 * SOH found on the line.
 *
 * @author Brian M. Coyner
 */
public class LogFileParserTest extends TestCase {

    public void testNullInputStreamFailsFast() {
        try {
            new LogMessageParser(null, new LinkedBlockingQueue<String>());
            fail("A null InputStream should fail fast.");

        } catch (IllegalArgumentException expected) {
        }
    }

    public void testNullBlockingQueueFailsFast() {
        try {
            new LogMessageParser(getClass().getResourceAsStream("/"), null);
            fail("A null BlockingQueue should fail fast.");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testNonEmptyBlockingQueueFailsFast() {
        try {
            BlockingQueue<String> fixMessages =
                    new LinkedBlockingQueue<String>();
            assertTrue(fixMessages.add("Brian"));
            new LogMessageParser(getClass().getResourceAsStream("/"), fixMessages);
            fail("A non-empty BlockingQueue should fail fast.");
        } catch (IllegalStateException expected) {
        }
    }

    public void testInputStreamWithNoFIXMessages() throws Exception {

        String noFIXMessages = "No\nMessages Are\nIn This\nStream";
        InputStream is = new ByteArrayInputStream(noFIXMessages.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        parseAndWait(is, queue);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
        assertEquals("DONE", queue.take());
    }

    public void testInputStreamWithOneFIXMessage() throws Exception {
        String message = "8=FIX.4.2\u00019=456\u000135=D\u000110=123\u0001";
        InputStream is = new ByteArrayInputStream(message.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        parseAndWait(is, queue);
        assertFalse(queue.isEmpty());
        assertEquals(2, queue.size());
        assertEquals(message, queue.take());
        assertEquals("DONE", queue.take());
    }

    public void testInputStreamWithTwoFIXMessages() throws Exception {
        String message = "8=FIX.4.2\u00019=456\u000135=D\u000110=123\u0001";

        String line = message + "\n" + message;

        // put a new line on the end
        InputStream is = new ByteArrayInputStream(line.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        parseAndWait(is, queue);
        assertFalse(queue.isEmpty());
        assertEquals(3, queue.size());
        assertEquals(message, queue.take());
        assertEquals(message, queue.take());
        assertEquals("DONE", queue.take());
    }

    /**
     * The parser should attempt to "fix" the FIX message. In this case,
     * the message is missing the last SOH character.
     */
    public void testFIXMessageThatDoesNotEndWithSOH() throws Exception {
        String oneMessage = "8=FIX.4.2\u00019=456\u000135=D\u000110=123";
        InputStream is = new ByteArrayInputStream(oneMessage.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        parseAndWait(is, queue);
        assertFalse(queue.isEmpty());
        assertEquals(2, queue.size());
        assertEquals(oneMessage + "\u0001", queue.take());
        assertEquals("DONE", queue.take());
    }

    public void testFIXMessageWithNonStandardOneCharacterDelimeter() throws Exception {
        String oneMessage = "8=FIX.4.2|9=456|35=D|10=123";
        InputStream is = new ByteArrayInputStream(oneMessage.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        parseAndWait(is, queue);
        assertFalse(queue.isEmpty());
        assertEquals(2, queue.size());
        assertEquals(oneMessage.replaceAll("\\|", "\u0001") + "\u0001", queue.take());
        assertEquals("DONE", queue.take());
    }

    public void testFIXMessageWithJunkAtTheBeginningOfTheString()
            throws Exception {
        String junkInTheTrunkMessage = "JunkInTheTrunk";
        String message = "8=FIX.4.2|9=456|35=D|10=123|";

        InputStream is = new ByteArrayInputStream((junkInTheTrunkMessage + message).getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        parseAndWait(is, queue);
        assertFalse(queue.isEmpty());
        assertEquals(2, queue.size());
        assertEquals(message.replaceAll("\\|", "\u0001"), queue.take());
        assertEquals("DONE", queue.take());
    }

    public void testIncompleteFIXMessage() throws Exception {
        String message = "8=FIX.";

        InputStream is = new ByteArrayInputStream(message.getBytes());
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        parseAndWait(is, queue);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
        assertEquals("DONE", queue.take());
    }

    private void parseAndWait(InputStream is, BlockingQueue<String> queue)
            throws InterruptedException {
        Thread t = new Thread(new LogMessageParser(is, queue));
        t.start();
        t.join(1000);
    }
}