package org.opentradingsolutions.log4fix.swing;

import org.opentradingsolutions.log4fix.model.MemoryLogModel;
import quickfix.SessionID;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author Brian M. Coyner
 */
public class LogViewBuilder {

    /**
     * Creates a tab pane containing one or more tabs. The behavior is to
     * show each configured QuickFIX <code>Session</code> in its own tab.
     *
     * @param memoryLogModels an iterator containing <code>MemoryLogModel</code>s
     * for each configured <code>Session</code>.
     * @return a component ready to show QuickFIX logs.
     */
    public JComponent createView(Iterator<MemoryLogModel> memoryLogModels) {

        JTabbedPane tabPane = new JTabbedPane();

        while (memoryLogModels.hasNext()) {
            MemoryLogModel memoryLogModel = memoryLogModels.next();
            tabPane.addTab(getTabTitle(memoryLogModel),
                    createTabForSession(memoryLogModel));
        }

        return tabPane;
    }

    public JComponent createView(MemoryLogModel memoryLogModel) {
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab(getTabTitle(memoryLogModel),
                createTabForSession(memoryLogModel));
        return tabPane;
    }

    private JComponent createTabForSession(MemoryLogModel memoryLogModel) {

        LogViewModel logViewModel = new LogViewModel(memoryLogModel);

        JComponent rawTable = createRawMessageTable(logViewModel);
        JComponent crackedTable = createCrackedMessageTable(logViewModel);

        JSplitPane messageView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        messageView.setBorder(null);

        messageView.setLeftComponent(new JScrollPane(rawTable));
        messageView.setRightComponent(new JScrollPane(crackedTable));

        JSplitPane messageAndEventView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        messageAndEventView.setResizeWeight(.66);
        messageAndEventView.setLeftComponent(messageView);
        messageAndEventView.setRightComponent(new JScrollPane(
                createEventMessageTable(logViewModel)));
        messageAndEventView.setBorder(null);

        JPanel mainView = new JPanel(new BorderLayout());
        mainView.add(messageAndEventView, BorderLayout.CENTER);

        return mainView;
    }

    private JComponent createEventMessageTable(LogViewModel viewModel) {
        return new JList(viewModel.getEventsListModel());
    }

    private JComponent createRawMessageTable(LogViewModel viewModel) {
        JTable table = new JTable(viewModel.getRawMessagesTableModel());
        table.setSelectionModel(viewModel.getRawMessagesSelectionModel());

        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn tableColumn = columns.nextElement();
            tableColumn.setCellRenderer(viewModel.getRawMessagesTableCellRenderer());
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        return table;
    }

    private JComponent createCrackedMessageTable(LogViewModel viewModel) {
        JTable table = new JTable(viewModel.getAdminMessagesCrackedModel());

        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn tableColumn = columns.nextElement();
            tableColumn.setCellRenderer(viewModel.getCrackedFieldsTableCellRenderer());
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        return table;
    }

    private String getTabTitle(MemoryLogModel memoryLogModel) {
        SessionID sessionId = memoryLogModel.getSessionId();
        if (sessionId == null) {
            return "No Session Id";
        } else {
            return sessionId.toString();
        }
    }
}
