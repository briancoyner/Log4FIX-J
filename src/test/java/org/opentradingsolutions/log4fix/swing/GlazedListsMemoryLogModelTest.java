package org.opentradingsolutions.log4fix.swing;

import junit.framework.TestCase;
import org.opentradingsolutions.log4fix.model.MemoryLogModel;
import quickfix.SessionID;

/**
 * @author Brian M. Coyner
 */
public class GlazedListsMemoryLogModelTest extends TestCase {

    private SessionID sessionId;
    private GlazedListsMemoryLogModel model;

    protected void setUp() throws Exception {
        sessionId = new SessionID("FIX.4.2", "sender", "target");
        model = new GlazedListsMemoryLogModel(sessionId);
    }

    public void testGlazedListsMemoryLogModelExtendsMemoryLogModel() {
        assertTrue(MemoryLogModel.class.isAssignableFrom(model.getClass()));
    }

    public void testGetSessionId() {
        assertSame(sessionId, model.getSessionId());
    }
}
