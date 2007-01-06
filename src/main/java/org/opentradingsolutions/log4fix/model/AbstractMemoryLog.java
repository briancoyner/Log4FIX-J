package org.opentradingsolutions.log4fix.model;

import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import quickfix.Log;
import quickfix.SessionID;

/**
 * @author Brian M. Coyner
 */
public abstract class AbstractMemoryLog implements Log {

    private DataDictionaryLoader dictionaryLoader;

    public AbstractMemoryLog(DataDictionaryLoader dictionaryLoader) {
        this.dictionaryLoader = dictionaryLoader;
    }

    public void clear() {
        getMemoryLogModel().clear();
    }

    public void onIncoming(String message) {
        log(message, true);
    }

    public void onOutgoing(String message) {
        log(message, false);
    }

    public void onEvent(String text) {
        getMemoryLogModel().addLogEvent(new LogEvent(text));
    }

    protected abstract SessionID getSessionId();

    protected abstract MemoryLogModel getMemoryLogModel();

    private void log(final String rawMessage, final boolean incoming) {
        SessionID sessionId = getSessionId();
        getMemoryLogModel().addLogMessage(new LogMessage(incoming, sessionId, rawMessage,
                dictionaryLoader.loadDictionary(sessionId)));
    }
}
