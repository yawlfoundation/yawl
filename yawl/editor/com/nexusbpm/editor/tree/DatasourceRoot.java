/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.tree;

/**
 * @author Matthew Sandoz
 *
 */
public class DatasourceRoot {

	private Object location;
	public DatasourceRoot(Object location) {this.location = location;}
	public DatasourceRoot() {}
	public Object getLocation() {
		return location;
	}
	public void setLocation(Object location) {
		this.location = location;
	}
	public String toString() { return location.toString();}
}
