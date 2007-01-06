package org.opentradingsolutions.log4fix.importer;

import org.opentradingsolutions.log4fix.datadictionary.ClassPathDataDictionaryLoader;
import org.opentradingsolutions.log4fix.datadictionary.DataDictionaryLoader;
import org.opentradingsolutions.log4fix.model.MemoryLogModel;
import org.opentradingsolutions.log4fix.swing.GlazedListsMemoryLogModel;
import org.opentradingsolutions.log4fix.swing.Log4FIX;

/**
 * @author Brian M. Coyner
 */
public class Main {

    public static void main(String[] args) throws Exception {

        DataDictionaryLoader dictionaryLoader = new ClassPathDataDictionaryLoader();
        SessionIdResolver sessionIdResolver = new PassThroughSessionIdResolver();

        MemoryLogModel memoryLogModel = new GlazedListsMemoryLogModel();
        ImporterMemoryLog importerMemoryLog = new ImporterMemoryLog(memoryLogModel,
                dictionaryLoader);
        ImporterModel model = new ImporterModel(importerMemoryLog, sessionIdResolver);
        ImporterAction importerAction = new ImporterAction(model);
        Log4FIX forImport = Log4FIX.createForImport(memoryLogModel, importerAction);
        forImport.show();
    }
}
