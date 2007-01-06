package org.opentradingsolutions.log4fix.importer;

import quickfix.SessionID;

/**
 * This interface defines a way for a session Id to be created based
 * on the values passed to {@link #resolveSessionId(String, String, String)}.
 * For example, an implementation may display a UI allowing the user
 * to correct any of the given values. This might be useful when
 * importing a log file.
 *
 * @author Brian M. Coyner
 */
public interface SessionIdResolver {

    SessionID resolveSessionId(String beginString, String senderCompId,
            String targetCompId);
}
