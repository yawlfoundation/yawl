/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.StringProducer;
import au.edu.qut.yawl.persistence.StringProducerYAWL;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class TestSpecificationFileDAO extends TestCase {
	
	private DAO dao;
	
	protected void setUp() throws Exception {
		super.setUp();
		dao = new DelegatedFileDAO();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private DAO getDAO() {
		return dao;
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.delete(YSpecification)'
	 */
	public void testDelete() throws YPersistenceException, URISyntaxException {
		DAO myDAO = getDAO();
		StringProducer spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class,pk);
		spec.setID(new File("DUMMY.XML").toURI().toASCIIString());
		myDAO.save(spec);
		assertTrue("file was not found at:" + spec.getID(), new File(new URI(spec.getID())).exists());
		myDAO.delete(spec);
		assertFalse(new File(new URI(spec.getID())).exists());
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.retrieve(Object)'
	 */
	public void testRetrieve() throws YPersistenceException {
		DAO myDAO = getDAO();
		StringProducer spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class,pk);	
		assertNotNull(spec);
	}
	
	public void testRetrieveByRestriction() throws YPersistenceException {
		DAO myDAO = getDAO();
		try {
			myDAO.retrieveByRestriction( YSpecification.class, new Unrestricted() );
			fail( "An exception should have been thrown.");
		}
		catch( UnsupportedOperationException e ) {
			// proper exception was thrown
		}
		try {
			myDAO.retrieveByRestriction( YSpecification.class, new PropertyRestriction(
					"ID", Comparison.EQUAL, "TestCompletedMappings.xml" ) );
			fail( "An exception should have been thrown.");
		}
		catch( UnsupportedOperationException e ) {
			// proper exception was thrown
		}
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.save(YSpecification)'
	 */
	public void testSave() throws YPersistenceException, URISyntaxException {
		DAO myDAO = getDAO();
		StringProducer spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class,pk);
		spec.setID(new File("DUMMY.XML").toURI().toASCIIString());
		myDAO.save(spec);
		
		assertTrue(new File(new URI(spec.getID())).exists());
	}

	/*
	 * Test method for 'au.edu.qut.yawl.persistence.dao.SpecificationFileDAO.getKey(YSpecification)'
	 */
	public void testGetKey() throws YPersistenceException {
		DAO myDAO = getDAO();
		StringProducer spx = StringProducerYAWL.getInstance();
		String pk = spx.getTranslatedFile("TestCompletedMappings.xml", true).getAbsoluteFile().getAbsolutePath();
		YSpecification spec = (YSpecification) myDAO.retrieve(YSpecification.class,pk);	
		assertEquals(spec.getID(), myDAO.getKey(spec));
	}

}
