package org.opentradingsolutions.log4fix.datadictionary;

import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.FixVersions;
import quickfix.SessionID;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Brian M. Coyner
 */
public class ClassPathDataDictionaryLoader implements DataDictionaryLoader {

    private Map<String,DataDictionary> dictionaryCache;

    public ClassPathDataDictionaryLoader() {
        dictionaryCache = new HashMap<String,DataDictionary>(5);
    }

    public DataDictionary loadDictionary(SessionID sessionId) {

        String beginString = sessionId.getBeginString();

        DataDictionary dictionary = dictionaryCache.get(beginString);
        if (dictionary != null) {
            return dictionary;
        }

        if (!(FixVersions.BEGINSTRING_FIX40.equals(beginString)
                || FixVersions.BEGINSTRING_FIX41.equals(beginString)
                || FixVersions.BEGINSTRING_FIX42.equals(beginString)
                || FixVersions.BEGINSTRING_FIX43.equals(beginString)
                || FixVersions.BEGINSTRING_FIX44.equals(beginString))) {

            throw new IllegalArgumentException("Invalid FIX BeginString: '" +
                    sessionId + "'.");
        }

        String dictionaryFileName = beginString.replaceAll("\\.", "") + ".xml";
        // the dictionary is loaded from the quickfix.jar file.
        InputStream ddis = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(dictionaryFileName);
        if (ddis == null) {
            throw new NullPointerException("Data Dictionary file '" +
                    dictionaryFileName + "' not found at root of CLASSPATH.");
        }

        try {
            dictionary = new DataDictionary(ddis);
            dictionaryCache.put(beginString, dictionary);
            return dictionary;
        } catch (ConfigError configError) {
            throw new RuntimeException("Error loading data dictionary file.",
                    configError);
        }
    }
}
