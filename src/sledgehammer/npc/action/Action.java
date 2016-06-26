package sledgehammer.npc.action;

import sledgehammer.npc.NPC;
import sledgehammer.util.Printable;

public abstract class Action extends Printable {
	public abstract boolean act(NPC npc);
	
}
