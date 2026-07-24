package com.krisapps.pantry.types.data;

import com.krisapps.pantry.util.DataManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class ConfigurationData {
    private String databaseLocation;
    private String logFileLocation;
    private LocalDateTime lastInventoryUpdate = null;
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

    public LocalDateTime getLastInventoryUpdate() {
        return lastInventoryUpdate;
    }

    public void setLastInventoryUpdate(LocalDateTime lastInventoryUpdate) {
        this.lastInventoryUpdate = lastInventoryUpdate;
    }

    public void setDebugEnabled(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebugEnabled() {
        return debug;
    }
}
