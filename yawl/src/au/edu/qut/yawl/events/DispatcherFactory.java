package au.edu.qut.yawl.events;


public class DispatcherFactory {
	private static EventDispatcher consoleDispatcher;
	private static EventDispatcher jmsDispatcher;
	private static EventDispatcher springDispatcher;
	
	public static EventDispatcher getConsoleDispatcher() {
		if( consoleDispatcher == null )
			consoleDispatcher = new ConsoleEventDispatcher();
		return consoleDispatcher;
	}
	
	public static EventDispatcher getJmsDispatcher() {
		if( jmsDispatcher == null )
			jmsDispatcher = new JMSEventDispatcher();
		return jmsDispatcher;
	}
	
	public static EventDispatcher getSpringDispatcher() {
		if( springDispatcher == null )
			springDispatcher = new SpringEventDispatcher();
		return springDispatcher;
	}
}
