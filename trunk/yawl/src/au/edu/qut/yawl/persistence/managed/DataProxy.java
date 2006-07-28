/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthew Sandoz
 *
 * @param <DataType>
 */
public class DataProxy<Type> {

	private DataContext context;
	private String label;
	private Serializable id;
	private Class dataClass;
	boolean isDirty = true;
	protected List<DataProxyStateChangeListener> listeners = new ArrayList<DataProxyStateChangeListener>();
	
	/**
	 * Adds a VetoableChangeListener to the listener list.
	 * @param l The listener to add.
	 */
	public void addChangeListener(DataProxyStateChangeListener l) {
		listeners.add(l);
	}

	/**
	 * Removes a VetoableChangeListener from the listener list.
	 * @param l The listener to remove.
	 */
	public void removeChangeListener(DataProxyStateChangeListener l) {
		listeners.remove(l);
	}

	/**
	 * Getter for property data.
	 * @return Value of property data.
	 */
	public Type getData() {
		return (Type) context.getData(this);
	}
	
	public DataProxy(DataContext context, DataProxyStateChangeListener listener) {
		this.context = context;
        if( listener != null )
            this.addChangeListener(listener);
	}

	public DataProxy() {
	}

	public DataContext getContext() {
		return context;
	}

	public void setContext(DataContext context) {
		this.context = context;
	}

	public Object getAttribute(String attributeName) {
		Object retval = null;
		try {
			String name = attributeName.substring(0,1).toUpperCase() + attributeName.substring(1);
			retval = context.getData(this).getClass().getMethod("get" + name, new Class[] {}).invoke(context.getData(this), new Object[] {});
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return retval;
	}

	public void setAttribute(String attributeName, Object attributeValue) throws PropertyVetoException {
		Object o = context.getData(this);
		Object oldval = attributeValue;
		String name = attributeName.substring(0,1).toUpperCase() + attributeName.substring(1);
		try {
			oldval = o.getClass().getMethod("get" + name, new Class[] {}).invoke(o, new Object[] {});
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireUpdated(attributeName, oldval, attributeValue);
	}

	public void fireDetached(Object value, DataProxy parent) {
		for (DataProxyStateChangeListener listener: listeners) {
			listener.proxyDetached(this, value, parent);
		}
	}
	
	public void fireAttached(Object value, DataProxy parent) {
		for (DataProxyStateChangeListener listener: listeners) {
			listener.proxyAttached(this, value, parent);
		}
	}
	
	public void fireUpdated(String attributeName, Object oldValue, Object newValue) {
		DataProxyStateChangeEvent evt = DataProxyStateChangeEvent
			.createPropertyChangeEvent(
					this, attributeName, oldValue, newValue, 
				DataProxyStateChangeEvent.Type.UPDATE);
		for (DataProxyStateChangeListener listener: listeners) {
			listener.propertyChange(evt);
		}
	}
	
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public Class getDataClass() {
		return dataClass;
	}

	public void setDataClass(Class dataClass) {
		this.dataClass = dataClass;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Serializable getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = id;
	}
}
