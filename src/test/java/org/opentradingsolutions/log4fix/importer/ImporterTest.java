package org.opentradingsolutions.log4fix.importer;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.opentradingsolutions.log4fix.datadictionary.ClassPathDataDictionaryLoader;
import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import org.opentradingsolutions.log4fix.model.AbstractSessionTestCase;
import org.opentradingsolutions.log4fix.model.LogMessage;
import org.opentradingsolutions.log4fix.model.MemoryLogModel;
import org.opentradingsolutions.log4fix.model.MockMemoryLogModel;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.SenderCompID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * @author Brian M. Coyner
 */
public class ImporterTest extends AbstractSessionTestCase {

    public static final String EXPECTED_MESSAGE_COUNT = "ExpectedMessageCount";
    public static final String EXPECTED_DIRECTION = "Direction";
    public static final String EXPECTED_BEGIN_STRING = "ExpectedBeginString";
    public static final String EXPECTED_SENDER_COMP_ID = "ExpectedSenderCompId";
    public static final String EXPECTED_TARGET_COMP_ID = "ExpectedTargetCompId";
    public static final String EXPECTED_DIRECTION_INCOMING_VALUE = "Incoming";
    public static final String EXPECTED_DIRECTION_OUTGOING_VALUE = "Outgoing";
    public static final String EXPECTED_DIRECTION_BOTH_VALUE = "Both";

    private String expectedDirection;
    private String expectedBeginString;
    private String expectedSenderCompId;
    private String expectedTargetCompId;
    private String logFileName;
    private int expectedMessageCount;


    public ImporterTest(String logFileName) {
        super("testLogFile");
        this.logFileName = logFileName;
    }

    public void testLogFile() throws Exception {
        System.out.println("ImporterTest.testLogFile: " + logFileName);

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
            }
        }

        is = getLogFileInputStream(logFileName);

        DataDictionaryLoader dictionaryLoader = new ClassPathDataDictionaryLoader();
        SessionIdResolver sessionIdResolver = new DefaultSessionIdResolver(
                expectedBeginString, expectedSenderCompId, expectedTargetCompId);

        MemoryLogModel logModel = new MockMemoryLogModel();
        ImporterMemoryLog log = new ImporterMemoryLog(logModel, dictionaryLoader);
        ImporterModel model = new ImporterModel(log, sessionIdResolver);
        Importer importer = new Importer(model, is);
        importer.start();

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
            }
        } else {
            assertNull(logModel.getSessionId());
        }
    }

    private InputStream getLogFileInputStream(String logFileName) {
        InputStream is = getClass().getResourceAsStream("/logs/" + logFileName);
        assertNotNull("Unable to load log test file: " + logFileName, is);
        return is;
    }

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
            suite.addTest(new ImporterTest(logFileName));
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

    private void assertDirection(String expectedDirection, LogMessage logMessage)
            throws Exception {
        if (EXPECTED_DIRECTION_INCOMING_VALUE.equals(expectedDirection)) {
            assertFalse(logMessage.isIncoming());
        } else if (EXPECTED_DIRECTION_OUTGOING_VALUE.equals(expectedDirection)) {
            assertTrue(logMessage.isIncoming());
        } else if (EXPECTED_DIRECTION_BOTH_VALUE.equals(expectedDirection)) {
            Message.Header header = logMessage.getMessage().getHeader();
            if (expectedTargetCompId.equals(header.getString(SenderCompID.FIELD))) {
                assertTrue(logMessage.isIncoming());
            } else {
                assertFalse(logMessage.isIncoming());
            }
        } else {
            fail("Invalid '" + EXPECTED_DIRECTION + "' value '" + expectedDirection +
                    "' in log file '" + logFileName + ". Valid " +
                    "values are '" + EXPECTED_DIRECTION_INCOMING_VALUE + "' " +
                    "or '" + EXPECTED_DIRECTION_OUTGOING_VALUE + "' " +
                    "or '" + EXPECTED_DIRECTION_BOTH_VALUE + "'.");
        }
    }
}
