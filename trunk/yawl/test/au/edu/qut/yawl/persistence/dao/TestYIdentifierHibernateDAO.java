package au.edu.qut.yawl.persistence.dao;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Set;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

public class TestYIdentifierHibernateDAO extends TestCase {
	
	
	protected void setUp() throws Exception {
		super.setUp();
		DAO hib = DAOFactory.getDAO( PersistenceType.HIBERNATE );
		DataContext context = new DataContext( hib );
		AbstractEngine.setDataContext(context);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private DAO getDAO() {
		return DAOFactory.getDAO( PersistenceType.HIBERNATE );
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveTwo() {
	
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc");
			hibernateDAO.save(yid);

			YIdentifier yid2 = new YIdentifier("abc");
			hibernateDAO.save(yid2);
			
			Object yid3 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
			assertNotNull(yid3);							
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() {
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc");
			hibernateDAO.save(yid);

			Object yid2 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
			assertNotNull(yid2);		
			
			hibernateDAO.delete(yid);
			Object yid3 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
			assertNull(yid3);		
			
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
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieve() {
	
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc");
			hibernateDAO.save(yid);

			Object yid2 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
			assertNotNull(yid2);							
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}

	public void testSaveChildButNotParent() {
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc");
			YIdentifier yid1 = yid.createChild();
			
			fail("Exception should be thrown"); //but which exception
		} catch (YPersistenceException e) {
			//success
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}
	
	public void testSaveAndRestoreHierarchy() {
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc2");		
			YIdentifier.saveIdentifier(yid, null, null);
			
			DataContext context = AbstractEngine.getDataContext();
			DataProxy parentProxy = context.getDataProxy( yid );
						
			YIdentifier yid1 = yid.createChild();
			YIdentifier yid2 = yid.createChild();
			yid2.createChild();

			Set s_old = yid.getDescendants();
			assertTrue("Wrong number of decendants1",s_old.size()==4);
			
			YIdentifier yidparent = (YIdentifier) hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
			assertNotNull(yidparent);							
			
			Set s = yidparent.getDescendants();
			assertTrue("Wrong number of decendants",s.size()==4);
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
		
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveThenExecute() {
	
		try {
			DAO hib = DAOFactory.getDAO( PersistenceType.HIBERNATE );
			DataContext context = new DataContext( hib );
			AbstractEngine.setDataContext(context);
			
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc");
			YIdentifier.saveIdentifier(yid,null,null);
			
//			YIdentifier yid2 = (YIdentifier) hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
			DataProxy proxy = context.getDataProxy( yid );
			Serializable key = context.getKeyFor( proxy );
			YIdentifier yid2 = (YIdentifier) context.retrieve( YIdentifier.class, key, null ).getData();
			assertNotNull(yid2);
			yid2.createChild();
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() {
	}


}
