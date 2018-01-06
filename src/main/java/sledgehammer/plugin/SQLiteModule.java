/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zombie.GameWindow;

/**
 * Module sub-class to handle SQLite operations and utilities.
 *
 * @author Jab
 */
public abstract class SQLiteModule extends Module {

    // @formatter:off
	public static final String SQL_STORAGE_CLASS_NULL    = "NULL"   ;
	public static final String SQL_STORAGE_CLASS_TEXT    = "TEXT"   ;
	public static final String SQL_STORAGE_CLASS_REAL    = "REAL"   ;
	public static final String SQL_STORAGE_CLASS_INTEGER = "INTEGER";
	public static final String SQL_STORAGE_CLASS_BLOB    = "BLOB"   ;
	public static final String SQL_AFFINITY_TYPE_TEXT    = "TEXT"   ;
	public static final String SQL_AFFINITY_TYPE_NUMERIC = "NUMERIC";
	public static final String SQL_AFFINITY_TYPE_INTEGER = "INTEGER";
	public static final String SQL_AFFINITY_TYPE_REAL    = "REAL"   ;
	public static final String SQL_STORAGE_CLASS_NONE    = "NONE"   ;
	// @formatter:on

    /**
     * The File Object of the SQLIte database File.
     */
    private File dbFile;

    /**
     * The SQLite connection.
     */
    private Connection connection = null;

    /**
     * Main constructor.
     *
     * @param connection The Connection of the SQLite database File.
     */
    public SQLiteModule(Connection connection) {
        this.connection = connection;
    }

    /**
     * Alternative constructor.
     *
     * @param fileName The String path to the SQLite database File.
     */
    public SQLiteModule(String fileName) {
        super();
        establishConnection(fileName);
    }

