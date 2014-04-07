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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opentradingsolutions.log4fix.core.LogEvent;
import org.opentradingsolutions.log4fix.core.MemoryLogModel;
import org.opentradingsolutions.log4fix.core.MockMemoryLogModel;
import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import org.opentradingsolutions.log4fix.datadictionary.SessionDataDictionaryLoader;

/**
 * @author Brian M. Coyner
 */
public class LogMessageBuilderTest extends TestCase {

    private BlockingQueue<String> queue;
    private Thread thread;
    private ImporterModel model;

    @Override
    protected void setUp() {
        model = createModel();

        queue = new LinkedBlockingQueue<String>(1);
        LogMessageBuilder builder = new LogMessageBuilder(model, queue);
        thread = new Thread(builder);
    }

    @Override
    protected void tearDown() throws Exception {
        assertFalse("The builder thread is still running.", thread.isAlive());
    }

    public void testPoisonPillTerminatesThread() throws Exception {

        // start the thread
        thread.start();

        // add the poison pill to the queue
        queue.put("DONE");

        // wait for the poison to take effect
        thread.join(500);

        assertFalse("The builder thread is still running.", thread.isAlive());
    }

    public void testLogBuilderEventMessages() throws Exception {
        thread.start();

        queue.put("DONE");
        thread.join(500);

        ImporterMemoryLog logger = model.getImporterMemoryLog();
        MemoryLogModel memoryModel = logger.getMemoryLogModel();
        List<LogEvent> events = memoryModel.getEvents();
        assertEquals("Event Count.", 3, events.size());
        assertTrue(events.get(0).getEvent().startsWith(LogMessageBuilder.EVENT_START));
        assertTrue(events.get(1).getEvent().startsWith(LogMessageBuilder.EVENT_MESSAGES_IMPORTED));
        assertTrue(events.get(2).getEvent().startsWith(LogMessageBuilder.EVENT_COMPLETE));
    }

    public void testErrorMessage() throws Exception {
        thread.start();

        final String errorMessage = LogMessageBuilder.EVENT_ERROR + " Test Error";
        queue.put(errorMessage);
        queue.put("DONE");
        thread.join(500);

        ImporterMemoryLog logger = model.getImporterMemoryLog();
        MemoryLogModel memoryModel = logger.getMemoryLogModel();
        List<LogEvent> events = memoryModel.getEvents();
        assertEquals("Event Count.", 4, events.size());
        assertTrue(events.get(0).getEvent().startsWith(LogMessageBuilder.EVENT_START));
        assertEquals(events.get(1).getEvent(), errorMessage);
        assertTrue(events.get(2).getEvent().startsWith(LogMessageBuilder.EVENT_MESSAGES_IMPORTED));
        assertTrue(events.get(3).getEvent().startsWith(LogMessageBuilder.EVENT_COMPLETE));
    }

    private ImporterModel createModel() {
        DataDictionaryLoader dataDictionaryLoader = new SessionDataDictionaryLoader();
        SessionIdResolver sessionIdResolver = new PassThroughSessionIdResolver();
        MemoryLogModel memoryLogModel = new MockMemoryLogModel();
        ImporterMemoryLog memoryLog = new ImporterMemoryLog(memoryLogModel, dataDictionaryLoader);
        return new ImporterModel(memoryLog, sessionIdResolver);
    }
}