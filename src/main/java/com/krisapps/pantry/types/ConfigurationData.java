package com.krisapps.pantry.types;

import com.krisapps.pantry.util.DataManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationData {
    private String databaseLocation;
    private String logFileLocation;
    private boolean debug;

    public ConfigurationData() {
        this.databaseLocation = Path.of(DataManager.getDataDirectory() + File.separator + "data.db").toString();
        this.logFileLocation = Paths.get(DataManager.getDataDirectory() + File.separator + "utility.log").toString();
    }

    public Path getDatabaseLocation() {
        return Path.of(databaseLocation);
    }

    public void setDatabaseLocation(Path databaseLocation) {
        this.databaseLocation = databaseLocation.toString();
    }

    public void setDatabaseLocation(String databaseLocation) {
        this.databaseLocation = databaseLocation;
    }

    public Path getLogFileLocation() {
        return Path.of(logFileLocation);
    }

    public void setLogFileLocation(Path logFileLocation) {
        this.logFileLocation = logFileLocation.toString();
    }

    public void setDebugEnabled(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebugEnabled() {
        return debug;
    }
}
