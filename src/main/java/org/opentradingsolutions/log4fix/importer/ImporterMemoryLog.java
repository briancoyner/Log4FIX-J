package org.opentradingsolutions.log4fix.importer;

import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import org.opentradingsolutions.log4fix.model.AbstractMemoryLog;
import org.opentradingsolutions.log4fix.model.MemoryLogModel;
import quickfix.SessionID;

/**
 * @author Brian M. Coyner
 */
public class ImporterMemoryLog extends AbstractMemoryLog {

    private SessionID sessionId;
    private MemoryLogModel model;

    public ImporterMemoryLog(MemoryLogModel model, DataDictionaryLoader dictionaryLoader) {
        super(dictionaryLoader);
        this.model = model;
    }

    protected MemoryLogModel getMemoryLogModel() {
        return model;
    }

    public void setSessionId(SessionID sessionId) {
        this.sessionId = sessionId;

        // @todo - I don't like this line of code... try to fix it.
        model.setSessionId(sessionId);
    }

    protected SessionID getSessionId() {
        return sessionId;
    }
}
