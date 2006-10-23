package au.edu.qut.yawl.events;


public class DispatcherFactory {
	private static YEventDispatcher consoleDispatcher;
	private static YEventDispatcher jmsDispatcher;
	private static EventDispatcher springDispatcher;
	
	public static YEventDispatcher getConsoleDispatcher() {
		if( consoleDispatcher == null )
			consoleDispatcher = new ConsoleEventDispatcher();
		return consoleDispatcher;
	}
	
	public static YEventDispatcher getJmsDispatcher() {
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
