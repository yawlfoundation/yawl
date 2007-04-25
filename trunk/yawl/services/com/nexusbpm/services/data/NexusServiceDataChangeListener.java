package com.nexusbpm.services.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public interface NexusServiceDataChangeListener extends PropertyChangeListener{

	public void propertyAdded(PropertyChangeEvent event);
	public void propertyRemoved(PropertyChangeEvent event);

}
