/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YPersistenceException;

public class TestYWorkItemHibernateDAO extends AbstractHibernateDAOTestCase {
	
	YSpecification testSpec;
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() throws YPersistenceException {
		DAO hibernateDAO = getDAO();
		YIdentifier yid = new YIdentifier("abc");
		hibernateDAO.save( yid );
//		YWorkItemID itemid = new YWorkItemID(yid,"test_task");
		YWorkItem item = new YWorkItem("testspec",yid,"test_task",true,false);
		
		hibernateDAO.save(item);
		
		YWorkItem item2 = (YWorkItem) hibernateDAO.retrieve(YWorkItem.class,hibernateDAO.getKey(item));
		assertNotNull(item2);
		
		hibernateDAO.delete(item);
		Object key = hibernateDAO.getKey(item);
		Object o = hibernateDAO.retrieve(YWorkItem.class,key);
		assertNull( "retrieval should have failed for work item with key " + key, o);
	}

//	/*
//	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
//	 */
//	public void testRetrieveByRestriction() {
//		try {
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveNoData() throws YPersistenceException {
		DAO hibernateDAO = getDAO();
		YIdentifier yid = new YIdentifier("abc");
//		YWorkItemID itemid = new YWorkItemID(yid,"test_task");			
		YWorkItem item = new YWorkItem("testspec",yid,"test_task",true,false);
		
		hibernateDAO.save(yid);
		hibernateDAO.save(item);
		
		YWorkItem item2 = (YWorkItem) hibernateDAO.retrieve(YWorkItem.class,hibernateDAO.getKey(item));
		assertNotNull(item2);
		
		/*
		 * Check for correctly restored values as well
		 * */
		assertTrue("dynamic creation error", item2.allowsDynamicCreation() == true );
		assertTrue("", item2.getCaseID().getId().equals("abc")==true );
		assertTrue("", item2.getCaseID().equals("abc")==true);
		assertTrue("", item2.getTaskID().equals("test_task")==true);
		assertTrue("", item2.getSpecificationID().equals("testspec")==true);

		hibernateDAO.delete(item2);
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveWithData() throws YPersistenceException, JDOMException, IOException {
		DAO hibernateDAO = getDAO();
		YIdentifier yid = new YIdentifier("abc");
//		YWorkItemID itemid = new YWorkItemID(yid,"test_task2");			
		YWorkItem item = new YWorkItem("testspec2",yid,"test_task2",true,false);
		
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
		hibernateDAO.save(yid);
		hibernateDAO.save(item);
		
		YWorkItem item2 = (YWorkItem) hibernateDAO.retrieve(YWorkItem.class,hibernateDAO.getKey(item));
		assertNotNull(item2);
		
		/*
		 * Check for correctly restored values as well
		 * */
		assertTrue("dynamic creation error", item2.allowsDynamicCreation() == true );
		assertTrue("case id error", item2.getCaseID().getId().equals("abc")==true );
		assertTrue("case id -identifier error", item2.getCaseID().equals("abc")==true);
		assertTrue("task id error", item2.getTaskID().equals("test_task2")==true);
		assertTrue("spec id error", item2.getSpecificationID().equals("testspec2")==true);
		assertTrue("Data error", item2.getDataString().replaceAll("[\\r\\n]","").replaceAll(" ","").equals(datastring));
		
		hibernateDAO.delete(item2);
	}
	
//	/*
//	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
//	 */
//	public void testGetKey() {
//	}
}
