/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

public class AbstractHibernateDAOTestCase extends AbstractTransactionalTestCase {
	protected final DAO getDAO() {
		// TODO fix when we fix the factory
		return (DAO) ((DAOFactory)BootstrapConfiguration.getInstance().getApplicationContext().getBean("daoFactory")).getDAO( null );
	}
}