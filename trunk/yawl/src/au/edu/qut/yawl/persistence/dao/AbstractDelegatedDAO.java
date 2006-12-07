/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YPersistenceException;

public abstract class AbstractDelegatedDAO implements DAO<Object> {
	private Map<Class,DAO> typeMap = new ClassMap<DAO>();
	
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
	
	/**
	 * Custom version of a map that maps Classes to values of any type.
	 * containsKey(key) will return true if the given class itself is a
	 * key or if a superclass of it is a key. get(key) will return the
	 * object associated with that class if the class itself is a key,
	 * or if a superclass of that class is a key it will return the
	 * value associated with that superclass.
	 * 
	 * Behavior is undefined if multiple classes from the same hierarchy
	 * are added as keys.
	 */
	private class ClassMap<V> extends HashMap<Class,V> {
		@Override
		public boolean containsKey( Object key ) {
			if( ! ( key instanceof Class ) ) {
				return false;
			}
			else if( super.containsKey( key ) ) {
				return true;
			}
			Class c = (Class) key;
			for( Class type : keySet() ) {
				if( type.isAssignableFrom( c ) ) {
					return true;
				}
			}
			return false;
		}

		@Override
		public V get( Object key ) {
			if( ! ( key instanceof Class ) ) {
				return null;
			}
			else if( super.containsKey( key ) ) {
				return super.get( key );
			}
			Class c = (Class) key;
			for( Class type : keySet() ) {
				if( type.isAssignableFrom( c ) ) {
					return super.get( type );
				}
			}
			return null;
		}
	}
}
