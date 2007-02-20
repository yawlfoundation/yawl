/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
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

public class TestYNetRunnerHibernateDAO extends AbstractHibernateDAOTestCase {
	YSpecification testSpec;
	YSpecification testSpec_comp;
	
	protected void setUp() throws Exception {
		super.setUp();

		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("TestMakeRecordingsBigTest.xml", true);
		File f2 = spx.getTranslatedFile("YAWL_Specification1.xml", true);
		
        testSpec = (YSpecification) YMarshal.unmarshalSpecifications(f.toURI().toString()).get(0);
        testSpec.setID("TestMakeRecordingsBigTest.xml");
        getDAO().save(testSpec);
        testSpec_comp = (YSpecification) YMarshal.unmarshalSpecifications(f2.toURI().toString()).get(0);
        testSpec_comp.setID("YAWL_Specification1.xml");
        getDAO().save(testSpec_comp);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() throws YDataStateException, YSchemaBuildingException, YPersistenceException {
		YNetRunner runner = new YNetRunner(testSpec, null);
		getDAO().save(runner);
		
		Object runner2 = getDAO().retrieve(YNetRunner.class,getDAO().getKey(runner));
		assertNotNull(runner2);
		
		getDAO().delete(runner);
		Object key = getDAO().getKey(runner);
		Object o = getDAO().retrieve(YNetRunner.class,key);
		assertNull( "retrieval should have failed for net runner with key " + key, o);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	public void testRetrieveByRestriction() throws YDataStateException, YSchemaBuildingException, YPersistenceException {
		YNetRunner runner = new YNetRunner(testSpec, null);
		getDAO().save(runner);

//		YNetRunner runner2 = new YNetRunner(testSpec.getRootNet(), null);
		//hibernateDAO.save(runner2);

		List runners = getDAO().retrieveByRestriction( YNetRunner.class, new PropertyRestriction(
				"YNetID", Comparison.LIKE, "%TestMakeRecordingsBigTest.xml" ) );
	
		assertEquals(runners.size(),1);
		
		YNetRunner r = (YNetRunner) runners.get(0);
		getDAO().delete(runner);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieve() throws YDataStateException, YSchemaBuildingException, YPersistenceException {
		YNetRunner runner = new YNetRunner(testSpec, null);
		getDAO().save(runner);
		Object runner2 = getDAO().retrieve(YNetRunner.class,getDAO().getKey(runner));
		assertNotNull(runner2);
		getDAO().delete(runner);
	}
	
	public void testSaveAndRetrieve2Runners() throws YDataStateException, YSchemaBuildingException, YPersistenceException {
		YNetRunner runner = new YNetRunner(testSpec, null);
		getDAO().save(runner);
		YNetRunner runner2 = new YNetRunner(testSpec, null);
		getDAO().save(runner2);
		
		Object runner3 = getDAO().retrieve(YNetRunner.class,getDAO().getKey(runner));
		assertNotNull(runner3);
		getDAO().delete(runner);
		getDAO().delete(runner2);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveComp() throws YPersistenceException, YDataStateException, YSchemaBuildingException {
		YNetRunner runner = new YNetRunner(testSpec_comp, null);
		getDAO().save(runner);
		Object runner2 = getDAO().retrieve(YNetRunner.class,getDAO().getKey(runner));
		assertNotNull(runner2);
	}
}
