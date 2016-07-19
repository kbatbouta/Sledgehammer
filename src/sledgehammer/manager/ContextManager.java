package sledgehammer.manager;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;

public class ContextManager extends Manager {
	
	public static final String NAME = "ContextManager";
	
	private Map<String, String> mapContexts;

	public ContextManager(SledgeHammer sledgeHammer) {
		super(sledgeHammer);
	}
	
	@Override
	public void onLoad() {
		mapContexts = new HashMap<>();
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onUpdate() {
	
	}

	@Override
	public void onShutDown() {
		mapContexts.clear();
		mapContexts = null;
	}
	
	@Override
	public String getName() { return NAME; }
	
	
}
