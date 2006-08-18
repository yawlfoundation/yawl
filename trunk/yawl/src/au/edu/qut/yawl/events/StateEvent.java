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

	@Override
	public String toString() {
		return "\n\n\n\n\n-----------\n" + state + "\n----------\n";
	}
}
