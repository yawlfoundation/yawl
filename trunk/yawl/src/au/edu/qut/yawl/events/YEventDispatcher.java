package au.edu.qut.yawl.events;

import java.io.Serializable;

public interface YEventDispatcher {

	public void fireEvent(Serializable o);
	
}
