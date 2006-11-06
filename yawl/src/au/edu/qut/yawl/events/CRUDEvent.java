/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.events;

// Create Read Update Delete
public class CRUDEvent {
	public static final String CREATE = "create";
	public static final String UPDATE = "update";
	public static final String DELETE = "delete";
	
	private String state;
	
	public CRUDEvent( String state ) {
		this.state = state;
	}
}
