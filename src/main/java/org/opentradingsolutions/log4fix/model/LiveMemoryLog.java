package org.opentradingsolutions.log4fix.model;

import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import quickfix.SessionID;

/**
 * @author Brian M. Coyner
 */
public class LiveMemoryLog extends AbstractMemoryLog {

    private SessionID sessionId;
    private MemoryLogModel model;

    public LiveMemoryLog(MemoryLogModel model, SessionID sessionId,
            DataDictionaryLoader dictionaryLoader) {
        super(dictionaryLoader);
        this.model = model;
        this.sessionId = sessionId;
    }

    protected SessionID getSessionId() {
        return sessionId;
    }

    protected MemoryLogModel getMemoryLogModel() {
        return model;
    }
}
