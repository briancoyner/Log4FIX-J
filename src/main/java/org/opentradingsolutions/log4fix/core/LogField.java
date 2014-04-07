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

import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldType;
import quickfix.field.MsgType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a single QuickFIX field. This object provides extra information about the
 * field in the context of its message. The QuickFIX <code>DataDictionary</code> is
 * used to provide information about whether or not the field is a header or trailer
 * field, if it is a required field, etc.
 *
 * @author Brian M. Coyner
 */
public class LogField {

    private final DataDictionary dictionary;
    private final Field field;
    private final FieldType fieldType;
    private final String fieldName;
    private final String fieldValueName;
    private final boolean required;
    private final boolean header;
    private final boolean trailer;
    private List<LogGroup> groups;

    public static LogField createLogField(MsgType messageType, Field field,
            DataDictionary dictionary) {
        return new LogField(messageType, field, dictionary);
    }

    /**
     * @param messageType what message the field is part of.
     * @param field       the actual field we are wrapping.
     * @param dictionary  dictionary used to look up field information.
     */
    protected LogField(MsgType messageType, Field field, DataDictionary dictionary) {
        this.dictionary = dictionary;
        this.field = field;

        final String messageTypeString = messageType.getValue();
        final int fieldTag = field.getTag();

        fieldType = dictionary.getFieldTypeEnum(fieldTag);
        fieldName = dictionary.getFieldName(fieldTag);
        fieldValueName = dictionary.getValueName(fieldTag, field.getObject().toString());
        required = getDataDictionary().isRequiredField(messageTypeString, fieldTag);

        header = getDataDictionary().isHeaderField(fieldTag);
        trailer = !header && getDataDictionary().isTrailerField(fieldTag);
    }

    public Iterator<LogField> group() {
        return null;
    }

    public Field getField() {
        return field;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public int getTag() {
        return field.getTag();
    }

    public Object getValue() {
        return field.getObject();
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValueName() {
        return fieldValueName;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isHeaderField() {
        return header;
    }

    public boolean isTrailerField() {
        return trailer;
    }

    public boolean isRepeatingGroup() {
        return groups != null;
    }

    /**
     * @return true if this this field is not a header field or a trailer field.
     */
    public boolean isBodyField() {
        return !isHeaderField() || !isTrailerField();
    }

    public DataDictionary getDataDictionary() {
        return dictionary;
    }

    public void addGroup(LogGroup group) {
        if (groups == null) {
            groups = new ArrayList<LogGroup>();
        }

        groups.add(group);
    }

    public List<LogGroup> getGroups() {
        return groups;
    }
}
