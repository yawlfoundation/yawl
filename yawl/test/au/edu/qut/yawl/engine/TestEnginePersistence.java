/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.engine;

import junit.framework.TestCase;
import au.edu.qut.yawl.exceptions.*;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import org.jdom.JDOMException;

import java.io.IOException;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.ArrayList;
import java.text.MessageFormat;

import org.hibernate.Query;
import org.hibernate.HibernateException;
/*
 * Author Lachlan Aldred
 * Date: 24/03/2006
 * Time: 11:28:45
 */
public class TestEnginePersistence extends TestCase {
    private YSpecification _specification;
    private AbstractEngine _engine;
    private MessageFormat format =
            new MessageFormat(
                    "<Funky>" +
                        "<FunkyNumber>{0}</FunkyNumber>" +
                        "<FunkyName>Funky</FunkyName>" +
                    "</Funky>");


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YStateException, YPersistenceException, YDataStateException, URISyntaxException {
        _engine = EngineFactory.createEngine();
        EngineClearer.clear(_engine);

        //use this spec because it has a case input param
        URL fileURL = getClass().getResource("TestFunkyStartParams.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = (YSpecification) YMarshal.
                    unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);

        _engine.addSpecifications(yawlXMLFile, false, new ArrayList());
    }



    /**
     * Tests the launching of a case containing an XML failure.
     * The xml failure throws an exception, but the
     * hib transaction isn't rolled-back/aborted - causing the next persistent
     * call on the engine to deadlock.
     */
    public void testLaunchCasePersistence() throws YPersistenceException, YSchemaBuildingException, YDataStateException, YStateException, InterruptedException {

        try {
            //Launch a case with a bad piece of XML data
            //(the string 'beef' is not a number).
            //This will cause a data state exception
            _engine.launchCase(
                null,
                _specification.getID(),
                format.format(new String[]{"beef"})
                , null);
        } catch (YDataStateException e) {
            //Catch the exception.
            // Try to start another case.
            //If it cannot proceed this means that hibernate is stuck.
            //The test should fail.
            Thread nextEngineCall = new Thread(new Runnable(){
                public void run() {
                    try{
                        _engine.launchCase(
                        null,
                        _specification.getID(),
                        format.format(new String[]{"beef"})
                        , null);
                    }catch(Exception e) {/*do nothing*/}
                }
            });
            nextEngineCall.start();
            Thread.sleep(5000);
            if(nextEngineCall.isAlive()) {
                fail("Engine deadlocked due to failure to abort " +
                        "a hibernate transaction.");
            }
        }
    }

    /**
     * Tests this scenario
     * 1. Case is launched
     * 2. workitem checked out
     * 3. case cancelled
     * 4. engine DB is checked for absence of left over work items
     */
    public void testPersistenceWithCaseCancellation() throws YSchemaBuildingException, YDataStateException, YPersistenceException, YStateException, YQueryException, YSyntaxException, IOException, JDOMException, HibernateException {


        String caseID = _engine.launchCase(
                null,
                _specification.getID(),
                format.format(new String[]{"1"})
                , null);
        YIdentifier id = _engine.getCaseID(caseID);
        YWorkItem enabledItem = (YWorkItem) _engine.getAvailableWorkItems().iterator().next();
        _engine.startWorkItem(enabledItem, null);
        _engine.cancelCase(id);

        //todo convert to new dao format
//        YPersistenceManager pmgr = new YPersistenceManager(YEngine.getPMSessionFactory());
//
//        pmgr.startTransactionalSession();
//        Query query = pmgr.createQuery("from au.edu.qut.yawl.engine.YWorkItem");
//
//        for (Iterator it = query.iterate(); it.hasNext();) {
//            YWorkItem witem = (YWorkItem) it.next();
//            if(witem.getCaseID().toString().startsWith(caseID)){
//                fail("Should have removed case id during cancel.");
//            }
//        }
//        pmgr.commit();
    }
}
