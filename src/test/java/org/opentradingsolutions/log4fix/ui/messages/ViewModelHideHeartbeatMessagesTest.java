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

package org.opentradingsolutions.log4fix.ui.messages;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

import org.opentradingsolutions.log4fix.core.AbstractSessionTestCase;
import org.opentradingsolutions.log4fix.core.LogMessage;
import org.opentradingsolutions.log4fix.core.MockMemoryLogModel;
import org.opentradingsolutions.log4fix.util.FIXMessageTestHelper;

/**
 * @author Brian M. Coyner
 */
public class ViewModelHideHeartbeatMessagesTest extends AbstractSessionTestCase {

    private JCheckBox checkBox;
    private ViewModel model;
    private MockMemoryLogModel memoryLogModel;
    private FIXMessageTestHelper testHelper;

    @Override
    public void doSetUp() {
        checkBox = new JCheckBox();
        memoryLogModel = new MockMemoryLogModel();
        model = new ViewModel(memoryLogModel);
        checkBox.addActionListener(model.getHideHeartbeatsActionListener());

        testHelper = new FIXMessageTestHelper(getSessionId());
    }

    public void testHideHeartbeatsWhenThereAreNoMessagesIsANoOp() {

        memoryLogModel.clear();
        checkBox.setSelected(true);
        assertEquals(0, memoryLogModel.getMessages().size());
    }

    public void testHideHeartbeatsWhenThereAreNoHeartbeatMessages() {

        List<LogMessage> messages = new ArrayList<LogMessage>();
        messages.add(new LogMessage(0, true, getSessionId(), testHelper.createValidMessage().toString(),
                getDictionary()));
        messages.add(new LogMessage(1, true, getSessionId(), testHelper.createValidMessage().toString(),
                getDictionary()));
        messages.add(new LogMessage(2, true, getSessionId(), testHelper.createValidMessage().toString(),
                getDictionary()));

        for (LogMessage message : messages) {
            memoryLogModel.addLogMessage(message);
        }

        checkBox.doClick();

        assertEquals(messages, memoryLogModel.getMessages());
        assertEquals(messages, model.getSortedList());
    }

    public void testHideOneHeartbeatMessage() {

        List<LogMessage> noHeartbeats = new ArrayList<LogMessage>();
        noHeartbeats.add(new LogMessage(0, true, getSessionId(), testHelper.createValidMessage().toString(),
                getDictionary()));
        noHeartbeats.add(new LogMessage(1, true, getSessionId(), testHelper.createValidMessage().toString(),
                getDictionary()));
        noHeartbeats.add(new LogMessage(2, true, getSessionId(), testHelper.createValidMessage().toString(),
                getDictionary()));

        for (LogMessage message : noHeartbeats) {
            memoryLogModel.addLogMessage(message);
        }

        // now create the heartbeats
        List<LogMessage> heartbeats = new ArrayList<LogMessage>();
        heartbeats.add(new LogMessage(3, true, getSessionId(), testHelper.createHeartbeatMessage().toString(),
                getDictionary()));

        for (LogMessage message : heartbeats) {
            memoryLogModel.addLogMessage(message);
        }

        // filter the heartbeats
        checkBox.doClick();

        List<LogMessage> allMessages = new ArrayList<LogMessage>();
        allMessages.addAll(noHeartbeats);
        allMessages.addAll(heartbeats);

        assertEquals(allMessages, memoryLogModel.getMessages());
        assertEquals(noHeartbeats, model.getSortedList());
        assertEquals(noHeartbeats, model.getFilteredList());
    }
}