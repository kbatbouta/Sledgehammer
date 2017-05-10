package sledgehammer.manager;

/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.List;

import sledgehammer.SledgeHammer;
import sledgehammer.interfaces.ContextListener;

public class ContextManager extends Manager {
	
	public static final String NAME = "ContextManager";
	
	private List<ContextListener> listContextListeners;

	@Override
	public void onLoad() {
		listContextListeners = new ArrayList<>();
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onUpdate() {
	}

	@Override
	public void onShutDown() {
		listContextListeners.clear();
		listContextListeners = null;
	}
	
	public void saveContexts() {
		// TODO.
	}
	
	public void loadContexts() {
		// TODO.
	}
	
	public String getContext(String context) {
		String result = null;
		
		for(ContextListener listener : getContextListeners()) {
			if(listener != null) {
				result = listener.getContext(context);
				if(result != null) break;
			}
		}
		
		return result;
	}
	
	public void register(ContextListener listener) {
		if(listener == null) throw new IllegalArgumentException("ContextListener is null!");
		List<ContextListener> listContextListeners = getContextListeners();
		if(!listContextListeners.contains(listener)) {
			listContextListeners.add(listener);
		}
	}
	
	public void unregister(ContextListener listener) {
		if(listener == null) throw new IllegalArgumentException("ContextListener is null!");
		List<ContextListener> listContextListeners = getContextListeners();
		listContextListeners.remove(listener);
	}
	
	public List<ContextListener> getContextListeners() {
		return listContextListeners;
	}
	
	@Override
	public String getName() { return NAME; }
	
	
}
