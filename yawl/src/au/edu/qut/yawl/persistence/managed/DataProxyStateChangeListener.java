/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeListener;

public interface DataProxyStateChangeListener extends PropertyChangeListener{

	void proxyDetached(DataProxy proxy, Object data, DataProxy parent);
	void proxyAttached(DataProxy proxy, Object data, DataProxy parent);
}
