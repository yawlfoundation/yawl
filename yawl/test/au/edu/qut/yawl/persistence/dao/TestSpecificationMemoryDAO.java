package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.net.URI;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.DAOFactory.Type;
import junit.framework.TestCase;

public class TestSpecificationMemoryDAO extends TestCase {

	YSpecification testSpec;
	
	protected void setUp() throws Exception {
		super.setUp();
		DAOFactory fileFactory = DAOFactory.getDAOFactory(DAOFactory.Type.FILE);
		SpecificationDAO fileDAO = fileFactory.getSpecificationModelDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("TestCompletedMappings.xml", true);
		testSpec = fileDAO.retrieve(f.getAbsolutePath());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() {
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		myDAO.save(testSpec);
		YSpecification spec = myDAO.retrieve(testSpec.getDbID());
		assertNotNull(spec);
		myDAO.delete(spec);
		spec = myDAO.retrieve(testSpec.getDbID());
		assertNull(spec);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	public void testRetrieve() {
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		myDAO.save(testSpec);
		Long pk = testSpec.getDbID();
		YSpecification spec = myDAO.retrieve(pk);	
		assertNotNull(spec);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSave() {
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		myDAO.save(testSpec);
		YSpecification spec2 = myDAO.retrieve(testSpec.getDbID());
		assertNotNull(spec2);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		myDAO.save(testSpec);
		YSpecification spec = myDAO.retrieve(testSpec.getDbID());
		assertEquals(spec.getID(), testSpec.getID());
	}

}
