/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.events;


public class StateEvent extends Event {
	public static final String ACTIVE = "active";
    public static final String ENTERED = "entered";
    public static final String EXECUTING = "executing";
    public static final String COMPLETE = "complete";
    
    private String state;
    
    public StateEvent( String state ) {
    	this.state = state;
    }
    
	public String getState() {
		return state;
	}

	@Override
	public String toString() {
		return "";//"\n\n-----------\n" + state + "\n----------\n";
	}
}
