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

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opentradingsolutions.log4fix.core.MemoryLogModel;
import org.opentradingsolutions.log4fix.core.MockMemoryLogModel;
import org.opentradingsolutions.log4fix.datadictionary.ClassPathDataDictionaryLoader;
import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;

/**
 * @author Brian M. Coyner
 */
public class ImporterTest extends TestCase {

    private Importer importer;
    private Thread testThread;
    private MockImporterCallback callback;
    private InputStream inputStream;
    private PipedOutputStream outputStream;
    private MemoryLogModel logModel;

    protected void setUp() throws Exception {

        callback = new MockImporterCallback();
        outputStream = new PipedOutputStream();
        inputStream = new PipedInputStream(outputStream);
        logModel = new MockMemoryLogModel();

        testThread = new Thread(new Runnable() {
            public void run() {

                DataDictionaryLoader ddLoader = new ClassPathDataDictionaryLoader();
                SessionIdResolver resolver = new PassThroughSessionIdResolver();
                ImporterMemoryLog log = new ImporterMemoryLog(logModel, ddLoader);
                ImporterModel importerModel = new ImporterModel(log, resolver);

                importer.start(importerModel, inputStream, callback);
            }
        });

        // bounded buffer.
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(1);
        importer = new Importer(queue);
    }

    public void testBlockedInputCorrectlyRespondsToCancelingTheImporter()
            throws Exception {

        testThread.start();

        outputStream.write("input stream inputStream never closed\n".getBytes());

        // give the input stream enough time to block before canceling the importer.
        Thread.sleep(200);

        // should close the stream interrupting the blocked input stream
        importer.stop();

        testThread.join(1000);

        assertCallback(1, 1, 1);
    }

    private void assertCallback(int startingInvokedCounter, int doneInvokedCounter,
            int canceledInvokedCounter) {
        assertEquals("Starting Callback Invoked Count.", startingInvokedCounter, callback.getStartingInvokedCounter());
        assertEquals("Done Callback Invoked Count.", doneInvokedCounter, callback.getDoneInvokedCounter());
        assertEquals("Canceling Callback Invoked Count.", canceledInvokedCounter, callback.getCancelingInvokedCounter());
    }

}
