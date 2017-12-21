package sledgehammer.test;

import java.util.UUID;

import sledgehammer.SledgeHammer;
import sledgehammer.lua.Node;
import sledgehammer.lua.permissions.PermissionGroup;
import sledgehammer.lua.permissions.PermissionUser;
import sledgehammer.module.permissions.ModulePermissions;

public class TestPermissions extends TestModule<ModulePermissions> {

	private UUID uuidUser1 = UUID.randomUUID();
	private PermissionUser user1;
	private UUID uuidUser2 = UUID.randomUUID();
	private PermissionUser user2;
	private String group1Name = "group1";
	private PermissionGroup group1;
	private String group2Name = "group2";
	private PermissionGroup group2;
	private String node1String = "sledgehammer.test.permission1";

	public TestPermissions() {
		initializeSledgehammer();
		setModule((ModulePermissions)SledgeHammer.instance.getPluginManager().getModule(ModulePermissions.class));
	}

	@Override
	public ModulePermissions createModule() {
		return null;
	}

	@Override
	public void run() {
		pause();
		println("Running Tests..");
		/*
		test1();
		pause();
		test2();
		pause();
		test3();
		pause();
		test4();
		pause();
		test5();
		pause();
		*/
		test6();
		println("Tests completed. Cleaning up...");
		cleanUp();
	}

	private void cleanUp() {
		println("Cleaning up.");
		try {
			deleteUser1();
		} catch (Exception e) {
		}
		try {
			deleteUser2();
		} catch (Exception e) {
		}
		try {
			deleteGroup1();
		} catch (Exception e) {
		}
		try {
			deleteGroup2();
		} catch (Exception e) {
		}
	}

	/**
	 * (Internal Method)
	 * 
	 * Create a <PermissionUser>, then delete it.
	 */
	private void test1() {
		try {
			println("Running Test 1: Create PermissionUser.");
			createUser1();
			pause();
			println("Deleting PermissionUser...");
			deleteUser1();
			println("Test 1 completed.");
		} catch (Exception e) {
			println("Test 1 failed.");
			cleanUp();
			e.printStackTrace();
		}
	}

	/**
	 * (Internal Method)
	 * 
	 * Create a <PermissionGroup>, then delete it.
	 */
	private void test2() {
		try {
			println("Running Test 2: Create PermissionGroup.");
			createGroup1();
			pause();
			println("Deleting PermissionGroup...");
			deleteGroup1();
			println("Test 2 completed.");
		} catch (Exception e) {
			println("Test 2 failed.");
			cleanUp();
			e.printStackTrace();
		}
	}

	/**
	 * (Internal Method)
	 * 
	 * Create a <PermissionUser>, a <PermissionGroup> and assign the user to that
	 * group. Remove the group from the user, and then delete the group and user.
	 */
	private void test3() {
		try {
			println("Running Test 3: Assign PermissionUser to PermissionGroup."
					+ "\nDelete PermissionGroup to verify that the PermissionUser is updated with no group.");
			createUser1();
			createGroup1();
			group1.addMember(user1, true);
			pause();
			println("Removing PermissionUser from PermissionGroup...");
			group1.removeMember(user1, true);
			pause();
			println("Deleting PermissionGroup...");
			deleteGroup1();
			pause();
			println("Deleting PermissionUser...");
			deleteUser1();
			println("Test 3 completed.");
		} catch (Exception e) {
			println("Test 3 failed.");
			cleanUp();
			e.printStackTrace();
		}
	}

	/**
	 * (Internal Method)
	 * 
	 * Tests <PermissionGroup> inheritance by creating 2 groups and 1
	 * <PermissionUser>. Group 1 will be assigned the parent for group 2. The user
	 * will be assigned to group 2. Group 2 will be deleted. Then the other
	 * permission objects will be deleted.
	 */
	private void test4() {
		try {
			println("Running Test 4: Group inheritance.");
			createGroup1();
			createGroup2();
			createUser1();
			group2.addMember(user1, true);
			group2.setParent(group1, true);
			pause();
			deleteGroup2();
			pause();
			deleteGroup1();
			println("Deleting PermissionUser...");
			deleteUser1();
			println("Test 4 completed.");
		} catch (Exception e) {
			println("Test 4 failed.");
			cleanUp();
			e.printStackTrace();
		}
	}

	/**
	 * (Internal Method)
	 * 
	 * Tests <PermissionUser> permission assignment.
	 */
	private void test5() {
		boolean flag = false;
		try {
			println("Running Test 5: User permission assignment.");
			createUser1();
			pause();
			println("Adding permission node: \"" + node1String + "\"...");
			Node node1 = user1.setPermission(node1String, true, true);
			flag = user1.hasPermission(node1String);
			println("User -> hasPermission(\"" + node1String + "\") = " + flag);
			pause();
			println("Setting node flag to false...");
			node1.setFlag(false, true);
			flag = user1.hasPermission(node1String);
			println("User -> hasPermission(\"" + node1String + "\") = " + flag);
			pause();
			user1.removeNode(node1, true);
			flag = user1.hasPermission(node1String);
			println("User -> hasPermission(\"" + node1String + "\") = " + flag);
			pause();
			println("Deleting PermissionUser...");
			deleteUser1();
			println("Test 5 completed.");
		} catch (Exception e) {
			println("Test 5 failed.");
			cleanUp();
			e.printStackTrace();
		}
	}

	private void test6() {
		boolean flag = false;
		try {
			println("Running Test 6: ");
			createGroup1();
			pause();
			createUser1();
			pause();
			group1.addMember(user1, true);
			pause();
			group1.setPermission(node1String, true, true);
			pause();
			flag = user1.hasPermission(node1String);
			println("User -> hasPermission(\"" + node1String + "\") = " + flag);
			pause();
			deleteGroup1();
			deleteUser1();
			println("Test 6 completed.");
		} catch (Exception e) {
			println("Test 6 failed.");
			cleanUp();
			e.printStackTrace();
		}
	}

	private void createUser1() {
		println("Creating PermissionUser 1...");
		println("UUID for PermissionUser 1: \"" + uuidUser1.toString() + "\".");
		user1 = getModule().createPermissionUser(uuidUser1);
	}

	private void deleteUser1() {
		getModule().deletePermissionUser(user1);
		user1 = null;
	}

	private void createUser2() {
		println("Creating PermissionUser 2...");
		println("UUID for PermissionUser 2: \"" + uuidUser2.toString() + "\".");
		user2 = getModule().createPermissionUser(uuidUser2);
	}

	private void deleteUser2() {
		getModule().deletePermissionUser(user2);
		user2 = null;
	}

	private void createGroup1() {
		println("Creating PermissionGroup 1...");
		group1 = getModule().createPermissionGroup(group1Name);
	}

	private void deleteGroup1() {
		getModule().deletePermissionGroup(group1);
		group1 = null;
	}

	private void createGroup2() {
		println("Creating PermissionGroup 2...");
		group2 = getModule().createPermissionGroup(group2Name);
	}

	private void deleteGroup2() {
		getModule().deletePermissionGroup(group2);
		group2 = null;
	}

	public static void main(String[] args) {
		new TestPermissions().runTest();
	}

	@Override
	public String getName() {
		return "TestPermissions";
	}
}
