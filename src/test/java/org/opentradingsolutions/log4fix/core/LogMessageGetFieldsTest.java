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

import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.MsgSeqNum;
import quickfix.field.OrdType;
import quickfix.field.SenderCompID;
import quickfix.field.SendingTime;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.field.TransactTime;
import quickfix.fix42.NewOrderSingle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.opentradingsolutions.log4fix.util.FIXMessageTestHelper;

/**
 * @author Brian M. Coyner
 */
public class LogMessageGetFieldsTest extends AbstractSessionTestCase {
    private Date sendingTime;
    private int messageSequenceNumber;
    private FIXMessageTestHelper testHelper;

    @Override
    protected void doSetUp() throws Exception {
        sendingTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2010-01-01 11:01:56");
        messageSequenceNumber = 1;
        testHelper = new FIXMessageTestHelper(getSessionId());
    }

    public void testInvalidMessage() {
        Message message = createValidMessage(sendingTime);

        String invalidRawMessage = testHelper.removeField(Symbol.FIELD, message.toString());
        LogMessage logMessage = new LogMessage(1, true, getSessionId(), invalidRawMessage, getDictionary());

        // the message should not be invalid yet because we yet to parse the message.
        if (!logMessage.isValid()) {
            Assert.fail("LogMessage has validation errors: " + logMessage.getValidationErrorMessages());
        }

        // parse the message then assert that errors are found.
        logMessage.getLogFields();

        List<ValidationError> errorMessages = logMessage.getValidationErrorMessages();
        assertEquals(1, errorMessages.size());
        assertNotNull(errorMessages.get(0).getMessage());
    }

    private Message createValidMessage(Date sendingTime) {
        return createMessage(sendingTime, true);
    }

    private Message createMessage(Date sendingTime, boolean isValid) {
        Message message = new NewOrderSingle(new ClOrdID("12345"),
                new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE),
                new Symbol("COYNER"), new Side(Side.BUY),
                new TransactTime(sendingTime), new OrdType(OrdType.MARKET));

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
