/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.util.List;
import java.util.Set;

import org.springframework.dao.InvalidDataAccessApiUsageException;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;

public class TestYIdentifierHibernateDAO extends AbstractHibernateDAOTestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
//		EngineFactory.createYEngine();
//		List<YIdentifier> identifiers = null;
//		try {
//			identifiers = getDAO().retrieveByRestriction(YIdentifier.class, new Unrestricted());
//		}
//		catch( Exception e ) {
//			e.printStackTrace();
//		}
//		if( identifiers != null ) {
//			List<YNetRunner> runners = getDAO().retrieveByRestriction(YNetRunner.class, new Unrestricted());
//			for (YNetRunner runner: runners) {
//				getDAO().delete(runner);
//			}
//			do {
//				identifiers = getDAO().retrieveByRestriction(YIdentifier.class, new Unrestricted());
//				if (identifiers.size() > 0) getDAO().delete(identifiers.get(0));
//				} while (identifiers.size() > 0);
//		}
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveTwo() throws YPersistenceException {
		DAO hibernateDAO = getDAO();
		YIdentifier yid = new YIdentifier();
		hibernateDAO.save(yid);

		YIdentifier yid2 = new YIdentifier();
		hibernateDAO.save(yid2);
		
//		Object yid3 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
//		assertNotNull(yid3);							
		hibernateDAO.delete(yid);
		hibernateDAO.delete(yid2);
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() throws YPersistenceException {
		DAO hibernateDAO = getDAO();
		YIdentifier yid = new YIdentifier();
		hibernateDAO.save(yid);

		Object yid2 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
		assertNotNull(yid2);
		
		hibernateDAO.delete(yid);
		Object key = hibernateDAO.getKey(yid);
		Object o = hibernateDAO.retrieve(YIdentifier.class,key);
		assertNull( "retrieval should have failed for identifier with key " + key, o);
	}

//	/*
//	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
//	 */
//	
//	public void testRetrieveByRestriction() {
//	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieve() throws YPersistenceException {
		DAO hibernateDAO = getDAO();
		YIdentifier yid = new YIdentifier();
		hibernateDAO.save(yid);

		Object yid2 = hibernateDAO.retrieve(YIdentifier.class,hibernateDAO.getKey(yid));
		assertNotNull(yid2);							
		hibernateDAO.delete(yid);
	}

	public void testSaveChildButNotParent() throws YPersistenceException {
		try {
			DAO hibernateDAO = getDAO();
			YIdentifier yid = new YIdentifier();
			YIdentifier child = yid.createChild();
			hibernateDAO.save(child);
            // we have to retrieve because the DAO may not flush after saving until you retrieve
            hibernateDAO.retrieve(YIdentifier.class, child.getId());
			fail(InvalidDataAccessApiUsageException.class.toString() + " should be thrown");
		} catch (InvalidDataAccessApiUsageException e) {
			//success
		}
	}
	
	public void testSaveAndRestoreHierarchy() throws YPersistenceException {
		YIdentifier yid = new YIdentifier();
		getDAO().save(yid);
//		YIdentifier.saveIdentifier(yid, null, null);
					
		YIdentifier yid1 = yid.createChild();
		YIdentifier yid2 = yid.createChild();
		yid2.createChild();

		Set s_old = yid.getDescendants();
		assertTrue("Wrong number of decendants1",s_old.size()==4);
		
		YIdentifier yidparent = (YIdentifier) getDAO().retrieve(YIdentifier.class,getDAO().getKey(yid));
		assertNotNull(yidparent);							
		
		Set s = yidparent.getDescendants();
		assertTrue("Wrong number of decendants",s.size()==4);
		getDAO().delete(yid);
	}
	
	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSaveAndRetrieveThenExecute() throws YPersistenceException {
		YIdentifier yid = new YIdentifier();
		getDAO().save(yid);
//		YIdentifier.saveIdentifier(yid,null,null);
		
		YIdentifier yid2 = (YIdentifier) getDAO().retrieve(YIdentifier.class,getDAO().getKey(yid));
//		DataProxy proxy = context.getDataProxy( yid );
//		Serializable key = context.getKeyFor( proxy );
//		YIdentifier yid2 = (YIdentifier) context.retrieve( YIdentifier.class, key, null ).getData();
		assertNotNull(yid2);
		yid2.createChild();
		getDAO().delete(yid);
	}
	
//	/*
//	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
//	 */
//	public void testGetKey() {
//	}
}
