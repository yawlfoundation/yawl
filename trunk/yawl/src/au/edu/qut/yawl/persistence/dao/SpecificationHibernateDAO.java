/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;
import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;

import au.edu.qut.yawl.elements.KeyValue;
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
import au.edu.qut.yawl.elements.data.YEnablementParameter;
import au.edu.qut.yawl.elements.data.YInputParameter;
import au.edu.qut.yawl.elements.data.YOutputParameter;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.engine.domain.YCaseData;

public class SpecificationHibernateDAO implements SpecificationDAO{
	
	private static SessionFactory sessions;
	private static AnnotationConfiguration cfg;
	private static boolean deleteAfterRun = true;
	private Session session;
	private static SpecificationHibernateDAO INSTANCE;

	private static Class[] classes = new Class[] {
						KeyValue.class,
						YFlow.class,
						YMultiInstanceAttributes.class,
						YCompositeTask.class,
						YAtomicTask.class,
						YTask.class,
						YInputCondition.class,
						YOutputCondition.class,
						YCondition.class,
						YExternalNetElement.class,
						YAWLServiceReference.class,
						YAWLServiceGateway.class,
						YVariable.class,
						YParameter.class,
						YInputParameter.class,
						YOutputParameter.class,
						YEnablementParameter.class,
						YDecomposition.class,
						YNet.class,
						YCaseData.class,
						YMetaData.class,
						YSpecification.class
				};
	
	private static void initializeSessions() {
		if ( sessions != null ) sessions.close();
		try {
			AnnotationConfiguration config = (AnnotationConfiguration) new AnnotationConfiguration()
	        .setProperty(Environment.USE_SQL_COMMENTS, "false")
	        .setProperty(Environment.SHOW_SQL, "false")
	        .setProperty(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
	        .setProperty(Environment.DRIVER, "org.postgresql.Driver")
	        .setProperty(Environment.URL, "jdbc:postgresql://localhost/dean2")
	        .setProperty(Environment.USER, "capsela")
	        .setProperty(Environment.PASS, "capsela")
//			.setProperty(Environment.HBM2DDL_AUTO, "create-drop")
			;
			cfg = config;
	        
			for (int i=0; i<classes.length; i++) {
				cfg.addAnnotatedClass( classes[i] );
			}
			sessions = cfg.buildSessionFactory();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Session openSession() throws HibernateException {
		session = sessions.openSession();
		return session;
	}
	
	protected String[] getAnnotatedPackages() {
		return new String[] {};
	}

	public boolean delete(YSpecification t) {
		try {
			initializeSessions();
			session = openSession();
			Transaction tx = session.beginTransaction();
			YSpecification spec = (YSpecification) session.get(YSpecification.class, (Serializable) getKey(t));
			session.delete(spec);
			tx.commit();
			session.close();
			return true;
		}
			catch (Exception e) {
				e.printStackTrace();
				try {
					if ( session!=null && session.isOpen() ) {
						if ( session.isConnected() ) session.connection().rollback();
						session.close();
					}
				}
				catch (Exception ignore) {ignore.printStackTrace();}
				try {
					if (sessions!=null) {
						sessions.close();
						sessions=null;
					}
				}
				catch (Exception ignore) {ignore.printStackTrace();}
				return false;
			}
	}

	public YSpecification retrieve(Object key) {
		try {
			initializeSessions();
			YSpecification retval;
			session = openSession();
			Transaction tx = session.beginTransaction();
			retval = (YSpecification) session.get(YSpecification.class, (Serializable) key);
			tx.commit();
			session.close();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			try {
				if ( session!=null && session.isOpen() ) {
					if ( session.isConnected() ) session.connection().rollback();
					session.close();
				}
			}
			catch (Exception ignore) {ignore.printStackTrace();}
			try {
				if (sessions!=null) {
					sessions.close();
					sessions=null;
				}
			}
			catch (Exception ignore) {ignore.printStackTrace();}
			return null;
		}
	}

	public int save(YSpecification m) {
		try {
			initializeSessions();
			Transaction tx;
			session = openSession();
			tx = session.beginTransaction();
//			session.delete(m);
//			tx.commit();
//			tx = session.beginTransaction();
			session.saveOrUpdate(m);
			System.out.println("Persisted " + m.getID());
			tx.commit();
			session.close();
			return 0;
		}
		catch (Exception e) {
			e.printStackTrace();
			try {
				if ( session!=null && session.isOpen() ) {
					if ( session.isConnected() ) session.connection().rollback();
					session.close();
				}
			}
			catch (Exception ignore) {ignore.printStackTrace();}
			try {
				if (sessions!=null) {
					sessions.close();
					sessions=null;
				}
			}
			catch (Exception ignore) {ignore.printStackTrace();}
			return 1;
		}
	}

    public Object getKey(YSpecification m) {
        return m.getDbID();
    }
}
