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

import quickfix.Log;
import quickfix.LogFactory;
import quickfix.SessionID;

import java.util.Map;

import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;

/**
 * An implementation of a QuickFIX {@link LogFactory}. This factory
 * creates a {@link LiveMemoryLog} for each configured {@link quickfix.Session}.
 *
 * @author Brian M. Coyner
 */
public class MemoryLogFactory implements LogFactory {

    private final DataDictionaryLoader dictionaryLoader;
    private final Map<SessionID, MemoryLogModel> memoryLogModels;

    /**
     * @param memoryLogModels a collection of {@link org.opentradingsolutions.log4fix.core.MemoryLogModel}s mapped using
     * a <code>SessionID</code>. There is one <code>MemoryLogModel</code> for each session ID.
     * @param dictionaryLoader
     */
    public MemoryLogFactory(Map<SessionID, MemoryLogModel> memoryLogModels, DataDictionaryLoader dictionaryLoader) {

        this.memoryLogModels = memoryLogModels;
        this.dictionaryLoader = dictionaryLoader;
    }

    /**
     * @throws UnsupportedOperationException because this method should not be used because it is deprecated in QFJ 1.4.
     */
    public Log create() {
        throw new UnsupportedOperationException("Use the 'create(SessionID)' method.");
    }

    /**
     * @return a new {@link LiveMemoryLog}.
     * @throws RuntimeException if the given <code>SessionID</code> is not found in the collection of
     * <code>MemoryLogModel</code> passed to the constructor.
     */
    public Log create(SessionID sessionId) {

        MemoryLogModel model = memoryLogModels.get(sessionId);
        if (model == null) {
            throw new RuntimeException("Unable to find core for Session Id: " + sessionId);
        }

        return new LiveMemoryLog(model, sessionId, dictionaryLoader);
    }
}
