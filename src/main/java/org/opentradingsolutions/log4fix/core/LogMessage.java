/*
 * The Log4FIX Software License
 * Copyright (c) 2006 - 2011 Brian M. Coyner  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Neither the name of the product (Log4FIX), nor Brian M. Coyner,
 *    nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL BRIAN M. COYNER OR
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

import org.opentradingsolutions.log4fix.util.FIXMessageHelper;
import quickfix.*;
import quickfix.DataDictionary.GroupInfo;
import quickfix.field.MsgType;
import quickfix.field.SendingTime;

import java.util.*;

/**
 * @author Brian M. Coyner
 */
public class LogMessage implements Comparable {

    public static final char DEFAULT_DELIMETER = '|';
    public static final char SOH_DELIMETER = (char) 0x01;

    public static final String INCOMING = "incoming";
    public static final String MESSAGE_TYPE_NAME = "messageTypeName";
    public static final String SENDING_TIME = "sendingTime";
    public static final String RAW_MESSAGE = "rawMessage";

    private final int messageIndex;
    private final SessionID sessionId;
    private final boolean incoming;
    private final String rawMessage;
    private final String messageTypeName;
    private final Date sendingTime;
    private final DataDictionary dictionary;

    private List<ValidationError> validationErrors;
    private boolean isValid;


    public LogMessage(int messageIndex, boolean incoming, SessionID sessionId, String rawMessage, DataDictionary dictionary) {
        this.messageIndex = messageIndex;

        isValid = true;
        this.dictionary = dictionary;
        this.rawMessage = rawMessage.replace(SOH_DELIMETER, DEFAULT_DELIMETER);
        this.sessionId = sessionId;
        this.incoming = incoming;

        sendingTime = lookupSendingTime();
        messageTypeName = lookupMessageTypeName();
    }

