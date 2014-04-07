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

import junit.framework.Test;
import junit.framework.TestSuite;
import quickfix.SessionID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.opentradingsolutions.log4fix.core.AbstractSessionTestCase;
import org.opentradingsolutions.log4fix.core.LogField;
import org.opentradingsolutions.log4fix.core.LogMessage;
import org.opentradingsolutions.log4fix.core.MemoryLogModel;
import org.opentradingsolutions.log4fix.core.MockMemoryLogModel;
import org.opentradingsolutions.log4fix.datadictionary.ClassPathDataDictionaryLoader;
import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import org.opentradingsolutions.log4fix.util.FIXMessageHelper;

/**
 * @author Brian M. Coyner
 */
public class ImporterFileTest extends AbstractSessionTestCase {

    public static final String EXPECTED_MESSAGE_COUNT = "ExpectedMessageCount";

    /**
     * Are the messages "Incoming", "Outgoing" or "Both" *
     */
    public static final String EXPECTED_DIRECTION = "Direction";
    public static final String EXPECTED_BEGIN_STRING = "ExpectedBeginString";
    public static final String EXPECTED_SENDER_COMP_ID = "ExpectedSenderCompId";
    public static final String EXPECTED_TARGET_COMP_ID = "ExpectedTargetCompId";
    public static final String IS_VALID = "IsValid";
    public static final String HAS_REPEATING_GROUPS = "HasRepeatingGroups";

    // direction values.
    public static final String EXPECTED_DIRECTION_INCOMING_VALUE = "Incoming";
    public static final String EXPECTED_DIRECTION_OUTGOING_VALUE = "Outgoing";
    public static final String EXPECTED_DIRECTION_BOTH_VALUE = "Both";

    private String expectedDirection;
    private String expectedBeginString;
    private String expectedSenderCompId;
    private String expectedTargetCompId;
    private boolean expectedValid;
    private String logFileName;
    private int expectedMessageCount;
    private boolean expectedHasRepeatingGroups;

    public ImporterFileTest(String logFileName) {
        super("testLogFile");
        this.logFileName = logFileName;
    }

