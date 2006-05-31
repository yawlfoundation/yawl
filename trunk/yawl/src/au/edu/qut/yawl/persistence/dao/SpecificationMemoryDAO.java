package au.edu.qut.yawl.persistence.dao;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import au.edu.qut.yawl.elements.YSpecification;

public class SpecificationMemoryDAO implements SpecificationDAO{
    
    private Map<String, YSpecification> specs = new HashMap<String, YSpecification>();
    
    public boolean delete(YSpecification m) {
        specs.remove(m.getID());
        return true;
    }
    
    public YSpecification retrieve(Object m) {
    	return specs.get(m);
    }

    public int save(YSpecification m) {
        specs.put(m.getID(), m);
        return 0;
    }
    
    public Serializable getKey(YSpecification m) {
        return m.getID();
    }

}
