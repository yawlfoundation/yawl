package au.edu.qut.yawl.persistence.dao;

import java.io.File;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;

public class TestSpecificationMemoryDAO extends TestCase {

	YSpecification testSpec;
	
	protected void setUp() throws Exception {
		super.setUp();
//		DAOFactory fileFactory = DAOFactory.getDAOFactory(DAOFactory.Type.FILE);
//		SpecificationDAO fileDAO = fileFactory.getSpecificationModelDAO();
		DAO fileDAO = DAOFactory.getDAO( PersistenceType.FILE );
		StringProducerXML spx = StringProducerYAWL.getInstance();
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
	public void testDelete() {
//		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY);
//		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
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
	public void testRetrieve() {
//		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY);
//		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		String pk = testSpec.getID();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class, pk);	
		assertNotNull(spec);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSave() {
//		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY);
//		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		YSpecification spec2 = (YSpecification) myDAO.retrieve(YSpecification.class, testSpec.getID());
		assertNotNull(spec2);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
//		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY);
//		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class, testSpec.getID());
		assertEquals(spec.getID(), testSpec.getID());
	}

}
