/*
 * The Log4FIX Software License
 * Copyright (c) 2006 - 2007 opentradingsolutions.org  All rights reserved.
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
 * 3. Neither the name of the product (Log4FIX), nor opentradingsolutions.org,
 *    nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL OPENTRADINGSOLUTIONS.ORG OR
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

import org.opentradingsolutions.log4fix.util.FIXMessageTestHelper;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.IDSource;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MarketDepth;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.SecurityID;
import quickfix.field.SenderCompID;
import quickfix.field.SendingTime;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.field.TransactTime;
import quickfix.fix42.MarketDataRequest;
import quickfix.fix42.MarketDataSnapshotFullRefresh;
import quickfix.fix42.NewOrderSingle;

import java.util.Date;
import java.util.List;

/**
 * @todo - I am not fond of the tests in this class. There has to be a better way
 * to test this class.
 *
 * @author Brian M. Coyner
 */
public class LogMessageGetFieldsTest extends AbstractSessionTestCase {
    private Date sendingTime;
    private int messageSequenceNumber;
    private FIXMessageTestHelper testHelper;

    @Override
    protected void doSetUp() throws Exception {
        sendingTime = new Date();
        messageSequenceNumber = 1;
        testHelper = new FIXMessageTestHelper(getSessionId());
    }

    public void testInvalidMessage() {
        Message message = createValidMessage(sendingTime);

        String invalidRawMessage = testHelper.removeField(Symbol.FIELD, message.toString());
        LogMessage logMessage = new LogMessage(1, true, getSessionId(), invalidRawMessage,
                getDictionary());

        assertTrue("The message should still be valid because we have not " +
                "parsed the message's fields.", logMessage.isValid());

        // parse the message then assert that errors were found.
        logMessage.getLogFields();

        List<ValidationError> errorMessages = logMessage.getValidationErrorMessages();
        assertEquals(1, errorMessages.size());
        assertNotNull(errorMessages.get(0).getMessage());
    }

//    /**
//     * The message is missing the symbol field and sending time header field.
//     */
//    public void testInvalidMessageWithMultipleProblems() {
//        Message message = createValidMessage(null);
//
//        String invalidRawMessage = FIXMessageTestHelper.removeField(
//                Symbol.FIELD, message.toString());
//        LogMessage logMessage = new LogMessage(true, getSessionId(), invalidRawMessage,
//                getDictionary());
//        assertFalse(logMessage.isValid());
//
//        logMessage.getLogFields();
//
//        List<Exception> errorMessages = logMessage.getValidationErrorMessages();
//        assertEquals(2, errorMessages.size());
//        assertNotNull(errorMessages.get(0).getMessage());
//        assertEquals(SendingTime.FIELD, ((FieldNotFound) errorMessages.get(1)).field);
//    }
//
//
//    public void testValidMessageWithOneRepeatingGroup() throws FieldNotFound {
//        Message message = createMarketDataMessage();
//        LogMessage logMessage = new LogMessage(true, getSessionId(),
//                message.toString(), getDictionary());
//        assertValidLogMessage(message, logMessage);
//
//        List<LogField> logFields = logMessage.getLogFields();
//
//        int expectedGroupFields = 1;
//        int actualGroupFields = 0;
//
//        for (LogField logField : logFields) {
//            if (logField.isRepeatingGroup()) {
//
//                actualGroupFields++;
//                List<LogGroup> groups = logField.getGroups();
//
//                // fail-fast if the group count is not correct
//                assertEquals(2, groups.size());
//                for (LogGroup logGroup : groups) {
//                    List<LogField> fields = logGroup.getFields();
//                    assertEquals(4, fields.size());
//                }
//            }
//        }
//
//        assertEquals("Group Count.", expectedGroupFields, actualGroupFields);
//    }
//
//    public void testValidMessageWithTwoRepeatingGroups() throws FieldNotFound {
//        Message message = createMarketDataRequest();
//        LogMessage logMessage = new LogMessage(true, getSessionId(),
//                message.toString(), getDictionary());
//        assertValidLogMessage(message, logMessage);
//
//        List<LogField> logFields = logMessage.getLogFields();
//
//        int expectedGroupFields = 2;
//        int actualGroupFields = 0;
//        for (LogField logField : logFields) {
//            if (logField.isRepeatingGroup()) {
//
//                actualGroupFields++;
//                List<LogGroup> groups = logField.getGroups();
//
//                if (logField.getTag() == NoRelatedSym.FIELD) {
//                    // fail-fast if the group count is not correct
//                    assertEquals(1, groups.size());
//                    for (LogGroup logGroup : groups) {
//                        List<LogField> fields = logGroup.getFields();
//                        assertEquals(3, fields.size());
//                    }
//                } else if (logField.getTag() == NoMDEntryTypes.FIELD){
//                    // fail-fast if the group count is not correct
//                    assertEquals(2, groups.size());
//                    for (LogGroup logGroup : groups) {
//                        List<LogField> fields = logGroup.getFields();
//                        assertEquals(1, fields.size());
//                    }
//                }
//            }
//        }
//
//        assertEquals("Group Count.", expectedGroupFields, actualGroupFields);
//
//    }

