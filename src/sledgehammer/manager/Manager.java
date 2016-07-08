package sledgehammer.manager;

import sledgehammer.util.Printable;

public abstract class Manager extends Printable {

	public abstract void onStart();
	
	public abstract void onShutDown();
	
}