    public void testLogFile() throws Exception {
        InputStream is = getLogFileInputStream(logFileName);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(EXPECTED_DIRECTION)) {
                expectedDirection = line.substring(line.indexOf("=") + 1);
            } else if (line.startsWith(EXPECTED_BEGIN_STRING)) {
                expectedBeginString = line.substring(line.indexOf("=") + 1);
            } else if (line.startsWith(EXPECTED_SENDER_COMP_ID)) {
                expectedSenderCompId = line.substring(line.indexOf("=") + 1);
            } else if (line.startsWith(EXPECTED_TARGET_COMP_ID)) {
                expectedTargetCompId = line.substring(line.indexOf("=") + 1);
            } else if (line.startsWith(EXPECTED_MESSAGE_COUNT)) {
                expectedMessageCount = Integer.valueOf(line.substring(
                        line.indexOf("=") + 1));
            } else if (line.startsWith(IS_VALID)) {
                expectedValid = "true".equals(line.substring(line.indexOf("=") + 1));
            } else if (line.startsWith(HAS_REPEATING_GROUPS)) {
                expectedHasRepeatingGroups = "true".equals(
                        line.substring(line.indexOf("=") + 1));
            }
        }

        is = getLogFileInputStream(logFileName);

        DataDictionaryLoader dictionaryLoader = new ClassPathDataDictionaryLoader();
        SessionIdResolver sessionIdResolver = new DefaultSessionIdResolver(
                expectedBeginString, expectedSenderCompId, expectedTargetCompId);

        MemoryLogModel logModel = new MockMemoryLogModel();
        ImporterMemoryLog log = new ImporterMemoryLog(logModel, dictionaryLoader);
        ImporterModel model = new ImporterModel(log, sessionIdResolver);
        Importer importer = new Importer();
        MockImporterCallback callback = new MockImporterCallback();
        importer.start(model, is, callback);

        if (expectedMessageCount > 0) {
            assertNotNull(logModel.getSessionId());
            assertBeginString(expectedBeginString, logModel.getSessionId());
            assertSenderCompId(expectedSenderCompId, logModel.getSessionId());
            assertTargetCompId(expectedTargetCompId, logModel.getSessionId());

            List<LogMessage> messages = log.getMemoryLogModel().getMessages();

            assertEquals(expectedMessageCount, messages.size());
            for (LogMessage logMessage : messages) {
                assertDirection(expectedDirection, logMessage);
                assertBeginString(expectedBeginString, logMessage);
                assertSenderCompId(expectedSenderCompId, logMessage);
                assertTargetCompId(expectedTargetCompId, logMessage);

                // @todo - currently this test suite does not support asserting messages
                // with repeating groups.
                if (!expectedHasRepeatingGroups) {
                    assertFields(logMessage);
                }
                assertIsValid(expectedValid, logMessage);
            }
        } else {
            assertNull(logModel.getSessionId());
        }
    }

    private void assertFields(LogMessage logMessage) {
        String rawMessage = logMessage.getRawMessage();
        String[] rawFields = rawMessage.split("\\" + LogMessage.DEFAULT_DELIMETER);

        List<LogField> list = logMessage.getLogFields();
        assertEquals("Field Count.", rawFields.length, list.size());

        for (int index = 0; index < rawFields.length; index++) {
            String rawField = rawFields[index];

            int tag = Integer.parseInt(rawField.substring(0, rawField.indexOf("=")));
            LogField logField = list.get(index);
            assertEquals("Field.", tag, logField.getField().getTag());
        }
    }

    private InputStream getLogFileInputStream(String logFileName) {
        InputStream is = getClass().getResourceAsStream("/logs/" + logFileName);
        assertNotNull("Unable to load log test file: " + logFileName, is);
        return is;
    }

    /**
     * Build a test suite of all files in the <em>logs</em> directory ending with
     * <em>.log</em>. Each log file specifies the expected values. Each log file
     * contains expected values that this test uses to assert the importer.
     * The properties used in the log file are specified as constants at the top
     * of this file.
     *
     * @return a test suite containing tests for each test log file.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();

        URL url = suite.getClass().getResource("/logs");
        File pathDirectory = new File(url.getFile());
        assertTrue(pathDirectory.isDirectory());
        String[] logFiles = pathDirectory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".log");
            }
        });

        for (String logFileName : logFiles) {
            suite.addTest(new ImporterFileTest(logFileName));
        }

        return suite;
    }

    private void assertBeginString(String expectedBeginString, LogMessage logMessage) {
        SessionID sessionId = logMessage.getSessionId();
        assertBeginString(expectedBeginString, sessionId);
    }

    private void assertBeginString(String expectedBeginString, SessionID sessionId) {
        assertEquals(expectedBeginString, sessionId.getBeginString());
    }

    private void assertSenderCompId(String expectedSenderCompId, LogMessage logMessage) {
        SessionID sessionId = logMessage.getSessionId();
        assertSenderCompId(expectedSenderCompId, sessionId);
    }

    private void assertSenderCompId(String expectedSenderCompId, SessionID sessionId) {
        assertEquals(expectedSenderCompId, sessionId.getSenderCompID());
    }

    private void assertTargetCompId(String expectedTargetCompId, LogMessage logMessage) {
        SessionID sessionId = logMessage.getSessionId();
        assertTargetCompId(expectedTargetCompId, sessionId);
    }

    private void assertTargetCompId(String expectedTargetCompId, SessionID sessionId) {
        assertEquals(expectedTargetCompId, sessionId.getTargetCompID());
    }

    private void assertIsValid(boolean isValid, LogMessage logMessage) {
        assertEquals(isValid, logMessage.isValid());

        if (!isValid) {
            assertNull("A valid message should not have any validation error messages.",
                    logMessage.getValidationErrorMessages());
        }
    }

    private void assertDirection(String expectedDirection, LogMessage logMessage) throws Exception {
        if (ImporterFileTest.EXPECTED_DIRECTION_INCOMING_VALUE.equals(expectedDirection)) {

            assertFalse(logMessage.isIncoming());
        } else if (ImporterFileTest.EXPECTED_DIRECTION_OUTGOING_VALUE.equals(expectedDirection)) {

            assertTrue(logMessage.isIncoming());
        } else if (ImporterFileTest.EXPECTED_DIRECTION_BOTH_VALUE.equals(expectedDirection)) {

            String rawMessage = logMessage.getRawMessage();
            char delimeter = LogMessage.DEFAULT_DELIMETER;
            String actualSenderCompId = FIXMessageHelper.getSenderCompId(rawMessage, delimeter);

            if (expectedTargetCompId.equals(actualSenderCompId)) {
                assertTrue(logMessage.isIncoming());
            } else {
                assertFalse(logMessage.isIncoming());
            }
        } else {
            fail("Invalid '" + ImporterFileTest.EXPECTED_DIRECTION + "' value '" + expectedDirection +
                    "' in log file '" + logFileName + ". Valid " +
                    "values are '" + ImporterFileTest.EXPECTED_DIRECTION_INCOMING_VALUE + "' " +
                    "or '" + ImporterFileTest.EXPECTED_DIRECTION_OUTGOING_VALUE + "' " +
                    "or '" + ImporterFileTest.EXPECTED_DIRECTION_BOTH_VALUE + "'.");
        }
    }
}
