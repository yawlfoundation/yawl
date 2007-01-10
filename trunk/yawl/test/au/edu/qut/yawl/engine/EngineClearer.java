/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.engine;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.YAWLTransactionAdvice;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

/**
 * @author Lachlan Aldred
 * Date: 26/11/2004
 * Time: 17:42:40
 */
public class EngineClearer {
    private static PrintStream out;
	/**
	 * <em>Warning:</em> This method will clear the engine and its data context! If
	 * the engine is connected to a database, the database will have all specifications
	 * removed!
	 */
    public static void clear(YEngineInterface engine) throws YPersistenceException {
        AbstractEngine.getWorkItemRepository().clear();
        
        Set<YSpecification> specs = engine.getSpecifications( false );
        log( specs.size() + " specs" );
        for( YSpecification spec : specs ) {
            log("\tspec " + spec.getID());
            Set caseIDs = engine.getCasesForSpecification(spec.getID());
            log("\t" + caseIDs.size() + " cases");
            for (Iterator iterator2 = caseIDs.iterator(); iterator2.hasNext();) {
                YIdentifier identifier = (YIdentifier) iterator2.next();
                log("\t\t" + identifier);
                if( engine.getNetRunner( identifier ) != null ) {
                    log("\t\tcancelling");
                    engine.cancelCase(identifier);
                }
                else {
                    log("\t\tdeleting");
                    assert identifier != null : "Identifier should not be null";
                    AbstractEngine.getDao().delete( identifier );
                }
            }
        }
        
        List<YIdentifier> ids = AbstractEngine.getDao().retrieveByRestriction(
                YIdentifier.class, new PropertyRestriction( "parent", Comparison.EQUAL, null ) );
        log( ids.size() + " identifiers" );
        for( YIdentifier id : ids ) {
            log( "\t" + id.toString() );
            if( engine.getNetRunner( id ) == null ) {
                log( "\tdeleting identifier" );
                AbstractEngine.getDao().delete( id );
            }
            else {
                log( "\tcancelling case" );
                engine.cancelCase(id);
            }
        }
        
        List<YNetRunner> runners = AbstractEngine.getDao().retrieveByRestriction(
                YNetRunner.class, new Unrestricted() );
        log( runners.size() + " net runners" );
        for( YNetRunner runner : runners ) {
            log( "\tdeleting " + runner );
            AbstractEngine.getDao().delete( runner );
        }
        
    	List<YSpecification> list = AbstractEngine.getDao().retrieveByRestriction(
    			YSpecification.class, new Unrestricted() );
        log(list.size() + " specifications");
    	for( YSpecification spec : list ) {
            log("\tdeleting spec " + spec.getID() + ":" + spec.getVersion() + "(" + spec.getDbID() + ")");
    		AbstractEngine.getDao().delete( spec );
    	}
        log("clear complete");
    }
    
    private static void log(String msg) {
        PrintStream out = EngineClearer.out;
        if( out != null ) {
            out.println(msg);
        }
    }
    
    public static void main(String[] args) throws Throwable {
        out = System.out;
        YAWLTransactionAdvice a = new YAWLTransactionAdvice();
        log("starting engine");
        EngineFactory.getTransactionalEngine();
        log("starting transaction");
        a.before( null, null, null );
        log("clearing database");
        clear( EngineFactory.getTransactionalEngine() );
        log("commiting");
        a.afterReturning( null, null, null, null );
        out = null;
    }
}
