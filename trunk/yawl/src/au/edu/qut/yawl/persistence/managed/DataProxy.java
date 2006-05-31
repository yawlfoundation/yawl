package au.edu.qut.yawl.persistence.managed;

import java.beans.VetoableChangeListener;

/**
 * @author SandozM
 *
 * @param <DataType>
 */
public class DataProxy {

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

	public DataContext getContext() {
		return context;
	}

	public Object getAttribute(String attributeName) {
		return null;
	}

	public void setAttribute(String attributeName, Object attributeValue) {
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
}
