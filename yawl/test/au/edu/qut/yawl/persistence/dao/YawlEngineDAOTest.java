/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.util.List;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import junit.framework.TestCase;

public class YawlEngineDAOTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLifecycle() {
		YawlEngineDAO dao = new YawlEngineDAO();
		YSpecification spec;
		try {
			spec = (YSpecification) dao.retrieve(YSpecification.class, "MakeRecordings");
			assertNotNull(spec);
			spec.setDocumentation("no");
			dao.save(spec);
			spec = (YSpecification) dao.retrieve(YSpecification.class, "MakeRecordings");
			assertEquals(spec.getDocumentation(), "no");
			spec.setDocumentation("yes");
			dao.save(spec);
			spec = (YSpecification) dao.retrieve(YSpecification.class, "MakeRecordings");
			assertEquals(spec.getDocumentation(), "yes");
			List list = (List) dao.retrieveByRestriction(YSpecification.class, new Unrestricted());
			assertNotNull(list);
			assertTrue(list.size() > 0);
			System.out.println(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
