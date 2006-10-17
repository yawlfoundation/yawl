package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;
import junit.framework.TestCase;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;

public class TestYNetRunnerHibernateDAO extends TestCase {
	
	YSpecification testSpec;
	YSpecification testSpec_comp;
	
	protected void setUp() throws Exception {
		super.setUp();

		DAO hib = DAOFactory.getDAO( PersistenceType.HIBERNATE );
		DataContext context = new DataContext( hib );
		AbstractEngine.setDataContext(context);
		
		DAO fileDAO = DAOFactory.getDAO( PersistenceType.FILE );
		StringProducer spx = StringProducerYAWL.getInstance();
		File f = spx.getTranslatedFile("TestMakeRecordingsBigTest.xml", true);		
		File f2 = spx.getTranslatedFile("YAWL_Specification1.xml", true);
		
        testSpec = (YSpecification) fileDAO.retrieve(YSpecification.class,f.getAbsolutePath());
        getDAO().save(testSpec);
        testSpec_comp = (YSpecification) fileDAO.retrieve(YSpecification.class,f2.getAbsolutePath());
        getDAO().save(testSpec_comp);
        
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		getDAO().delete(testSpec);
		getDAO().delete(testSpec_comp);
	}

	private DAO getDAO() {
		return DAOFactory.getDAO( PersistenceType.HIBERNATE );
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() {
		try {
			DAO hibernateDAO = getDAO();
			YNetRunner runner = new YNetRunner(testSpec.getRootNet(), null);
			hibernateDAO.save(runner);
			
			Object runner2 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
			assertNotNull(runner2);
			
			hibernateDAO.delete(runner);
			Object runner3 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
			assertNull(runner3);
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	public void testRetrieveByRestriction() {
		try {
			DAO hibernateDAO = getDAO();
			YNetRunner runner = new YNetRunner(testSpec.getRootNet(), null);
			hibernateDAO.save(runner);

//			YNetRunner runner2 = new YNetRunner(testSpec.getRootNet(), null);
			//hibernateDAO.save(runner2);

			List runners = hibernateDAO.retrieveByRestriction( YNetRunner.class, new PropertyRestriction(
					"YNetID", Comparison.LIKE, "%TestMakeRecordingsBigTest.xml" ) );
		
			assertTrue(runners.size()==1);
			
			YNetRunner r = (YNetRunner) runners.get(0);
			hibernateDAO.delete(runner);
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		System.err.println( sw.toString() );
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieve() {
		try {
			DAO hibernateDAO = getDAO();
			YNetRunner runner = new YNetRunner(testSpec.getRootNet(), null);
			hibernateDAO.save(runner);
			Object runner2 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
			assertNotNull(runner2);
			hibernateDAO.delete(runner);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}
	
	public void testSaveAndRetrieve2Runners() {
		try {
			DAO hibernateDAO = getDAO();
			YNetRunner runner = new YNetRunner(testSpec.getRootNet(), null);
			hibernateDAO.save(runner);
			YNetRunner runner2 = new YNetRunner(testSpec.getRootNet(), null);
			hibernateDAO.save(runner2);
			
			Object runner3 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
			assertNotNull(runner3);
			hibernateDAO.delete(runner);
			hibernateDAO.delete(runner2);

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveComp() {
		try {
			
			StringProducer spx = StringProducerYAWL.getInstance();
			File f = spx.getTranslatedFile("YAWL_Specification1.xml", true);
			AbstractEngine engine = EngineFactory.createEngine(true);
			LinkedList errors = new LinkedList();
			engine.addSpecifications(f, false, errors);		

			DAO hibernateDAO = getDAO();
			YNetRunner runner = new YNetRunner(engine.getSpecification("YAWL_Specification1.xml").getRootNet(), null);
			YNetRunner.saveNetRunner(runner, null);
			Object runner2 = hibernateDAO.retrieve(YNetRunner.class,hibernateDAO.getKey(runner));
			assertNotNull(runner2);
			hibernateDAO.delete(runner);
			engine.unloadSpecification("YAWL_Specification1.xml");

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}
}
