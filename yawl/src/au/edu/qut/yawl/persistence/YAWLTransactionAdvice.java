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
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;

public class YAWLTransactionAdvice implements ThrowsAdvice, MethodBeforeAdvice, AfterReturningAdvice {
	private static final Log LOG = LogFactory.getLog( YAWLTransactionAdvice.class );

	private static SessionFactory sessionFactory;
	private static Session session;

	private Transaction tx = null;
	
	public static Session openSession() throws HibernateException {
		if (session==null || !session.isOpen()) {
			session = getFactory().openSession();
		}
		return session;
	}
	
	public YAWLTransactionAdvice() {
	}
	
	public void setSessionFactory(SessionFactory fac) {
		sessionFactory = fac;
	}
	
	public static Session getSession() {
		return session;
	}
	
	public SessionFactory getSessionFactory() {
		return getFactory();
	}
	
	public static SessionFactory getFactory() {
		if (sessionFactory == null) {
			throw new Error("SHOULD NEVER HAPPEN - SESSION FACTORY WAS NULL. SHOULD BE INITED BY SPRING");
		}
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
