package org.opentradingsolutions.log4fix.datadictionary;

import junit.framework.TestCase;
import quickfix.DataDictionary;
import quickfix.FixVersions;
import quickfix.SessionID;

import java.lang.reflect.Field;

/**
 * @author Brian M. Coyner
 */
public class ClassPathDataDictionaryLoaderTest extends TestCase {
    public static final String SENDER_COMP_ID = "coyner";
    public static final String TARGET_COMP_ID = "bce";
    private DataDictionaryLoader loader;

    protected void setUp() throws Exception {
        loader = new ClassPathDataDictionaryLoader();
    }

    public void testFIX40DataDictionaryLoads() throws Exception {
        assertDataDictionary(FixVersions.BEGINSTRING_FIX40);
    }

    public void testFIX41DataDictionaryLoads() throws Exception {
        assertDataDictionary(FixVersions.BEGINSTRING_FIX41);
    }

    public void testFIX42DataDictionaryLoads() throws Exception {
        assertDataDictionary(FixVersions.BEGINSTRING_FIX42);
    }

    public void testFIX43DataDictionaryLoads() throws Exception {
        assertDataDictionary(FixVersions.BEGINSTRING_FIX43);
    }

    public void testFIX44DataDictionaryLoads() throws Exception {
        assertDataDictionary(FixVersions.BEGINSTRING_FIX44);
    }

    private void assertDataDictionary(String beginString) throws Exception {
        SessionID sessionId = new SessionID(beginString,
                SENDER_COMP_ID, TARGET_COMP_ID);
        DataDictionary dataDictionary = loader.loadDictionary(sessionId);
        assertNotNull(dataDictionary);

        assertValidationSettingIsTurnedOn(dataDictionary, "hasVersion");
        assertValidationSettingIsTurnedOn(dataDictionary, "checkFieldsOutOfOrder");
        assertValidationSettingIsTurnedOn(dataDictionary, "checkFieldsHaveValues");
        assertValidationSettingIsTurnedOn(dataDictionary, "checkUserDefinedFields");

    }

    /**
     * The <code>DataDictionary</code> does not expose the "check" fields, so
     * we use reflection to assert that all message validation checks are "on".
     */
    private void assertValidationSettingIsTurnedOn(DataDictionary dataDictionary,
            String propertyName) throws NoSuchFieldException, IllegalAccessException {

        Field field = dataDictionary.getClass().getDeclaredField(propertyName);
        field.setAccessible(true);
        assertTrue(field.getBoolean(dataDictionary));
    }
}
