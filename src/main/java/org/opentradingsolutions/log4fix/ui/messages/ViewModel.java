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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.beans.BeanTableFormat;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import ca.odell.glazedlists.impl.matchers.NotMatcher;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.treetable.TreeTableModel;
import org.opentradingsolutions.log4fix.core.LogEvent;
import org.opentradingsolutions.log4fix.core.LogField;
import org.opentradingsolutions.log4fix.core.LogMessage;
import org.opentradingsolutions.log4fix.core.MemoryLogModel;
import org.opentradingsolutions.log4fix.core.ValidationError;
import org.opentradingsolutions.log4fix.ui.fields.FieldTreeNode;
import org.opentradingsolutions.log4fix.ui.fields.FieldTreeTableModel;
import org.opentradingsolutions.log4fix.ui.fields.RootNode;

/**
 * Simple core representing raw and cracked FIX messages. This core provides
 * various Swing core objects that are used to bind the data to Swing table components.
 *
 * @author Brian M. Coyner
 */
public class ViewModel implements ListSelectionListener {

    private EventTableModel<LogMessage> rawMessagesTableModel;
    private EventSelectionModel<LogMessage> rawMessagesSelectionModel;
    private TableCellRenderer rawMessagesTableCellRenderer;

    private final FieldTreeTableModel treeTableModel;
    private final MemoryLogModel memoryLogModel;
    private final SortedList<LogMessage> sortedList;
    private final FilterList<LogMessage> filteredList;
    private final EventListModel<LogEvent> eventsListModel;

    public ViewModel(MemoryLogModel memoryLogModel) {
        this.memoryLogModel = memoryLogModel;

        EventList<LogMessage> originalList = (EventList<LogMessage>) memoryLogModel.getMessages();
        filteredList = new FilterList<LogMessage>(originalList);
        sortedList = new SortedList<LogMessage>(filteredList);
        createRaw(sortedList);

        treeTableModel = new FieldTreeTableModel();
        eventsListModel = new EventListModel<LogEvent>((EventList<LogEvent>) memoryLogModel.getEvents());
    }

    public EventListModel<LogEvent> getEventsListModel() {
        return eventsListModel;
    }

    SortedList<LogMessage> getSortedList() {
        return sortedList;
    }

    FilterList getFilteredList() {
        return filteredList;
    }

    public EventSelectionModel<LogMessage> getRawMessagesSelectionModel() {
        return rawMessagesSelectionModel;
    }

    public TableCellRenderer getRawMessagesTableCellRenderer() {
        return rawMessagesTableCellRenderer;
    }

    public TableModel getRawMessagesTableModel() {
        return rawMessagesTableModel;
    }

    public TreeTableModel getAdminMessagesCrackedModel() {
        return treeTableModel;
    }

    public void valueChanged(ListSelectionEvent e) {

        if (e.getValueIsAdjusting()) {
            return;
        }

        if (!rawMessagesSelectionModel.isSelectionEmpty()) {
            EventList<LogMessage> selectedMessage = rawMessagesSelectionModel.
                    getSelected();

            RootNode rootNode = new RootNode();

            LogMessage logMessage = selectedMessage.get(0);

            List<LogField> logFields = logMessage.getLogFields();
            for (LogField logField : logFields) {
                FieldTreeNode fieldNode = new FieldTreeNode(logField);
                rootNode.addFieldTreeNode(fieldNode);
            }

            List<ValidationError> list = logMessage.getValidationErrorMessages();
            if (list != null && list.size() > 0) {

                // this string is for the "log" output... not a LogEvent.
                String logMessageErrors = "Message Errors (" +
                        logMessage.getMessageTypeName() + "):";
                memoryLogModel.addLogEvent(new LogEvent(logMessageErrors));
                for (ValidationError validationError : list) {
                    String message = validationError.getMessage();

                    memoryLogModel.addLogEvent(new LogEvent("    " + message));

                    logMessageErrors += "\n    " + message;
                }

                Logger.getAnonymousLogger().info(logMessageErrors);

                // we are done with the messages
                list.clear();
            }

            treeTableModel.setRoot(rootNode);
        } else {
            treeTableModel.setRoot(null);
        }
    }

    private void createRaw(EventList<LogMessage> adminMessages) {
        String[] properties = {LogMessage.INCOMING, LogMessage.MESSAGE_TYPE_NAME,
                LogMessage.SENDING_TIME, LogMessage.RAW_MESSAGE};
        String[] columnNames = {"Direction", "Message Type", "Sending Time",
                "Raw Message"};

        TableFormat<LogMessage> adminTableFormat = new BeanTableFormat<LogMessage>(
                LogMessage.class, properties, columnNames);

        rawMessagesTableModel = new EventTableModel<LogMessage>(adminMessages,
                adminTableFormat);
        rawMessagesSelectionModel = new EventSelectionModel<LogMessage>(adminMessages);
        rawMessagesSelectionModel.addListSelectionListener(this);
        rawMessagesTableCellRenderer = new RawMessageTableCellRenderer();
    }

    public ActionListener getSortByMessageIndexActionListener() {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JCheckBox checkbox = (JCheckBox) e.getSource();
                if (checkbox.isSelected()) {
                    sortedList.setComparator(GlazedLists.reverseComparator());
                } else {
                    sortedList.setComparator(null);
                }
            }
        };
    }

    public ActionListener getHideHeartbeatsActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                if (checkBox.isSelected()) {

                    String[] filters = {"Heartbeat"};
                    TextMatcher<LogMessage> textMatcher = new TextMatcher<LogMessage>(
                            filters, new HeartbeatFilterator(), TextMatcherEditor.CONTAINS);
                    Matcher<LogMessage> matcher = new NotMatcher<LogMessage>(textMatcher);
                    filteredList.setMatcher(matcher);
                } else {
                    filteredList.setMatcher(null);
                }
            }
        };
    }
}