package org.opentradingsolutions.log4fix.model;

import junit.framework.TestCase;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.field.MsgType;
import quickfix.field.Symbol;

import java.io.InputStream;

/**
 * @todo - write data-driven tests based on all QF data dictionary files.  
 *
 * @author Brian M. Coyner
 */
public class LogFieldTest extends TestCase {

    public void testFIX42RequiredBodyField() throws ConfigError {

        MsgType messageType = new MsgType(MsgType.ORDER_SINGLE);
        Field field = new Symbol("COYNER");

        InputStream ddis = getClass().getResourceAsStream("/FIX42.xml");
        DataDictionary dd = new DataDictionary(ddis);

        LogField logField = new LogField(messageType, field, dd);

        assertSame(dd, logField.getDataDictionary());
        assertSame(field, logField.getField());
        assertEquals(dd.getFieldName(field.getTag()), logField.getFieldName());
        assertEquals(dd.getFieldTypeEnum(field.getTag()), logField.getFieldType());
        assertEquals(dd.getValueName(field.getTag(), field.getObject().toString()),
                logField.getFieldValueName());
        assertEquals(field.getObject(), logField.getValue());
        assertTrue(logField.isBodyField());
        assertEquals(dd.isHeaderField(field.getTag()), logField.isHeaderField());
        assertEquals(dd.isTrailerField(field.getTag()), logField.isTrailerField());
    }
}
