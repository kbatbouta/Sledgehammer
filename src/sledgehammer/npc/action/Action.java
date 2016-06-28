package sledgehammer.npc.action;

import sledgehammer.util.Printable;
import zombie.sledgehammer.npc.NPC;

public abstract class Action extends Printable {
	public abstract boolean act(NPC npc);
	
}
