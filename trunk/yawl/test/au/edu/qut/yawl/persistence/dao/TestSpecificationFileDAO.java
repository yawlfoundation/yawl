package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class TestSpecificationFileDAO extends TestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private DAO getDAO() {
		return DAOFactory.getDAO( PersistenceType.FILE );
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() {
		DAO myDAO = getDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class,pk);
		spec.setID(new File("DUMMY.XML").toURI().toASCIIString());
		myDAO.save(spec);
		try {
			assertTrue("file was not found at:" + spec.getID(), new File(new URI(spec.getID())).exists());
			myDAO.delete(spec);
			assertFalse(new File(new URI(spec.getID())).exists());
		} catch (URISyntaxException e) {
			fail("couldnt handle built uri from " + spec.getID());
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	public void testRetrieve() {
		DAO myDAO = getDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class,pk);	
		assertNotNull(spec);
	}
	
	public void testRetrieveByRestriction() {
		DAO myDAO = getDAO();
		try {
			myDAO.retrieveByRestriction( YSpecification.class, new Unrestricted() );
			fail( "An exception should have been thrown.");
		}
		catch( UnsupportedOperationException e ) {
			// proper exception was thrown
		}
		try {
			myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
					"ID", Comparison.EQUAL, "TestCompletedMappings.xml" ) );
			fail( "An exception should have been thrown.");
		}
		catch( UnsupportedOperationException e ) {
			// proper exception was thrown
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSave() {
		DAO myDAO = getDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class,pk);
		spec.setID(new File("DUMMY.XML").toURI().toASCIIString());
		myDAO.save(spec);
		try {
			assertTrue(new File(new URI(spec.getID())).exists());
		} catch (URISyntaxException e) {
			fail("couldnt save due to uri problems");
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
		DAO myDAO = getDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class,pk);	
		assertEquals(spec.getID(), myDAO.getKey(spec));
	}

}
