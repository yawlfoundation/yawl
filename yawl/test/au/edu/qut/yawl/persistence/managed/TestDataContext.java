package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.List;
import java.util.Set;

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

	public void testFileDataContext() {
		DAO memdao = DAOFactory.getDAOFactory(DAOFactory.Type.FILE).getSpecificationModelDAO();
		DataContext dc = new DataContext(memdao);
		DataProxy<YSpecification> dp;
		dp = dc.getDataProxy("./bin/xyz.xml", this); // gets a "virtual"
		DataProxy root = dp;
		Set l = dc.getChildren(root);
		System.out.println(l.size());
		for (DataProxy o: (Set<DataProxy>) l) {
			System.out.println(o.getData().toString() + ":" + o.getData().getClass().getName());
		}
	}
	
	
	public void testMemDataContext() {
		DAO memdao = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY).getSpecificationModelDAO();
		DataContext dc = new DataContext(memdao);
		DataProxy<YSpecification> dp;
		dp = dc.getDataProxy("/home/msandoz", this);
		DataProxy root = dp;
		
		getSpecProxy(dc, "/home/aTest", "aTest");
		getSpecProxy(dc, "/home/msandoz/aTest", "aTest");
		dp = getSpecProxy(dc, "/home/msandoz/templates/bTest", "bTest");
		YSpecification spec = dp.getData();
		DataProxy dp2 = dc.getDataProxy(spec, this);

		System.out.println(dc.getChildren(root).size());
		
		assertEquals(dp, dp2);
		Object o = dc.getKeyFor(dp);
		assertNotNull(o);
		assertEquals(o, spec.getID());
		lastEvent = null;
		try {
			dp.setAttribute("name", "aNewName");
		}
		catch(Exception e) {fail("should have been able to set attribute");}
		assertEquals(lastEvent.getSource(), spec);
		assertEquals(lastEvent.getPropertyName(), "name");
		assertEquals(lastEvent.getOldValue(), "bTest");
		assertEquals(lastEvent.getNewValue(), "aNewName");
		dc.put(dp);
		dc.remove(dp);
		try {
			dc.getKeyFor(dp2);
			fail("Should have thrown an exception");
		} catch (NullPointerException npe) {}
	}

	public DataProxy<YSpecification> getSpecProxy(DataContext dc, String uri, String name) {
		YSpecification spec = new YSpecification();
		spec.setName(name);
		spec.setID(uri);
		DataProxy<YSpecification> dp = dc.getDataProxy(spec, this);
		dc.put(dp);
		return dp;
	}
	
}
