package com.krisapps.pantry.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import com.krisapps.pantry.types.data.ConfigurationData;
import com.krisapps.pantry.types.data.inventory.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            instance.initialize();
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

                stmt.execute("PRAGMA foreign_keys = ON;");
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
            // Create groups table
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS "groups" (
                    	"id"	INTEGER NOT NULL,
                    	"name"	TEXT NOT NULL DEFAULT 'Unnamed group',
                    	"description"	TEXT NOT NULL DEFAULT 'No description.',
                    	"parent_group_id"	INTEGER NOT NULL DEFAULT 0,
                    	PRIMARY KEY("id" AUTOINCREMENT),
                    	CONSTRAINT "parent_group_fk" FOREIGN KEY("parent_group_id") REFERENCES "groups"("id")
                    )
                    """);

            // Create categories table
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS "categories" (
                    	"id"	INTEGER NOT NULL,
                    	"name"	TEXT NOT NULL DEFAULT 'Unnamed category',
                    	"description"	TEXT NOT NULL DEFAULT 'No description.',
                    	PRIMARY KEY("id" AUTOINCREMENT)
                    )
                    """);

            // Create locations table
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS "locations" (
                    	"id"	INTEGER NOT NULL,
                    	"name"	TEXT NOT NULL DEFAULT 'Unnamed location' UNIQUE,
                    	"description"	TEXT NOT NULL DEFAULT 'No description.',
                    	"parent_location_id"	INTEGER,
                    	PRIMARY KEY("id" AUTOINCREMENT),
                    	CONSTRAINT "parent_location_fk" FOREIGN KEY("parent_location_id") REFERENCES "locations"("id")
                    )
                    """);

            // Create inventory table
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS "inventory" (
                    	"id"	INTEGER NOT NULL,
                    	"name"	TEXT NOT NULL,
                    	"quantity"	REAL NOT NULL DEFAULT 0,
                    	"quantity_per_pack"	REAL NOT NULL DEFAULT 0,
                    	"preferred_min_quantity"	INTEGER NOT NULL DEFAULT 1,
                    	"expires_at"	REAL,
                    	"last_stocked_at"	REAL,
                    	"last_stocked_quantity"	REAL,
                    	"location"	INTEGER,
                    	"description"	TEXT NOT NULL DEFAULT 'No description.',
                    	"type"	TEXT NOT NULL DEFAULT 'GENERIC',
                    	"group_id"	INTEGER DEFAULT 0,
                    	"category_id"	INTEGER DEFAULT 0,
                    	PRIMARY KEY("id" AUTOINCREMENT),
                    	FOREIGN KEY("category_id") REFERENCES "categories"("id"),
                    	FOREIGN KEY("group_id") REFERENCES "groups"("id"),
                    	CONSTRAINT "location_fk" FOREIGN KEY("location") REFERENCES "locations"("id")
                    );
                    """);

            statement.execute("INSERT OR IGNORE INTO locations (id, name, description) VALUES (0, 'No location assigned', 'There is no location assigned to this item.')");
            statement.execute("INSERT OR IGNORE INTO groups (id, name, description) VALUES (0, 'No group assigned', 'There is no group assigned to this item.')");
            statement.execute("INSERT OR IGNORE INTO categories (id, name, description) VALUES (0, 'No category assigned', 'There is no category assigned to this item.')");
            statement.execute("PRAGMA foreign_keys = ON;");
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

    public ItemLocation mapRowToLocation(ResultSet row) throws SQLException {
        return new ItemLocation(
                row.getInt("id"),
                row.getString("name"),
                row.getString("description"),
                null /* Replace with query for location based on id */
        );
    }

    public ItemGroup mapRowToGroup(ResultSet row) throws SQLException {
        return new ItemGroup(
                row.getInt("id"),
                row.getString("name"),
                row.getString("description"),
                row.getInt("parent_group_id") != 0 ? getGroup(row.getInt("parent_group_id")).orElse(null) : null
        );
    }

    public ItemCategory mapRowToCategory(ResultSet row) throws SQLException {
        return new ItemCategory(
                row.getInt("id"),
                row.getString("name"),
                row.getString("description")
        );
    }

    public PantryItem mapRowToItem(ResultSet row) throws SQLException {
        return new PantryItem(
                row.getInt("id"),
                row.getString("name"),
                row.getString("description"),
                row.getDouble("quantity"),
                row.getDouble("quantity_per_pack"),
                row.getDouble("preferred_min_quantity"),
                row.getDate("expires_at") != null ? row.getDate("expires_at").toLocalDate() : null,
                row.getTimestamp("last_stocked_at") != null ? row.getTimestamp("last_stocked_at").toLocalDateTime() : null,
                row.getDouble("last_stocked_quantity"),
                getLocation(row.getInt("location")).orElse(null),
                ItemType.valueOf(row.getString("type")),
                getGroup(row.getInt("group_id")).orElse(ItemGroup.NONE),
                getCategory(row.getInt("category_id")).orElse(ItemCategory.NONE)
        );
    }

    public Optional<ItemLocation> getLocation(int locationId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (locationId == -1) {
            throw new IllegalArgumentException("Location ID cannot be -1!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("SELECT * FROM locations WHERE id = ?")) {
            statement.setInt(1, locationId);
            ResultSet response = statement.executeQuery();

            if (response.next()) {
                return Optional.of(mapRowToLocation(response));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.logStackTrace(e);
            return Optional.empty();
        }
    }

    public ArrayList<ItemLocation> getLocations() {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("SELECT * FROM locations;")) {
            ResultSet response = statement.executeQuery();
            ArrayList<ItemLocation> locations = new ArrayList<>();

            while (response.next()) {
                locations.add(mapRowToLocation(response));
            }

            return locations;
        } catch (SQLException e) {
            logger.logStackTrace(e);
            return new ArrayList<>();
        }
    }

    public Optional<ItemGroup> getGroup(int groupId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (groupId == -1) {
            throw new IllegalArgumentException("Group ID cannot be -1!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("SELECT * FROM groups WHERE id = ?")) {
            statement.setInt(1, groupId);
            ResultSet response = statement.executeQuery();

            if (response.next()) {
                return Optional.of(mapRowToGroup(response));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.logStackTrace(e);
            return Optional.empty();
        }
    }

    public ArrayList<ItemGroup> getGroups() {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("SELECT * FROM groups;")) {
            ResultSet response = statement.executeQuery();
            ArrayList<ItemGroup> locations = new ArrayList<>();

            while (response.next()) {
                locations.add(mapRowToGroup(response));
            }

            return locations;
        } catch (SQLException e) {
            logger.logStackTrace(e);
            return new ArrayList<>();
        }
    }

    public Optional<ItemCategory> getCategory(int categoryId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (categoryId == -1) {
            throw new IllegalArgumentException("Category ID cannot be -1!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("SELECT * FROM categories WHERE id = ?")) {
            statement.setInt(1, categoryId);
            ResultSet response = statement.executeQuery();

            if (response.next()) {
                return Optional.of(mapRowToCategory(response));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.logStackTrace(e);
            return Optional.empty();
        }
    }

    public ArrayList<ItemCategory> getCategories() {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("SELECT * FROM categories;")) {
            ResultSet response = statement.executeQuery();
            ArrayList<ItemCategory> locations = new ArrayList<>();

            while (response.next()) {
                locations.add(mapRowToCategory(response));
            }

            return locations;
        } catch (SQLException e) {
            logger.logStackTrace(e);
            return new ArrayList<>();
        }
    }

    public Optional<PantryItem> getItem(int itemId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (itemId == -1) {
            throw new IllegalArgumentException("Item ID cannot be -1!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("SELECT * FROM inventory WHERE id = ?")) {
            statement.setInt(1, itemId);
            ResultSet response = statement.executeQuery();

            if (response.next()) {
                return Optional.of(mapRowToItem(response));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.logStackTrace(e);
            return Optional.empty();
        }
    }

    public ArrayList<PantryItem> getItems() {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("SELECT * FROM inventory;")) {
            ResultSet response = statement.executeQuery();
            ArrayList<PantryItem> items = new ArrayList<>();

            while (response.next()) {
                items.add(mapRowToItem(response));
            }

            return items;
        } catch (SQLException e) {
            logger.logStackTrace(e);
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

    public void addLocation(ItemLocation location) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (location == null) {
            return;
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("INSERT INTO locations (name, description, parent_location_id) VALUES (?, ?, ?)")) {
            statement.setString(1, location.name());
            statement.setString(2, location.description());
            statement.setInt(3, location.hasParentLocation() ? location.parent().databaseId() : null);
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    public void updateLocation(int databaseId, ItemLocation data) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (databaseId == -1) {
            throw new IllegalArgumentException("Location ID cannot be -1!");
        }

        if (data == null) {
            throw new IllegalArgumentException("Location data cannot be null!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("UPDATE locations SET name = ?, description = ?, parent_location_id = ? WHERE id = ?")) {
            statement.setString(1, data.name());
            statement.setString(2, data.description());
            statement.setInt(3, data.hasParentLocation() ? data.parent().databaseId() : null);
            statement.setInt(4, databaseId);
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    public void deleteLocation(int databaseId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (databaseId == -1) {
            throw new IllegalArgumentException("Location ID cannot be -1!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("DELETE FROM locations WHERE id = ?")) {
            statement.setInt(1, databaseId);
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    public void addGroup(ItemGroup group) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (group == null) {
            return;
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("INSERT INTO groups (name, description) VALUES (?, ?)")) {
            statement.setString(1, group.name());
            statement.setString(2, group.description());
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    public void updateGroup(int databaseId, ItemGroup data) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (databaseId == -1) {
            throw new IllegalArgumentException("Group ID cannot be -1!");
        }

        if (data == null) {
            throw new IllegalArgumentException("Group data cannot be null!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("UPDATE groups SET name = ?, description = ? WHERE id = ?")) {
            statement.setString(1, data.name());
            statement.setString(2, data.description());
            statement.setInt(3, databaseId);
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    public void deleteGroup(int databaseId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (databaseId == -1) {
            throw new IllegalArgumentException("Group ID cannot be -1!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("DELETE FROM groups WHERE id = ?")) {
            statement.setInt(1, databaseId);
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    /**
     * Updates an item's group to the supplied group ID.
     * @param itemId The item whose group to change.
     * @param groupId The group ID of the group to assign to the item.
     */
    public void updateItemGroup(int itemId, int groupId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (itemId == -1) {
            throw new IllegalArgumentException("Item ID cannot be -1!");
        }

        if (groupId == -1) {
            throw new IllegalArgumentException("Group ID cannot be -1!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("UPDATE inventory SET group_id = ? WHERE id = ?;")) {
            statement.setInt(1, groupId);
            statement.setInt(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    /**
     * Sets an item's group to be <code>0</code> (no group assigned).
     * @param itemId The item whose group to change.
     */
    public void clearItemGroup(int itemId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (itemId == -1) {
            throw new IllegalArgumentException("Item ID cannot be -1!");
        }

        updateItemGroup(itemId, 0);
    }

    public void addCategory(ItemCategory category) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (category == null) {
            return;
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("INSERT INTO categories (name, description) VALUES (?, ?)")) {
            statement.setString(1, category.name());
            statement.setString(2, category.description());
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    public void updateCategory(int databaseId, ItemCategory data) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (databaseId == -1) {
            throw new IllegalArgumentException("Category ID cannot be -1!");
        }

        if (data == null) {
            throw new IllegalArgumentException("Category data cannot be null!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("UPDATE categories SET name = ?, description = ? WHERE id = ?")) {
            statement.setString(1, data.name());
            statement.setString(2, data.description());
            statement.setInt(3, databaseId);
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    public void deleteCategory(int databaseId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (databaseId == -1) {
            throw new IllegalArgumentException("Category ID cannot be -1!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("DELETE FROM categories WHERE id = ?")) {
            statement.setInt(1, databaseId);
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    /**
     * Updates an item's category to the supplied category ID.
     * @param itemId The item whose category to change.
     * @param categoryId The category ID of the category to assign to the item.
     */
    public void updateItemCategory(int itemId, int categoryId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (itemId == -1) {
            throw new IllegalArgumentException("Item ID cannot be -1!");
        }

        if (categoryId == -1) {
            throw new IllegalArgumentException("Category ID cannot be -1!");
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("UPDATE inventory SET category_id = ? WHERE id = ?;")) {
            statement.setInt(1, categoryId);
            statement.setInt(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    /**
     * Sets an item's category to be <code>0</code> (no category assigned).
     * @param itemId The item whose category to change.
     */
    public void clearItemCategory(int itemId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (itemId == -1) {
            throw new IllegalArgumentException("Item ID cannot be -1!");
        }

        updateItemCategory(itemId, 0);
    }

    public void addInventoryItem(PantryItem item) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (item == null) {
            return;
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("INSERT INTO inventory (name, quantity, quantity_per_pack, preferred_min_quantity, expires_at, last_stocked_at, last_stocked_quantity, location, description, type, group_id, category_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, item.name());
            statement.setDouble(2, item.quantity());
            statement.setDouble(3, item.quantityPerPack());
            statement.setDouble(4, item.preferredMinimumQuantity());
            statement.setDate(5, item.expiresAt() != null ? Date.valueOf(item.expiresAt()) : null);
            statement.setTimestamp(6, item.lastStockedAt() != null ? Timestamp.valueOf(item.lastStockedAt()) : null);
            statement.setDouble(7, item.lastStockedQuantity());
            statement.setInt(8, item.location() != null ? item.location().databaseId() : 0);
            statement.setString(9, item.description());
            statement.setString(10, item.type().name());
            statement.setInt(11, item.group().databaseId());
            statement.setInt(12, item.category().databaseId());
            statement.executeUpdate();
            logger.debug("New item added to the database: " + item);
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }
    
    public void updateInventoryItem(int itemId, PantryItem data) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (itemId == -1) {
            throw new IllegalArgumentException("Item ID cannot be -1!");
        }

        if (data == null) {
            return;
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("UPDATE inventory SET name = ?, quantity = ?, quantity_per_pack = ?, preferred_min_quantity = ?, expires_at = ?, last_stocked_at = ?, last_stocked_quantity = ?, location = ?, description = ?, type = ?, group_id = ?, category_id = ? WHERE id = ?")) {
            statement.setString(1, data.name());
            statement.setDouble(2, data.quantity());
            statement.setDouble(3, data.quantityPerPack());
            statement.setDouble(4, data.preferredMinimumQuantity());
            statement.setDate(5, Date.valueOf(data.expiresAt()));
            statement.setTimestamp(6, Timestamp.valueOf(data.lastStockedAt()));
            statement.setDouble(7, data.lastStockedQuantity());
            statement.setInt(8, data.location() != null ? data.location().databaseId() : 0);
            statement.setString(9, data.description());
            statement.setString(10, data.type().name());
            statement.setInt(11, data.group().databaseId());
            statement.setInt(12, data.category().databaseId());
            statement.setInt(13, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }
    
    public void deleteInventoryItem(int itemId) {
        if (currentConnection == null) {
            currentConnection = getDatabaseConnection();
        }

        if (itemId == -1) {
            return;
        }

        try (PreparedStatement statement = currentConnection.prepareStatement("DELETE FROM inventory WHERE id = ?")) {
            statement.setInt(1, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.logStackTrace(e);
        }
    }

    //</editor-fold>

}
