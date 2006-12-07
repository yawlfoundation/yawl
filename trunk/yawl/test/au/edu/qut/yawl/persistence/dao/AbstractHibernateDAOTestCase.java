/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import org.hibernate.Transaction;

import au.edu.qut.yawl.persistence.YAWLTransactionAdvice;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;

public class AbstractHibernateDAOTestCase extends AbstractDAOTestCase {
	Transaction tx;
	
	protected void setUp() throws Exception {
		super.setUp();
        
        closeSession();
        YAWLTransactionAdvice.openSession();
        tx = YAWLTransactionAdvice.getSession().getTransaction();
        tx.begin();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		tx.rollback();
		
		closeSession();
	}
	
	private void closeSession() {
		if( YAWLTransactionAdvice.getSession() != null && YAWLTransactionAdvice.getSession().isOpen() ) {
        	YAWLTransactionAdvice.getSession().close();
        }
	}

	protected final DAO getDAO() {
		return DAOFactory.getDAO( PersistenceType.SPRING );
	}
}
