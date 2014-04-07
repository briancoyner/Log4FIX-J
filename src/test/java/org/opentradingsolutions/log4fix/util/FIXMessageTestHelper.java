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

package org.opentradingsolutions.log4fix.util;

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
import quickfix.fix42.Heartbeat;
import quickfix.fix42.NewOrderSingle;

import java.util.Date;

/**
 * @author Brian M. Coyner
 */
public class FIXMessageTestHelper {

    private int messageSequenceNumber = 1;
    private SessionID sessionId;

    public FIXMessageTestHelper(SessionID sessionId) {
        this.sessionId = sessionId;
    }

    public Message createValidMessage() {
        return createMessage(new Date(), true);
    }

    public Message createValidMessage(Date sendingTime) {
        return createMessage(sendingTime, true);
    }

    public Message createMessage(Date sendingTime, boolean isValid) {
        Message message = new NewOrderSingle(new ClOrdID("12345"),
                new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE),
                new Symbol("COYNER"), new Side(Side.BUY),
                new TransactTime(new Date()), new OrdType(OrdType.MARKET));

        setHeaderFields(message, sendingTime, isValid);
        return message;
    }

    public void setHeaderFields(Message message, Date sendingTime, boolean isValid) {
        message.getHeader().setString(SenderCompID.FIELD, sessionId.getSenderCompID());
        message.getHeader().setString(TargetCompID.FIELD, sessionId.getTargetCompID());
        message.getHeader().setInt(MsgSeqNum.FIELD, messageSequenceNumber++);

        if (sendingTime != null) {
            if (isValid) {
                message.getHeader().setUtcTimeStamp(SendingTime.FIELD, sendingTime, true);
            } else {
                message.setUtcTimeStamp(SendingTime.FIELD, sendingTime, true);
            }
        }
    }

    public String removeField(int tag, String rawMessage) {
        return removeField(tag, rawMessage, (char) 0x01);
    }

    public String removeField(int tag, String rawMessage, char delimeter) {
        int messageTypeIndex = rawMessage.indexOf(String.valueOf(tag));
        String msg = rawMessage.substring(0, messageTypeIndex);
        int nextFieldIndex = rawMessage.indexOf(delimeter, messageTypeIndex);
        msg += rawMessage.substring(nextFieldIndex + 1);
        return msg;
    }

    public Message createHeartbeatMessage() {
        return new Heartbeat();
    }
}
