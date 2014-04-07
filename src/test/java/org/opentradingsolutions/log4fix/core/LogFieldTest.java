/*
 * The Log4FIX Software License
 * Copyright (c) 2006 - 2011 Brian M. Coyner All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 3. Neither the name of the product (Log4FIX), nor Brian M. Coyner,
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL BRIAN M. COYNER OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package org.opentradingsolutions.log4fix.core;

import junit.framework.TestCase;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.field.MsgType;
import quickfix.field.Symbol;

import java.io.InputStream;

/**
 * @author Brian M. Coyner
 * @todo - write data-driven tests based on all QF data dictionary files.
 */
public class LogFieldTest extends TestCase {

    public void testFIX42RequiredBodyField() throws ConfigError {

        MsgType messageType = new MsgType(MsgType.ORDER_SINGLE);
        Field field = new Symbol("COYNER");

        InputStream ddis = getClass().getResourceAsStream("/FIX42.xml");
        DataDictionary dd = new DataDictionary(ddis);

        LogField logField = LogField.createLogField(messageType, field, dd);

        assertSame(dd, logField.getDataDictionary());
        assertSame(field, logField.getField());
        assertEquals(dd.getFieldName(field.getTag()), logField.getFieldName());
        assertEquals(dd.getFieldTypeEnum(field.getTag()), logField.getFieldType());
        assertEquals(dd.getValueName(field.getTag(), field.getObject().toString()), logField.getFieldValueName());
        assertEquals(field.getObject(), logField.getValue());
        assertTrue(logField.isBodyField());
        assertEquals(dd.isHeaderField(field.getTag()), logField.isHeaderField());
        assertEquals(dd.isTrailerField(field.getTag()), logField.isTrailerField());
    }
}
