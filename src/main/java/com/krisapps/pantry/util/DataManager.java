package com.krisapps.pantry.util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import com.krisapps.pantry.types.ConfigurationData;
import com.krisapps.pantry.util.misc.LocalDateTimeTypeAdapter;
import com.krisapps.pantry.util.misc.LocalDateTypeAdapter;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("ConstantConditions")
public class DataManager {

    private static final Gson gson = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();
    private static DataManager instance;
    private final File configFile;
    private Path databaseFilePath;
    private static final Path DATA_DIRECTORY_PATH = Path.of(System.getProperty("user.home") + File.separator + "KrisApps Pantry");

    private boolean isSaving = false;
    private ConfigurationData configurationData;
    private Connection currentConnection;

    public static Logging logger = Logging.getInstance();

    private DataManager() {
        this.configFile = Path.of(DATA_DIRECTORY_PATH.toString(), "config.json").toFile();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public static Path getDataDirectory() {
        return DATA_DIRECTORY_PATH;
    }

    public static void log(String msg) {
        if (msg.toLowerCase().contains("failed") || msg.toLowerCase().contains("error") || msg.toLowerCase().contains("fail") || msg.toLowerCase().contains("couldn't") || msg.toLowerCase().contains("could not")) {
            logger.info(msg, Level.SEVERE);
        } else {
            logger.info(msg, Level.INFO);
        }
    }

    public static void log(String msg, String modulePrefix) {
        if (msg.toLowerCase().contains("failed") || msg.toLowerCase().contains("error") || msg.toLowerCase().contains("fail") || msg.toLowerCase().contains("couldn't") || msg.toLowerCase().contains("could not")) {
            logger.log(msg, modulePrefix, Level.SEVERE);
        } else {
            logger.log(msg, modulePrefix, Level.INFO);
        }
    }

    public static void log(String msg, Level level) {
        logger.info(msg, level);
    }

    public static void log(String msg, String modulePrefix, Level level) {
        logger.log(msg, modulePrefix, level);
    }

    public void initialize() {
        loadConfigurationData();
        databaseFilePath = configurationData.getDatabaseLocation();
        logger.initialize(configurationData.getLogFileLocation());
        logger.setEnableDebug(configurationData.isDebugEnabled());

        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();

            try (Statement stmt = currentConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA user_version")) {
                int version = rs.next() ? rs.getInt(1) : 0;

                if (version == 0) {
                    initializeDatabase(currentConnection);
                }

            } catch (SQLException e) {
                log("Failed to check database state. Error: " + e.getMessage());
                logger.logStackTrace(e);
            }

        }
    }

    public void reinitializeCurrentDatabase() {
        initializeDatabase(currentConnection);
    }

    private void initializeDatabase(Connection currentConnection) {
        try (Statement statement = currentConnection.createStatement()) {

            // Init code

            statement.execute("PRAGMA user_version = 1");
            log("Database successfully initialized!");
        } catch (SQLException e) {
            log("Failed to initialize database: " + e.getMessage());
            logger.logStackTrace(e);
        }
    }

    private void firstTimeFileSetup() {
        log("No files found, initializing first-time setup.");

        try {
            log("Creating a data directory at: " + DATA_DIRECTORY_PATH);
            Files.createDirectory(DATA_DIRECTORY_PATH);
        } catch (IOException e) {
            log("Failed to create data directory: " + e.getMessage());
        }

        createConfigurationFile();
        log("Files successfully created.");
    }

