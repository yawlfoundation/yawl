package au.edu.qut.yawl.engine;

import junit.framework.TestCase;

import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.util.SpringTestConfiguration;
import au.edu.qut.yawl.util.SpringTestConfiguration.Configuration;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

public class TestEngineClearing extends TestCase {

	private DAO dao;
	
	protected void setUp() throws Exception {
		SpringTestConfiguration.setupTestConfiguration(Configuration.DEFAULT);
		dao = (DAO) ((DAOFactory)BootstrapConfiguration.getInstance().getApplicationContext().getBean("daoFactory")).getDAO( null );
		SpringTestConfiguration.startRealTransaction();
	}

	protected void tearDown() throws Exception {
		SpringTestConfiguration.commitTransaction();
	}

	public void testClearer() throws Exception{
		YEngine engine = EngineFactory.createYEngine();
//		List<YNetRunner> runners = dao.retrieveByRestriction(YNetRunner.class, new Unrestricted());
//		for (YNetRunner runner: runners) {
//			dao.delete(runner);
//		}
//		List<YIdentifier> identifiers;
//		do {
//		identifiers = dao.retrieveByRestriction(YIdentifier.class, new Unrestricted());
//		if (identifiers.size() > 0) dao.delete(identifiers.get(0));
//		} while (identifiers.size() > 0);
	}
	
}
