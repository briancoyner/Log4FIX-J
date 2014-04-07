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

import quickfix.DataDictionary;
import quickfix.Log;
import quickfix.SessionID;

import java.util.concurrent.atomic.AtomicInteger;

import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;

/**
 * QuickFIX/J {@link Log} implementation that maintains all QuickFIX messages
 * in memory.
 *
 * @author Brian M. Coyner
 */
public abstract class AbstractMemoryLog implements Log {

    private final DataDictionaryLoader dictionaryLoader;
    private final AtomicInteger index;

    public AbstractMemoryLog(DataDictionaryLoader dictionaryLoader) {
        this.dictionaryLoader = dictionaryLoader;
        index = new AtomicInteger();
    }

    @Override
    public void clear() {
        getMemoryLogModel().clear();
    }

    @Override
    public void onIncoming(String message) {
        log(message, true);
    }

    @Override
    public void onOutgoing(String message) {
        log(message, false);
    }

    @Override
    public void onEvent(String text) {
        getMemoryLogModel().addLogEvent(new LogEvent(text));
    }

    @Override
    public void onErrorEvent(String s) {
        // TODO - new to QFJ 1.5. Currently ignored.
    }

    protected abstract SessionID getSessionId();

    protected abstract MemoryLogModel getMemoryLogModel();

    private void log(final String rawMessage, final boolean incoming) {
        SessionID sessionId = getSessionId();
        int messageIndex = index.getAndIncrement();

        DataDictionary dictionary = dictionaryLoader.loadDictionary(sessionId);
        LogMessage message = new LogMessage(messageIndex, incoming, sessionId, rawMessage, dictionary);
        getMemoryLogModel().addLogMessage(message);
    }
}