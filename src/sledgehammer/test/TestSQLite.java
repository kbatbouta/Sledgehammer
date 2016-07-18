package sledgehammer.test;
//package sledgehammer.test;
//
//import java.sql.SQLException;
//
//import zombie.core.Core;
//import zombie.network.DataBaseBuffer;
//import zombie.network.ServerWorldDatabase;
//
public class TestSQLite {
//	
//	public static void println(String... messages) {
//		for (String message : messages)
//			System.out.println("[TEST-CASE] SQLite: " + message);
//	}
//	
//	public static void main(String[] args) {
//		try {
//			ServerWorldDatabase.instance.create();
//			Core.GameSaveWorld = "servertest";
//			Factions.instance = new Factions();
//			println("Schema Version: " + Factions.instance.getSchemaVersion());
//			testTableDefinitions();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static void testTableDefinitions() {
//		String[][] tableFactions = Factions.instance.getTableDefinitions(Factions.TABLE_FACTIONS);
//		println("" + Factions.instance.doesFieldExist(tableFactions, "name"));
//		println("" + Factions.instance.doesFieldExist(tableFactions, "name2"));
//		println("" + Factions.instance.doesFieldExist(tableFactions, "abbreviation"));
//		println("" + Factions.instance.doesFieldExist(tableFactions, "tag"));
//		println("" + Factions.instance.doesFieldExist(tableFactions, "owner"));
//	}
}
