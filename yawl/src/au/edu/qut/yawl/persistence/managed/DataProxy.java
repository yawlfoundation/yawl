package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

/**
 * @author SandozM
 *
 * @param <DataType>
 */
public class DataProxy implements PropertyChangeListener {

	public void propertyChange( PropertyChangeEvent evt ) {
		// TODO delegate, over override?  probably being overridden by subclasses anyway
		
	}

	private DataContext context;

	boolean isDirty = true;

	/**
	 * Utility field used by constrained properties.
	 */
	private java.beans.VetoableChangeSupport vetoableChangeSupport = new java.beans.VetoableChangeSupport(
			this);

	/**
	 * Adds a VetoableChangeListener to the listener list.
	 * @param l The listener to add.
	 */
	public void addVetoableChangeListener(java.beans.VetoableChangeListener l) {

		vetoableChangeSupport.addVetoableChangeListener(l);
	}

	/**
	 * Removes a VetoableChangeListener from the listener list.
	 * @param l The listener to remove.
	 */
	public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {

		vetoableChangeSupport.removeVetoableChangeListener(l);
	}

	/**
	 * Getter for property data.
	 * @return Value of property data.
	 */
	public Object getData() {
		return context.getData(this);
	}

	public DataProxy(DataContext context, VetoableChangeListener listener) {
		this.context = context;
		this.addVetoableChangeListener(listener);
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
		return null;
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
		PropertyChangeEvent evt = new PropertyChangeEvent(o, attributeName, oldval, attributeValue);
		this.vetoableChangeSupport.fireVetoableChange(evt);
		
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
}
