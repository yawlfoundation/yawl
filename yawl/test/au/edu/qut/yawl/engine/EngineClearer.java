/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.engine;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.managed.DataProxy;

/**
 * @author Lachlan Aldred
 * Date: 26/11/2004
 * Time: 17:42:40
 */
public class EngineClearer {
	/**
	 * <em>Warning:</em> This method will clear the engine and its data context! If
	 * the engine is connected to a database, the database will have all specifications
	 * removed!
	 */
    public static void clear(AbstractEngine engine) throws YPersistenceException {
        while (engine.getSpecIDs().iterator().hasNext()) {
            String specID = (String) engine.getSpecIDs().iterator().next();
            Set caseIDs = engine.getCasesForSpecification(specID);
            for (Iterator iterator2 = caseIDs.iterator(); iterator2.hasNext();) {
                YIdentifier identifier = (YIdentifier) iterator2.next();
                engine.cancelCase(identifier);
            }
            engine.removeSpecification(specID);
        }
        
    	List<DataProxy> list = AbstractEngine.getDataContext().retrieveByRestriction(
    			YSpecification.class, new Unrestricted(), null );
    	for( DataProxy proxy : list ) {
    		AbstractEngine.getDataContext().delete( proxy );
    	}
    }
}
