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
