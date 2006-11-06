/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

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