    /**
     * Alternative constructor.
     *
     * @param file The File Object of the SQLite database File.
     */
    public SQLiteModule(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File is null!");
        }
        dbFile = file;
    }

    /**
     * Establishes the Connection handle for the SQLite database File.
     *
     * @param fileName The String name of the File Object for the SQLite database
     *                 File.
     */
    public void establishConnection(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Database File name is null or empty!");
        }
        String finalFileName = fileName;
        if (fileName.contains(".")) {
            finalFileName = finalFileName.split(".")[0];
        }
        dbFile = new File("database" + File.separator + fileName + ".db");
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        establishConnection();
    }

    /**
     * Establishes the Connection handle for the SQLite database File defined for
     * the SQLiteModule.
     */
    public void establishConnection() {
        if (dbFile == null) {
            throw new IllegalStateException("Database File has not been defined yet!");
        }
        dbFile.setReadable(true, false);
        dbFile.setExecutable(true, false);
        dbFile.setWritable(true, false);
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setConnection(connection);
    }

    /**
     * Returns the schema version of the SQLite database File.
     *
     * @return Returns the Integer value of the schema version.
     */
    public int getSchemaVersion() {
        PreparedStatement statement;
        try {
            statement = prepareStatement("PRAGMA user_version");
            ResultSet result = statement.executeQuery();
            int version = result.getInt(1);
            result.close();
            statement.close();
            return version;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Sets the schema version of the SQLite database File.
     *
     * @param version The Integer version to set.
     * @return Returns true if the schema version is successfully applied to the
     * SQLite database File.
     */
    public boolean setSchemaVersion(int version) {
        try {
            PreparedStatement statement = prepareStatement("PRAGMA user_version = " + version);
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param table The SQLite table to count.
     * @return Returns the Integer amount of rows in the given SQLite table.
     * @throws SQLException Thrown for any SQL Exceptions.
     */
    public int getRowCount(String table) throws SQLException {
        PreparedStatement statement = prepareStatement("SELECT COUNT(*) FROM " + table);
        ResultSet set = statement.executeQuery();
        int count = 0;
        while (set.next()) {
            count++;
        }
        return count;
    }

    /**
     * @param table     The 2D String Array of the table definition to check.
     * @param fieldName The String field name to test.
     * @return Returns true if the field exists in the given SQLite table.
     */
    public boolean doesFieldExist(String[][] table, String fieldName) {
        for (String[] field : table) {
            if (field != null && field[0] != null && field[0].equalsIgnoreCase(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param table     The 2D String Array of the table definition to check.
     * @param fieldName The String field name to test.
     * @param type      The String type to verify.
     * @return Returns true if the field exists in the given SQLite table, and that
     * it is the type given.
     */
    public boolean doesFieldExistWithType(String[][] table, String fieldName, String type) {
        for (String[] field : table) {
            if (field != null && field[0] != null && field[0].equalsIgnoreCase(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a column to the given table if it does not exists.
     *
     * @param tableName The String name of the SQLite table.
     * @param fieldName The String field name of the column being tested.
     * @param type      The String type the column represents in the SQLite table.
     * @throws SQLException
     */
    public void addTableColumnIfNotExists(String tableName, String fieldName, String type) throws SQLException {
        String[][] tableDefinition = getTableDefinitions(tableName);
        if (!doesFieldExistWithType(tableDefinition, fieldName, type)) {
            addTableColumn(tableName, fieldName, type);
        }
    }

    /**
     * Adds a column to the given table.
     *
     * @param tableName The String name of the SQLite table.
     * @param fieldName The String field name of the column being tested.
     * @param type      The String type the column represents in the SQLite table.
     * @throws SQLException
     */
    public void addTableColumn(String tableName, String fieldName, String type) throws SQLException {
        Statement statement = createStatement();
        statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + fieldName + " " + type);
        statement.close();
    }

    /**
     * Renames a column on a SQLite table.
     *
     * @param tableName    The String name of the SQLite table.
     * @param fieldName    The String name of the column to change.
     * @param fieldNameNew The String name to set for the column.
     * @throws SQLException
     */
    public void renameTableColumn(String tableName, String fieldName, String fieldNameNew) throws SQLException {
        String[][] table = getTableDefinitions(tableName);
        String tableNameBackup = tableName + "_backup";
        String columnString = "(";
        String columnString2 = "";
        String columnString3 = "(";
        String columnString4 = "";
        for (String[] field : table) {
            columnString += field[0] + " " + field[1] + ",";
            columnString2 += field[0] + ",";
            String name = field[0];
            if (name.equalsIgnoreCase(fieldName)) {
                name = fieldNameNew;
            }
            columnString3 += name + " " + field[1] + ",";
            columnString4 += name + ",";
        }
        columnString = columnString.substring(0, columnString.length() - 1) + ")";
        columnString2 = columnString2.substring(0, columnString2.length() - 1);
        columnString3 = columnString3.substring(0, columnString3.length() - 1) + ")";
        columnString4 = columnString4.substring(0, columnString4.length() - 1);
        String query1 = "ALTER TABLE " + tableName + " RENAME TO " + tableNameBackup + ";";
        String query2 = "CREATE TABLE " + tableName + columnString3 + ";";
        String query3 = "INSERT INTO " + tableName + "(" + columnString4 + ") SELECT " + columnString2 + " FROM "
                + tableNameBackup + ";";
        String query4 = "DROP TABLE " + tableNameBackup + ";";
        PreparedStatement statement;
        connection.setAutoCommit(false); // BEGIN TRANSACTION;
        statement = prepareStatement(query1);
        statement.executeUpdate();
        statement = prepareStatement(query2);
        statement.executeUpdate();
        statement = prepareStatement(query3);
        statement.executeUpdate();
        connection.commit(); // COMMIT;
        connection.setAutoCommit(true);
        // restartConnection();
        statement = prepareStatement(query4);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * @param tableName The String name of the SQLite table.
     * @return Returns a 2D String Array definition of the SQlite table.
     */
    public String[][] getTableDefinitions(String tableName) {
        List<String[]> arrayBuilder = new ArrayList<>();
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("PRAGMA table_info(" + tableName + ")");
            ResultSet result = statement.executeQuery();
            result.next();
            do {
                String[] field = new String[2];
                field[0] = result.getString("name");
                field[1] = result.getString("type");
                arrayBuilder.add(field);
            } while (result.next());
            String[][] table = new String[arrayBuilder.size()][2];
            for (int x = 0; x < arrayBuilder.size(); x++) {
                table[x] = arrayBuilder.get(x);
            }
            result.close();
            statement.close();
            return table;
        } catch (SQLException e) {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param tableName  The String name of the SQLite table.
     * @param targetName The String column name in the SQLite table to grab for each row
     *                   definition.
     * @return Returns a List of String definitions for each row in the SQLite
     * table column targeted.
     * @throws SQLException
     */
    public List<String> getAll(String tableName, String targetName) throws SQLException {
        PreparedStatement statement;
        List<String> list = new ArrayList<>();
        statement = prepareStatement("SELECT * FROM " + tableName);
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            list.add(result.getString(targetName));
        }
        result.close();
        statement.close();
        return list;
    }

    /**
     * @param tableName   The String name of the SQLite table.
     * @param targetNames The List of String names of the target columns in the SQLite
     *                    table to grab.
     * @return Returns a Map of String column names, that points to a List of
     * <String> definitions for each row in the SQLite table.
     * @throws SQLException
     */
    public Map<String, List<String>> getAll(String tableName, String[] targetNames) throws SQLException {
        // Create a Map to store each field respectively in lists.
        Map<String, List<String>> map = new HashMap<>();
        // Create a List for each field.
        for (String field : targetNames) {
            List<String> listField = new ArrayList<>();
            map.put(field, listField);
        }
        // Create a statement retrieving matched rows.
        PreparedStatement statement;
        statement = prepareStatement("SELECT * FROM " + tableName);
        // Execute and fetch iterator for returned rows.
        ResultSet result = statement.executeQuery();
        // Go through each row.
        while (result.next()) {
            // For each row, we go through the field(s) desired, and store their values in
            // the same order.
            for (String field : targetNames) {
                List<String> listField = map.get(field);
                listField.add(result.getString(field));
            }
        }
        // Close SQL handlers, and return the map.
        result.close();
        statement.close();
        return map;
    }

    /**
     * Returns a map of the first matched row of a given table.
     *
     * @param tableName  The String name of the SQLite table.
     * @param matchName  The String name of the column of the table to search for the
     *                   value given.
     * @param matchValue The String value given for the column to match.
     * @return Returns the Map definition of the row definition in the SQLite
     * table with each String column as the key, and the String
     * definition as the value.
     * @throws SQLException
     */
    public Map<String, String> getRow(String tableName, String matchName, String matchValue) throws SQLException {
        Map<String, String> map = new HashMap<>();
        // Create a statement retrieving matched rows.
        PreparedStatement statement;
        statement = prepareStatement(
                "SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
        // Execute and fetch iterator for returned rows.
        ResultSet result = statement.executeQuery();
        // Go through the first matched row.
        if (result.next()) {
            ResultSetMetaData meta = result.getMetaData();
            // Go through each column.
            for (int index = 0; index < meta.getColumnCount(); index++) {
                String columnName = meta.getColumnName(index);
                String value = result.getString(index);
                map.put(columnName, value);
            }
        }
        // Close SQL handlers, and return the map.
        result.close();
        statement.close();
        return map;
    }

    /**
     * Returns a map of the first matched row of a given table.
     *
     * @param tableName   The String name of the SQLite table.
     * @param matchNames  A List of String names of the columns of the table to match.
     * @param matchValues The List of String values of the columns to match. (This must
     *                    be the same length as the List prior.
     * @return Returns a Map of the first row that matches the column definitions
     * given with each String column as the key, and the String
     * definition as the value.
     * @throws SQLException
     */
    public Map<String, String> getRow(String tableName, String[] matchNames, String[] matchValues) throws SQLException {
        Map<String, String> map = new HashMap<>();
        // Create a statement retrieving matched rows.
        PreparedStatement statement;
        String s = "SELECT * FROM " + tableName + " WHERE ";
        for (int index = 0; index < matchNames.length; index++) {
            s += matchNames[index] + " = \"" + matchValues[index] + "\" AND ";
        }
        s = s.substring(0, s.length() - 5) + ";";
        statement = prepareStatement(s);
        // Execute and fetch iterator for returned rows.
        ResultSet result = statement.executeQuery();
        // Go through the first matched row.
        if (result.next()) {
            ResultSetMetaData meta = result.getMetaData();
            // Go through each column.
            for (int index = 0; index < meta.getColumnCount(); index++) {
                String columnName = meta.getColumnName(index);
                String value = result.getString(index);
                map.put(columnName, value);
            }
        }
        // Close SQL handlers, and return the map.
        result.close();
        statement.close();
        return map;
    }

    /**
     * @param tableName   The String name of the SQLite table.
     * @param matchName   The String column of the table to match.
     * @param matchValue  The String value of the column to match.
     * @param targetNames A String Array of target columns to retrieve definitions.
     * @return Returns a Map of all rows with the target column matching the value
     * given. The Map stores each targeted column as the key to a List of
     * String definitions for each defined column in the given Array
     * sequentially.
     * @throws SQLException
     */
    public Map<String, List<String>> getAll(String tableName, String matchName, String matchValue, String[] targetNames)
            throws SQLException {
        // Create a Map to store each field respectively in lists.
        Map<String, List<String>> map = new HashMap<>();
        // Create a List for each field.
        for (String field : targetNames) {
            List<String> listField = new ArrayList<>();
            map.put(field, listField);
        }
        // Create a statement retrieving matched rows.
        PreparedStatement statement;
        statement = prepareStatement(
                "SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
        // Execute and fetch iterator for returned rows.
        ResultSet result = statement.executeQuery();
        // Go through each row.
        while (result.next()) {
            // listMatches.add(result.getString(matchName));
            // For each row, we go through the field(s) desired, and store their values in
            // the same order.
            for (String field : targetNames) {
                List<String> listField = map.get(field);
                listField.add(result.getString(field));
            }
        }
        // Close SQL handlers, and return the map.
        result.close();
        statement.close();
        return map;
    }

    /**
     * @param tableName  The String name of the SQLite table.
     * @param matchName  The String column name to match.
     * @param matchValue The String definition to match.
     * @param targetName The String target column definition to return.
     * @return Returns a List of String definitions for the target column.
     * @throws SQLException
     */
    public List<String> getAll(String tableName, String matchName, String matchValue, String targetName)
            throws SQLException {
        PreparedStatement statement;
        List<String> list = new ArrayList<>();
        statement = prepareStatement(
                "SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            list.add(result.getString(targetName));
        }
        result.close();
        statement.close();
        return list;
    }

    /**
     * @param tableName  The String name of the SQLite table.
     * @param matchName  The String column to match.
     * @param matchValue The String definition to match.
     * @param targetName The String targeted column to return.
     * @return Returns the String value of the first matched row in the table with
     * the given column target with the value provided.
     * @throws SQLException
     */
    public String get(String tableName, String matchName, String matchValue, String targetName) throws SQLException {
        PreparedStatement statement;
        statement = prepareStatement(
                "SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            String targetValue = result.getString(targetName);
            result.close();
            statement.close();
            return targetValue;
        }
        result.close();
        statement.close();
        return null;
    }

    /**
     * @param tableName  The String name of the SQLite table.
     * @param matchName  The String column to match.
     * @param matchValue The String definition to match.
     * @return Returns true if the table contains a row that matches the value
     * without being case-sensitive.
     * @throws SQLException
     */
    public boolean hasIgnoreCase(String tableName, String matchName, String matchValue) throws SQLException {
        PreparedStatement statement;
        if (matchValue == null) {
            matchValue = "NULL";
        }
        statement = prepareStatement("SELECT * FROM " + tableName);
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            String matchedValue = result.getString(matchName);
            if (matchedValue.equalsIgnoreCase(matchValue)) {
                result.close();
                statement.close();
                return true;
            }
        }
        result.close();
        statement.close();
        return false;
    }

    /**
     * @param tableName  The String name of the SQLite table.
     * @param matchName  The String column to match.
     * @param matchValue The String definition to match.
     * @return Returns true if the table contains a row that matches the value.
     * (Case-Sensitive)
     * @throws SQLException
     */
    public boolean has(String tableName, String matchName, String matchValue) throws SQLException {
        PreparedStatement statement;
        if (matchValue == null) {
            matchValue = "NULL";
        }
        statement = prepareStatement(
                "SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            result.close();
            statement.close();
            return true;
        }
        result.close();
        statement.close();
        return false;
    }

    /**
     * @param tableName   The String name of the SQLite table.
     * @param matchNames  A String Array of the columns to match.
     * @param matchValues A String Array of the definitions to match. (The Array must be
     *                    the same length of the prior)
     * @return Returns true if the table contains a row that matches all columns and
     * the definitions provided.
     * @throws SQLException
     */
    public boolean has(String tableName, String[] matchNames, String[] matchValues) throws SQLException {
        PreparedStatement statement;
        if (matchNames == null) {
            throw new IllegalArgumentException("Match names array is null!");
        }
        if (matchValues == null) {
            throw new IllegalArgumentException("Match values array is null!");
        }
        if (matchNames.length != matchValues.length) {
            throw new IllegalArgumentException("Match name array and match field array are different sizes!");
        }
        String statementString = "SELECT * FROM " + tableName + " WHERE ";
        for (int x = 0; x < matchNames.length; x++) {
            if (x > 0) {
                statementString += " AND ";
            }
            statementString += "\"" + matchNames[x] + "\" = \"" + matchValues[x] + "\"";
        }
        statement = prepareStatement(statementString);
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            result.close();
            statement.close();
            return true;
        }
        result.close();
        statement.close();
        return false;
    }

    /**
     * Sets a definition for each SQLite table row with the provided required column
     * definitions.
     *
     * @param table       The String name of the SQLite table.
     * @param matchName   The String column to match.
     * @param matchValue  The String definition to match.
     * @param targetName  The String targeted column to define.
     * @param targetValue The String definition to set.
     * @throws SQLException
     */
    public void set(String table, String matchName, String matchValue, String targetName, String targetValue)
            throws SQLException {
        if (has(table, matchName, matchValue)) {
            update(table, matchName, matchValue, targetName, targetValue);
        } else {
            insert(table, new String[]{matchName, targetName}, new String[]{matchValue, targetValue});
        }
    }

    /**
     * Sets a list of definitions for each SQLite table row with the provided
     * required column definitions
     *
     * @param table      The String name of the SQLite table.
     * @param matchName  The String column to match.
     * @param matchValue The String definition to match.
     * @param fields     A String Array of columns to define.
     * @param values     A String Array of definitions to set.
     * @throws SQLException
     */
    public void set(String table, String matchName, String matchValue, String[] fields, String[] values)
            throws SQLException {
        if (has(table, matchName, matchValue)) {
            update(table, matchName, matchValue, fields, values);
        } else {
            insert(table, fields, values);
        }
    }

    /**
     * Updates all rows on a SQLite table that matches the column definitions with
     * the definitions provided.
     *
     * @param table      The String name of the SQLite table.
     * @param matchName  The String column to match.
     * @param matchValue The String definition to match.
     * @param fields     A String Array of columns to define.
     * @param values     A String Array of definitions to set.
     */
    private void update(String table, String matchName, String matchValue, String[] fields, String[] values) {
        String setString = "";
        for (int index = 0; index < fields.length; index++) {
            setString += fields[index] + " = \"" + values[index] + "\",";
        }
        setString = setString.substring(0, setString.length() - 1);
        String query = "UPDATE " + table + " SET " + setString + " WHERE " + matchName.length() + " = \""
                + matchValue.length() + ";";
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(query);
            statement.executeQuery().close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            stackTrace(e);
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                stackTrace("Failed to close statement.", e);
            }
        }
    }

    /**
     * Updates all rows on a SQLite table that matches the column definitions with
     * the definitions provided.
     *
     * @param table       The String name of the SQLite table.
     * @param matchName   The String column to match.
     * @param matchValue  The String definition to match.
     * @param targetName  The String column to define.
     * @param targetValue The String definition to set.
     * @throws SQLException
     */
    public void update(String table, String matchName, String matchValue, String targetName, String targetValue)
            throws SQLException {
        String stringStatement = "UPDATE " + table + " SET " + targetName + " = ? WHERE " + matchName + " = ?";
        PreparedStatement statement = prepareStatement(stringStatement);
        statement.setString(1, targetValue);
        statement.setString(2, matchValue);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Inserts a row into a SQLite table.
     *
     * @param table  The String name of the SQLite table.
     * @param names  The String columns to define.
     * @param values The String definitions to set.
     * @throws SQLException
     */
    public void insert(String table, String[] names, String[] values) throws SQLException {
        String nameBuild = "(";
        for (String name : names) {
            nameBuild += name + ",";
        }
        nameBuild = nameBuild.substring(0, nameBuild.length() - 1) + ")";
        String valueBuild = "(";
        for (@SuppressWarnings("unused")
                String value : values) {
            valueBuild += "?,";
        }
        valueBuild = valueBuild.substring(0, valueBuild.length() - 1) + ")";
        PreparedStatement statement = prepareStatement("INSERT INTO " + table + nameBuild + " VALUES " + valueBuild);
        for (int index = 0; index < values.length; index++) {
            statement.setString(index + 1, values[index]);
        }
        statement.executeUpdate();
        statement.close();
    }

    /**
     * @return Returns the Connection to the SQLite database File.
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Sets the Connection to the SQLite database File.
     *
     * @param connection The Connection to set.
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * @return Returns the String path to the SQLite database directory.
     */
    public String getDBCacheDirectory() {
        return GameWindow.getCacheDir() + File.separator + "db" + File.separator;
    }

    /**
     * Short-hand of a PreparedStatement declaration.
     *
     * @param query The String SQL query.
     * @return Returns a PreparedStatement Object.
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String query) throws SQLException {
        return getConnection().prepareStatement(query);
    }

    /**
     * Short-hand of a Statement declaration.
     *
     * @return Returns a Statement Object.
     * @throws SQLException
     */
    public Statement createStatement() throws SQLException {
        return getConnection().createStatement();
    }
}