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

import static org.opentradingsolutions.log4fix.importer.LogMessageParser.POISON_PILL;

import junit.framework.TestCase;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

/**
 * @author Brian M. Coyner
 */
public class LogMessageParserTest extends TestCase {

    private PipedInputStream in;
    private BlockingQueue<String> queue;
    private PipedOutputStream out;
    private static final int POLL_TIMEOUT = 500;
    private Thread parserThread;
    private CountDownLatch didAddToQueueLatch;

    @Override
    public void setUp() throws Exception {
        out = new PipedOutputStream();
        in = new PipedInputStream(out);

        didAddToQueueLatch = new CountDownLatch(1);

        queue = new LinkedBlockingQueue<String>(1) {
            @Override
            public void put(String s) throws InterruptedException {
                super.put(s);

                // kind of a hack because we are using intimate knowledge of what API calls the parser uses. It works, though.
                didAddToQueueLatch.countDown();
            }
        };
        parserThread = new Thread(new LogMessageParser(in, queue));
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

        parserThread.start();

        // interrupt the parserThread and see if the parserThread publishes
        // a poison-pill to the queue
        parserThread.interrupt();

        assertEquals(POISON_PILL, queue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    public void testInterruptingBlockedQueuePublishesPoisonPillAndThreadDies() throws Exception {

        parserThread.start();

        // the bounded queue (size 1) is empty at this time
        assertEquals(1, queue.remainingCapacity());

        // write a single message
        out.write(("8=FIX.4.2\u00019=456\u000135=D\u000110=568\u0001" + "\n").getBytes());

        // wait for the parser to receive and enqueue the message FIX message
        boolean success = didAddToQueueLatch.await(5, TimeUnit.SECONDS);
        if (!success) {
            Assert.fail("The FIX message was not enqueued on the parser's queue.");
        }

        assertFullQueue();

        // interrupt the thread and remove the messages from the queue.
        parserThread.interrupt();
        parserThread.join(LogMessageParser.CANCELATION_TIMEOUT + 300);

        assertFullQueue();

        // We should have only have the poison pill. The original message is discarded when the thread is interrupted
        // to make room for the poison pill. Remember that the queue size is 1. Therefore the parser simply clears the
        // entire queue of messages to make room for the single poison pill message.
        assertEquals("DONE", queue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    private void assertFullQueue() {
        assertEquals(1, queue.size());
        assertEquals(0, queue.remainingCapacity());
    }
}
