/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;

import au.edu.qut.yawl.elements.KeyValue;
import au.edu.qut.yawl.elements.SpecVersion;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YMetaData;
import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.state.IdentifierSequence;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YCaseData;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemID;
import au.edu.qut.yawl.events.Event;
import au.edu.qut.yawl.events.YCaseEvent;
import au.edu.qut.yawl.events.YDataEvent;
import au.edu.qut.yawl.events.YErrorEvent;
import au.edu.qut.yawl.events.YServiceError;
import au.edu.qut.yawl.events.YWorkItemEvent;

public class YAWLTransactionAdvice implements ThrowsAdvice, MethodBeforeAdvice, AfterReturningAdvice {
	private static final Log LOG = LogFactory.getLog( YAWLTransactionAdvice.class );

	private static SessionFactory sessionFactory;
	private static Session session;

	private Transaction tx = null;
	
	private static AnnotationConfiguration cfg;
	private static Class[] classes = new Class[] {
		Event.class,
        IdentifierSequence.class,
		KeyValue.class,
		YAtomicTask.class,
		YAWLServiceGateway.class,
		YAWLServiceReference.class,
		YCaseData.class,
		YCaseEvent.class,
		YCompositeTask.class,
		YCondition.class,
		YDataEvent.class,
		YDecomposition.class,
		YExternalNetElement.class,
		YFlow.class,
		YIdentifier.class,
		YInputCondition.class,
		YInternalCondition.class,
		YMetaData.class,
		YMultiInstanceAttributes.class,
		YNet.class,
		YNetRunner.class,
		YOutputCondition.class,
		YParameter.class,
		YSpecification.class,
		SpecVersion.class,
		YTask.class,
		YVariable.class,
		YWorkItem.class,
		YWorkItemEvent.class,
		YWorkItemID.class,
		YErrorEvent.class,
		YServiceError.class};
	
	private synchronized static void initializeSessions() {
		//if ( sessionFactory != null ) sessionFactory.close();
		try {
			AnnotationConfiguration config = (AnnotationConfiguration) new AnnotationConfiguration()
//	        .setProperty(Environment.USE_SQL_COMMENTS, "false")
//	        .setProperty(Environment.SHOW_SQL, "false")
//	        .setProperty(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
//	        .setProperty(Environment.DRIVER, "org.postgresql.Driver")
//	        .setProperty(Environment.URL, "jdbc:postgresql://localhost/dean2")
//	        .setProperty(Environment.USER, "postgres")
//	        .setProperty(Environment.PASS, "admin")

//			.setProperty(Environment.HBM2DDL_AUTO, "create")
//			.setProperty(Environment.HBM2DDL_AUTO, "create-drop")
//			.setProperty(Environment.HBM2DDL_AUTO, "update")
			;
			cfg = config;
	        
			for (int i=0; i<classes.length; i++) {
				cfg.addAnnotatedClass( classes[i] );
			}
			sessionFactory = cfg.buildSessionFactory();
			session = sessionFactory.openSession();
		}
		catch (Error e) {
			e.printStackTrace();
		}
	}
	
	public static Session openSession() throws HibernateException {
		if (sessionFactory==null) {
			initializeSessions();
		}
		
		if (session==null || !session.isOpen()) {
			session = sessionFactory.openSession();
		}
		
		return session;
	}
	
	public YAWLTransactionAdvice() {
		// TODO Auto-generated constructor stub
	}
	
	public void setSessionFactory(SessionFactory fac) {
		sessionFactory = fac;
		session = sessionFactory.openSession();
		
	}
	
	public static Session getSession() {
		return session;
	}
	
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public static SessionFactory getFactory() {
		return sessionFactory;
	}
	

	public void before(Method m, Object[] args, Object target) throws Throwable {
		try {
			synchronized (openSession()) {
				tx = session.beginTransaction();
                tx.begin();
			}
		} catch (Exception e) {
			e.printStackTrace();					
		}
		
	}

	public synchronized void afterReturning(Object returnValue, Method m, Object[] args, Object target) throws Throwable {
		try {
			synchronized (session) {
				//System.out.println("Exiting engine " + m.getName() + " - > Commit");
				tx.commit();
				//System.out.println("Exiting engine " + m.getName() + " - > Commit Complete");
			}
		} catch (Exception e) {
			e.printStackTrace();					
		}
	}

    public void afterThrowing(Exception ex) {
		synchronized (session) {
			LOG.error( ex );
			ex.printStackTrace();
			
        	tx.rollback();
			
        	session.close();
		}
    }

}
