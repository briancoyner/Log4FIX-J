package org.opentradingsolutions.log4fix.importer;

import org.opentradingsolutions.log4fix.model.LogMessage;
import quickfix.SessionID;
import quickfix.field.MsgType;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

/**
 * @author Brian M. Coyner
 */
public class LogMessageBuilder implements Runnable {

    private final BlockingQueue<String> fixMessages;
    private ImporterModel model;
    private SessionID senderSessionId;

    public LogMessageBuilder(ImporterModel model,
            BlockingQueue<String> fixMessages) {
        this.model = model;
        this.fixMessages = fixMessages;
    }

    public void run() {

        ImporterMemoryLog logger = model.getImporterMemoryLog();

        logger.onEvent("Start: " + new Date());
        try {
            while (true) {
                String rawMessage = fixMessages.take();
                if ("DONE".equals(rawMessage)) {
                    break;
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

                SessionID currentSessionId = new SessionID(beginString, senderCompId,
                        targetCompId);

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

                //messageCount++;
            }

            logger.onEvent("Done: " + new Date());

            //logger.onEvent("Message Count: " + messageCount);
            //logger.onEvent(new Date().toString());
        } catch (InterruptedException e) {
            logger.onEvent(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            logger.onEvent("Done");
        }
    }

    private boolean isIncomingMessage(SessionID currentSessionId,
            SessionID senderSessionId) {

        boolean incoming;
        if (senderSessionId.getSenderCompID().equals(currentSessionId.getSenderCompID())
                && senderSessionId.getTargetCompID()
                .equals(currentSessionId.getTargetCompID())) {
            incoming = false;
        } else if (senderSessionId.getSenderCompID().equals(currentSessionId.getTargetCompID())
                && senderSessionId.getTargetCompID().equals(currentSessionId.getSenderCompID())) {
            incoming = true;
        } else {
            throw new RuntimeException("Dang it! There is other session data " +
                    "mixed in. Log4FIX does not handle this.");
        }
        return incoming;
    }
}
