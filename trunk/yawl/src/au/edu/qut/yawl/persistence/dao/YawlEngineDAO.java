package au.edu.qut.yawl.persistence.dao;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceB_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.Interface_Client;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.XmlUtilities;

public class YawlEngineDAO implements DAO {

	private InterfaceA_EnvironmentBasedClient iaClient;
	private InterfaceB_EnvironmentBasedClient ibClient;
	private String sessionHandle;
	private String engineUri = "http://localhost:8080/yawl";
	private String user = "admin";
	private String password = "YAWL";
	private boolean configurationDirty = true;
	
	protected synchronized void resetConnection() throws IOException {
		iaClient = new InterfaceA_EnvironmentBasedClient(
				engineUri + "/ia");
		ibClient = new InterfaceB_EnvironmentBasedClient(
				engineUri + "/ib");
		sessionHandle = iaClient.connect(user, password);
		configurationDirty = false;
	}
	
	public boolean delete(Object object) {
		try {
			execute(new DeleteCommand(object.toString()));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
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
					new Unrestricted());

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
	
	//this is a very rough prototype of the connection logic eventually to be shared by the entire engine dao.	
	public Object retrieve(Class type, Object key) {
			try {
				return execute(new RetrieveCommand(key.toString()));
			} catch (Exception e) {
				return null;
			}
	}

	public List retrieveByRestriction(Class type, Restriction restriction) {
		try {
			return (List) execute(new RetrieveByRestrictionCommand(type, restriction));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void save(Object object) throws YPersistenceException {
		try {
			execute(new SaveCommand((YSpecification) object));
		} catch (Exception e) {
			e.printStackTrace();
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
		if (this.user == null || !this.user.equals(user)) {
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
		/*
		 * we need the restriction converter if we can get the engine to return
		 * the specs based on it.
		 */
		public Class type;
		public Restriction restriction;
		public RetrieveByRestrictionCommand(Class type, Restriction restriction) {
			this.type = type;
			this.restriction = restriction;
		}
		public Object execute() throws Exception {
			return ibClient.getSpecificationList(sessionHandle);
		}
	}
}
