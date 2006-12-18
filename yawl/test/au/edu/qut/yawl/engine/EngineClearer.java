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
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;
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
    public static void clear(YEngineInterface engine) throws YPersistenceException {
        AbstractEngine.getWorkItemRepository().clear();
        
        List<DataProxy> ids = AbstractEngine.getDataContext().retrieveByRestriction( YIdentifier.class,
                new PropertyRestriction( "parent", Comparison.EQUAL, null),
                null );
//        List<DataProxy> runners = AbstractEngine.getDataContext().retrieveByRestriction( YNetRunner.class,
//                new PropertyRestriction( "archived", Comparison.EQUAL, false),
//                null );
        
        for( DataProxy<YIdentifier> proxy : ids ) {
            YIdentifier id = proxy.getData();
            if( engine.getNetRunner( id ) == null ) {
                AbstractEngine.getDataContext().delete( AbstractEngine.getDataContext().getDataProxy( id ) );
            }
            else {
                engine.cancelCase(id);
            }
        }
        
//        for( DataProxy<YNetRunner> proxy : runners ) {
//            YNetRunner runner = proxy.getData();
//            engine.cancelCase( runner.getCaseID() );
//        }
        
        List<DataProxy> runners = AbstractEngine.getDataContext().retrieveAll( YNetRunner.class, null );
        for( DataProxy<YNetRunner> proxy : runners ) {
            AbstractEngine.getDataContext().delete( proxy );
        }
        
        Set<YSpecification> specs = engine.getSpecifications();
        for( YSpecification spec : specs ) {
            Set caseIDs = engine.getCasesForSpecification(spec.getID());
            for (Iterator iterator2 = caseIDs.iterator(); iterator2.hasNext();) {
                YIdentifier identifier = (YIdentifier) iterator2.next();
                engine.cancelCase(identifier);
            }
        }
        
    	List<DataProxy> list = AbstractEngine.getDataContext().retrieveByRestriction(
    			YSpecification.class, new Unrestricted(), null );
    	for( DataProxy proxy : list ) {
    		AbstractEngine.getDataContext().delete( proxy );
    	}
    }
}
