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
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class TestYNetRunnerHibernateDAO extends AbstractHibernateDAOTestCase {
	YSpecification testSpec;
	YSpecification testSpec_comp;
	
	protected void setUp() throws Exception {
		super.setUp();

		DAO hib = getDAO();
		AbstractEngine.setDao(hib);
		
		DAO fileDAO = DAOFactory.getDAO( PersistenceType.FILE );
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("TestMakeRecordingsBigTest.xml", true);		
		File f2 = spx.getTranslatedFile("YAWL_Specification1.xml", true);
		
        testSpec = (YSpecification) fileDAO.retrieve(YSpecification.class,f.getAbsolutePath());
        getDAO().save(testSpec);
        testSpec_comp = (YSpecification) fileDAO.retrieve(YSpecification.class,f2.getAbsolutePath());
        getDAO().save(testSpec_comp);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() throws YDataStateException, YSchemaBuildingException, YPersistenceException {
		DAO hibernateDAO = getDAO();
		YNetRunner runner = new YNetRunner(testSpec, null);
		hibernateDAO.save(runner);
		
		Object runner2 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
		assertNotNull(runner2);
		
		hibernateDAO.delete(runner);
		Object key = hibernateDAO.getKey(runner);
		Object o = hibernateDAO.retrieve(YNetRunner.class,key);
		assertNull("" + o, o);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	public void testRetrieveByRestriction() throws YDataStateException, YSchemaBuildingException, YPersistenceException {
		DAO hibernateDAO = getDAO();
		YNetRunner runner = new YNetRunner(testSpec, null);
		hibernateDAO.save(runner);

//		YNetRunner runner2 = new YNetRunner(testSpec.getRootNet(), null);
		//hibernateDAO.save(runner2);

		List runners = hibernateDAO.retrieveByRestriction( YNetRunner.class, new PropertyRestriction(
				"YNetID", Comparison.LIKE, "%TestMakeRecordingsBigTest.xml" ) );
	
		assertTrue(runners.size()==1);
		
		YNetRunner r = (YNetRunner) runners.get(0);
		hibernateDAO.delete(runner);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieve() throws YDataStateException, YSchemaBuildingException, YPersistenceException {
		DAO hibernateDAO = getDAO();
		YNetRunner runner = new YNetRunner(testSpec, null);
		hibernateDAO.save(runner);
		Object runner2 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
		assertNotNull(runner2);
		hibernateDAO.delete(runner);
	}
	
	public void testSaveAndRetrieve2Runners() throws YDataStateException, YSchemaBuildingException, YPersistenceException {
		DAO hibernateDAO = getDAO();
		YNetRunner runner = new YNetRunner(testSpec, null);
		hibernateDAO.save(runner);
		YNetRunner runner2 = new YNetRunner(testSpec, null);
		hibernateDAO.save(runner2);
		
		Object runner3 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
		assertNotNull(runner3);
		hibernateDAO.delete(runner);
		hibernateDAO.delete(runner2);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveComp() throws YPersistenceException, YDataStateException, YSchemaBuildingException {
		DAO hibernateDAO = getDAO();
		YNetRunner runner = new YNetRunner(testSpec_comp, null);
		hibernateDAO.save(runner);
		Object runner2 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
		assertNotNull(runner2);
	}
}
