/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.util.List;
import java.util.Map;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.util.ClassMap;

public abstract class AbstractDelegatedDAO implements DAO<Object> {
	protected Map<Class,DAO> typeMap = new ClassMap<DAO>();
	
	public void addType( Class type, DAO dao ) {
		typeMap.put( type, dao );
	}
	
	protected DAO getDAOForType( Class type ) {
		return typeMap.get( type );
	}
	
	public final Object getKey( Object object ) throws YPersistenceException {
		checkTypeSupported( object.getClass() );
		return typeMap.get( object.getClass() ).getKey( object );
	}
	
	public final void save( Object object ) throws YPersistenceException {
		checkTypeSupported( object.getClass() );
		typeMap.get( object.getClass() ).save( object );
	}
	
	public final Object retrieve( Class type, Object key ) throws YPersistenceException {
		checkTypeSupported( type );
		return typeMap.get( type ).retrieve( type, key );
	}
	
	public final List<Object> retrieveByRestriction( Class type, Restriction restriction ) throws YPersistenceException {
		checkTypeSupported( type );
		return typeMap.get( type ).retrieveByRestriction( type, restriction );
	}
	
	public final void delete( Object object ) throws YPersistenceException {
		checkTypeSupported( object.getClass() );
		typeMap.get( object.getClass() ).delete( object );
	}
	
	public List getChildren( Object object ) throws YPersistenceException {
		return getDAOForType( YSpecification.class ).getChildren( object );
	}
	
	private final void checkTypeSupported( Class type ) {
		if( ! typeMap.containsKey( type ) ) {
			throw new IllegalArgumentException( "DAO does not support the type " + type );
		}
	}
}
