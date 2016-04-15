package zirc.event;

import zirc.wrapper.Player;

public class PVPAttackEvent extends Event {
	
	public static final String ID = "PVPAttackEvent";
	
	private Player playerAttacking;
	private Player playerAttacked;
	private String weapon;
	
	public PVPAttackEvent(Player attacking, Player attacked, String weapon) {
		super();
		this.playerAttacking = attacking;
		this.playerAttacked = attacked;
		this.weapon = weapon;
	}
	
	public Player getPlayerAttacking() {
		return this.playerAttacking;
	}
	
	public Player getPlayerAttacked() {
		return this.playerAttacked;
	}
	
	public String getWeapon() {
		return this.weapon;
	}

	@Override
	public String getLogMessage() {
		return playerAttacking.getUsername() + " is attacking " + playerAttacked.getUsername() + ".";
	}

	@Override
	public String getName() {
		return ID;
	}

}
