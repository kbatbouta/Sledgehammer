package sledgehammer.modules.core;
import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.objects.LuaObject;
import sledgehammer.wrapper.Player;
import zombie.network.GameServer;

public class CoreClientListener implements EventListener {

	private ModuleCore module;
	
	public CoreClientListener(ModuleCore module) {
		this.module = module;
	}
	
	public String[] getTypes() {
		return new String[] { ClientEvent.ID };
	}
	

	public void handleEvent(Event e) {
		// Cast to proper Event sub-class.
		ClientEvent event = (ClientEvent) e;
		
		// Get event content.
		String module     = event.getModule();
		String command    = event.getCommand();
		KahluaTable table = event.getTable();
		Player player     = event.getPlayer();
		
		if (module.equalsIgnoreCase("core")) {
			if (command.equalsIgnoreCase("handshake")) {
				// We just want to ping back to the client saying we received the request.
				event.respond();
			}
			
			if(command.equalsIgnoreCase("requestInfo")) {
				
				// Create a table to return.
				KahluaTable rTable = LuaObject.newTable();
				
				// Set the ID of the player.
				rTable.rawset("playerID", (double) player.getID());
				System.out.println(rTable.rawget("playerID"));
				// Return the command with the table.
				event.respond(rTable);
			}
		}
	}

	@Override
	public boolean runSecondary() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
