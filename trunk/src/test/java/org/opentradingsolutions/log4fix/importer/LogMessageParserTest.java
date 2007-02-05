/*
 * The Log4FIX Software License
 * Copyright (c) 2006 - 2007 opentradingsolutions.org  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Neither the name of the product (Log4FIX), nor opentradingsolutions.org,
 *    nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL OPENTRADINGSOLUTIONS.ORG OR
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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Brian M. Coyner
 */
public class LogMessageParserTest extends TestCase {
    private MockInputStream in;
    private BlockingQueue<String> queue;
    private PipedOutputStream out;
    private static final int POLL_TIMEOUT = 500;
    private Thread thread;

    @Override
    public void setUp() throws Exception {
        out = new PipedOutputStream();
        in = new MockInputStream(out);
        queue = new LinkedBlockingQueue<String>(1);
        thread = new Thread(new LogMessageParser(in, queue));
    }

    @Override
    public void tearDown() {
        //assertFalse(thread.isAlive());

        // the stream may be null if a test wants to set it to null.
       // if (in != null) {
       //     assertTrue("The parser should take ownership of the stream and close it when " +
       //             "the thread dies.", in.isClosed());
       // }
    }

    public void testConstructParserWithNonEmptyQueueFailsFast() {
        queue = new LinkedBlockingQueue<String>();
        queue.add("Brian");
        try {
            new LogMessageParser(in, queue);
            fail("An empty queue must be passed to the parser.");
        } catch (IllegalStateException expected) {
        }
    }

    public void testConstructParserWithNullQueueFailsFast() {
        try {
            new LogMessageParser(in, null);
            fail("A non-null queue must be passed to the parser.");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testConstructParserWithNullInputStreamFailsFast() {
        in = null;
        try {
            new LogMessageParser(in, queue);
            fail("A non-null input stream must be passed to the parser.");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testInterruptingBlockedInputStreamPublishesPoisonPillAndThreadDies()
            throws Exception {

        thread.start();

        // interrupt the thread and see if the thread publishes
        // a poison-pill to the queue
        thread.interrupt();

        assertEquals("DONE", queue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    public void testInterruptingBlockedQueuePublishesPoisonPillAndThreadDies()
            throws Exception {

        thread.start();

        // the bounded queue is empty at this time
        assertEquals(1, queue.remainingCapacity());

        // write two messages to the stream
        String message = "8=FIX.4.2\u00019=456\u000135=D\u000110=123\u0001";
        out.write((message + "\n").getBytes());
        out.write(("8=FIX.4.2\u00019=456\u000135=D\u000110=568\u0001" + "\n").getBytes());

        waitForInputStreamToBecomeEmpty();

        assertFullQueue();

        // interrupt the thread and remove the messages from the queue.
        thread.interrupt();
        thread.join(500);

        assertFullQueue();

        // We should have two messages on the queue (original message and poison-pill).
        // The second message we attempted to put on the queue is lost due to the
        // interruption, which is what we expect.
        assertEquals(message, queue.take());
        assertEquals("DONE", queue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    public void testInterruptingBlockedQueueTimesOutIfThePoisonPillCannotAddedToTheQueue()
            throws Exception {

        thread.start();

        // write message to the stream
        String message = "8=FIX.4.2\u00019=456\u000135=D\u000110=123\u0001";
        out.write((message + "\n").getBytes());

        waitForInputStreamToBecomeEmpty();
        assertFullQueue();

        thread.interrupt();
        thread.join(LogMessageParser.CANCELATION_TIMEOUT + 500);
        assertEmptyQueue();
    }

    /**
     * Wait for parser to extract all messages from the stream. This ensures that our
     * bounded queue is blocked waiting to add another message.
     */
    private void waitForInputStreamToBecomeEmpty() throws IOException,
            InterruptedException {

        final int maxTries = 20;
        int attempts = 0;
        while (attempts++ < maxTries && in.available() != 0) {
            Thread.sleep(50);
        }

        assertEquals("Available bytes.", 0, in.available());
    }

    private void assertFullQueue() {
        assertEquals(1, queue.size());
        assertEquals(0, queue.remainingCapacity());
    }

    private void assertEmptyQueue() {
        assertEquals(0, queue.size());
        assertEquals(1, queue.remainingCapacity());
    }

    /**
     * Simple mock that allows our tests to assert the stream's "closed" status.
     */
    private class MockInputStream extends PipedInputStream {

        private boolean isClosed;

        public MockInputStream(PipedOutputStream out) throws IOException {
            super(out);
        }

        @Override
        public void close() throws IOException {
            super.close();
            isClosed = true;
        }

        public boolean isClosed() {
            return isClosed;
        }
    }
}
