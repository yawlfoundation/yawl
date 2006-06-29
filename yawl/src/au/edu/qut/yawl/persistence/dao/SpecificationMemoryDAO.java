package au.edu.qut.yawl.persistence.dao;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.edu.qut.yawl.elements.MockYSpecification;
import au.edu.qut.yawl.elements.YSpecification;

import com.nexusbpm.editor.tree.DatasourceRoot;

public class SpecificationMemoryDAO implements SpecificationDAO{
    
    private Map<Long, YSpecification> specs = new HashMap<Long, YSpecification>();
    private long nextdbID = 19;
    public static YSpecification testSpec = MockYSpecification.getSpecification();

    public SpecificationMemoryDAO() {
    	save(testSpec); //remove this later...
    }    
    
    public boolean delete(YSpecification m) {
        specs.remove(m.getDbID());
        return true;
    }

    public YSpecification retrieve(Object m) {
    	return specs.get(m); //m should be the dbid 
    }

    public int save(YSpecification spec) {
        if (spec.getDbID() == null) spec.setDbID(getNextdbID());
    	specs.put(spec.getDbID(), spec);
        return 0;
    }

    public Serializable getKey(YSpecification m) {
        return m.getDbID();
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
