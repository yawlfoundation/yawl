/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.util.List;

public interface DAO<Type> {
	public Object getKey(Type object);
	  public void save(Type object);
	  public Type retrieve(Class type, Object key);
	  public List<Type> retrieveAll(Class type);
	  public boolean delete(Type object);
	  public List getChildren(Object object);
}
