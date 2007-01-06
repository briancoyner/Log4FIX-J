package org.opentradingsolutions.log4fix.model;

import junit.framework.TestCase;
import quickfix.DataDictionary;
import quickfix.SessionID;

import java.io.InputStream;

/**
 * @author Brian M. Coyner
 */
public abstract class AbstractSessionTestCase extends TestCase {

    private SessionID sessionId;
    private DataDictionary dictionary;

    public AbstractSessionTestCase() {
    }

    public AbstractSessionTestCase(String methodName) {
        super(methodName);
    }

    public final void setUp() throws Exception {
        sessionId = new SessionID("FIX.4.2", "sender", "target");
        InputStream ddis = getClass().getResourceAsStream("/FIX42.xml");
        assertNotNull("Cannot find FIX42.xml file on the classpath.", ddis);
        dictionary = new DataDictionary(ddis);

        doSetUp();
    }

    protected void doSetUp() throws Exception {
    }

    public SessionID getSessionId() {
        return sessionId;
    }

    public DataDictionary getDictionary() {
        return dictionary;
    }
}
