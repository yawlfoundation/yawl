package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeListener;

public interface DataProxyStateChangeListener extends PropertyChangeListener{

	void proxyDetached(DataProxy proxy, Object data);
	void proxyAttached(DataProxy proxy, Object data);
}
