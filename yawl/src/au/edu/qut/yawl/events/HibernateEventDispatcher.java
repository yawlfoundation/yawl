package au.edu.qut.yawl.events;

import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.persistence.managed.DataProxy;

public class HibernateEventDispatcher implements YEventDispatcher {

	public void fireEvent(Object o) {
        try {
        	DataProxy dataProxy = AbstractEngine.getDataContext().createProxy(o, null);
        	AbstractEngine.getDataContext().attachProxy(dataProxy, o, null);
        	AbstractEngine.getDataContext().save(dataProxy);
        } catch (Exception e) {
        	// HERE some event dispatcher exception
        	// should be thrown to indicate that the event
        	// was not sent
        	e.printStackTrace();
        }
	}

}
