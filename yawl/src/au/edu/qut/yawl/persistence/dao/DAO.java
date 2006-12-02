/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.util.List;

import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.exceptions.YPersistenceException;

public interface DAO<Type> {
	public Object getKey(Type object) throws YPersistenceException;
	  public void save(Type object) throws YPersistenceException;
	  public Type retrieve(Class type, Object key) throws YPersistenceException;
	  public List<Type> retrieveByRestriction(Class type, Restriction restriction) throws YPersistenceException;
	  public boolean delete(Type object) throws YPersistenceException;
	  public List getChildren(Object object) throws YPersistenceException;
}
