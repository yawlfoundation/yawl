/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.events;

import java.io.Serializable;

import au.edu.qut.yawl.engine.AbstractEngine;

public class HibernateEventDispatcher implements YEventDispatcher {

	public void fireEvent(Serializable o) {
        try {
        	AbstractEngine.getDao().save(o);
        } catch (Exception e) {
        	// HERE some event dispatcher exception
        	// should be thrown to indicate that the event
        	// was not sent
        	e.printStackTrace();
        }
	}

}
