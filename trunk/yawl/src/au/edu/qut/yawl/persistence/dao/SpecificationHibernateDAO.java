/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
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
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.engine.domain.YCaseData;

public class SpecificationHibernateDAO implements SpecificationDAO{
	private static final Log LOG = LogFactory.getLog(SpecificationHibernateDAO.class);

	private static SessionFactory sessionFactory;
	private static AnnotationConfiguration cfg;
	private static SpecificationHibernateDAO INSTANCE;
	private static Session session;
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
						YDecomposition.class,
						YNet.class,
						YCaseData.class,
						YMetaData.class,
						YSpecification.class
				};
	
	private static void initializeSessions() {
		if ( sessionFactory != null ) sessionFactory.close();
		try {
			AnnotationConfiguration config = (AnnotationConfiguration) new AnnotationConfiguration()
	        .setProperty(Environment.USE_SQL_COMMENTS, "false")
	        .setProperty(Environment.SHOW_SQL, "false")
	        .setProperty(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
	        .setProperty(Environment.DRIVER, "org.postgresql.Driver")
	        .setProperty(Environment.URL, "jdbc:postgresql://localhost/dean2")
	        .setProperty(Environment.USER, "capsela")
	        .setProperty(Environment.PASS, "capsela")
//			.setProperty(Environment.HBM2DDL_AUTO, "create")
//			.setProperty(Environment.HBM2DDL_AUTO, "create-drop")
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
	
	private Session openSession() throws HibernateException {
		if (session == null) {
			initializeSessions();
		}
		return session;
	}
	
	protected String[] getAnnotatedPackages() {
		return new String[] {};
	}

	public boolean delete(YSpecification t) {
		Session session = null;
		try {
			session = openSession();
			Transaction tx = session.beginTransaction();
			YSpecification spec = (YSpecification) session.get(YSpecification.class, (Serializable) getKey(t));
			System.out.println(">>>>" + spec.getDbID());
			for (YDecomposition decomp: spec.getDecompositions()) {
				System.out.println("><" + decomp.getId() + ":" + decomp.getDbID());
			}
			session.delete(spec);
			tx.commit();
			return true;
		}
		catch(ObjectDeletedException ode) {
			LOG.error("Deletion failure", ode);
			return false;
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
					if (sessionFactory!=null) {
						sessionFactory.close();
						sessionFactory=null;
					}
				}
				catch (Exception ignore) {
					LOG.error(ignore);
				}
				return false;
			}
	}

	public YSpecification retrieve(Object key) {
		Session session = null;
		try {
			YSpecification retval;
			session = openSession();
			Transaction tx = session.beginTransaction();
			retval = (YSpecification) session.get(YSpecification.class, (Serializable) key);
			tx.commit();
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
			catch (Exception ignore) {			
				LOG.error(ignore);
			}
			try {
				if (sessionFactory!=null) {
					sessionFactory.close();
					sessionFactory=null;
				}
			}
			catch (Exception ignore) {ignore.printStackTrace();}
			return null;
		}
	}

	public int save(YSpecification m) {
		Session session = null;
		try {
			Transaction tx;
			session = openSession();
			tx = session.beginTransaction();
			session.saveOrUpdate(m);
			LOG.info("Persisting " + m.getDbID());
			tx.commit();
			return 0;
		}
		catch (HibernateException e2) {
			LOG.error(e2);
		}
		catch (Exception e) {
			LOG.error(e);
			try {
				if ( session!=null && session.isOpen() ) {
					if ( session.isConnected() ) session.connection().rollback();
					session.close();
				}
			}
			catch (Exception ignore) {ignore.printStackTrace();}
			try {
				if (sessionFactory!=null) {
					sessionFactory.close();
					sessionFactory=null;
				}
			}
			catch (Exception ignore) {
				LOG.error(ignore);
			}
		}
		return 1;
	}

    public Serializable getKey(YSpecification m) {
        return m.getDbID();
    }

    public List getChildren(Object parent) {
    	List retval = new ArrayList();
		Session session = openSession();
		Criteria query = session.createCriteria(YSpecification.class);
//		query.setParameter(0, parent.toString());
		retval = query.list();
		LOG.debug("retrieving " + Arrays.asList(retval));
		return retval;
	}

}
