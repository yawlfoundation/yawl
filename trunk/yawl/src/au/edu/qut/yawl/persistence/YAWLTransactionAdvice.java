/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.lang.reflect.Method;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.hibernate.Transaction;

public class YAWLTransactionAdvice implements ThrowsAdvice, MethodBeforeAdvice, AfterReturningAdvice {

	private static SessionFactory sessionFactory;
	private static Session session;

	private Transaction tx = null;
	
	public YAWLTransactionAdvice() {
		// TODO Auto-generated constructor stub
	}
	
	public void setSessionFactory(SessionFactory fac) {
		this.sessionFactory = fac;
		session = sessionFactory.openSession();
		
	}
	
	public static Session getSession() {
		return session;
	}
	
	
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	public static SessionFactory getFactory() {
		return sessionFactory;
	}
	

	public void before(Method m, Object[] args, Object target) throws Throwable {
		try {
			synchronized (session) {
				tx = session.beginTransaction();
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
			
        	tx.rollback();
			
		}
    }

}
