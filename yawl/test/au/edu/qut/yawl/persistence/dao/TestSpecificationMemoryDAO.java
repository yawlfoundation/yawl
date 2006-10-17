package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class TestSpecificationMemoryDAO extends TestCase {

	YSpecification testSpec;
	
	protected void setUp() throws Exception {
		super.setUp();
		DAO fileDAO = DAOFactory.getDAO( PersistenceType.FILE );
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("TestCompletedMappings.xml", true);
		testSpec = (YSpecification) fileDAO.retrieve(YSpecification.class, f.getAbsolutePath());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private DAO getDAO() {
		return DAOFactory.getDAO( PersistenceType.MEMORY );
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() throws YPersistenceException {
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class, testSpec.getID());
		assertNotNull(spec);
		myDAO.delete(spec);
		spec = (YSpecification) myDAO.retrieve(YSpecification.class, testSpec.getID());
		assertNull(spec);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	public void testRetrieve() throws YPersistenceException {
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		String pk = testSpec.getID();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class, pk);	
		assertNotNull(spec);
	}
	
	public void testRetrieveByRestriction() throws YPersistenceException {
		DAO myDAO = getDAO();
		
		testSpec.setDocumentation( "asdf test 1234" );
		myDAO.save( testSpec );
		
		YSpecification spec2 = new YSpecification( "TEST_URI" );
		spec2.setDocumentation( "asdfblahasdf" );
		myDAO.save( spec2 );
		
		List specs = myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
				"documentation", Comparison.EQUAL, "asdf test 1234" ) );
		assertNotNull( specs );
		assertTrue( "" + specs.size(), specs.size() == 1 );
		
		myDAO.delete( spec2 );
		
		specs = myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
				"documentation", Comparison.NOT_EQUAL, "asdf test 1234" ) );
		assertNotNull( specs );
		assertTrue( "" + specs.size(), specs.size() == 0 );
		
		myDAO.save( spec2 );
		
		specs = myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
				"documentation", Comparison.LIKE, "asdf%1234" ) );
		assertNotNull( specs );
		assertTrue( "" + specs.size(), specs.size() == 1 );
		
		specs = myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
				"documentation", Comparison.LIKE, "asdf%" ) );
		assertNotNull( specs );
		assertTrue( "" + specs.size(), specs.size() == 2 );
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSave() throws YPersistenceException {
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		YSpecification spec2 = (YSpecification) myDAO.retrieve(YSpecification.class, testSpec.getID());
		assertNotNull(spec2);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() throws YPersistenceException {
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class, testSpec.getID());
		assertEquals(spec.getID(), testSpec.getID());
	}

}
