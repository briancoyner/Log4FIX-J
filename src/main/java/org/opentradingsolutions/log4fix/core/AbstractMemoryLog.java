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

import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import quickfix.Log;
import quickfix.SessionID;

/**
 * QuickFIX/J {@link Log} implementation that maintains all QuickFIX messages
 * in memory.
 *
 * @author Brian M. Coyner
 */
public abstract class AbstractMemoryLog implements Log {

    private DataDictionaryLoader dictionaryLoader;

    public AbstractMemoryLog(DataDictionaryLoader dictionaryLoader) {
        this.dictionaryLoader = dictionaryLoader;
    }

    public void clear() {
        getMemoryLogModel().clear();
    }

    public void onIncoming(String message) {
        log(message, true);
    }

    public void onOutgoing(String message) {
        log(message, false);
    }

    public void onEvent(String text) {
        getMemoryLogModel().addLogEvent(new LogEvent(text));
    }

    protected abstract SessionID getSessionId();

    protected abstract MemoryLogModel getMemoryLogModel();

    private void log(final String rawMessage, final boolean incoming) {
        SessionID sessionId = getSessionId();
        getMemoryLogModel().addLogMessage(new LogMessage(incoming, sessionId, rawMessage,
                dictionaryLoader.loadDictionary(sessionId)));
    }
}