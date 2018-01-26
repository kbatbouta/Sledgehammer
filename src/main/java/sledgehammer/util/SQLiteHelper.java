/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.util;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import zombie.GameWindow;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public abstract class SQLiteHelper extends Printable {

  public static final String SQL_STORAGE_CLASS_NULL = "NULL";
  public static final String SQL_STORAGE_CLASS_TEXT = "TEXT";
  public static final String SQL_STORAGE_CLASS_REAL = "REAL";
  public static final String SQL_STORAGE_CLASS_INTEGER = "INTEGER";
  public static final String SQL_STORAGE_CLASS_BLOB = "BLOB";
  public static final String SQL_AFFINITY_TYPE_TEXT = "TEXT";
  public static final String SQL_AFFINITY_TYPE_NUMERIC = "NUMERIC";
  public static final String SQL_AFFINITY_TYPE_INTEGER = "INTEGER";
  public static final String SQL_AFFINITY_TYPE_REAL = "REAL";
  public static final String SQL_STORAGE_CLASS_NONE = "NONE";

  private Connection connection = null;

  public SQLiteHelper(Connection connection) {
    this.connection = connection;
  }

  File dbFile;

  public SQLiteHelper() {
    super();
  }

  public SQLiteHelper(String fileName) {
    super();
    establishConnection(fileName);
  }

  public SQLiteHelper(File file) {
    if (file == null) {
      throw new IllegalArgumentException("File is null!");
    }
    dbFile = file;
  }

  public void establishConnection(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      throw new IllegalArgumentException("Database File name is null or empty!");
    }
    dbFile = new File(getDBCacheDirectory() + fileName + ".db");
    if (!dbFile.exists()) {
      try {
        if (dbFile.createNewFile()) {
          println(fileName + ".db created.");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    establishConnection();
  }

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

  public int getRowCount(String table) throws SQLException {
    PreparedStatement statement = prepareStatement("SELECT COUNT(*) FROM " + table);
    ResultSet set = statement.executeQuery();
    int count = 0;
    while (set.next()) count++;
    return count;
  }

  public boolean doesFieldExist(String[][] table, String fieldName) {
    for (String[] field : table) {
      if (field != null && field[0] != null && field[0].equalsIgnoreCase(fieldName)) {
        return true;
      }
    }
    return false;
  }

  public boolean doesFieldExistWithType(String[][] table, String fieldName, String type) {
    for (String[] field : table) {
      if (field != null && field[0] != null && field[0].equalsIgnoreCase(fieldName)) {
        return true;
      }
    }
    return false;
  }

  public void addTableColumnIfNotExists(String tableName, String fieldName, String type)
      throws SQLException {
    String[][] tableDefinition = getTableDefinitions(tableName);
    if (!doesFieldExistWithType(tableDefinition, fieldName, type)) {
      Statement statement = createStatement();
      statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + fieldName + " " + type);
      statement.close();
    }
  }

  public void addTableColumn(String tableName, String fieldName, String type) throws SQLException {
    Statement statement = createStatement();
    statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + fieldName + " " + type);
    statement.close();
  }

  public void renameTableColumn(String tableName, String fieldName, String fieldNameNew)
      throws SQLException {
    String[][] table = getTableDefinitions(tableName);
    String tableNameBackup = tableName + "_backup";
    String columnString;
    String columnString2;
    String columnString3;
    String columnString4;
    StringBuilder columnStringBuilder = new StringBuilder("(");
    StringBuilder columnString2Builder = new StringBuilder();
    StringBuilder columnString3Builder = new StringBuilder("(");
    StringBuilder columnString4Builder = new StringBuilder();
    for (String[] field : table) {
      columnStringBuilder.append(field[0]).append(" ").append(field[1]).append(",");
      columnString2Builder.append(field[0]).append(",");
      String name = field[0];
      if (name.equalsIgnoreCase(fieldName)) {
        name = fieldNameNew;
      }
      columnString3Builder.append(name).append(" ").append(field[1]).append(",");
      columnString4Builder.append(name).append(",");
    }
    columnString4 = columnString4Builder.toString();
    columnString3 = columnString3Builder.toString();
    columnString2 = columnString2Builder.toString();
    columnString = columnStringBuilder.toString();
    columnString = columnString.substring(0, columnString.length() - 1) + ")";
    columnString2 = columnString2.substring(0, columnString2.length() - 1);
    columnString3 = columnString3.substring(0, columnString3.length() - 1) + ")";
    columnString4 = columnString4.substring(0, columnString4.length() - 1);
    String query1 = "ALTER TABLE " + tableName + " RENAME TO " + tableNameBackup + ";";
    String query2 = "CREATE TABLE " + tableName + columnString3 + ";";
    String query3 =
        "INSERT INTO "
            + tableName
            + "("
            + columnString4
            + ") SELECT "
            + columnString2
            + " FROM "
            + tableNameBackup
            + ";";
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
    statement = prepareStatement(query4);
    statement.executeUpdate();
    statement.close();
  }

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

  public List<String> getAll(String tableName, String targetName) throws SQLException {
    PreparedStatement statement;
    List<String> list = new ArrayList<>();
    statement = prepareStatement("SELECT * FROM " + tableName);
    ResultSet result = statement.executeQuery();
    while (result.next()) list.add(result.getString(targetName));
    result.close();
    statement.close();
    return list;
  }

  public Map<String, List<String>> getAll(String tableName, String[] targetNames)
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
   * @param tableName The name of the Table.
   * @param matchName The column to match.
   * @param matchValue The value to match on the column.
   * @return Returns a map of the first matched row of a given table.
   * @throws SQLException Thrown when any issues arise.
   */
  public Map<String, String> getRow(String tableName, String matchName, String matchValue)
      throws SQLException {
    Map<String, String> map = new HashMap<>();
    // Create a statement retrieving matched rows.
    PreparedStatement statement;
    statement =
        prepareStatement(
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

  public Map<String, List<String>> getAll(
      String tableName, String matchName, String matchValue, String[] targetNames)
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
    statement =
        prepareStatement(
            "SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
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

  public List<String> getAll(
      String tableName, String matchName, String matchValue, String targetName)
      throws SQLException {
    PreparedStatement statement;
    List<String> list = new ArrayList<>();
    statement =
        prepareStatement(
            "SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
    ResultSet result = statement.executeQuery();
    while (result.next()) {
      list.add(result.getString(targetName));
    }
    result.close();
    statement.close();
    return list;
  }

  public String get(String tableName, String matchName, String matchValue, String targetName)
      throws SQLException {
    PreparedStatement statement;
    statement =
        prepareStatement(
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

  public boolean hasIgnoreCase(String tableName, String matchName, String matchValue)
      throws SQLException {
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

  public boolean has(String tableName, String matchName, String matchValue) throws SQLException {
    PreparedStatement statement;
    if (matchValue == null) {
      matchValue = "NULL";
    }
    statement =
        prepareStatement(
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

  public boolean has(String tableName, String[] matchNames, String[] matchValues)
      throws SQLException {
    PreparedStatement statement;
    if (matchNames == null) {
      throw new IllegalArgumentException("Match names array is null!");
    }
    if (matchValues == null) {
      throw new IllegalArgumentException("Match values array is null!");
    }
    if (matchNames.length != matchValues.length) {
      throw new IllegalArgumentException(
          "Match name array and match field array are different sizes!");
    }
    StringBuilder statementString = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");
    for (int x = 0; x < matchNames.length; x++) {
      if (x > 0) {
        statementString.append(" AND ");
      }
      statementString
          .append("\"")
          .append(matchNames[x])
          .append("\" = \"")
          .append(matchValues[x])
          .append("\"");
    }
    statement = prepareStatement(statementString.toString());
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

  public void set(
      String table, String matchName, String matchValue, String targetName, String targetValue)
      throws SQLException {
    if (has(table, matchName, matchValue)) {
      update(table, matchName, matchValue, targetName, targetValue);
    } else {
      insert(table, new String[] {matchName, targetName}, new String[] {matchValue, targetValue});
    }
  }

  public void set(
      String table,
      String identifierField,
      String identifierValue,
      String[] fields,
      String[] values)
      throws SQLException {
    if (has(table, identifierField, identifierValue)) {
      update(table, identifierField, identifierValue, fields, values);
    } else {
      insert(table, fields, values);
    }
  }

  private void update(
      String table,
      String identifierField,
      String identifierValue,
      String[] fields,
      String[] values) {
    StringBuilder setStringBuilder = new StringBuilder();
    for (int index = 0; index < fields.length; index++) {
      setStringBuilder.append(fields[index]).append(" = \"").append(values[index]).append("\",");
    }
    String setString = setStringBuilder.toString();
    setString = setString.substring(0, setString.length() - 1);
    String query =
        "UPDATE "
            + table
            + " SET "
            + setString
            + " WHERE "
            + identifierField.length()
            + " = \""
            + identifierValue.length()
            + ";";
    PreparedStatement statement = null;
    try {
      statement = prepareStatement(query);
      statement.executeQuery().close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      stackTrace(e);
    } finally {
      try {
        Objects.requireNonNull(statement).close();
      } catch (SQLException e) {
        stackTrace(e);
      }
    }
  }

  public void update(
      String table, String matchName, String matchValue, String targetName, String targetValue)
      throws SQLException {
    String stringStatement =
        "UPDATE " + table + " SET " + targetName + " = ? WHERE " + matchName + " = ?";
    PreparedStatement statement = prepareStatement(stringStatement);
    statement.setString(1, targetValue);
    statement.setString(2, matchValue);
    statement.executeUpdate();
    statement.close();
  }

  public void insert(String table, String[] names, String[] values) throws SQLException {
    StringBuilder nameBuildBuilder = new StringBuilder("(");
    for (String name : names) {
      nameBuildBuilder.append(name).append(",");
    }
    String nameBuild = nameBuildBuilder.toString();
    nameBuild = nameBuild.substring(0, nameBuild.length() - 1) + ")";
    StringBuilder valueBuildBuilder = new StringBuilder("(");
    for (@SuppressWarnings("unused") String value : values) {
      valueBuildBuilder.append("?,");
    }
    String valueBuild = valueBuildBuilder.toString();
    valueBuild = valueBuild.substring(0, valueBuild.length() - 1) + ")";
    PreparedStatement statement =
        prepareStatement("INSERT INTO " + table + nameBuild + " VALUES " + valueBuild);
    for (int index = 0; index < values.length; index++) {
      statement.setString(index + 1, values[index]);
    }
    statement.executeUpdate();
    statement.close();
  }

  public String encrypt(String previousPwd) {
    if (previousPwd == null || previousPwd.isEmpty()) {
      return "";
    } else {
      byte[] encrypted = null;
      try {
        encrypted = MessageDigest.getInstance("MD5").digest(previousPwd.getBytes());
      } catch (NoSuchAlgorithmException var6) {
        println("Can\'t encrypt password");
        var6.printStackTrace();
      }
      StringBuilder hashString = new StringBuilder();
      for (byte crypt : Objects.requireNonNull(encrypted)) {
        String hex = Integer.toHexString(crypt);
        if (hex.length() == 1) {
          hashString.append('0');
          hashString.append(hex.charAt(hex.length() - 1));
        } else {
          hashString.append(hex.substring(hex.length() - 2));
        }
      }
      return hashString.toString();
    }
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public String getDBCacheDirectory() {
    return GameWindow.getCacheDir() + File.separator + "db" + File.separator;
  }

  public Connection getConnection() {
    return this.connection;
  }

  public PreparedStatement prepareStatement(String query) throws SQLException {
    return getConnection().prepareStatement(query);
  }

  public Statement createStatement() throws SQLException {
    return getConnection().createStatement();
  }
}
