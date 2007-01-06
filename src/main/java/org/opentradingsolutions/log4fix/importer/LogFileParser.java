package org.opentradingsolutions.log4fix.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

/**
 * Parses the input stream looking for raw FIX message strings. If a FIX
 * message string is found that string is added to the queue to be processed
 * later.
 *
 * @author Brian M. Coyner
 */
public class LogFileParser implements Runnable {

    private final InputStream logFileInputStream;
    private final BlockingQueue<String> fixMessages;

    /**
     * @param logFileInputStream a non-null stream that may or may not
     * contain raw FIX message fields. It is assumed that the FIX message
     * fields are delimited with the SOH character.
     * @param fixMessages a non-null, empty queue.
     */
    public LogFileParser(InputStream logFileInputStream,
            BlockingQueue<String> fixMessages) {

        if (logFileInputStream == null) {
            throw new IllegalArgumentException("The log file input stream is null.");
        }

        if (fixMessages == null) {
            throw new IllegalArgumentException("The queue is null.");
        }

        if (!fixMessages.isEmpty()) {
            throw new IllegalStateException("The queue must be initially empty.");
        }

        this.logFileInputStream = logFileInputStream;
        this.fixMessages = fixMessages;
    }

    public void run() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                logFileInputStream));

        String line;
        while ((line = getLine(reader)) != null) {

            String rawMessage = extractFIXMessage(line);
            if (rawMessage != null) {
                try {
                    fixMessages.put(rawMessage);
                } catch (InterruptedException e) {
                    // @todo - ???
                }
            }
        }

        try {
            fixMessages.put("DONE");
        } catch (InterruptedException e) {
            // @todo - ??
        }
    }

    private String getLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractFIXMessage(String line) {
        int beginIndex = line.indexOf("8=");

        if (beginIndex == -1) {
            return null;
        }

        int endIndex = line.lastIndexOf("\u0001");
        if (endIndex != -1) {
            return line.substring(beginIndex, endIndex + 1);
        }
        return null;

//        int checksumFieldIndex = line.indexOf("10=");
//        int messageEndIndex = line.indexOf((char) 0x01, checksumFieldIndex);
//        return line.substring(beginIndex, messageEndIndex + 1);
    }
}
