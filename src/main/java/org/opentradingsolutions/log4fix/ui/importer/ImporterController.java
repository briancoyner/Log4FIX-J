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

package org.opentradingsolutions.log4fix.ui.importer;

import java.io.File;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.opentradingsolutions.log4fix.importer.Importer;
import org.opentradingsolutions.log4fix.importer.ImporterCallback;
import org.opentradingsolutions.log4fix.importer.ImporterModel;

/**
 * @author Brian M. Coyner
 */
public class ImporterController {

    private final ActionStart start;
    private final Action stop;
    private final JLabel busyText;

    public ImporterController(Importer service, ImporterModel model) {
        start = new ActionStart(service, model, new DefaultImporterController());
        stop = new ActionStop(service);

        busyText = new JLabel();
        busyText.setVisible(false);
    }

    public Action getStart() {
        return start;
    }

    public Action getStop() {
        return stop;
    }

    public JComponent getBusyIcon() {
        return busyText;
    }

    public void importWithFile(File file) {
        start.importFile(file);
    }

    private class DefaultImporterController implements ImporterCallback {
        public void starting() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    busyText.setText("Importing...");
                    busyText.setVisible(true);
                    start.setEnabled(false);
                    stop.setEnabled(true);
                }
            });
        }

        public void canceling() {
            busyText.setText("Cancelling...");
        }

        public void done() {

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    busyText.setVisible(false);
                    start.setEnabled(true);
                    stop.setEnabled(false);

                }
            });
        }
    }
}
