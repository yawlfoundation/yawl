/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.io.File;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;

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
	public void testDelete() {
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
	public void testRetrieve() {
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class, myDAO.getKey(testSpec));	
		assertNotNull(spec);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSave() {
		DAO hibernateDAO = getDAO();
		hibernateDAO.save(testSpec);
		YSpecification spec2 = (YSpecification) hibernateDAO.retrieve(YSpecification.class,testSpec.getDbID());
		assertNotNull(spec2);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
	}

}
