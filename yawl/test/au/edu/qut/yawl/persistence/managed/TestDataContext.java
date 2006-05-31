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
		assertNotNull(spec);
	}
	
}
