package org.opentradingsolutions.log4fix.datadictionary;

import quickfix.DataDictionary;
import quickfix.Session;
import quickfix.SessionID;

/**
 * @author Brian M. Coyner
 */
public class SessionDataDictionaryLoader implements DataDictionaryLoader {
    public DataDictionary loadDictionary(SessionID sessionId) {
        Session session = Session.lookupSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Unable to locate Session object " +
                    "for " + sessionId + ".");
        }

        return session.getDataDictionary();
    }
}
