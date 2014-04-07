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

package org.opentradingsolutions.log4fix.importer;

import quickfix.SessionID;
import quickfix.field.MsgType;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

import org.opentradingsolutions.log4fix.core.LogMessage;

/**
 * @author Brian M. Coyner
 */
public class LogMessageBuilder implements Runnable {

    public static final String EVENT_START = "Start";
    public static final String EVENT_ERROR = "ERROR";
    public static final String EVENT_MESSAGES_IMPORTED = "Messages Imported";
    public static final String EVENT_COMPLETE = "Complete";

    private final BlockingQueue<String> fixMessages;
    private final ImporterModel model;

    private SessionID senderSessionId;

    public LogMessageBuilder(ImporterModel model, BlockingQueue<String> fixMessages) {
        this.model = model;
        this.fixMessages = fixMessages;
    }

    public void run() {

        ImporterMemoryLog logger = model.getImporterMemoryLog();

        logger.onEvent(EVENT_START + ": " + new Date());
        int messageCount = 0;

        try {
            while (true) {
                String rawMessage = fixMessages.take();
                if ("DONE".equals(rawMessage)) {
                    break;
                } else if (rawMessage.startsWith(EVENT_ERROR)) {
                    logger.onEvent(rawMessage);
                    continue;
                }

                int beginIndex = 2; // 8= takes up 0 and 1... value starts at 2.
                int endIndex = rawMessage.indexOf(LogMessage.SOH_DELIMETER, beginIndex);
                String beginString = rawMessage.substring(2, endIndex);

                beginIndex = rawMessage.indexOf("35=", endIndex) + 3;
                endIndex = rawMessage.indexOf(LogMessage.SOH_DELIMETER, beginIndex);
                String messageType = rawMessage.substring(beginIndex, endIndex);

                beginIndex = rawMessage.indexOf("49=", endIndex) + 3;
                endIndex = rawMessage.indexOf(LogMessage.SOH_DELIMETER, beginIndex);
                String senderCompId = rawMessage.substring(beginIndex, endIndex);

                beginIndex = rawMessage.indexOf("56=", endIndex) + 3;
                endIndex = rawMessage.indexOf(LogMessage.SOH_DELIMETER, beginIndex);
                String targetCompId = rawMessage.substring(beginIndex, endIndex);

                SessionID currentSessionId = new SessionID(beginString, senderCompId, targetCompId);

                // hopefully the first message we find is the initiator's logon...
                // @todo - currently it is assumed that the person running this
                // application is always the initiator. Fix this.
                if (senderSessionId == null) {
                    if (MsgType.LOGON.equals(messageType)) {
                        senderSessionId = currentSessionId;
                    } else {
                        // the logon message is missing... resolve the session Id.
                        currentSessionId = model.getSessionIdResolver().resolveSessionId(
                                currentSessionId.getBeginString(),
                                currentSessionId.getSenderCompID(),
                                currentSessionId.getTargetCompID());
                        senderSessionId = currentSessionId;
                    }

                    logger.setSessionId(senderSessionId);
                    logger.onEvent("Initiator Session Id: " + senderSessionId);
                }

                if (isIncomingMessage(currentSessionId, senderSessionId)) {
                    logger.onIncoming(rawMessage);
                } else {
                    logger.onOutgoing(rawMessage);
                }
                messageCount++;
            }
        } catch (InterruptedException e) {
            logger.onEvent("**** ERROR ****");
            logger.onEvent("The message builder thread was interrupted due to the " +
                    "following error:");
            logger.onEvent(" - " + e.getMessage());

            // print some extra information about the failure if multiple sessions
            // were found in the log file.
            if (e instanceof MultipleSessionsException) {
                MultipleSessionsException mse = (MultipleSessionsException) e;
                logger.onEvent(" - First Session Id: " + mse.getFirstSessionId());
                logger.onEvent(" - Second Session Id: " + mse.getSecondSessionId());
            }

            logger.onEvent("****  END  ****");
        } finally {
            logger.onEvent(EVENT_MESSAGES_IMPORTED + ": " + messageCount);
            logger.onEvent(EVENT_COMPLETE + ": " + new Date());
        }
    }

    private boolean isIncomingMessage(SessionID currentSessionId,
            SessionID senderSessionId) throws InterruptedException {

        boolean incoming;
        if (senderSessionId.getSenderCompID().equals(currentSessionId.getSenderCompID())
                && senderSessionId.getTargetCompID().equals(currentSessionId.getTargetCompID())) {

            incoming = false;
        } else if (senderSessionId.getSenderCompID().equals(currentSessionId.getTargetCompID())
                && senderSessionId.getTargetCompID().equals(currentSessionId.getSenderCompID())) {

            incoming = true;
        } else {
            throw new MultipleSessionsException(senderSessionId, currentSessionId);
        }
        return incoming;
    }
}