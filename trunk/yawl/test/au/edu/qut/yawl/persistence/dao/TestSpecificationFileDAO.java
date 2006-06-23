package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.DAOFactory.Type;
import junit.framework.TestCase;

public class TestSpecificationFileDAO extends TestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() {
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.FILE);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = myDAO.retrieve(pk);
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
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.FILE);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = myDAO.retrieve(pk);	
		assertNotNull(spec);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSave() {
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.FILE);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = myDAO.retrieve(pk);
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
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.FILE);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = myDAO.retrieve(pk);	
		assertEquals(spec.getID(), myDAO.getKey(spec));
	}

}
