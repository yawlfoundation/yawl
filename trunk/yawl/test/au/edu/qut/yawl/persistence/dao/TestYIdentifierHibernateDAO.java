/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Set;

import org.hibernate.ObjectDeletedException;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

public class TestYIdentifierHibernateDAO extends AbstractHibernateDAOTestCase {
	
	
	protected void setUp() throws Exception {
		super.setUp();
		DAO hib = getDAO();
		DataContext context = new DataContext( hib );
		AbstractEngine.setDataContext(context);
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveTwo() {
	
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc");
			hibernateDAO.save(yid);

			YIdentifier yid2 = new YIdentifier("abc2");
			hibernateDAO.save(yid2);
			
			Object yid3 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
			assertNotNull(yid3);							
			hibernateDAO.delete(yid);
			hibernateDAO.delete(yid2);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() throws YPersistenceException {
		DAO hibernateDAO = getDAO();
		YIdentifier yid = new YIdentifier("abc_delete");
		hibernateDAO.save(yid);

		Object yid2 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
		assertNotNull(yid2);
		
		hibernateDAO.delete(yid);
		try {
			Object key = hibernateDAO.getKey(yid);
			hibernateDAO.retrieve(YIdentifier.class,key);
			fail( "retrieval should have failed for identifier with key " + key);
		}
		catch( YPersistenceException e ) {
			// proper exception is ObjectDeletedException
			if( ! ( e.getCause() instanceof ObjectDeletedException ) ) {
				throw new YPersistenceException( e );
			}
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
			YIdentifier yid = new YIdentifier("abc_retreive");
			hibernateDAO.save(yid);

			Object yid2 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
			assertNotNull(yid2);							
			hibernateDAO.delete(yid);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
		}
	}

	public void testSaveChildButNotParent() {
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc_ex");
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
			YIdentifier yid = new YIdentifier("abc_restore");		
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
			hibernateDAO.delete(yid);
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
			DAO hib = getDAO();
			DataContext context = new DataContext( hib );
			AbstractEngine.setDataContext(context);
			
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier("abc_ret");
			YIdentifier.saveIdentifier(yid,null,null);
			
//			YIdentifier yid2 = (YIdentifier) hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
			DataProxy proxy = context.getDataProxy( yid );
			Serializable key = context.getKeyFor( proxy );
			YIdentifier yid2 = (YIdentifier) context.retrieve( YIdentifier.class, key, null ).getData();
			assertNotNull(yid2);
			yid2.createChild();
			hibernateDAO.delete(yid);
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
