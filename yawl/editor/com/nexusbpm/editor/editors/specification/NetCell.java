package com.nexusbpm.editor.editors.specification;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.events.StateEvent;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class NetCell<Type> extends DefaultGraphCell implements DataProxyStateChangeListener{

	//to be notified of proxy state changes
	public void proxyAttached(DataProxy proxy, Object data, DataProxy parent) {}
	public void proxyAttaching(DataProxy proxy, Object data, DataProxy parent) {}
	public void proxyDetached(DataProxy proxy, Object data, DataProxy parent) {}
	public void proxyDetaching(DataProxy proxy, Object data, DataProxy parent) {}
	public void propertyChange(PropertyChangeEvent evt) {
		
		//TODO update on all changes
		//also remove self as listener to UO when removing gcc from this as listener
		//also mirror this for inserting.

		if (evt.getPropertyName().equals(EditorDataProxy.STATE_ATTRIBUTE)) {
			Object oldValue = this.attributes.get(ACTIVITY_STATE);
			this.attributes.put(ACTIVITY_STATE, evt.getNewValue());
			if ((oldValue != null && !(oldValue.equals(evt.getNewValue()))) || oldValue == null) {
				support.firePropertyChange(ACTIVITY_STATE, oldValue, evt.getNewValue());
			}
		}
	}

	private static final Log LOG = LogFactory.getLog(NetCell.class);

	public static final String LABEL = "netcell.label";
	public static final String TYPE = "netcell.type";
	public static final String ACTIVITY_STATE = "netcell.state";
	public PropertyChangeSupport support = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		EditorDataProxy proxy = ((EditorDataProxy) this.getUserObject());
		if (!proxy.containsChangeListener(this)) {
			proxy.addChangeListener(this);
			updateFromUserObject();
		}
		support.addPropertyChangeListener(listener);
	}
	public void firePropertyChange(PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		((EditorDataProxy) this.getUserObject()).removeChangeListener(this);
		support.removePropertyChangeListener(listener);
	}
	public NetCell(EditorDataProxy proxy, AttributeMap arg1) {
		super(proxy, arg1);
		updateFromUserObject();
	}
	
	public void updateFromUserObject() {
		this.attributes.put(LABEL, ((EditorDataProxy)getUserObject()).getLabel());
		this.attributes.put(TYPE, ((EditorDataProxy)getUserObject()).getData().getClass().getName());
		this.attributes.put(ACTIVITY_STATE, StateEvent.ACTIVE);
	}
	
	public YExternalNetElement getTask() {
		return (YExternalNetElement) ((EditorDataProxy) getUserObject()).getData();
	}
	
	public NetCell(EditorDataProxy arg0) {
		this(arg0, null);
	}
	
	public Type getUserObject() {
		return (Type) super.getUserObject();
	}
		
}
