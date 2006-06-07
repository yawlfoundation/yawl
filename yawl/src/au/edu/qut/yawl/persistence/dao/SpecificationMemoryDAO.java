package au.edu.qut.yawl.persistence.dao;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.edu.qut.yawl.elements.YSpecification;

public class SpecificationMemoryDAO implements SpecificationDAO{
    
    private Map<Long, YSpecification> specs = new HashMap<Long, YSpecification>();
    private long nextdbID = 1;
    public boolean delete(YSpecification m) {
        specs.remove(m.getID());
        return true;
    }

    public YSpecification retrieve(Object m) {
    	return specs.get(m);
    }

    public int save(YSpecification spec) {
        if (spec.getDbID() == null) spec.setDbID(getNextdbID());
    	specs.put(spec.getDbID(), spec);
        return 0;
    }

    public Serializable getKey(YSpecification m) {
        return m.getID();
    }

	public List getChildren(Object o) {
		if (o == null) throw new IllegalArgumentException("Parent can not be null");
		if (!(o instanceof String)) throw new IllegalArgumentException("Parent must be a String.");
		String path = (String) o;
//		int pathstart = path.lastIndexOf("/") - 1;
//		path = path.substring(0, pathstart);
		List retval = new ArrayList();
		for (YSpecification value: specs.values()) {
			String key = value.getID();
			if (key != null && key.startsWith(path)) {
				retval.add(specs.get(key));
			}
		}
		return retval;
	}

	private long getNextdbID() {
		return nextdbID++;
	}

}
