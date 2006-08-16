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

public abstract class AbstractDelegatedDAO implements DAO<Object> {
	private Map<Class,DAO> typeMap = new HashMap<Class,DAO>();
	
	public void addType( Class type, DAO dao ) {
		typeMap.put( type, dao );
	}
	
	protected DAO getDAOForType( Class type ) {
		return typeMap.get( type );
	}
	
	public final Object getKey( Object object ) {
		checkTypeSupported( object.getClass() );
		return typeMap.get( object.getClass() ).getKey( object );
	}
	
	public final void save( Object object ) {
		checkTypeSupported( object.getClass() );
		typeMap.get( object.getClass() ).save( object );
	}
	
	public final Object retrieve( Class type, Object key ) {
		checkTypeSupported( type );
		return typeMap.get( type ).retrieve( type, key );
	}
	
	public final List<Object> retrieveAll( Class type ) {
		checkTypeSupported( type );
		return typeMap.get( type ).retrieveAll( type );
	}
	
	public final boolean delete( Object object ) {
		checkTypeSupported( object.getClass() );
		return typeMap.get( object.getClass() ).delete( object );
	}
	
	private final void checkTypeSupported( Class type ) {
		if( ! typeMap.containsKey( type ) ) {
			throw new IllegalArgumentException( "DAO does not support the type " + type );
		}
	}
}
