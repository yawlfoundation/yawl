/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceB_EnvironmentBasedClient;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.XmlUtilities;

public class YawlEngineDAO implements DAO {
	private InterfaceA_EnvironmentBasedClient iaClient;
	private InterfaceB_EnvironmentBasedClient ibClient;
	private String sessionHandle;
	private String engineUri;
	private String userName;
	private String password;
	private boolean configurationDirty = true;
	
	protected synchronized void resetConnection() throws IOException {
		iaClient = new InterfaceA_EnvironmentBasedClient(
				engineUri + "/ia");
		ibClient = new InterfaceB_EnvironmentBasedClient(
				engineUri + "/ib");
		sessionHandle = iaClient.connect(userName, password);
		configurationDirty = false;
	}
	
	public boolean delete(Object object) {
		try {
			execute(new DeleteCommand(object.toString()));
			return true;
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	public List getChildren(Object parent) {
		/*
		 * needs to see if this is a folder; if so, look for matching specs.
		 */
		List retval = new ArrayList();
		String filter = "";

		if (parent instanceof DatasourceFolder) {
			DatasourceFolder folder = (DatasourceFolder) parent;
			filter = folder.getPath();
			if (!filter.endsWith("/")) {
				filter = filter + "/";
			}
			// this method is stolen from hibernatedao but for the next line...
			// which needs to be changed to add a restriction regarding uri match
			List tmp = retrieveByRestriction(YSpecification.class,
					new PropertyRestriction( "ID", Comparison.LIKE, filter + "%" ) );
//			List tmp = retrieveByRestriction(YSpecification.class,
//					new Unrestricted());

			Set traversal = new HashSet(tmp);

			for (Object o : traversal) {
				String id = getID(o);
				if (id != null && id.startsWith(filter)) {
					if (PersistenceUtilities.contains(id, filter) != null) {
						retval.add(new DatasourceFolder(PersistenceUtilities
								.contains(id, filter), folder));
					} else {
						assert o instanceof YSpecification : "object not a specification";
						retval.add(o);
					}
				}
			}
		}
		return retval;
	}

	private String getID(Object object) {
		if (object instanceof YSpecification) {
			return ((YSpecification) object).getID();
		} else {
			return object.toString();
		}
	}

	public Object getKey(Object object) {
		if (object != null && object instanceof YSpecification) {
			return ((YSpecification) object).getID();
		}
		return null;
	}

	protected Object execute(RemoteCommand c) throws Exception{
		try {
			if (configurationDirty) {
				resetConnection();
			}
			return c.execute();
		} catch (YAuthenticationException e) {
				resetConnection();
				return c.execute();
		}
	}
	
	public Object retrieve(Class type, Object key) {
		try {
			return execute(new RetrieveCommand(key.toString()));
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
}

	public Object executeStatement(String query, String params) throws SQLException{
		try {
			return execute(new ExecuteStatementCommand(query, params));
		} catch (Exception e) {
			SQLException s = new SQLException(e.getMessage());
			s.initCause(e);
			throw s;
		}
}

	public List retrieveByRestriction(Class type, Restriction restriction) {
		try {
			return (List) execute(new RetrieveByRestrictionCommand(type, restriction));
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	public void save(Object object) throws YPersistenceException {
		try {
			execute(new SaveCommand((YSpecification) object));
		} catch (Exception e) {
			throw new YPersistenceException( e );
		}
	}

	public String getEngineUri() {
		return engineUri;
	}

	public void setEngineUri(String engineUri) {
		this.engineUri = engineUri;
		if (this.engineUri == null || !this.engineUri.equals(engineUri)) {
			configurationDirty = true;
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		if (this.password == null || !this.password.equals(password)) {
			configurationDirty = true;
		}
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String user) {
		this.userName = user;
		if (this.userName == null || !this.userName.equals(user)) {
			configurationDirty = true;
		}
	}

	public interface RemoteCommand {
		public Object execute() throws Exception;
	}
	
	public class RetrieveCommand implements RemoteCommand{
		private String key;
		public RetrieveCommand(String key) {this.key = key;}
		public Object execute() throws Exception{
			Exception e;
			YSpecification retval = null;
			String xml = ibClient.getSpecification(key.toString(), YawlEngineDAO.this.sessionHandle);
			e = XmlUtilities.getError(xml);
			if (e == null) {
				List l = YMarshal.unmarshalSpecifications(xml, "imported");
				if (l != null && l.size() == 1) {
					retval = (YSpecification) l.get(0);
				}
			} else {
				throw e;
			}
			return retval;
		}
	} 

	public class SaveCommand implements RemoteCommand {
		public YSpecification object;
		public SaveCommand(YSpecification object) {this.object = object;}
		public Object execute() throws Exception {
				iaClient.unloadSpecification(object.getID(), sessionHandle);
				String xml = YMarshal.marshal((YSpecification) object);
				String returnXml = iaClient.uploadSpecification(xml, ((YSpecification) object).getID(), sessionHandle);
				Exception e = XmlUtilities.getError(returnXml);
				if (e != null) throw e;
				else return null;
		}
	}

	public class DeleteCommand implements RemoteCommand {
		public String id;
		public DeleteCommand(String id) {this.id = id;}
		public Object execute() throws Exception {
			String xml = iaClient.unloadSpecification(id, sessionHandle);
				Exception e = XmlUtilities.getError(xml);
				if (e != null) throw e;
				else return null;
		}
	}

	public class RetrieveByRestrictionCommand implements RemoteCommand {
		public Class type;
		public Restriction restriction;
		public RetrieveByRestrictionCommand(Class type, Restriction restriction) {
			this.type = type;
			this.restriction = restriction;
		}
		public Object execute() throws Exception {
			return ibClient.getSpecificationsByRestriction(restriction, sessionHandle);
		}
	}
	
	public class ExecuteStatementCommand implements RemoteCommand {
		public String query;
		public String params = "";
		public ExecuteStatementCommand(String query, String params) {
			this.query = query;
			this.params = params;
		}
		public Object execute() throws Exception {
			String xml = ibClient.launchCase(query, params, sessionHandle);
				Exception e = XmlUtilities.getError(xml);
				if (e != null) throw e;
				else return xml;
		}
	}
}
