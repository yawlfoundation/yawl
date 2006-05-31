package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import junit.framework.TestCase;

public class TestDataContext extends TestCase implements VetoableChangeListener{

	private PropertyChangeEvent lastEvent;
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		lastEvent = evt;
	}

	public void testDataContext() {
		DAO memdao = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY).getSpecificationModelDAO();
		DataContext dc = new DataContext(memdao);
		DataProxy dp = dc.newObject(YSpecification.class, this);
		assertNotNull(dp);
		YSpecification spec = (YSpecification) dp.getData();
		spec.setName("aTestName");
		assertNotNull(spec);
		DataProxy dp2 = dc.getDataProxy(spec);
		assertEquals(dp, dp2);
		Object o = dc.getKeyFor(dp);
		assertNotNull(o);
		assertEquals(o, spec.getName());
		lastEvent = null;
		try {
			dp.setAttribute("name", "aNewName");
		}
		catch(Exception e) {fail("should have been able to set attribute");}
		assertEquals(lastEvent.getSource(), spec);
		assertEquals(lastEvent.getPropertyName(), "name");
		assertEquals(lastEvent.getOldValue(), "aTestName");
		assertEquals(lastEvent.getNewValue(), "aNewName");
		dc.put(dp);
		dc.remove(dp);
		dc.remove(dp);

	}
	
}
