/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeEvent;

public class DataProxyStateChangeEvent extends PropertyChangeEvent{

	protected Type type;
	
	public static DataProxyStateChangeEvent createDetachEvent(Object proxy, Object value) {
		return new DataProxyStateChangeEvent(proxy, null, value, null, Type.DETACH);
	}
	
	public static DataProxyStateChangeEvent createAttachEvent(Object proxy, Object value) {
		return new DataProxyStateChangeEvent(proxy, null, null, value, Type.ATTACH);
	}
	
	public static DataProxyStateChangeEvent createPropertyChangeEvent(Object source, String propertyName,
		     Object oldValue, Object newValue, Type type) {
		return new DataProxyStateChangeEvent(source, propertyName, oldValue, newValue, Type.UPDATE);
	}
	
	protected DataProxyStateChangeEvent(Object source, String propertyName,
		     Object oldValue, Object newValue, Type type) {
		super(source, propertyName, oldValue, newValue);
		this.type = Type.UPDATE;
	}
	
	public enum Type {UPDATE, ATTACH, DETACH};
	
	public Type getType() {return type;}

}
