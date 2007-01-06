package org.opentradingsolutions.log4fix.datadictionary;

import quickfix.DataDictionary;
import quickfix.SessionID;

/**
 * @author Brian M. Coyner
 */
public interface DataDictionaryLoader {

    DataDictionary loadDictionary(SessionID sessionId);
}
