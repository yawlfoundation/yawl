package au.edu.qut.yawl.persistence.dao;

import au.edu.qut.yawl.elements.YSpecification;

public interface DAO<Type> {
	  public int save(Type t);
	  public boolean delete(Type t);
	  public Type retrieve(java.lang.Object key);
	  public Object getKey(Type t);
}
