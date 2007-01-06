package org.opentradingsolutions.log4fix.swing;

import ca.odell.glazedlists.swing.EventTableModel;
import org.opentradingsolutions.log4fix.model.LogField;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Brian M. Coyner
 */
public class LogFieldTableCellRenderer extends DefaultTableCellRenderer {

    private Color dataFieldColor = new Color(198, 158, 236);
    private Color headerFieldColor = new Color(252, 152, 108);
    private Color trailerFieldColor = new Color(88, 211, 113);
    public static final String REQUIRED_FIELD = "Y";
    public static final String OPTIONAL_FIELD = "";

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component comp = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        EventTableModel<LogField> tableModel = (EventTableModel<LogField>)
                table.getModel();
        LogField field = tableModel.getElementAt(row);

        if (field.isHeaderField()) {
            setBackground(headerFieldColor);
        } else if (field.isTrailerField()) {
            setBackground(trailerFieldColor);
        } else {
            setBackground(dataFieldColor);
        }

        if (column == 0) {
            if (field.isRequired()) {
                setValue(REQUIRED_FIELD);
            } else {
                setValue(OPTIONAL_FIELD);
            }
        }

        if (isSelected) {
            setForeground(Color.WHITE);
        } else {
            setForeground(Color.BLACK);
        }

        return comp;
    }
}
