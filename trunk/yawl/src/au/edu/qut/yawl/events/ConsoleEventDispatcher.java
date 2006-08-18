package au.edu.qut.yawl.events;


public class ConsoleEventDispatcher implements EventDispatcher {
	protected ConsoleEventDispatcher() {
	}
	public void fireEvent( Event event ) {
		System.out.println( event.toString() );
	}
}
