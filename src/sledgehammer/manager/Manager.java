package sledgehammer.manager;

import sledgehammer.util.Printable;

public abstract class Manager extends Printable {

	abstract void onStart();
	
	abstract void onShutDown();
	
}
