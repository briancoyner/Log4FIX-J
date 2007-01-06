package org.opentradingsolutions.log4fix.model;

import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import quickfix.Log;
import quickfix.LogFactory;
import quickfix.SessionID;

import java.util.Map;

/**
 * An implementation of a QuickFIX {@link LogFactory}. This factory
 * creates a {@link LiveMemoryLog} for each configured {@link quickfix.Session}.
 *
 * @author Brian M. Coyner
 */
public class MemoryLogFactory implements LogFactory {
    private DataDictionaryLoader dictionaryLoader;
    private Map<SessionID, MemoryLogModel> memoryLogModels;


    /**
     * @param memoryLogModels a collection of {@link org.opentradingsolutions.log4fix.model.MemoryLogModel}s mapped using
     * a <code>SessionID</code>. There is one <code>MemoryLogModel</code> for each
     * @param dictionaryLoader
     */
    public MemoryLogFactory(Map<SessionID, MemoryLogModel> memoryLogModels,
            DataDictionaryLoader dictionaryLoader) {

        this.memoryLogModels = memoryLogModels;
        this.dictionaryLoader = dictionaryLoader;
    }

    /**
     * @return a new {@link LiveMemoryLog}
     * @throws RuntimeException if the given <code>SessionID</code> is not found
     * in the collection of <code>MemoryLogModel</code> passed to the constructor.
     */
    public Log create(SessionID sessionId) {

        MemoryLogModel model = memoryLogModels.get(sessionId);
        if (model == null) {
            throw new RuntimeException("Unable to find model for Session Id: " +
                    sessionId);
        }

        return new LiveMemoryLog(model, sessionId, dictionaryLoader);
    }
}
