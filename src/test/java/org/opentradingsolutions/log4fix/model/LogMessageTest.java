package org.opentradingsolutions.log4fix.model;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.OrdType;
import quickfix.field.SenderCompID;
import quickfix.field.SendingTime;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.field.TransactTime;
import quickfix.fix42.NewOrderSingle;

import java.util.Date;

/**
 * @author Brian M. Coyner
 */
public class LogMessageTest extends AbstractSessionTestCase {
    private Date sendingTime;
    private int messageSequenceNumber;

    protected void doSetUp() throws Exception {
        sendingTime = new Date();
        messageSequenceNumber = 1;
    }

    public void testMessageWithoutSendingTimeReturnsNull() {
        Message message = createValidMessage(null);
        LogMessage logMessage = new LogMessage(true, getSessionId(),
                message.toString(), getDictionary());
        assertNull(logMessage.getSendingTime());
    }

    public void testValidMessage() throws FieldNotFound {

        Message message = createValidMessage(sendingTime);
        LogMessage logMessage = new LogMessage(true, getSessionId(),
                message.toString(), getDictionary());
        assertValidLogMessage(message, logMessage);
    }

//    public void testInvalidMessage() throws FieldNotFound {
//
//        Message message = createInvalidMessage(sendingTime);
//        LogMessage logMessage = new LogMessage(true, sessionId,
//                message.toString(), dictionary);
//
//        assertInvalidLogMessage(message, logMessage);
//    }

    private void assertValidLogMessage(Message message,
            LogMessage logMessage) throws FieldNotFound {
        assertLogMessage(message, true, logMessage);

    }

//    private void assertInvalidLogMessage(Message message,
//            LogMessage logMessage) throws FieldNotFound {
//        assertLogMessage(message, false, logMessage);
//    }

    private void assertLogMessage(Message message, boolean isValidMessage,
            LogMessage logMessage) throws FieldNotFound {

        assertEquals(isValidMessage, logMessage.isValid());
        assertTrue(logMessage.isIncoming());
        assertSame(getSessionId(), logMessage.getSessionId());
        assertEquals(message.toString().replace(LogMessage.SOH_DELIMETER,
                LogMessage.DEFAULT_LOG_MESSAGE_DELIMETER),
                logMessage.getRawMessage());

        assertEquals(getDictionary().getValueName(MsgType.FIELD,
                message.getHeader().getString(MsgType.FIELD)),
                logMessage.getMessageTypeName());

        if (isValidMessage) {
            assertNull(logMessage.getValidationErrorMessage());
        } else {
            assertNotNull(logMessage.getValidationErrorMessage());
        }
    }

    private Message createValidMessage(Date sendingTime) {
        return createMessage(sendingTime, true);
    }

//    private Message createInvalidMessage(Date sendingTime) {
//        return createMessage(sendingTime, false);
//    }

    private Message createMessage(Date sendingTime, boolean isValid) {
        Message message = new NewOrderSingle(new ClOrdID("12345"),
                new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE),
                new Symbol("COYNER"), new Side(Side.BUY),
                new TransactTime(new Date()), new OrdType(OrdType.MARKET));

        SessionID sessionId = getSessionId();
        message.getHeader().setString(SenderCompID.FIELD, sessionId.getSenderCompID());
        message.getHeader().setString(TargetCompID.FIELD, sessionId.getTargetCompID());
        message.getHeader().setInt(MsgSeqNum.FIELD, messageSequenceNumber);

        if (sendingTime != null) {
            if (isValid) {
                message.getHeader().setUtcTimeStamp(SendingTime.FIELD, sendingTime, true);
            } else {
                message.setUtcTimeStamp(SendingTime.FIELD, sendingTime, true);
            }
        }
        return message;
    }
}
