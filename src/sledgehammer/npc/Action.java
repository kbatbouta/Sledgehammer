package sledgehammer.npc;

import sledgehammer.util.Printable;

public abstract class Action extends Printable {
	
	public abstract void start(NPC npc);
	
	public abstract void update(NPC npc);
	
	public abstract void end(NPC npc);
	
}
