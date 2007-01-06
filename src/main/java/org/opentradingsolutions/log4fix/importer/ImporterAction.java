package org.opentradingsolutions.log4fix.importer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Brian M. Coyner
 */
public class ImporterAction extends AbstractAction {

    private ImporterModel model;
    private JFileChooser fileChooser;

    public ImporterAction(ImporterModel model) {
        super("Import");
        this.model = model;
    }

    public void actionPerformed(ActionEvent e) {
        maybeCreateFileChooser();

        setEnabled(false);
        if (openLogFile()) {

            final File selectedFile = fileChooser.getSelectedFile();
            model.setLastAccessedFilePath(selectedFile.getPath());

            Runnable r = new Runnable() {
                public void run() {

                    try {
                        FileInputStream fis = new FileInputStream(selectedFile);
                        Importer importer = new Importer(model, fis);
                        importer.start(); // importer blocks here.
                        setEnabled(true);
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
                }
            };

            new Thread(r, "Importer Thread").start();
        } else {
            setEnabled(true);
        }
    }

    private void maybeCreateFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser(model.getLastAccessedFilePath());
            FileFilter filter = new FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".log")
                            || f.getName().endsWith(".in")
                            || f.getName().endsWith(".out");
                }

                public String getDescription() {
                    // @todo - make this configurable
                    return "Log Files (*.log, *.in, *.out)";
                }


            };

            fileChooser.setFileFilter(filter);
        }
    }

    private boolean openLogFile() {
        return fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION;
    }
}