package sledgehammer.event;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import sledgehammer.wrapper.Player;

public class PlayerInfoEvent extends PlayerEvent {

	public static final String ID = "PlayerInfoEvent";
	
	private Vector3f position;
	private Vector2f metaPosition;
	private boolean asleep = false;
	private boolean onFire = false;
	private String log = null;
	
	public PlayerInfoEvent(Player player, float x, float y, float z, float mx, float my, boolean onFire, boolean asleep) {
		super(player);
		metaPosition = new Vector2f(mx, my);
		position = new Vector3f(x, y, z);
		setOnFire(onFire);
		setAsleep(asleep);
	}

	private void setAsleep(boolean asleep) {
		this.asleep = asleep;
	}

	public boolean isAsleep() {
		return this.asleep;
	}
	
	private void setOnFire(boolean onFire) {
		this.onFire = onFire;
	}

	public boolean isOnFire() {
		return this.onFire;
	}

	@Override
	public String getLogMessage() {
		return this.log;
	}
	
	public void setLogMessage(String log) {
		this.log = log;
	}
	
	public Vector3f getPosition() {
		return this.position;
	}
	
	public Vector2f getMetaPosition() {
		return this.metaPosition;
	}

	@Override public String getID() { return ID; }
}