    public SessionID getSessionId() {
        return sessionId;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public String getMessageTypeName() {
        return messageTypeName;
    }

    /**
     * A message is valid if all required fields are found in the message and the
     * checksum value is correct. If true then the {@link #getValidationErrorMessages()}
     * has one or more <code>Exception</code>s explaining the problems with the message
     * as reported by QuickFIX/J.
     *
     * @return true if the message is valid.
     */
    public boolean isValid() {
        return isValid && (validationErrors == null || validationErrors.size() == 0);
    }

    public boolean isIncoming() {
        return incoming;
    }

    /**
     * The sending time of the message. This value may be null if the
     * {@link SendingTime} field is not found in the message. If the value is null
     * then the {@link #getValidationErrorMessages()} contains an <code>Exception</code>
     * describing the problem.
     *
     * @return the sending time of the message or null if the message was missing the
     *         sending time.
     */
    public Date getSendingTime() {
        return sendingTime;
    }

    /**
     * This method should only be called if {@link #isValid()} returns
     * <code>true</code>.
     *
     * @return a list of one or more validation error messages; null if there are
     *         no validation error messages.
     */
    public List<ValidationError> getValidationErrorMessages() {
        return validationErrors;
    }

    /**
     * This method collects all <code>Fields</code> into a map. Each field
     * is picked out of the raw message string and is looked up from the map. This
     * ensures that we display the fields in the same order as the raw message string.
     * Why? Because the message class does not provide a way to get the fields in
     * the sent order. The only time we care about the field order is when logging.
     * <p/>
     * This method executes on the Event Dispatch Thread, so we are not slowing down
     * the quickfix thread.
     * <p/>
     * This object does <strong>not</strong> cache the field objects. Each invocation
     * of this method creates a new list of <code>LogField</code> objects. This is
     * done so that memory utilization is kept low. The caller should clear the returned
     * list when it is finished with the values. The typical caller would clear the
     * list when a new message is displayed. In fact, this is exactly what the
     * {@link org.opentradingsolutions.log4fix.ui.messages.ViewModel} does.
     *
     * @return locally created <tt>List</tt> of <tt>LogField</tt> objects; not a
     *         cached value.
     */
    public List<LogField> getLogFields() {


        Message message = createMessage();

        List<LogField> logFields = new ArrayList<LogField>();

        Map<Integer, Field> allFields = getAllFields(message);

        String[] fields = rawMessage.split("\\|");

        for (String fieldString : fields) {
            int indexOfEqual = fieldString.indexOf('=');
            int tag = Integer.parseInt(fieldString.substring(0, indexOfEqual));

            Field field = allFields.remove(tag);
            if (field != null) {
                logFields.add(createLogField(message, field));
            }
        }

        return logFields;
    }

    public int compareTo(Object o) {
        LogMessage rhs = (LogMessage) o;
        int rhsMessageIndex = rhs.messageIndex;
        return (messageIndex < rhsMessageIndex ? -1 : (messageIndex == rhsMessageIndex ? 0 : 1));
    }


    public String toString() {
        return "" + messageIndex;
    }

    private LogField createLogField(Message message, Field field) {

        MsgType messageType = getMessageType(message);
        String messageTypeValue = messageType.getValue();

        LogField logField = LogField.createLogField(messageType, field, dictionary);

        addGroup(message, field, messageType, messageTypeValue, logField, dictionary);
        return logField;
    }

    private void addGroup(FieldMap messageOrGroup, Field field, MsgType messageType, String messageTypeValue, LogField logField, DataDictionary parentDictionary) {
        
        final DataDictionary.GroupInfo groupInfo = parentDictionary.getGroup(messageTypeValue, field.getTag());
        if (groupInfo != null) {

            int delimeterField = groupInfo.getDelimeterField();
            Group group = new Group(field.getTag(), delimeterField);
            int numberOfGroups = Integer.valueOf((String) field.getObject());

            for (int index = 0; index < numberOfGroups; index++) {
                LogGroup logGroup = new LogGroup(messageType, field, dictionary);

                try {
                    messageOrGroup.getGroup(index + 1, group);

                    Iterator groupIterator = group.iterator();
                    while (groupIterator.hasNext()) {
                        Field groupField = (Field) groupIterator.next();
                        LogField fieldForLogGroup = LogField.createLogField(messageType, groupField, dictionary);
                        //check if this is, itself a group
                        addGroup(group, groupField, messageType, messageTypeValue, fieldForLogGroup, groupInfo.getDataDictionary() );
                        
                        logGroup.addField(fieldForLogGroup);

                    }
                } catch (FieldNotFound fieldNotFound) {
                    addValidationError(new ValidationError(fieldNotFound.getMessage()));
                }

                logField.addGroup(logGroup);
            }
        }
    }

    private Message createMessage() {
        String sohMessage = rawMessage.replace(DEFAULT_DELIMETER, SOH_DELIMETER);
        try {
            return new Message(sohMessage, dictionary, true);
        } catch (InvalidMessage invalidMessage) {

            addValidationError(new ValidationError(invalidMessage.getMessage()));

            try {
                return new Message(sohMessage, dictionary, false);
            } catch (InvalidMessage ugh) {
                addValidationError(new ValidationError("Failed to parse message without validation. " + ugh.getMessage()));
                return null;
            }
        }
    }

    private Map<Integer, Field> getAllFields(Message genericMessage) {
        Map<Integer, Field> allFields = new LinkedHashMap<Integer, Field>();

        Iterator iterator = genericMessage.getHeader().iterator();
        while (iterator.hasNext()) {
            Field field = (Field) iterator.next();
            allFields.put(field.getTag(), field);
        }

        iterator = genericMessage.iterator();
        while (iterator.hasNext()) {
            Field field = (Field) iterator.next();
            int tag = field.getTag();
            if (!allFields.containsKey(tag)) {
                allFields.put(tag, field);
            }
        }

        iterator = genericMessage.getTrailer().iterator();
        while (iterator.hasNext()) {
            Field field = (Field) iterator.next();
            allFields.put(field.getTag(), field);
        }

        return allFields;
    }

    private void addValidationError(ValidationError validationError) {

        if (validationErrors == null) {
            validationErrors = new ArrayList<ValidationError>();
        }

        validationErrors.add(validationError);
        isValid = false;
    }

    private String lookupMessageTypeName() {
        String messageTypeValue = FIXMessageHelper.getMessageType(rawMessage, DEFAULT_DELIMETER);

        if (messageTypeValue == null) {
            isValid = false;
            return null;
        }
        return dictionary.getValueName(MsgType.FIELD, messageTypeValue);
    }

    private Date lookupSendingTime() {
        try {
            Date date = FIXMessageHelper.getSendingTime(rawMessage, DEFAULT_DELIMETER);
            if (date == null) {
                addValidationError(new ValidationError("SendingTime missing"));
                return date;
            }
            return date;
        } catch (FieldConvertError fieldConvertError) {
            addValidationError(new ValidationError(fieldConvertError.getMessage()));
            return null;
        }
    }

    private MsgType getMessageType(Message message) {
        try {
            return (MsgType) message.getHeader().getField(new MsgType());
        } catch (FieldNotFound fieldNotFound) {
            throw new RuntimeException(fieldNotFound);
        }
    }
}