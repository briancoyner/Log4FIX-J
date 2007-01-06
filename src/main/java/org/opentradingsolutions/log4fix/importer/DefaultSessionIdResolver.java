package org.opentradingsolutions.log4fix.importer;

import quickfix.SessionID;

/**
 * @author Brian M. Coyner
 */
public class DefaultSessionIdResolver implements SessionIdResolver {
    
    private SessionID sessionId;

    public DefaultSessionIdResolver(String realBeginString, String realSenderCompId,
            String realTargetCompId) {

        sessionId = new SessionID(realBeginString, realSenderCompId,
                realTargetCompId);
    }

    public SessionID resolveSessionId(String beginString, String senderCompId,
            String targetCompId) {
        return sessionId;
    }
}
