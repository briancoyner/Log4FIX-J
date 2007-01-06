package org.opentradingsolutions.log4fix.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.beans.BeanTableFormat;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import org.opentradingsolutions.log4fix.model.LogEvent;
import org.opentradingsolutions.log4fix.model.LogField;
import org.opentradingsolutions.log4fix.model.LogMessage;
import org.opentradingsolutions.log4fix.model.MemoryLogModel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.util.List;

/**
 * Simple model representing raw and cracked FIX messages. This model provides
 * various Swing model objects that are used to bind the data to Swing table components.
 *
 * @author Brian M. Coyner
 */
public class LogViewModel implements ListSelectionListener {

    private EventTableModel<LogMessage> rawMessagesTableModel;
    private EventSelectionModel<LogMessage> rawMessagesSelectionModel;
    private TableCellRenderer rawMessagesTableCellRenderer;

    private EventTableModel<LogField> crackedMessageModel;
    private BasicEventList<LogField> crackedFields;
    private LogFieldTableCellRenderer crackedFieldsTableCellRenderer;

    private EventListModel<LogEvent> eventsListModel;
    private EventList<LogMessage> selectedMessage;

    public LogViewModel(MemoryLogModel memoryLogModel) {

        createRaw(memoryLogModel.getMessages());

        crackedFields = new BasicEventList<LogField>();
        createCracked(crackedFields);

        createEvents(memoryLogModel.getEvents());
    }

    public EventListModel<LogEvent> getEventsListModel() {
        return eventsListModel;
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

    public TableModel getAdminMessagesCrackedModel() {
        return crackedMessageModel;
    }

    public void valueChanged(ListSelectionEvent e) {

        crackedFields.clear();
        if (!rawMessagesSelectionModel.isSelectionEmpty()) {
            selectedMessage = rawMessagesSelectionModel.getSelected();
            crackedFields.addAll(selectedMessage.get(0).getLogFields());
        }
    }

    public TableCellRenderer getCrackedFieldsTableCellRenderer() {
        return crackedFieldsTableCellRenderer;
    }

    private void createCracked(EventList<LogField> crackedFields) {
        TableFormat<LogField> crackedTableFormat =
                new BeanTableFormat<LogField>(LogField.class,
                        new String[]{"required", "tag", "value", "fieldName",
                                "fieldValueName"},
                        new String[]{"Is Required", "Tag", "Value", "Field Name",
                                "Field Value Name"});
        crackedMessageModel = new EventTableModel<LogField>(crackedFields,
                crackedTableFormat);
        crackedFieldsTableCellRenderer = new LogFieldTableCellRenderer();

    }

    private void createRaw(List<LogMessage> adminMessages) {
        String[] properties = {LogMessage.INCOMING, LogMessage.MESSAGE_TYPE_NAME,
                LogMessage.SENDING_TIME, LogMessage.RAW_MESSAGE};
        String[] columnNames = {"Direction", "Message Type", "Sending Time",
                "Raw Message"};

        TableFormat<LogMessage> adminTableFormat = new BeanTableFormat<LogMessage>(
                LogMessage.class, properties, columnNames);

        rawMessagesTableModel = new EventTableModel<LogMessage>(
                (EventList<LogMessage>) adminMessages, adminTableFormat);
        rawMessagesSelectionModel = new EventSelectionModel<LogMessage>(
                (EventList<LogMessage>) adminMessages);
        rawMessagesSelectionModel.addListSelectionListener(this);
        rawMessagesTableCellRenderer = new LogMessageTableCellRenderer();
    }

    private void createEvents(List<LogEvent> events) {
        eventsListModel = new EventListModel<LogEvent>((EventList<LogEvent>) events);
    }
}