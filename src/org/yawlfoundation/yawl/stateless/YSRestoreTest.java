package org.yawlfoundation.yawl.stateless;

import org.jdom2.Element;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.stateless.listener.YCaseEventListener;
import org.yawlfoundation.yawl.stateless.listener.YTimerEventListener;
import org.yawlfoundation.yawl.stateless.listener.YWorkItemEventListener;
import org.yawlfoundation.yawl.stateless.listener.event.*;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * This class tests the unloading (marshaling) or a case running in one engine, and
 * restoring it to a different engine.
 * @author Michael Adams
 */
public class YSRestoreTest implements YCaseEventListener, YWorkItemEventListener,
         YTimerEventListener {

    // for this simple test we'll use only two engines
    private final YStatelessEngine _engine1;
    private final YStatelessEngine _engine2;

    // a reference to the root net runner of the case. This var is not mandatory,
    // since every case and work item event also has a reference to the root net runner
    private YNetRunner _caseRunner;


    public YSRestoreTest() {

        // passing a case idle time sets a timer for all cases on that engine.
        // Different engines can have different idle timer values.
        // Each engine is assigned a unique integer for identification. You can get it
        // via YStatelessEngine#getEngineNbr.
        _engine1 = initEngine(1000);
        _engine2 = initEngine(5000);
    }


    // Creates an engine and add this class to some of its listeners. In this example we
    // ignore log and exception events since they're not relevant to this test.
    public YStatelessEngine initEngine(long idleTimeMSecs) {

        // Passing an idle time (in msecs) enables case monitoring. Once set, if there
        // is no action on the case for the duration set, a CASE_IDLE_TIMEOUT event
        // will be generated (see below).
        // Note you can also enable case monitoring by calling an empty constructor,
        // then using YStatelessEngine#setIdleCaseTimer(long msecs).
        // You can reset the duration at any time, which will reset the idle timers for
        // all current cases on that engine.
        // Passing a negative timer value will disable case monitoring.
        YStatelessEngine engine = new YStatelessEngine(idleTimeMSecs);
        engine.addCaseEventListener(this);
        engine.addWorkItemEventListener(this);
        engine.addTimerEventListener(this);
        return engine;
    }


    /**
     * Runs a single case of the specification supplied
     * @param specxml the XML of the specification
     */
    public void runOneCase(String specxml) {
        try {

            // we have to first transform the XML to a YSpecification object
            YSpecification spec = _engine1.unmarshalSpecification(specxml);
            
            // Now we can kick off the case.
            _caseRunner = _engine1.launchCase(spec);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Respond to a case level events
     * @param event the event object, with certain data describing the event
     */
    @Override
    public void handleCaseEvent(YCaseEvent event) {

        // first print out some info about the event
        // Note that an integer identifying the engine that generated the event is
        // passed in the event object
        print(event, "Engine:", String.valueOf(event.getEngineNbr()),
                "CaseID:", event.getCaseID().toString());

        // for a CASE_COMPLETE event, if the case has completed or been cancelled, exit
        if (event.getEventType() == YEventType.CASE_COMPLETED ||
                event.getEventType() == YEventType.CASE_CANCELLED) {
                System.exit(0);
        }

        // The case is idle, so let's unload it from one engine and load it onto another
        if (event.getEventType() == YEventType.CASE_IDLE_TIMEOUT) {
            try {
                if (event.getEngineNbr() == 1) {
                    String caseXML = _engine1.unloadCase(event.getCaseID());

                    // then, sometime later...
                    _caseRunner = _engine2.restoreCase(caseXML);
                }
                else {
                    System.out.println("Engine 2 idle time out");
                    System.exit(1);
                }
            }
            catch (YStateException e) {
                e.printStackTrace();
            }
            catch (YSyntaxException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Respond to any timer events
     * @param event the event object, with certain data describing the event
     */
    @Override
    public void handleTimerEvent(YTimerEvent event) {

        // NOTE: The engine will maintain (but not persist) timers that are begun during
        // the execution of a case, and will emit events for action. When the engine
        // object is discarded, so are the outstanding timers, if any.
        // When a case is unloaded from an engine, it's timers are cancelled and a
        // timer cancelled event is generated.
        // When a case is restored, timers are restarted.
        print(event,"Engine:", String.valueOf(event.getEngineNbr()),
                "Item:", event.getItem().getIDString(),
                "Expires:", event.getExpiryTimeString());
    }


    /**
     * Respond to work item events: ITEM_ENABLED, ITEM_STARTED, ITEM_COMPLETED,
     *     ITEM_STATUS_CHANGE, ITEM_CANCEL, ITEM_DATA_VALUE_CHANGE,
     *     ITEM_CHECK_PRECONSTRAINTS, ITEM_CHECK_POSTCONSTRAINTS
     * @param event the event object, with certain data describing the event
     */
    @Override
    public void handleWorkItemEvent(YWorkItemEvent event) {

        // this is arguably the most important listener method, as it responds to work item
        // events to progress the case
        try {
            // get the work item from the event object
            YWorkItem item = event.getWorkItem();

            // print some basic event info
            print(event, "Engine:", String.valueOf(event.getEngineNbr()),
                    "Item:", item.getIDString());

            // which engine has sent this event? In prod, you'd have a map of engines
            YStatelessEngine engine = event.getEngineNbr() == 1 ? _engine1 : _engine2;

            switch (event.getEventType()) {

                // if the item is enabled, we can choose to start it
                case ITEM_ENABLED_REANNOUNCE:    // on case restore to an engine
                case ITEM_ENABLED: {
                    engine.startWorkItem(item);
                    break;
                }

                // once the item is started, we can do some work with it
                case ITEM_STARTED: {

                    // not sure if this check is necessary
                    if (item.hasCompletedStatus()) {
                        print(null,
                                "**Item: ", item.getIDString(), "has already completed**");
                    }

                    // complete the item to the same engine
                    else processWorkItem(item, engine);
//                    else _engine.cancelCase(_caseRunner);
                    break;
                }

                // just print out any state changes
                case ITEM_STATUS_CHANGE: {
                     print(event,"Item:", item.getIDString(), "from:",
                             event.getPreviousStatus().toString(), "to:",
                             event.getCurrentStatus().toString());
                     break;
                }
            }
        }

        // catches any data validation errors on checkout and/or checkin
        catch (YDataStateException ydse) {
            print(null, "**Exception in handleWorkItemEvent: ",
                                    "Item:", event.getWorkItem().getIDString());
            ydse.printStackTrace();
        }
        catch (YStateException yse) {
            if (! event.getWorkItem().hasCompletedStatus()) {
                print(null, "**Exception in handleWorkItemEvent: ",
                        "Item:", event.getWorkItem().getIDString());
                yse.printStackTrace();
            }
        }
        catch (Exception e) {
            print(null, "**Exception in handleWorkItemEvent: ",
                    "Item:", event.getWorkItem().getIDString());
            e.printStackTrace();
        }
    }


    /**
     * Simulates the processing of a work item: extracts the work item data, does a
     * simple update, then completes the work item
     *
     * @param item   the work item to work on and then complete
     * @param engine
     * @throws YQueryException       if the data assignments are malformed
     * @throws YEngineStateException if the engine isn't in running state (it will be)
     * @throws YStateException       if the state object is out-of-date
     * @throws YDataStateException   if the data string is malformed
     */
    private void processWorkItem(YWorkItem item, YStatelessEngine engine) throws YQueryException,
            YEngineStateException, YStateException, YDataStateException {

        // extract the data element from the work item
        Element eData = item.getDataElement();

        // update the value of the one variable in the example spec. Naturally, this is
        // particular to the example, and would need to change for other specs
        String value = getDataValue(eData, "blert");
        if (value != null) {
            print(null, "Value:", value);
            StringBuilder sb = new StringBuilder(value);
            sb.append(':').append(item.getIDString());
            setDataValue(eData, "blert", sb.toString());
        }
        
        // the engine needs it to be a string (at this stage...)
        // For simplicity, this data is relevant only to the spec running for this example
        String updatedData = item.getTaskID().equals("A") ? "<A><blert>8</blert></A>" :
                JDOMUtil.elementToString(eData);


        // tell the engine to complete it
        engine.completeWorkItem(item, updatedData, null);
    }


    /**
     * A simple print method
     * @param event the event to print
     * @param args the sequence of strings to print
     */
    private void print(YEvent event, String... args) {
        StringBuilder sb = new StringBuilder();
        if (event != null) {
            sb.append(event.getTimeStamp()).append(": ");
            sb.append(event.getEventType().toString()).append(" ");
        }
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        System.out.println(sb);
    }


    /**
     * Extracts the value of a simple work item variable
     * @param data the work item data element
     * @param varName the name of the variable
     * @return the variable's value
     */
    private String getDataValue(Element data, String varName) {
        return data != null ? data.getChildText(varName) : null;
    }


    /**
      * Assigns a value to a simple work item variable
      * @param data the work item data element
      * @param varName the name of the variable
      * @param value the value to assign
      */
    private void setDataValue(Element data, String varName, String value) {
        Element eChild = data.getChild(varName);
        eChild.setText(value);
    }


    /**
     * This one just converts a log item to a string so we can print it
     * @param item the log item, containing some logged info
     * @return the flattened string
     */
    private String dataItemToString(YLogDataItem item) {
        return item != null ? item.toXMLShort() : null;
    }


    /**
     * This runs the example above
     * @param args there are none
     */
    public static void main(String[] args) {
        // a shortcut so I can test inside the IDE
        String specFile = "/Users/adamsmj/Documents/temp/simpleTimerSpec.yawl" ;

        // read file contents into a String
        String specXML = StringUtil.fileToString(specFile);

        // run one instance of the spec
        new YSRestoreTest().runOneCase(specXML);

    }
}
