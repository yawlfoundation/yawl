package com.nexusbpm.editor.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ActionEventSupport {
	private Object sourceObject;

	private final List<ActionListener> listeners = new ArrayList<ActionListener>();

	public ActionEventSupport() {
		sourceObject = null;
	}

	public ActionEventSupport(Object sourceObject) {
		this.sourceObject = sourceObject;
	}

	public void addListener(ActionListener listener) {
		listeners.add(listener);
	}

	public void fireEvent() {
		fireEvent(0, null);
	}

	public void fireEvent(ActionEvent event) {
		for (ActionListener listener: listeners) {
			listener.actionPerformed(event);
		}
	}

	public void fireEvent(int id, String command) {
		fireEvent(sourceObject, id, command);
	}

	public void fireEvent(Object source, int id, String command) {
		ActionEvent event = new ActionEvent(source, id, command);
		fireEvent(event);
	}

	public void removeListener(ActionListener listener) {
		listeners.remove(listener);
	}
}
