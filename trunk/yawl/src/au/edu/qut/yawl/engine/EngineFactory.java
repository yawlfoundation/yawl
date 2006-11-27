/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.edu.qut.yawl.exceptions.YPersistenceException;

/**
 * This is just a skeleton implementation.  It is most definitely the wrong way to go about this,
 * but I am not about to go refactor 20 classes before I know something works or not.
 * 
 * @author Dean Mao
 * @created Mar 14, 2006
 */
public class EngineFactory {
	private static final String CONTEXT_CONFIG_LOCATION = "applicationContext.xml"; // TODO context config file location
	private static ApplicationContext application_context = null;

	public static void resetEngine() {
		engine = null;
		transactionalengine = null;
	}

	private static YEngine engine;
	
	/*
	 * Store the transactional version of the engine as well
	 * This is used by the EngineGatewayImpl class
	 * */
	private static YEngineInterface transactionalengine;

	public static YEngineInterface getTransactionalEngine() throws YPersistenceException {
		createYEngine(true);
		if (transactionalengine==null) {
			return engine;
		} 
		return transactionalengine;
		
	}
	
	public EngineFactory() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static YEngine getExistingEngine() {
		return engine;
	}
	
	public static ApplicationContext getApplicationContext() {
		return application_context;
	}
	public static void setApplicationContext(ApplicationContext context) {
		application_context = context;
	}
	
	public static YEngine createYEngine(boolean journalising) throws YPersistenceException {
		if (journalising) {
			if (engine==null) {
				if (application_context==null) {
					application_context = new ClassPathXmlApplicationContext(CONTEXT_CONFIG_LOCATION);
				}
				/*
				 * If this is a journalising engine
				 * we need to use the spring framework and get the interceptor bean
				 * to ensure transactionality
				 * */

				transactionalengine = (YEngineInterface) application_context.getBean("EngineInterceptor2");
				engine = transactionalengine.getYEngine();
				//engine = transactionalengine.gffetYEngine();
				transactionalengine.setJournalising(journalising);
				transactionalengine.initialise();


			} 
		} else {			
			if (engine==null) {
				engine = YEngine.createInstance(journalising);
				engine.initialise();
			}

		}
		return engine;
	}
	
	public static YEngine createYEngine() throws YPersistenceException {
		return createYEngine(false);
	}

	/**
	 * Right now this method makes no sense because there is only one type of engine
	 * 
	 * @return
	 */
	public static AbstractEngine createEngine() throws YPersistenceException {
		return createYEngine();
	}
	
	/**
	 * Right now this method makes no sense because there is only one type of engine
	 * 
	 * @param journalising
	 * @return
	 */
	public static AbstractEngine createEngine(boolean journalising) throws YPersistenceException {
		return createYEngine(journalising);
	}
}
