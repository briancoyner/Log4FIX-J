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

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.MsgType;

import java.util.Date;
import java.util.List;

import org.opentradingsolutions.log4fix.util.FIXMessageTestHelper;

/**
 * @author Brian M. Coyner
 */
public class LogMessageTest extends AbstractSessionTestCase {
    private Date sendingTime;
    private FIXMessageTestHelper testHelper;

    @Override
    protected void doSetUp() throws Exception {
        sendingTime = new Date();
        testHelper = new FIXMessageTestHelper(getSessionId());
    }

    public void testGetMessageTypeWhenTheMessageTypeIsMissing() {
        Message message = testHelper.createValidMessage(sendingTime);

        String invalidRawMessage = testHelper.removeField(MsgType.FIELD, message.toString());
        LogMessage logMessage = new LogMessage(1, true, getSessionId(), invalidRawMessage, getDictionary());

        assertNull(logMessage.getMessageTypeName());
        assertFalse(logMessage.isValid());
        assertNull(logMessage.getValidationErrorMessages());
    }

    public void testMessageWithoutSendingTimeIsAnInvalidMessage() {
        Message message = testHelper.createValidMessage(null);
        LogMessage logMessage = new LogMessage(1, true, getSessionId(), message.toString(), getDictionary());

        assertNull(logMessage.getSendingTime());
        assertFalse("A message without a sending time should not be valid.",
                logMessage.isValid());

        List<ValidationError> errorMessages = logMessage.getValidationErrorMessages();
        assertEquals(1, errorMessages.size());
    }

    public void testValidMessage() throws FieldNotFound {

        Message message = testHelper.createValidMessage(sendingTime);
        LogMessage logMessage = new LogMessage(1, true, getSessionId(), message.toString(), getDictionary());
        assertValidLogMessage(message, logMessage);
    }

    private void assertValidLogMessage(Message message, LogMessage logMessage) throws FieldNotFound {
        assertLogMessage(message, true, logMessage);

    }

    private void assertLogMessage(Message message, boolean isValidMessage, LogMessage logMessage) throws FieldNotFound {

        assertEquals(isValidMessage, logMessage.isValid());
        assertTrue(logMessage.isIncoming());
        assertSame(getSessionId(), logMessage.getSessionId());
        assertEquals(message.toString().replace(LogMessage.SOH_DELIMETER, LogMessage.DEFAULT_DELIMETER),
                logMessage.getRawMessage());

        assertEquals(getDictionary().getValueName(MsgType.FIELD, message.getHeader().getString(MsgType.FIELD)),
                logMessage.getMessageTypeName());

        if (isValidMessage) {
            assertNull(logMessage.getValidationErrorMessages());
        } else {
            assertNotNull(logMessage.getValidationErrorMessages());
        }
    }
}
