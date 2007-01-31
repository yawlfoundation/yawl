/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.util.SpringTestConfiguration;
import au.edu.qut.yawl.util.SpringTestConfiguration.Configuration;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

public class AbstractTransactionalTestCase extends TestCase {
	public AbstractTransactionalTestCase() {
		super();
	}

	public AbstractTransactionalTestCase(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		SpringTestConfiguration.setupTestConfiguration(Configuration.DEFAULT);
		SpringTestConfiguration.startNewTestTransaction();
		DAO dao = (DAO) ((DAOFactory)BootstrapConfiguration.getInstance().getApplicationContext().getBean("daoFactory")).getDAO( null );
		saveServiceReference( dao, "http://localhost:8080/yawlSMSInvoker/ib" );
		saveServiceReference( dao, "http://localhost:8080/timeService/ib" );
		saveServiceReference( dao, "http://localhost:8080/yawlWSInvoker/" );
		saveServiceReference( dao, "http://localhost:8080/workletService/ib" );
		saveServiceReference( dao, "http://localhost:8080/NexusServiceInvoker/" );
	}
	
	private void saveServiceReference( DAO dao, String refStr ) throws Exception {
		if( dao.retrieve( YAWLServiceReference.class, refStr ) == null ) {
			YAWLServiceReference ref = new YAWLServiceReference( refStr );
			dao.save(ref);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		SpringTestConfiguration.rollbackTestTransaction();
	}
}
