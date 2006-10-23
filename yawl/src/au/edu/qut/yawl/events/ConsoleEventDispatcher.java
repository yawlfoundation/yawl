package au.edu.qut.yawl.events;

import java.io.Serializable;


public class ConsoleEventDispatcher implements YEventDispatcher {
	protected ConsoleEventDispatcher() {
	}
	public void fireEvent( Serializable event ) {
		System.out.println( event.toString() );
	}
}
