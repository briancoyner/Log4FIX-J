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

package org.opentradingsolutions.log4fix;

import quickfix.LogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opentradingsolutions.log4fix.core.MemoryLogFactory;
import org.opentradingsolutions.log4fix.core.MemoryLogModel;
import org.opentradingsolutions.log4fix.core.MemoryLogModelFactory;
import org.opentradingsolutions.log4fix.datadictionary.SessionDataDictionaryLoader;
import org.opentradingsolutions.log4fix.ui.importer.ImporterController;
import org.opentradingsolutions.log4fix.ui.messages.ViewBuilder;

/**
 * Here is an example of how you might integrate Log4FIX with your QuickFIX/J
 * application:
 * <p/>
 * <pre>
 * SessionSettings sessionSettings = . . .;
 * <p/>
 * // Log4FIX requires a QuickFIX SessionSettings object
 * Log4Fix log4Fix = Log4Fix.createForLiveUpdates(sessionSettings);
 * LogFactory logFactory = log4Fix.getLogFactory();
 * <p/>
 * Application application = . . .;
 * MessageStoreFactory storeFactory = . . .;
 * SessionFactory sessionFactory = new DefaultSessionFactory(
 *         trader, storeFactory, logFactory);
 * <p/>
 * // finish QuickFIX configuration
 * <p/>
 * // show the UI
 * log4Fix.show();
 * </pre>
 * <p/>
 * <p>
 * The above obtains a <tt>Log4FIX</tt> instance by passing the
 * <tt>quickfix.SessionSettings</tt> object. Log4FIX interrogates the session settings
 * for all statically configured sessions and registers a {@link MemoryLogModel} that
 * receives all FIX message matching the session Id. This allows Log4FIX to route
 * messages to the appropriate "view".
 * </p>
 * <p/>
 * <p/>
 * You can {@link #show()} the Log4FIX frame anytime. You do not have to wait
 * for the session to connect to the counterparty.
 *
 * @author Brian M. Coyner
 */
public class Log4FIX {

    private MemoryLogFactory logFactory;
    public JFrame frame;

    /**
     * Factory method that creates a Log4FIX instance for displaying real-time
     * FIX messages.
     *
     * @param sessionSettings valid QuickFIX/J settings object.
     * @return an instance that is ready to display real-time messages.
     * @see #getLogFactory()
     * @see #show()
     */
    public static Log4FIX createForLiveUpdates(SessionSettings sessionSettings) {
        return createForLiveUpdates(MemoryLogModelFactory.getMemoryLogModels(sessionSettings));
    }

    /**
     * Factory method that creates a Log4FIX instance for displaying real-time
     * FIX messages.
     *
     * @param memoryLogModelsBySessionId map containing session Ids
     *                                   and <tt>MemoryLogModel</tt>s.
     * @return an instance that is ready to display real-time messages.
     * @see #getLogFactory()
     * @see #show()
     */
    public static Log4FIX createForLiveUpdates(Map<SessionID, MemoryLogModel> memoryLogModelsBySessionId) {

        Log4FIX log4FIX = new Log4FIX();
        log4FIX.logFactory = new MemoryLogFactory(memoryLogModelsBySessionId, new SessionDataDictionaryLoader());

        ViewBuilder viewBuilder = new ViewBuilder();

        final Iterator<MemoryLogModel> memoryLogModels = memoryLogModelsBySessionId.values().iterator();

        log4FIX.frame = new JFrame("Log4FIX");
        log4FIX.frame.add(viewBuilder.createView(memoryLogModels), BorderLayout.CENTER);
        log4FIX.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        return log4FIX;
    }

    /**
     * Factory method that creates a Log4FIX instance for displaying imported
     * FIX messages.
     */
    public static Log4FIX createForImport(final MemoryLogModel memoryLogModel, ImporterController controller) {
        ViewBuilder viewBuilder = new ViewBuilder();

        Log4FIX log4FIX = new Log4FIX();
        log4FIX.frame = new JFrame("Log4FIX");
        log4FIX.frame.add(viewBuilder.createView(memoryLogModel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(new JButton(controller.getStart()));
        buttonPanel.add(new JButton(controller.getStop()));
        buttonPanel.add(new JButton(new AbstractAction("Clear") {
            public void actionPerformed(ActionEvent e) {
                memoryLogModel.clear();
            }
        }
                ));

        buttonPanel.add(controller.getBusyIcon());

        log4FIX.frame.add(buttonPanel, BorderLayout.NORTH);
        log4FIX.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return log4FIX;
    }

    /**
     * Displays the Log4FIX frame in the event thread.
     */
    public void show() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    /**
     * Closes the Log4FIX frame in the event thread.
     */
    public void close() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.dispose();
            }
        });
    }

    /**
     * A {@link quickfix.LogFactory} is used by QuickFIX/J to create
     * {@link quickfix.Log}s.
     *
     * @return may return null if created for displaying an imported log file.
     */
    public LogFactory getLogFactory() {
        return logFactory;
    }
}
