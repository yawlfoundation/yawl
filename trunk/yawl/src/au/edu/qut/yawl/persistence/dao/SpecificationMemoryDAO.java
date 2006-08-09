package au.edu.qut.yawl.persistence.dao;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.MockYSpecification;
import au.edu.qut.yawl.elements.YSpecification;

public class SpecificationMemoryDAO implements SpecificationDAO{
    
	private static final Log LOG = LogFactory.getLog( SpecificationMemoryDAO.class );
    private Map<String, YSpecification> specs = new HashMap<String, YSpecification>();
    private long nextdbID = 19;
    public static YSpecification testSpec = MockYSpecification.getSpecification();

    public SpecificationMemoryDAO() {
    	save(testSpec); // TODO remove this later...
    }    
    
    public boolean delete(YSpecification m) {
        specs.remove(m.getID());
        return true;
    }

    public YSpecification retrieve(Object m) {
    	return specs.get(m); //m should be the dbid 
    }

    public int save(YSpecification spec) {
        if (spec.getDbID() == null) spec.setDbID(getNextdbID());
    	specs.put(spec.getID(), spec);
        return 0;
    }

    public Serializable getKey(YSpecification m) {
        return m.getID();
    }

	public List getChildren(Object o) {
		if (o == null) throw new IllegalArgumentException("Parent can not be null");
		List retval = new ArrayList();
		if (o instanceof String || o instanceof DatasourceRoot) {

		String path = o.toString();
		for (YSpecification value: specs.values()) {
			String key = value.getID();
			if (key != null && key.startsWith(path)) {
				if (contains(key, path) != null) {
					retval.add(contains(key, path));
				} else {
					retval.add(value);
				}
			}
		}
		}
		return retval;
	}

	private long getNextdbID() {
		return nextdbID++;
	}

	private static String contains(String full, String partial) {
		String retval = null;
		if (full.startsWith(partial)) {
			int x = full.lastIndexOf("/");
			int y = full.indexOf("/", partial.length() + 1);
			if (x > partial.length()) {
				retval = full.substring(0, y);
			}
		}
		return retval;
	}
	
}
