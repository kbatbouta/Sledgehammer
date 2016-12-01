package sledgehammer.event;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import sledgehammer.wrapper.Player;

public class PlayerInfoEvent extends PlayerEvent {

	public static final String ID = "PlayerInfoEvent";
	private String log = null;
	
	public PlayerInfoEvent(Player player) {
		super(player);
	}

	@Override
	public String getLogMessage() {
		return this.log;
	}
	
	public void setLogMessage(String log) {
		this.log = log;
	}
	
	public Vector3f getPosition() {
		return getPlayer().getPosition();
	}
	
	public Vector2f getMetaPosition() {
		return getPlayer().getMetaPosition();
	}

	@Override public String getID() { return ID; }
}
