package org.opentradingsolutions.log4fix.swing;

import org.opentradingsolutions.log4fix.datadictionary.SessionDataDictionaryLoader;
import org.opentradingsolutions.log4fix.importer.ImporterAction;
import org.opentradingsolutions.log4fix.model.MemoryLogFactory;
import org.opentradingsolutions.log4fix.model.MemoryLogModel;
import quickfix.LogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Brian M. Coyner
 */
public class Log4FIX {

    private MemoryLogFactory logFactory;
    public JFrame frame;

    public static Log4FIX createForLiveUpdates(SessionSettings sessionSettings) {
        return createForLiveUpdates(MemoryLogModelFactory.
                getMemoryLogModels(sessionSettings));
    }

    public static Log4FIX createForLiveUpdates(Map<SessionID,
            MemoryLogModel> memoryLogModelsBySessionId) {

        Log4FIX log4FIX = new Log4FIX();
        log4FIX.logFactory = new MemoryLogFactory(memoryLogModelsBySessionId,
                new SessionDataDictionaryLoader());

        LogViewBuilder viewBuilder = new LogViewBuilder();

        final Iterator<MemoryLogModel> memoryLogModels = memoryLogModelsBySessionId.
                values().iterator();

        log4FIX.frame = new JFrame("Log4FIX");
        log4FIX.frame.add(viewBuilder.createView(memoryLogModels), BorderLayout.CENTER);
        log4FIX.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        return log4FIX;
    }

    public static Log4FIX createForImport(final MemoryLogModel memoryLogModel,
            ImporterAction importerAction) {
        LogViewBuilder viewBuilder = new LogViewBuilder();

        Log4FIX log4FIX = new Log4FIX();
        log4FIX.frame = new JFrame("Log4FIX");
        log4FIX.frame.add(viewBuilder.createView(memoryLogModel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(new JButton(importerAction));
        buttonPanel.add(new JButton(new AbstractAction("Clear") {
            public void actionPerformed(ActionEvent e) {
                memoryLogModel.clear();
            }
        }
        ));

        log4FIX.frame.add(buttonPanel, BorderLayout.NORTH);
        log4FIX.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return log4FIX;
    }

    public void show() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public void close() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.dispose();
            }
        });
    }

    /**
     * @return may return null if created for displaying an imported log file.
     */
    public LogFactory getLogFactory() {
        return logFactory;
    }

}
