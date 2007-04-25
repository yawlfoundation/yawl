package com.nexusbpm.services.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public class NexusServiceDataChangeSupport extends PropertyChangeSupport {

	private Object source; //cant get at super!
	
	public NexusServiceDataChangeSupport(Object source) {
		super(source);
		this.source = source;
	}
	
	public void firePropertyAdd(String propName, Object newValue) {
		PropertyChangeEvent evt = new PropertyChangeEvent(source, propName, null, newValue);
		for (PropertyChangeListener listener: getPropertyChangeListeners()) {
			((NexusServiceDataChangeListener) listener).propertyAdded(evt);
		};
	}
	public void firePropertyRemove(String propName) {
		PropertyChangeEvent evt = new PropertyChangeEvent(source, propName, null, null);
		for (PropertyChangeListener listener: (PropertyChangeListener[]) getPropertyChangeListeners()) {
			((NexusServiceDataChangeListener) listener).propertyRemoved(evt);
		};
	}

}
