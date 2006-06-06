package com.nexusbpm.editor.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This is just like a HashMap only that all the values are actually sets.
 * When you put in a value, you get back a set which contains that value.
 * Should you add another value that has the same key as the first value, the
 * value will be added to the set pointed to by the key (so the set should
 * have two values in it now). This is mainly used to group populations of data
 * into a single key. You can make a tree representation solely using
 * information from the HashBag.
 *
 * @author Dean Mao
 * @created Feb 18, 2004
 */
public class HashBag extends HashMap {
	/**
	 * Adds the given value to the set contained at the given location.
	 *
	 * @param key   the key defining where to place the value.
	 * @param value the value to be added to the set.
	 * @return the set that the value was added to.
	 */
	public Object put( Object key, Object value ) {
		Set hs = (Set) this.get( key );
		if( hs == null ) {
			hs = new HashSet();
			super.put( key, hs );
		}//if
		hs.add( value );
		return hs;
	}//put()

}//HashBag
