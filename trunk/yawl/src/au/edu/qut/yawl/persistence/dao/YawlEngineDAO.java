package au.edu.qut.yawl.persistence.dao;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
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

public class YawlEngineDAO implements DAO {

	private InterfaceA_EnvironmentBasedClient iaClient;

	private InterfaceB_EnvironmentBasedClient ibClient;

	private String sessionAHandle;

	private String sessionBHandle;
	
	private String engineUri = "http://localhost:8080/yawl/";
	
	private String user = "admin";
	
	private String password = "YAWL";

	public YawlEngineDAO() {
	}

	public void initialize() throws IOException {
		iaClient = new InterfaceA_EnvironmentBasedClient(
				engineUri + "/ia");
		sessionAHandle = iaClient.connect(user, password);
		ibClient = new InterfaceB_EnvironmentBasedClient(
				engineUri + "/ib");
		sessionBHandle = iaClient.connect(user, password);
	}
	
	
	public boolean delete(Object object) {
		try {
			iaClient.unloadSpecification(object.toString(), sessionAHandle);
			return true;
		} catch (IOException e) {
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

	public Exception getError(String xml) {
		Exception retval = null;
		if (!Interface_Client.successful(xml)) {
			try {
				String error = URLDecoder.decode(Interface_Client.stripOuterElement(
						Interface_Client.stripOuterElement(xml)), "UTF-8");
				InputStream is = new ByteArrayInputStream(((String) error).getBytes());
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String s = br.readLine();
				StringTokenizer st = new StringTokenizer(s, ":");
				String name = st.nextElement().toString();
				String message = st.nextElement().toString();
				List<StackTraceElement> list = new ArrayList<StackTraceElement>();
				while ((s = br.readLine()) != null) {
					st = new StringTokenizer(s, "(:) ");
					st.nextElement();
					String fqName = st.nextElement().toString();
					int whereislastdot = fqName.lastIndexOf(".");
					String method = fqName.substring(whereislastdot + 1);
					String clazz = fqName.substring(0, whereislastdot);
					String sourceFile = st.nextElement().toString();
					int lineNumber = -1;
					try {
						lineNumber = Integer.parseInt(st.nextElement().toString());
					} catch (Exception nfe) {
					}
					StackTraceElement e = new StackTraceElement(clazz, method, sourceFile, lineNumber);
					list.add(e);
				}
				Constructor c = Class.forName(name).getConstructor(new Class[] {String.class});
				retval = (Exception) c.newInstance(new Object[] {message});
				retval.setStackTrace(list.toArray(new StackTraceElement[] {}));
			} catch (Exception e) {
				retval = e;
			}
		}
		return retval;
	}
	
	//this is a very rough prototype of the connection logic eventually to be shared by the entire engine dao.	
	public Object retrieve(Class type, Object key) {
		YSpecification retval = null;
		String xml;
		Exception e;
		try {
			if (sessionBHandle == null) {
				initialize();
			}
			xml = ibClient.getSpecification(key.toString(), sessionBHandle);
			e = getError(xml);
			if (e == null) {
				List l = YMarshal.unmarshalSpecifications(xml, "imported");
				if (l != null && l.size() == 1) {
					retval = (YSpecification) l.get(0);
				}
			} else {
				initialize();
				xml = ibClient.getSpecification(key.toString(), sessionBHandle);
				e = getError(xml);
				if (e == null) {
					List l = YMarshal.unmarshalSpecifications(xml, "imported");
					if (l != null && l.size() == 1) {
						retval = (YSpecification) l.get(0);
					}
				} else {
					throw e;
				}
			}			
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return retval;
	}

	public List retrieveByRestriction(Class type, Restriction restriction) {
		/*
		 * we need the restriction converter if we can get the engine to return
		 * the specs based on it.
		 */
		List retval = null;
		try {
			retval = ibClient.getSpecificationList(sessionBHandle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	public void save(Object object) throws YPersistenceException {
		try {
			if (object instanceof YSpecification) {
				String xml = YMarshal.marshal((YSpecification) object);
				iaClient.uploadSpecification(xml, ((YSpecification) object)
						.getID(), sessionAHandle);
			}
			iaClient.uploadSpecification(sessionAHandle, sessionBHandle,
					sessionAHandle);
		} catch (Exception e) {
			YPersistenceException ype = new YPersistenceException(
					"engine dao unable to save specification", e);
			throw ype;
		}
	}

	public static void main(String[] args) {
			YawlEngineDAO dao = new YawlEngineDAO();
			Object o = dao.retrieve(YSpecification.class, "MakeRecordings");
			System.out.println(o);
	}

	public String getEngineUri() {
		return engineUri;
	}

	public void setEngineUri(String engineUri) {
		this.engineUri = engineUri;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
