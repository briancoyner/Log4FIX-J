package org.opentradingsolutions.log4fix.importer;

import quickfix.SessionID;

/**
 * @author Brian M. Coyner
 */
public class PassThroughSessionIdResolver implements SessionIdResolver {
    public SessionID resolveSessionId(String beginString, String senderCompId,
            String targetCompId) {
        return new SessionID(beginString, senderCompId, targetCompId);
    }
}
