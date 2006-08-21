package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.io.StringReader;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;
import junit.framework.TestCase;
import au.edu.qut.yawl.persistence.StringProducerXML;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItemID;

public class TestYWorkItemHibernateDAO extends TestCase {
	
	YSpecification testSpec;
	
	protected void setUp() throws Exception {
		super.setUp();

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
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc");
			YWorkItemID itemid = new YWorkItemID(yid,"test_task");			
			YWorkItem item = new YWorkItem("testspec",itemid,true,false);
			
			hibernateDAO.save(item);
			
			YWorkItem item2 = (YWorkItem) hibernateDAO.retrieve(YWorkItem.class,hibernateDAO.getKey(item));
			assertNotNull(item2);
			
			hibernateDAO.delete(item);
			YWorkItem item3 = (YWorkItem) hibernateDAO.retrieve(YWorkItem.class,hibernateDAO.getKey(item));
			assertNull(item3);
			
						
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown here");
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	
	public void testRetrieveByRestriction() {
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveNoData() {
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc");
			YWorkItemID itemid = new YWorkItemID(yid,"test_task");			
			YWorkItem item = new YWorkItem("testspec",itemid,true,false);
			
			hibernateDAO.save(item);
			
			YWorkItem item2 = (YWorkItem) hibernateDAO.retrieve(YWorkItem.class,hibernateDAO.getKey(item));
			assertNotNull(item2);
			
			/*
			 * Check for correctly restored values as well
			 * */
			assertTrue("", item2.allowsDynamicCreation() == true );
			assertTrue("", item2.getCaseID().getId().equals("abc")==true );
			assertTrue("", item2.getWorkItemID().getCaseID().equals("abc")==true);
			assertTrue("", item2.getWorkItemID().getTaskID().equals("test_task")==true);
			assertTrue("", item2.getSpecificationID().equals("testspec")==true);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown here");
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveWithData() {
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc2");
			YWorkItemID itemid = new YWorkItemID(yid,"test_task2");			
			YWorkItem item = new YWorkItem("testspec2",itemid,true,false);
			
			String datastring = "<data><somedata1>XYZ</somedata1>" +
			"<somedata2>XYZ</somedata2>" +
			"<somedata3>XYZ</somedata3>" +
			"<somedata4>XYZ</somedata4>" +
			"<somedata5>XYZ</somedata5>" +
			"<somedata6>XYZ</somedata6>" +
			"<somedata7>XYZ</somedata7>" +
			"<somedata8>XYZ</somedata8>" +
			"<somedata9>XYZ</somedata9>" +
			"<somedata10>XYZ</somedata10>" +
			"<somedata11>XYZ</somedata11>" +
			"<somedata12>XYZ</somedata12>" +
			"<somedata13>XYZ</somedata13>" +
			"</data>";
	
			
			SAXBuilder builder = new SAXBuilder();
			Document d = builder.build( new StringReader(datastring));
					
			Element e = (Element) d.getRootElement().clone();
			item.setInitData(e);
			hibernateDAO.save(item);
			
			YWorkItem item2 = (YWorkItem) hibernateDAO.retrieve(YWorkItem.class,hibernateDAO.getKey(item));
			assertNotNull(item2);
			
			/*
			 * Check for correctly restored values as well
			 * */
			assertTrue("dynamic creation error", item2.allowsDynamicCreation() == true );
			assertTrue("case id error", item2.getCaseID().getId().equals("abc2")==true );
			assertTrue("case id -identifier error", item2.getWorkItemID().getCaseID().equals("abc2")==true);
			assertTrue("task id error", item2.getWorkItemID().getTaskID().equals("test_task2")==true);
			assertTrue("spec id error", item2.getSpecificationID().equals("testspec2")==true);
			assertTrue("Data error", item2.getDataString().replaceAll("[\\r\\n]","").replaceAll(" ","").equals(datastring));
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown here");
		}
	}

	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
	}


}
