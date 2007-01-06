package org.opentradingsolutions.log4fix.model;

import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldType;
import quickfix.field.MsgType;

/**
 * Represents a single QuickFIX field. This object provides extra information about the
 * field in the context of its message. The QuickFIX <code>DataDictionary</code> is
 * used to provide information about whether or not the field is a header or trailer
 * field, if it is a required field, etc.
 *
 * @author Brian M. Coyner
 */
public class LogField {

    private Field field;
    private FieldType fieldType;
    private String fieldName;
    private String fieldValueName;
    private boolean required;
    private boolean header;
    private boolean trailer;
    private DataDictionary dictionary;

    /**
     * @param messageType what message the field is part of.
     * @param field the actual field we are wrapping.
     * @param dictionary dictionary used to look up field information.
     */
    public LogField(MsgType messageType, Field field, DataDictionary dictionary) {
        this.dictionary = dictionary;
        this.field = field;

        fieldType = dictionary.getFieldTypeEnum(field.getTag());
        fieldName = dictionary.getFieldName(field.getTag());
        fieldValueName = dictionary.getValueName(field.getTag(),
                field.getObject().toString());
        required = getDataDictionary().isRequiredField(messageType.getValue(),
                field.getTag());
        header = getDataDictionary().isHeaderField(field.getTag());
        if (!header) {
            trailer = getDataDictionary().isTrailerField(field.getTag());
        }
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

    public DataDictionary getDataDictionary() {
        return dictionary;
    }

    /**
     * @return true if this this field is not a header field or a trailer field.
     */
    public boolean isBodyField() {
        return !isHeaderField() || !isTrailerField();
    }
}
