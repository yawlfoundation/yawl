/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class TestSpecificationHibernateDAO extends TestCase {

	YSpecification testSpec;
	
	
	protected void setUp() throws Exception {
		super.setUp();
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
	public void testDelete() throws YPersistenceException {
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		Object key = myDAO.getKey(testSpec);
		YSpecification spec2 = (YSpecification) myDAO.retrieve(YSpecification.class, key);
		assertNotNull(spec2);
		myDAO.delete(spec2);
		YSpecification spec3 = (YSpecification) myDAO.retrieve(YSpecification.class, key);
		assertNull("After deletion, should retrieve a null.",spec3);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	public void testRetrieve() throws YPersistenceException {
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class, myDAO.getKey(testSpec));	
		assertNotNull(spec);
		myDAO.delete(testSpec);
	}
	
	public void testRetrieveByRestriction() throws YPersistenceException {
		DAO myDAO = getDAO();
		
		// TODO we need to clean up possible failed previous runs before we run this test
		// once we ensure tests are rolled back after completion, this should be removed
		List specs = myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
				"documentation", Comparison.EQUAL, "asdf test 1234" ) );
		for( Object object : specs ) {
			myDAO.delete( object );
		}
		
		testSpec.setDocumentation( "asdf test 1234" );
		myDAO.save( testSpec );
		specs = myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
				"documentation", Comparison.EQUAL, "asdf test 1234" ) );
		assertNotNull( specs );
		assertTrue( "" + specs.size(), specs.size() == 1 );
		
		// TODO ensure that the database is rolled back for each test so that tests are completely
		// independent, then uncomment the following test
//		specs = myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
//				"documentation", Comparison.NOT_EQUAL, "asdf test 1234" ) );
//		assertNotNull( specs );
//		assertTrue( "" + specs.size(), specs.size() == 0 );
		
		// TODO since tests are not truly independent, this next part can possible fail
		// depending on the state of the database
		specs = myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
				"documentation", Comparison.LIKE, "asdf%1234" ) );
		assertNotNull( specs );
		assertTrue( "" + specs.size(), specs.size() == 1 );
		myDAO.delete( testSpec );
	}

	public void testRetrieveAndReload() {
		try {
			DAO myDAO = getDAO();
			myDAO.save(testSpec);
			YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class, myDAO.getKey(testSpec));	
			assertNotNull(spec);
						
			YNetRunner runner = new YNetRunner(spec.getRootNet(), null);
			myDAO.delete(testSpec);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception should not be thrown");
		}
	
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
	}

}
