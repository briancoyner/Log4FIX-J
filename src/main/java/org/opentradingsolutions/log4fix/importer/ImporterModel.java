package org.opentradingsolutions.log4fix.importer;

/**
 * @author Brian M. Coyner
 */
public class ImporterModel {

    private String lastAccessedFilePath;
    private SessionIdResolver sessionIdResolver;
    private ImporterMemoryLog importMemoryLog;

    public ImporterModel(ImporterMemoryLog logger, SessionIdResolver sessionIdResolver) {
        this.importMemoryLog = logger;
        this.sessionIdResolver = sessionIdResolver;
    }

    public ImporterModel(ImporterMemoryLog importerMemoryLog,
            SessionIdResolver sessionIdResolver, String initialFilePath) {

        this.importMemoryLog = importerMemoryLog;
        this.sessionIdResolver = sessionIdResolver;
        lastAccessedFilePath = initialFilePath;
    }

    public ImporterMemoryLog getImporterMemoryLog() {
        return importMemoryLog;
    }

    public SessionIdResolver getSessionIdResolver() {
        return sessionIdResolver;
    }

    public String getLastAccessedFilePath() {
        return lastAccessedFilePath;
    }

    public void setLastAccessedFilePath(String lastAccessedFilePath) {
        this.lastAccessedFilePath = lastAccessedFilePath;
    }
}