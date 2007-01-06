package org.opentradingsolutions.log4fix.swing;

import org.opentradingsolutions.log4fix.model.MemoryLogModel;
import quickfix.SessionID;
import quickfix.SessionSettings;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Brian M. Coyner
 */
public class MemoryLogModelFactory {
    public static Map<SessionID, MemoryLogModel> getMemoryLogModels(SessionSettings settings) {

        Map<SessionID, MemoryLogModel> memoryLogModelsBySessionId =
                new LinkedHashMap<SessionID, MemoryLogModel>();
        Iterator iterator = settings.sectionIterator();
        while (iterator.hasNext()) {
            SessionID sessionId = (SessionID) iterator.next();
            memoryLogModelsBySessionId.put(sessionId, new GlazedListsMemoryLogModel(sessionId
            ));
        }
        return memoryLogModelsBySessionId;
    }
}
