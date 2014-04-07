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

package org.opentradingsolutions.log4fix.importer;

import java.io.File;

import org.opentradingsolutions.log4fix.Log4FIX;
import org.opentradingsolutions.log4fix.core.GlazedListsMemoryLogModel;
import org.opentradingsolutions.log4fix.core.MemoryLogModel;
import org.opentradingsolutions.log4fix.datadictionary.ClassPathDataDictionaryLoader;
import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import org.opentradingsolutions.log4fix.ui.importer.ImporterController;

/**
 * @author Brian M. Coyner
 */
public class Main {

    /**
     * This is the main entry point when starting Log4FIX in "standalone" mode.
     * Use this starting point to import log files.
     *
     * @param args may contain a single absolute path to a log file that automatically imports.
     * @throws Exception if the application fails to start.
     */
    public static void main(String[] args) throws Exception {

        DataDictionaryLoader dictionaryLoader = new ClassPathDataDictionaryLoader();
        SessionIdResolver sessionIdResolver = new PassThroughSessionIdResolver();

        MemoryLogModel memoryLogModel = new GlazedListsMemoryLogModel();
        ImporterMemoryLog importerMemoryLog = new ImporterMemoryLog(memoryLogModel, dictionaryLoader);
        ImporterModel model = new ImporterModel(importerMemoryLog, sessionIdResolver);
        ImporterController controller = new ImporterController(new Importer(), model);

        Log4FIX forImport = Log4FIX.createForImport(memoryLogModel, controller);
        forImport.show();

        if (args.length == 1) {
            String pathToFile = args[0];
            File file = new File(pathToFile);
            if (file.exists()) {
                controller.importWithFile(file);
            } else {
                importerMemoryLog.onEvent("File Not Found: " + pathToFile);
            }
        }
    }
}
