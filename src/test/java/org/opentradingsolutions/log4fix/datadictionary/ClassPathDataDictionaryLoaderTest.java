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

    public void testFIXT11DataDictionaryLoads() throws Exception {
        assertDataDictionary(FixVersions.BEGINSTRING_FIXT11);
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
