package au.edu.qut.yawl.persistence.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceB_EnvironmentBasedClient;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.unmarshal.YMarshal;

public class YawlEngineDAO implements DAO {

	InterfaceA_EnvironmentBasedClient iaClient;

	InterfaceB_EnvironmentBasedClient ibClient;

	String sessionAHandle;

	String sessionBHandle;

	public YawlEngineDAO() throws IOException {
		iaClient = new InterfaceA_EnvironmentBasedClient(
				"http://localhost:8080/yawl/ia");
		sessionAHandle = iaClient.connect("admin", "YAWL");
		ibClient = new InterfaceB_EnvironmentBasedClient(
				"http://localhost:8080/yawl/ib");
		sessionBHandle = iaClient.connect("admin", "YAWL");
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
			// this method is stolen from hibernatedao but for the next line
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

	public Object retrieve(Class type, Object key) {
		YSpecification retval = null;
		try {
			String xml = ibClient.getSpecification(key.toString(),
					sessionBHandle);
			List l = YMarshal.unmarshalSpecifications(xml, "abc");
			if (l != null && l.size() == 1) {
				retval = (YSpecification) l.get(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		try {
			YawlEngineDAO dao = new YawlEngineDAO();
			Object o = dao.retrieve(YSpecification.class, "MakeRecordings");
			System.out.println(o.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
