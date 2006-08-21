package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.util.List;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;
import au.edu.qut.yawl.persistence.managed.DataContext;
import junit.framework.TestCase;
import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;

public class TestYNetRunnerHibernateDAO extends TestCase {
	
	YSpecification testSpec;
	
	protected void setUp() throws Exception {
		super.setUp();

		DAO hib = DAOFactory.getDAO( PersistenceType.HIBERNATE );
		DataContext context = new DataContext( hib );
		AbstractEngine.setDataContext(context);
		
		DAO fileDAO = DAOFactory.getDAO( PersistenceType.FILE );
		StringProducerXML spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("TestMakeRecordingsBigTest.xml", true);

        testSpec = (YSpecification) fileDAO.retrieve(YSpecification.class,f.getAbsolutePath());

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private DAO getDAO() {
		return DAOFactory.getDAO( PersistenceType.HIBERNATE );
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() {
		try {
			DAO hibernateDAO = getDAO();
			YNetRunner runner = new YNetRunner(testSpec.getRootNet(), null);
			hibernateDAO.save(runner);
			
			Object runner2 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
			assertNotNull(runner2);
			
			hibernateDAO.delete(runner);
			Object runner3 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
			assertNull(runner3);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown here");
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	
	public void testRetrieveByRestriction() {
		try {
			DAO hibernateDAO = getDAO();
			YNetRunner runner = new YNetRunner(testSpec.getRootNet(), null);
			hibernateDAO.save(runner);

//			YNetRunner runner2 = new YNetRunner(testSpec.getRootNet(), null);
			//hibernateDAO.save(runner2);

			List runners = hibernateDAO.retrieveByRestriction( YNetRunner.class, new PropertyRestriction(
					"YNetID", Comparison.EQUAL, "file:/D:/Yawlstuff/JavaForgeBuild/trunk/yawl/classes/au/edu/qut/yawl/persistence/TestMakeRecordingsBigTest.xml" ) );
		
			assertTrue(runners.size()==1);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieve() {
		try {
			DAO hibernateDAO = getDAO();
			YNetRunner runner = new YNetRunner(testSpec.getRootNet(), null);
			hibernateDAO.save(runner);
			Object runner2 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
			assertNotNull(runner2);
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown here");
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
	}


}
