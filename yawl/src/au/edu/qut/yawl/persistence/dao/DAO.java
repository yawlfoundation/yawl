package au.edu.qut.yawl.persistence.dao;

import java.io.Serializable;
import java.util.List;

public interface DAO<Type> {
	  public int save(Type t);
	  public boolean delete(Type t);
	  public Type retrieve(java.lang.Object key);
	  public Serializable getKey(Type t);
	  public List getChildren(Object o);
}
