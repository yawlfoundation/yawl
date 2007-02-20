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

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;
import au.edu.qut.yawl.unmarshal.YMarshal;

public class TestSpecificationHibernateDAO extends AbstractHibernateDAOTestCase {
	YSpecification testSpec;
	
	
	protected void setUp() throws Exception {
		super.setUp();
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("TestMakeRecordingsBigTest.xml", true);

        testSpec = (YSpecification) YMarshal.unmarshalSpecifications(f.toURI().toString()).get(0);
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
		key = myDAO.getKey(spec2);
		Object o = myDAO.retrieve(YSpecification.class,key);
		assertNull( "retrieval should have failed for specification with key " + key, o);
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
		
		testSpec.setDocumentation( "asdf test 1234" );
		myDAO.save( testSpec );
		List specs = myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
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

	public void testRetrieveAndReload() throws YPersistenceException, YDataStateException, YSchemaBuildingException {
		DAO myDAO = getDAO();
		myDAO.save(testSpec);
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class, myDAO.getKey(testSpec));	
		assertNotNull(spec);
					
		YNetRunner runner = new YNetRunner(spec, null);
		myDAO.delete(testSpec);
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
	}

}