    private void createConfigurationFile() {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                ConfigurationData config = new ConfigurationData();
                saveConfigurationData(config);
            }
        } catch (IOException e) {
            log("Could not create a new data file - " + e.getMessage());
        }
    }

    public void saveConfigurationData(ConfigurationData data) {
        isSaving = true;

        if (!configFile.exists()) {
            createConfigurationFile();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile, false), StandardCharsets.UTF_16));

            writer.write(gson.toJson(data));
            writer.close();
        } catch (IOException e) {
            log("Data saving failed: " + e.getMessage());
        }
        isSaving = false;
    }

    public void saveCurrentConfigurationData() {
        if (configurationData == null) {
            return;
        }
        saveConfigurationData(configurationData);
    }

    public boolean isSaving() {
        return isSaving;
    }

    /**
     * Loads configuration data from the disk.
     *
     * @return The loaded data.
     */
    public ConfigurationData getConfigurationData() {

        if (configurationData != null) {
            return configurationData;
        } else {
            if (!configFile.exists()) {
                firstTimeFileSetup();
            }

            InputStreamReader inputStreamReader;
            try {
                inputStreamReader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_16);
                JsonReader reader = new JsonReader(inputStreamReader);
                ConfigurationData output = gson.fromJson(reader, ConfigurationData.class);
                if (output == null) {
                    output = new ConfigurationData();
                }
                return output;
            } catch (IOException e) {
                log("Failed to retrieve data from data file: " + e.getMessage());
                return new ConfigurationData();
            }
        }
    }

    private void loadConfigurationData() {
        configurationData = getConfigurationData();
    }

    private Connection getDatabaseConnection() {
        try {
            logger.debug("Database connection ready!");
            return DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath);
        } catch (SQLException e) {
            Platform.runLater(() -> PopupManager.showPopup("Database initialization error!", "The following error was encountered when initializing the database:\n" + e.getMessage(), Alert.AlertType.ERROR));
            log("");
        }
        return null;
    }

    //<editor-fold desc="System data modification">

    /**
     * Drops the specified table.
     * This operation cannot be undone.
     *
     * @param tableName The table to drop.
     * @return <code>true</code> if the table was dropped, <code>false</code> otherwise.
     */
    public boolean dropTable(String tableName) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("DROP TABLE IF EXISTS " + tableName + ";")) {
            statement.execute();
            return true;
        } catch (SQLException e) {
            PopupManager.showPopup("Couldn't drop table " + tableName, "Something went wrong when trying to drop table '" + tableName + "'. Error details: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Drops all tables in the database.
     * This operation cannot be undone.
     *
     * @return <code>true</code> if the table was dropped, <code>false</code> otherwise.
     */
    public boolean dropAllTables() {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        for (String table : getTables()) {
            try (PreparedStatement statement = currentConnection.prepareStatement("DROP TABLE " + table + ";")) {
                statement.execute();
            } catch (SQLException e) {
                PopupManager.showPopup("Couldn't drop table '" + table + "'", "Something went wrong when trying to drop table '" + table + "'. Error details: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    //</editor-fold>


    // <editor-fold desc="Data access">

    public List<String> getTables() {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        try (ResultSet response = currentConnection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            ArrayList<String> tables = new ArrayList<>();
            while (response.next()) {
                tables.add(response.getString("table_name"));
            }

            return tables;
        } catch (SQLException e) {
            PopupManager.showPopup("Failed to retrieve data!", "An SQL error was encountered when querying table data. Error details: " + e.getMessage(), Alert.AlertType.ERROR);
            return new ArrayList<>();
        }
    }

    // </editor-fold>

    // <editor-fold desc="Data modification">

    /**
     * Updates the saved database path.
     *
     * @param location The path of a SQLite database file.
     */
    public void updateDatabaseLocation(Path location) {
        if (location == null) return;
        if (configurationData == null) {
            initialize();
        }
        configurationData.setDatabaseLocation(location);
    }

    public void updateLogLocation(Path location) {
        if (location == null) return;
        if (configurationData == null) {
            initialize();
        }
        configurationData.setLogFileLocation(location);
    }

    public void updateDebugEnabled(boolean enabled) {
        if (configurationData == null) {
            initialize();
        }
        configurationData.setDebugEnabled(enabled);
    }
    //</editor-fold>

}
