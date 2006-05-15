/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import au.edu.qut.yawl.exceptions.*;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.unmarshal.YMarshal;
import org.jdom.JDOMException;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

/**
 * 
 * @author Lachlan Aldred
 * Date: 25/02/2005
 * Time: 14:56:46
 * This file remains the property of the Queensland University of
 * Technology.
 * You do not have permission to use, view, execute or modify the source outside the terms
 * of the YAWL licence.
 * For more information about the YAWL licence refer to the 'downloads' section under
 * http://www.yawl-system.com
 */
public class TestEngineAgainstABeta4Spec extends TestCase {
    private YSpecification _specification;
    private AbstractEngine _engine;
    private YNetRunner _netRunner;


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException {
        URL fileURL = getClass().getResource("MakeRecordings(Beta4).xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = null;

        _specification = (YSpecification) YMarshal.
                unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);

        _engine =  EngineFactory.createYEngine();
    }


    public void testExpectedNormalOperation() throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        synchronized (this) {
            EngineClearer.clear(_engine);
            _engine.loadSpecification(_specification);
            YIdentifier id = _engine.startCase(_specification.getID(), null, null);
            _netRunner = (YNetRunner) _engine._caseIDToNetRunnerMap.get(id);
            {
                //execute task "decideName"
                Set availableItems = _engine.getAvailableWorkItems();
                assertTrue(availableItems.size() == 1);
                YWorkItem wiDecideName_Enabled = (YWorkItem) availableItems.iterator().next();
                assertTrue(wiDecideName_Enabled.getTaskID().equals("decideName"));

                YWorkItem wiDecideName_Executing =
                        _engine.startWorkItem(wiDecideName_Enabled, "admin");

                assertTrue(wiDecideName_Executing.getStatus().equals(YWorkItem.statusExecuting));

                _engine.completeWorkItem(wiDecideName_Executing,
                        "<DecideAlbumName>" +
                        "<nameOfRecord>The Fred experience.</nameOfRecord>" +
                        "</DecideAlbumName>");
            }
            {
                //execute task "decideSongs"
                Set availableItems = _engine.getAvailableWorkItems();
                assertTrue(availableItems.size() == 1);
                YWorkItem wiDecideSongs_Enabled = (YWorkItem) availableItems.iterator().next();
                assertTrue(wiDecideSongs_Enabled.getTaskID().equals("decideSongs"));

                YWorkItem wiDecideSongs_Executing =
                        _engine.startWorkItem(wiDecideSongs_Enabled, "admin");

                assertTrue(wiDecideSongs_Executing.getStatus().equals(YWorkItem.statusExecuting));

                _engine.completeWorkItem(wiDecideSongs_Executing,
                        "<DecideWhichSongsToRecord>" +
                            "<songlist>" +
                                "<song>" +
                                    "<songName>Experience my Fredness</songName>" +
                                    "<songSpecification>" +
                                        "This is my story,  " +
                                        "My song about Fred for you,  " +
                                        "I will sing it through." +
                                    "</songSpecification>" +
                                    "<songLength>" +
                                        "<minutes>1</minutes>" +
                                        "<seconds>1</seconds>" +
                                    "</songLength>" +
                                    "<selected>true</selected>" +
                                    "<sequenceNumber>1</sequenceNumber>" +
                                "</song>" +
                            "</songlist>" +
                        "</DecideWhichSongsToRecord>");
            }
            {
                //execute task "prepare" this is the first task of a decomposition
                //inside of the multi instance composite task "record"
                Set availableItems = _engine.getAvailableWorkItems();
                assertTrue(availableItems.size() == 1);
                YWorkItem wiRecord_Enabled = (YWorkItem) availableItems.iterator().next();
                assertTrue(wiRecord_Enabled.getTaskID().equals("prepare"));

                YWorkItem wiRecord_Executing =
                        _engine.startWorkItem(wiRecord_Enabled, "admin");

                assertTrue(wiRecord_Executing.getStatus().equals(YWorkItem.statusExecuting));


            }
        }
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestEngineAgainstABeta4Spec.class);
        return suite;
    }
}
