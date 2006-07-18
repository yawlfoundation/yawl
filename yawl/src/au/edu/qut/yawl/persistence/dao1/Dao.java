/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence.dao1;

import java.io.Serializable;

import au.edu.qut.yawl.exceptions.YPersistenceException;

/**
 * Standard interface for defining a DAO object.  
 * 
 * @author Dean Mao
 * @created Oct 27, 2005
 */
public interface Dao {
	public void update(Serializable obj) throws YPersistenceException;
	public void delete(Serializable obj) throws YPersistenceException;
	public void create(Serializable obj) throws YPersistenceException;
	public void startTransaction() throws YPersistenceException;
	public void commitTransaction() throws YPersistenceException;
	public void rollbackTransaction() throws YPersistenceException;
}
