package org.opentradingsolutions.log4fix.swing;

import ca.odell.glazedlists.swing.EventTableModel;
import org.opentradingsolutions.log4fix.model.LogMessage;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * @author Brian M. Coyner
 */
public class LogMessageTableCellRenderer extends DefaultTableCellRenderer {

    private Color incomingColor = new Color(131, 218, 102);
    private Color outgoingColor = new Color(233, 173, 89);

    private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static final String RECEIVING = "Receiving";
    public static final String SENDING = "Sending";

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component comp = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        EventTableModel<LogMessage> tableModel = (EventTableModel<LogMessage>)
                table.getModel();
        LogMessage message = tableModel.getElementAt(row);


        // sending time column
        if (column == 2) {
            setValue(formatter.format(value));
        }

        if (message.isIncoming()) {
            if (column == 0) {
                setValue(RECEIVING);
            }
            setBackground(isSelected ? incomingColor.darker().darker() : incomingColor);
        } else {
            if (column == 0) {
                setValue(SENDING);
            }

            setBackground(isSelected ? outgoingColor.darker().darker() : outgoingColor);
        }

        if (isSelected) {
            setForeground(Color.WHITE);
        } else {
            setForeground(Color.BLACK);
        }

        return comp;
    }
}
