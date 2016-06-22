package sledgehammer.module;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zombie.GameWindow;

public abstract class SQLModule extends Module {
	private Connection connection = null;
	
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
	
	public SQLModule(Connection connection) {
		this.connection = connection;
	}
	
	File dbFile;
	
	public SQLModule() {
		super();
	}
	
	public SQLModule(String fileName) {
		super();
		establishConnection(fileName);
	}
	
	public SQLModule(File file) {
		if(file == null) throw new IllegalArgumentException("File is null!");
		
		dbFile = file;
		
	}
	
	public void establishConnection(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("Database File name is null or empty!");
		}
		
		String finalFileName = fileName;
		if(fileName.contains(".")) {
			finalFileName = finalFileName.split(".")[0];
		}
		
		dbFile = new File(getDBCacheDirectory() + fileName + ".db");
		
		if (!dbFile.exists()) {
			try {
				dbFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		establishConnection();
	}
	
	public void establishConnection() {
		if (dbFile == null) throw new IllegalStateException("Database File has not been defined yet!");
		dbFile.setReadable(true, false);
		dbFile.setExecutable(true, false);
		dbFile.setWritable(true, false);

		Connection connection = null;
		
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
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
		} catch(SQLException e) {
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
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean doesFieldExist(String[][] table, String fieldName) {
		for(String[] field : table) {
			if(field != null && field[0] != null && field[0].equalsIgnoreCase(fieldName)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean doesFieldExistWithType(String[][] table, String fieldName, String type) {
		for(String[] field : table) {
			if(field != null && field[0] != null && field[0].equalsIgnoreCase(fieldName)) {
				return true;
			}
		}
		return false;
	}
	
	public void addTableColumnIfNotExists(String tableName, String fieldName, String type) throws SQLException {
		String[][] tableDefinition = getTableDefinitions(tableName);
		if(!doesFieldExistWithType(tableDefinition, fieldName, type)) {			
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
	
	public void renameTableColumn(String tableName, String fieldName, String fieldNameNew) throws SQLException {
		String[][] table = getTableDefinitions(tableName);
		String tableNameBackup = tableName + "_backup";
		
		String columnString = "(";
		String columnString2 = "";
		String columnString3 = "(";
		String columnString4 = "";
		for(String[] field : table) {
			columnString += field[0] + " " + field[1] + ",";
			columnString2 += field[0] + ",";
			
			String name = field[0];
			if(name.equalsIgnoreCase(fieldName)) name = fieldNameNew;
			columnString3 += name + " " + field[1] + ",";
			columnString4 += name + ",";
		}
		columnString = columnString.substring(0, columnString.length() - 1) + ")";
		columnString2 = columnString2.substring(0, columnString2.length() - 1);

		columnString3 = columnString3.substring(0, columnString3.length() - 1) + ")";
		columnString4 = columnString4.substring(0, columnString4.length() - 1);
		
		String query1 = "ALTER TABLE " + tableName + " RENAME TO " + tableNameBackup + ";";
		String query2 = "CREATE TABLE " + tableName + columnString3 + ";";
		String query3 = "INSERT INTO " + tableName + "(" + columnString4 + ") SELECT " + columnString2 + " FROM " + tableNameBackup + ";";
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
		//restartConnection();
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
			} while(result.next());
					
			String[][] table = new String[arrayBuilder.size()][2];
			for(int x = 0; x < arrayBuilder.size(); x++) {
				table[x] = arrayBuilder.get(x);
			}
			result.close();
			statement.close();
			return table;
		} catch (SQLException e) {
			if(statement != null) {
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
	
	
	public Map<String, List<String>> getAll(String tableName, String[] targetNames) throws SQLException {
		
		// Create a Map to store each field respectively in lists.
		Map<String, List<String>> map = new HashMap<>();
		
		// Create a List for each field. 
		for(String field : targetNames) {
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
			
			// For each row, we go through the field(s) desired, and store their values in the same order.			
			for(String field : targetNames) {
				List<String> listField = map.get(field);
				listField.add(result.getString(field));
			}
			
		}
		// Close SQL handlers, and return the map.
		result.close();
		statement.close();
		return map;
	}
	
	public Map<String, List<String>> getAll(String tableName, String matchName, String matchValue, String[] targetNames) throws SQLException {
	
		// Create a Map to store each field respectively in lists.
		Map<String, List<String>> map = new HashMap<>();
		
		// List<String> listMatches = new ArrayList<>();
		
		// Create a List for each field. 
		for(String field : targetNames) {
			List<String> listField = new ArrayList<>();
			map.put(field, listField);
		}

		// Create a statement retrieving matched rows.
		PreparedStatement statement;
		statement = prepareStatement("SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
		
		// Execute and fetch iterator for returned rows.
		ResultSet result = statement.executeQuery();
		
		// Go through each row.
		while (result.next()) {
			
			// listMatches.add(result.getString(matchName));
			
			// For each row, we go through the field(s) desired, and store their values in the same order.			
			for(String field : targetNames) {
				List<String> listField = map.get(field);
				listField.add(result.getString(field));
			}
			
		}
		// Close SQL handlers, and return the map.
		result.close();
		statement.close();
		return map;
	}
	
	public List<String> getAll(String tableName, String matchName, String matchValue, String targetName) throws SQLException {
		PreparedStatement statement;
		List<String> list = new ArrayList<>();
		statement = prepareStatement("SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
		ResultSet result = statement.executeQuery();
		while (result.next()) list.add(result.getString(targetName));
		result.close();
		statement.close();
		return list;
	}
	
	public String get(String tableName, String matchName, String matchValue, String targetName) throws SQLException {
		PreparedStatement statement;
		statement = prepareStatement("SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
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
	
	public boolean hasIgnoreCase(String tableName, String matchName, String matchValue) throws SQLException {
		PreparedStatement statement;
		if(matchValue == null) matchValue = "NULL";
		statement = prepareStatement("SELECT * FROM " + tableName);
		ResultSet result = statement.executeQuery();
		
		while (result.next()) {
			String matchedValue = result.getString(matchName);
			if(matchedValue.equalsIgnoreCase(matchValue)) {
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
		if(matchValue == null) matchValue = "NULL";
		statement = prepareStatement("SELECT * FROM " + tableName + " WHERE " + matchName + " = \"" + matchValue + "\"");
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
	
	public boolean has(String tableName, String[] matchNames, String[] matchValues) throws SQLException {
		PreparedStatement statement;
		if(matchNames == null) throw new IllegalArgumentException("Match names array is null!");
		if(matchValues == null) throw new IllegalArgumentException("Match values array is null!");
		if(matchNames.length != matchValues.length) throw new IllegalArgumentException("Match name array and match field array are different sizes!");
		
		String statementString = "SELECT * FROM " + tableName + " WHERE ";
		
		for(int x = 0; x < matchNames.length; x++) {
			if(x > 0) statementString += " AND ";
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
	
	public String encrypt(String previousPwd) {
		if (previousPwd == null || previousPwd.isEmpty()) {
			return "";
		} else {
			byte[] crypted = null;

			try {
				crypted = MessageDigest.getInstance("MD5")
						.digest(previousPwd.getBytes());
			} catch (NoSuchAlgorithmException var6) {
				println("Can\'t encrypt password");
				var6.printStackTrace();
			}
			StringBuilder hashString = new StringBuilder();
			for (int i = 0; i < crypted.length; ++i) {
				String hex = Integer.toHexString(crypted[i]);
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
	
//	public void restartConnection() {
//		try {
//			try {
//				connection.close();
//				connection = null;
//				
//				File dbFile = new File(GameWindow.getCacheDir() + File.separator + "db" + File.separator + GameServer.ServerName + ".db");
//				dbFile.setReadable(true, false);
//				dbFile.setExecutable(true, false);
//				dbFile.setWritable(true, false);
//				DebugLog.log("user database \"" + dbFile.getPath() + "\"");
//				Class.forName("org.sqlite.JDBC");
//				if (!dbFile.exists()) {
//					try {
//						dbFile.createNewFile();
//						connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
//					} catch (Exception var11) {
//						var11.printStackTrace();
//                        LoggerManager.getLogger("admin").write("SQLite failed to create Connection to db file.");
//                        SledgeHammer.instance.handleCommand(null, "quit");
//					}
//				}
//
//				if (connection == null) {
//					try {
//						connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
//					} catch (Exception var10) {
//						DebugLog.log("failed to create user database, server shut down");
//                        LoggerManager.getLogger("admin").write("SQLite failed to create Connection to db file.");
//                        SledgeHammer.instance.handleCommand(null, "quit");
//					}
//				}
//
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			DataBaseBuffer.setConnection(connection);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
	
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
