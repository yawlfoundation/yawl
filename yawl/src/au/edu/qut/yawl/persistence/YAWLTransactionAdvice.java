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
		System.out.println("Entering engine  " + m.getName() + " - > Starting transaction");
		System.out.println(session.isOpen());
		tx = session.beginTransaction();
	}

	public void afterReturning(Object returnValue, Method m, Object[] args, Object target) throws Throwable {
		System.out.println("Exiting engine " + m.getName() + " - > Commit");
		tx.commit();
	}

    public void afterThrowing(Exception ex) {
        System.out.println("Exception occured -> Rollback");
        tx.rollback();
    }

}
