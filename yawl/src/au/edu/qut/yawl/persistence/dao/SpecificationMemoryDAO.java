package au.edu.qut.yawl.persistence.dao;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexusbpm.editor.tree.DatasourceRoot;

import au.edu.qut.yawl.elements.YSpecification;

public class SpecificationMemoryDAO implements SpecificationDAO{
    
    private Map<Long, YSpecification> specs = new HashMap<Long, YSpecification>();
    private long nextdbID = 1;
    public static YSpecification testSpec = new YSpecification("/home/sandozm/templates/testing/testspec");
    static {
    	testSpec.setName("My test specification");    	
    }
    
    public SpecificationMemoryDAO() {
    	save(testSpec);
    }    
    
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
		List retval = new ArrayList();
		if (o instanceof String || o instanceof DatasourceRoot) {

		String path = o.toString();
		for (YSpecification value: specs.values()) {
			String key = value.getID();
			if (key != null && key.startsWith(path)) {
				if (ab(path, key) != null) {
					retval.add(ab(path, key));
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

	private static String ab(String a, String b) {
		String retval = null;
//		a = "/home/msandoz/a/b/c.efg";
//		b = "/home/msandoz/a/b/c.efg";
		if (b.startsWith(a)) {
			int x = b.lastIndexOf("/");
			int y = b.indexOf("/", a.length() + 1);
			System.out.println("stats:" + x + ":" + y + ":" + a + ":" + b);
			if (x > a.length()) {
				retval = b.substring(0, y);
			}
		}
		System.out.println(retval);
		return retval;
	}

	public static void main(String[] s) {
		System.out.println("i return " + ab("/home/msandoz/a","/home/msandoz/a/b/c.efg"));
	}
	
}
