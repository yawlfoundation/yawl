/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.command;

import java.beans.PropertyChangeEvent;

import junit.framework.TestCase;
import au.edu.qut.yawl.persistence.dao.AbstractHibernateDAOTestCase;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;
import au.edu.qut.yawl.util.SpringTestConfiguration;
import au.edu.qut.yawl.util.SpringTestConfiguration.Configuration;

/**
 * 
 * @author Dean Mao
 * @created Aug 4, 2006
 */
public abstract class CommandTestCase extends AbstractHibernateDAOTestCase implements DataProxyStateChangeListener {

	protected DataProxy<DatasourceFolder> rootProxy;
	protected DataContext dataContext;
	
	public CommandTestCase() {
		super();
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
//		DAO memdao = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY).getSpecificationModelDAO();
//		SpringTestConfiguration.setupTestConfiguration(Configuration.DEFAULT);
//		DAO memdao = SpringTestConfiguration.getDAOFactory().getDAO( PersistenceType.MEMORY );
		dataContext = new DataContext(getDAO());
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
