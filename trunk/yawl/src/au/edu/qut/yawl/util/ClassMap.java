/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.util;

import java.util.HashMap;

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
 * 
 * @author Nathan Rose
 */
public class ClassMap<V> extends HashMap<Class,V> {
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
