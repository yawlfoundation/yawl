package com.nexusbpm.command;

import java.beans.PropertyChangeEvent;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;
import junit.framework.TestCase;

/**
 * 
 * @author Dean Mao
 * @created Aug 4, 2006
 */
public abstract class CommandTestCase extends TestCase implements DataProxyStateChangeListener {

	protected DataProxy<String> rootProxy;
	protected DataContext dataContext;
	
	@Override
	protected void setUp() throws Exception {
		DAO memdao = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY).getSpecificationModelDAO();
		dataContext = new DataContext(memdao);
		DatasourceRoot root = new DatasourceRoot("virtual://memory/");
		rootProxy = dataContext.createProxy( root, null );
		dataContext.attachProxy(rootProxy, root, null);
	}

	public void proxyAttached( DataProxy proxy, Object data, DataProxy parent ) {
	}

	public void proxyAttaching( DataProxy proxy, Object data, DataProxy parent ) {
	}

	public void proxyDetached( DataProxy proxy, Object data, DataProxy parent ) {
	}

	public void proxyDetaching( DataProxy proxy, Object data, DataProxy parent ) {
	}

	public void propertyChange( PropertyChangeEvent evt ) {
	}
}