    private void assertValidLogMessage(Message message,
            LogMessage logMessage) throws FieldNotFound {
        assertLogMessage(message, true, logMessage);

    }

    private void assertLogMessage(Message message, boolean isValidMessage,
            LogMessage logMessage) throws FieldNotFound {

        assertEquals(isValidMessage, logMessage.isValid());
        assertTrue(logMessage.isIncoming());
        assertSame(getSessionId(), logMessage.getSessionId());
        assertEquals(message.toString().replace(LogMessage.SOH_DELIMETER,
                LogMessage.DEFAULT_DELIMETER),
                logMessage.getRawMessage());

        assertEquals(getDictionary().getValueName(MsgType.FIELD,
                message.getHeader().getString(MsgType.FIELD)),
                logMessage.getMessageTypeName());

        if (isValidMessage) {
            assertNull(logMessage.getValidationErrorMessages());
        } else {
            assertNotNull(logMessage.getValidationErrorMessages());
        }
    }

    private Message createValidMessage(Date sendingTime) {
        return createMessage(sendingTime, true);
    }

    private Message createMarketDataMessage() {
        MarketDataSnapshotFullRefresh message =
                new MarketDataSnapshotFullRefresh(new Symbol("AAPL"));

        MarketDataSnapshotFullRefresh.NoMDEntries group =
                new MarketDataSnapshotFullRefresh.NoMDEntries();

        group.set(new MDEntryType('0'));
        group.set(new MDEntryPx(12.32));
        group.set(new MDEntrySize(100));
        group.set(new OrderID("ORDERID"));
        message.addGroup(group);

        group.set(new MDEntryType('1'));
        group.set(new MDEntryPx(12.32));
        group.set(new MDEntrySize(100));
        group.set(new OrderID("ORDERID"));
        message.addGroup(group);

        setHeaderFields(message, sendingTime, true);

        return message;
    }

    private Message createMarketDataRequest() {
        MarketDataRequest message = new MarketDataRequest(new MDReqID("1"),
                new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES),
                new MarketDepth(0));

        MarketDataRequest.NoRelatedSym group =
                new MarketDataRequest.NoRelatedSym();
        group.set(new Symbol("ADSX"));
        group.set(new SecurityID("SECRET"));
        group.set(new IDSource("1"));
        message.addGroup(group);


        MarketDataRequest.NoMDEntryTypes type = new MarketDataRequest.NoMDEntryTypes();
        type.set(new MDEntryType(MDEntryType.BID));
        message.addGroup(type);

        type = new MarketDataRequest.NoMDEntryTypes();
        type.set(new MDEntryType(MDEntryType.OFFER));
        message.addGroup(type);

        setHeaderFields(message, sendingTime, true);

        return message;
    }

    private Message createMessage(Date sendingTime, boolean isValid) {
        Message message = new NewOrderSingle(new ClOrdID("12345"),
                new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE),
                new Symbol("COYNER"), new Side(Side.BUY),
                new TransactTime(new Date()), new OrdType(OrdType.MARKET));

        setHeaderFields(message, sendingTime, isValid);
        return message;
    }

    private void setHeaderFields(Message message, Date sendingTime, boolean isValid) {
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
    }
}
