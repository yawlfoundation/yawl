/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.exceptions.YPersistenceException;

/**
 * This is just a skeleton implementation.  It is most definitely the wrong way to go about this,
 * but I am not about to go refactor 20 classes before I know something works or not.
 * 
 * @author Dean Mao
 * @created Mar 14, 2006
 */
public class EngineFactory {
	
	private static YEngine engine;
	
	public EngineFactory() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static YEngine createYEngine(boolean journalising) {
		try {
			if (engine == null) {
				engine = YEngine.createInstance( journalising );
			}
			return engine;
		}
		catch( YPersistenceException ype ) {
			// ignored
		}
		return null;
	}
	
	public static YEngine createYEngine() {
		return createYEngine(false);
	}

	/**
	 * Right now this method makes no sense because there is only one type of engine
	 * 
	 * @return
	 */
	public static AbstractEngine createEngine() {
		return createYEngine();
	}
	
	/**
	 * Right now this method makes no sense because there is only one type of engine
	 * 
	 * @param journalising
	 * @return
	 */
	public static AbstractEngine createEngine(boolean journalising) {
		return createYEngine(journalising);
	}
}
