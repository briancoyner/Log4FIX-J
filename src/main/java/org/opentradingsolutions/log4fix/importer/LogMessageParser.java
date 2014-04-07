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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Parses the input stream looking for raw FIX message strings. If a FIX
 * message string is found that string is added to the queue to be processed
 * later.
 * <p/>
 * Interrupting the thread causes the parser to add a poison pill onto the queue and
 * gracefully terminate. By default, the thread waits for a brief period of time
 * while attempting to add the poison pill. If the poison pill cannot be added to the
 * queue because the queue is full then the parser empties the queue, closes the input
 * stream, and terminates gracefully. It is assumed that if the poison pill cannot be
 * added that the consumer thread is no longer responding.
 *
 * @author Brian M. Coyner
 */
public class LogMessageParser implements Runnable {

    public static final String POISON_PILL = "DONE";
    public static final String SOH_STRING = "\u0001";

    /**
     * The amount of time (ms) to wait while trying to add the poison pill to the
     * queue.
     */
    public static final int CANCELATION_TIMEOUT = 1000;

    private final InputStream inputStream;
    private final BlockingQueue<String> fixMessages;

    /**
     * The parser takes ownership of the input stream once the {@link #run()} method
     * executes. The parser is responsible for closing the input stream upon completion
     * of the {@link #run()} method. If the parser fails construction the input stream
     * is left in its current state and the client is responsible for taking corrective
     * action.
     *
     * @param inputStream a non-null stream that may or may not already
     *                    contain data ready for reading (i.e. raw FIX message fields). It is assumed that
     *                    the FIX message fields are delimited with the SOH character and terminated
     *                    with a new line character.
     * @param fixMessages a non-null, empty queue.
     * @throws IllegalArgumentException if the input stream is null or the queue is null.
     * @throws IllegalStateException    if the queue is not empty.
     */
    public LogMessageParser(InputStream inputStream, BlockingQueue<String> fixMessages) {

        if (inputStream == null) {
            throw new IllegalArgumentException("The log file input stream is null.");
        }

        if (fixMessages == null) {
            closeInputStream(inputStream);
            throw new IllegalArgumentException("The queue is null.");
        }

        if (!fixMessages.isEmpty()) {
            closeInputStream(inputStream);
            throw new IllegalStateException("The queue must be initially empty.");
        }

        this.inputStream = inputStream;
        this.fixMessages = fixMessages;
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = getLine(reader)) != null) {

                if (Thread.interrupted()) {
                    break;
                }
                String rawMessage = extractFIXMessage(line, reader);
                if (rawMessage != null) {
                    addMessageToQueue(rawMessage);
                }
            }

            addPoisonPillToQueue();
        } finally {
            try {
                reader.close();
            } catch (IOException ohWell) {
            }
        }
    }

    private void addMessageToQueue(String rawMessage) {
        try {
            fixMessages.put(rawMessage);
        } catch (InterruptedException e) {
            // restore the interrupted state.
            Thread.currentThread().interrupt();
        }
    }

    private void addPoisonPillToQueue() {

        try {
            if (!fixMessages.offer(POISON_PILL, CANCELATION_TIMEOUT, TimeUnit.MILLISECONDS)) {
                fixMessages.clear();
                fixMessages.offer(POISON_PILL);
            }
        } catch (InterruptedException e) {
            // the thread is going to exit.
        }
    }

    private String getLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            return "JUNK";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractFIXMessage(String line, BufferedReader reader) {

        int messageStartIndex = line.indexOf("8=");
        if (messageStartIndex == -1) {
            return null;
        }

        try {

            // keep reading the input stream until we find the delimeter
            String delimeter = getDelimeter(line);
            while (delimeter == null) {
                String nextLine = getLine(reader);

                // delimeter not found... message is not complete
                if (nextLine == null) {
                    break;
                }

                // keep building the string
                line += nextLine;
                delimeter = getDelimeter(line);
            }

            // we should have found the delimeter
            int checksumFieldIndex = line.indexOf(delimeter + "10=");
            while (checksumFieldIndex == -1) {
                String nextLine = getLine(reader);
                if (nextLine == null) {
                    break;
                }

                line += nextLine;
                checksumFieldIndex = line.indexOf(delimeter + "10=");
            }

            if (checksumFieldIndex != -1) {
                int checksumFieldEndIndex = line.indexOf(delimeter, checksumFieldIndex + 1);
                if (checksumFieldEndIndex == -1) {
                    checksumFieldEndIndex = checksumFieldIndex + 6;
                }
                int offset = checksumFieldEndIndex - checksumFieldIndex + 1;

                line = correctDelimeter(line, delimeter);
                line = line.substring(messageStartIndex, checksumFieldIndex + offset);
                if (!line.endsWith(SOH_STRING)) {
                    line += SOH_STRING;
                }
                return line;
            }
        } catch (Throwable failure) {
            return "ERROR: " + line;
        }
        return null;
    }

    private String correctDelimeter(String line, String delimeter) {

        // body length field is arbitrary... we use this field to figure out
        // if the log file uses a different delimeter other than SOH. An assumption
        // is made that the delimeter is the same for all fields. If not, then
        // an exception may raise stopping the thread.
        if (!delimeter.equals(SOH_STRING)) {
            if (delimeter.length() == 1) {
                line = line.replaceAll("\\" + delimeter, SOH_STRING);
            } else {
                line = line.replaceAll(delimeter, SOH_STRING);
            }
        }

        return line;
    }

    private String getDelimeter(String line) {
        int endIndex = line.indexOf("9=");
        if (endIndex == -1) {
            return null;
        }
        return line.substring(endIndex - 1, endIndex);
    }

    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ohWell) {
            }
        }

    }
}
