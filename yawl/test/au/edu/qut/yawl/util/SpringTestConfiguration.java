package au.edu.qut.yawl.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

import com.nexusbpm.services.LocalClientConfiguration;

public class SpringTestConfiguration {
	public static enum Configuration {DEFAULT, NONE, JMS, NEXUS_CLIENT};
	
	private static Map<Configuration, BootstrapConfiguration> configs = new HashMap<Configuration, BootstrapConfiguration>();
	
	private static TransactionStatus transaction;

	public static final String ROOT_APPLICATION_CONTEXT_FILENAME="testresources/applicationContext.xml";
	public static final String TEST_JMS_CLIENT_APPLICATION_CONTEXT_FILENAME="testresources/jmsClientApplicationContext.xml";
	public static final String TEST_QUARTZ_APPLICATION_CONTEXT_FILENAME="testresources/quartzServerApplicationContext.xml";
	public static final String TEST_JMS_CLIENT_PROPERTIES_FILENAME = "testresources/jms.client.properties";
	public static final String TEST_QUARTZ_SERVER_PROPERTIES_FILENAME = "testresources/quartz.server.properties";
	
	public static final String TEST_NEXUS_APPLICATION_CONTEXT_FILENAME="testresources/YawlClientApplicationContext.xml";
	public static final String TEST_NEXUS_APPLICATION_PROPERTIES_FILENAME = "testresources/editor.properties";

	
	private static BootstrapConfiguration createTestConfiguration(String contextFilename, String propertiesFilename) {
		LocalClientConfiguration lc = new LocalClientConfiguration(
				contextFilename,
				propertiesFilename
		);
		return lc;
	}
	
	public static Object getBean(String description) {
		return BootstrapConfiguration.getInstance().getApplicationContext().getBean(description);
	}
	
	public static void setupTestConfiguration(Configuration configuration) {
		if( !configs.containsKey( configuration ) ) {
			switch(configuration) {
			case DEFAULT:
				configs.put(
						Configuration.DEFAULT,
						createTestConfiguration(ROOT_APPLICATION_CONTEXT_FILENAME, "") );
				break;
			case JMS:
				configs.put(
						Configuration.JMS,
						createTestConfiguration(
								TEST_JMS_CLIENT_APPLICATION_CONTEXT_FILENAME,
								TEST_JMS_CLIENT_PROPERTIES_FILENAME) );
				break;
			case NEXUS_CLIENT:
				configs.put(
						Configuration.NEXUS_CLIENT,
						createTestConfiguration(
								TEST_NEXUS_APPLICATION_CONTEXT_FILENAME,
								TEST_NEXUS_APPLICATION_PROPERTIES_FILENAME) );
				break;
			case NONE:
			default:
			}
			
		}
		if( configs.get( configuration ) != null )
			BootstrapConfiguration.setInstance( configs.get( configuration ) );
	}
	
	public static DAOFactory getDAOFactory() {
		return (DAOFactory) getBean("daoFactory");
	}
	
	public static HibernateTransactionManager getTransactionManager() {
		return (HibernateTransactionManager) getBean("transactionManager");
	}
	
	public static void startTestTransaction() {
		HibernateTransactionManager txm = getTransactionManager();
    	transaction = txm.getTransaction(new DefaultTransactionDefinition());
    	transaction.setRollbackOnly();
	}
	
	public static void startNewTestTransaction() {
		if(transaction != null && !transaction.isCompleted()) {
			rollbackTestTransaction();
		}
		startTestTransaction();
	}
	
	public static void startRealTransaction() {
		HibernateTransactionManager txm = getTransactionManager();
    	transaction = txm.getTransaction(new DefaultTransactionDefinition());
	}
	
	public static void rollbackTestTransaction() {
		HibernateTransactionManager txm = getTransactionManager();
		txm.rollback(transaction);
	}

	public static void commitTransaction() {
		HibernateTransactionManager txm = getTransactionManager();
		txm.commit(transaction);
	}

}
