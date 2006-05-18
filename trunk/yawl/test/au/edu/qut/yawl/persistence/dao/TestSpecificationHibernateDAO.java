/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.net.URI;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.DAOFactory.Type;
import junit.framework.TestCase;

public class TestSpecificationHibernateDAO extends TestCase {

	YSpecification testSpec;
	
	
	protected void setUp() throws Exception {
		super.setUp();
		DAOFactory fileFactory = DAOFactory.getDAOFactory(DAOFactory.Type.FILE);
		SpecificationDAO fileDAO = fileFactory.getSpecificationModelDAO();
		StringProducerXML spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("TestMakeRecordingsBigTest.xml", true);
		String uri = f.toURI().toString();
		testSpec = fileDAO.retrieve(uri);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() {
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.HIBERNATE);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		myDAO.save(testSpec);
		Object key = myDAO.getKey(testSpec);
		YSpecification spec2 = myDAO.retrieve(key);
		assertNotNull(spec2);
		myDAO.delete(spec2);
		YSpecification spec3 = myDAO.retrieve(key);
		assertNull("After deletion, should retrieve a null.",spec3);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	public void testRetrieve() {
		DAOFactory myFactory = DAOFactory.getDAOFactory(DAOFactory.Type.HIBERNATE);
		SpecificationDAO myDAO = myFactory.getSpecificationModelDAO();
		myDAO.save(testSpec);
		YSpecification spec = myDAO.retrieve(myDAO.getKey(testSpec));	
		assertNotNull(spec);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSave() {
		DAOFactory hibernateFactory = DAOFactory.getDAOFactory(DAOFactory.Type.HIBERNATE);
		SpecificationDAO hibernateDAO = hibernateFactory.getSpecificationModelDAO();
		hibernateDAO.save(testSpec);
		YSpecification spec2 = hibernateDAO.retrieve(testSpec.getDbID());
		assertNotNull(spec2);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
	}

}
