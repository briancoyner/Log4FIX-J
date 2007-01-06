package org.opentradingsolutions.log4fix.model;

import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.MsgType;
import quickfix.field.SendingTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brian M. Coyner
 */
public class LogMessage {

    public static final char DEFAULT_LOG_MESSAGE_DELIMETER = '|';
    public static final char SOH_DELIMETER = (char) 0x01;

    public static final String INCOMING = "incoming";
    public static final String MESSAGE_TYPE_NAME = "messageTypeName";
    public static final String SENDING_TIME = "sendingTime";
    public static final String RAW_MESSAGE = "rawMessage";

    private SessionID sessionId;
    private boolean incoming;
    private String rawMessage;
    private Message message;
    private String messageTypeName;
    private Date sendingTime;
    private DataDictionary dictionary;
    private String validationErrorMessage;

    public LogMessage(boolean incoming, SessionID sessionId,
            String rawMessage, DataDictionary dictionary) {
        this.dictionary = dictionary;
        this.rawMessage = rawMessage.replace(SOH_DELIMETER,
                DEFAULT_LOG_MESSAGE_DELIMETER);
        this.sessionId = sessionId;
        this.incoming = incoming;
        this.message = createMessage(rawMessage, dictionary);
        sendingTime = lookupSendingTime();
        messageTypeName = lookupMessageTypeName();
    }

    public SessionID getSessionId() {
        return sessionId;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public Message getMessage() {
        return message;
    }

    public String getMessageTypeName() {
        return messageTypeName;
    }

    public boolean isValid() {
        return message.hasValidStructure();
    }

    public boolean isIncoming() {
        return incoming;
    }

    public Date getSendingTime() {
        return sendingTime;
    }

    /**
     * This method should only be called if {@link #isValid()} returns
     * <code>true</code>.
     *
     * @return the reason why this message is invalid or null if the message
     * is valid.
     */
    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    /**
     * This method collects all <code>Fields</code> into a map. Each field
     * is picked out of the raw message string and is looked up from the map. This
     * ensures that we display the fields in the same order as the raw message string.
     * Why? Because the message class does not provide a way to get the fields in
     * the sent order. The only time we care about the field order is when logging.
     *
     * <p/>
     *
     * This method executes on the Event Dispatch Thread, so we are not slowing down
     * the quickfix thread.
     */
    public List<LogField> getLogFields() {

        List<LogField> logFields = new ArrayList<LogField>();

        Map<Integer, Field> allFields = getAllFields(message);

        String[] fields = rawMessage.split("\\|");

        for (String fieldString : fields) {
            int indexOfEqual = fieldString.indexOf('=');
            int tag = Integer.parseInt(fieldString.substring(0, indexOfEqual));

            Field field = allFields.remove(tag);
            logFields.add(new LogField(getMessageType(), field, dictionary));
        }

        return logFields;
    }

    private Message createMessage(String rawMessage, DataDictionary dictionary) {
        try {
            return new Message(rawMessage, dictionary, true);
        } catch (InvalidMessage invalidMessage) {
            validationErrorMessage = invalidMessage.getMessage();

            try {
                return new Message(rawMessage, false);
            } catch (InvalidMessage ugh) {
                // should not happen.
                throw new RuntimeException("Failed to parse message without validation.",
                        ugh);
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
            allFields.put(field.getTag(), field);
        }

        iterator = genericMessage.getTrailer().iterator();
        while (iterator.hasNext()) {
            Field field = (Field) iterator.next();
            allFields.put(field.getTag(), field);
        }

        return allFields;
    }

    private Date lookupSendingTime() {
        try {
            return message.getHeader().getUtcTimeStamp(SendingTime.FIELD);
        } catch (FieldNotFound fieldNotFound) {
            return null;
        }
    }

    private String lookupMessageTypeName() {

        try {
            String messageTypeValue = getMessageTypeAsString();
            return dictionary.getValueName(MsgType.FIELD, messageTypeValue);
        } catch (FieldNotFound fieldNotFound) {
            return "Field [MsgType=" + MsgType.FIELD + "] not found.";
        }
    }

    private MsgType getMessageType() {
        try {
            return (MsgType) message.getHeader().getField(new MsgType());
        } catch (FieldNotFound fieldNotFound) {
            throw new RuntimeException(fieldNotFound);
        }
    }

    private String getMessageTypeAsString() throws FieldNotFound {
        return message.getHeader().getString(MsgType.FIELD);
    }
}