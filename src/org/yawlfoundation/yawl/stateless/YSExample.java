package org.yawlfoundation.yawl.stateless;

import org.jdom2.Element;
import org.yawlfoundation.yawl.exceptions.YDataStateException;
import org.yawlfoundation.yawl.exceptions.YEngineStateException;
import org.yawlfoundation.yawl.exceptions.YQueryException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.stateless.listener.YCaseEventListener;
import org.yawlfoundation.yawl.stateless.listener.YLogEventListener;
import org.yawlfoundation.yawl.stateless.listener.YTimerEventListener;
import org.yawlfoundation.yawl.stateless.listener.YWorkItemEventListener;
import org.yawlfoundation.yawl.stateless.listener.event.*;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * @author Michael Adams
 * @date 30/9/20
 */
public class YSExample implements YCaseEventListener, YWorkItemEventListener,
        YLogEventListener, YTimerEventListener {

    private final YStatelessEngine _engine;             // the 'interface' to the engine
    int _caseCount;
    long _startTime;
    YNetRunner _caseRunner;

    public YSExample() {
        _engine = new YStatelessEngine();

        // add this object as a listener to the 4 different event types implemented above
        // (the fifth one, for exception events, is not being tested here)
        _engine.addCaseEventListener(this);
        _engine.addLogEventListener(this);
        _engine.addWorkItemEventListener(this);
        _engine.addTimerEventListener(this);
    }


    /**
     * Runs a single case of the specification supplied
     * @param specxml the XML of the specification
     */
    public void runOneCase(String specxml) {
        _startTime = System.currentTimeMillis();
        try {

            // we have to first transform the XML to a YSpecification object
            YSpecification spec = _engine.unmarshalSpecification(specxml);
            elapsed("Spec loaded in: ");

            _caseCount = 1;             // so we can exit when the case completes

            // Now we can kick off the case.
            // There are different variations of launchCase - this one will generate
            // a random caseID. Another variation (see the runMany method below)
            // allows you to supply the caseID.
            // The case state object returned by launchcase can be used by the client
            // to persist the current state. We don't do that in this example.
            _caseRunner = _engine.launchCase(spec);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Launches a specified number of cases of a specification
     * @param specxml the XML of the specification
     * @param count the number of instances to launch
     */
    public void runMany(String specxml, int count) {
        _startTime = System.currentTimeMillis();
        _caseCount = count;                 // flag for exiting when all cases completed

        try {
            // Transform the spec XML to a YSpecification object
            YSpecification spec = _engine.unmarshalSpecification(specxml);
            elapsed("Spec loaded in: ");

            // launch the cases, using i as the case id for each
            for (int i=1; i<= count; i++) {
                _engine.launchCase(spec, "" + i);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Respond to case level events: CASE_START, CASE_COMPLETE, CASE_CANCELLED,
     *     CASE_DEADLOCKED, CASE_SUSPENDING, CASE_SUSPENDED, CASE_RESUMED,
     *     CASE_CHECK_PRECONSTRAINTS, CASE_CHECK_POSTCONSTRAINTS
     * @param event the event object, with certain data describing the event
     */
    @Override
    public void handleCaseEvent(YCaseEvent event) {

        // for this example, just print out some info about the event
        print(event, "CaseID:", event.getCaseID().toString());

        // for a CASE_COMPLETE event, if all cases have completed, exit
        if (event.getEventType() == YEventType.CASE_COMPLETED ||
                event.getEventType() == YEventType.CASE_CANCELLED) {
            if (--_caseCount == 0) {
                elapsed("Completed in: ");
                System.exit(0);
            }
        }

        if (event.getEventType() == YEventType.CASE_IDLE_TIMEOUT) {
            try {
                String caseXML = _engine.unloadCase(event.getCaseID());
                System.out.println(caseXML);
            }
            catch (YStateException e) {
                throw new RuntimeException(e);
            }
        }

    }


    /**
     * Respond to logging events
     * @param event the event object, with certain data describing the event
     */
    @Override
    public void handleLogEvent(YLogEvent event) {

        // just print the log event info
        switch (event.getEventType())  {
            case ITEM_STATUS_CHANGE: {
                YWorkItem item = event.getWorkItem();
                print(event, "(Log)", "Item:", item.getIDString(), "to:",
                        event.getItemStatus());
                break;
            }
            case ITEM_DATA_VALUE_CHANGE: {
                YWorkItem item = event.getWorkItem();
                YLogDataItemList dataList = event.getLogData();
                String dataItem = ! (dataList == null || dataList.isEmpty()) ?
                        dataItemToString(dataList.get(0)) : "null";
                print(event, "Item:", item.getIDString(), "Data:", dataItem);
                break;
            }
            case ITEM_COMPLETED: {
                YWorkItem item = event.getWorkItem();
                print(event, "(Log)", "Item:", item.getIDString());
                break;
            }
            default: {
                print(event, "(Log)", "SpecID:",
                       event.getSpecID().toFullString(), "CaseID:", event.getCaseID().toString());
                break;
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
        print(event,"Item:", event.getItem().getIDString(),
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
            print(event, "Item:", item.getIDString());

            switch (event.getEventType()) {

                // if the item is enabled, we can choose to start it
                case ITEM_ENABLED: {
                    _engine.startWorkItem(item);
                    break;
                }

                // once the item is started, we can do some work with it
                case ITEM_STARTED: {

                    // not sure if this check is necessary
                    if (item.hasCompletedStatus()) {
                        print(null,
                                "**Item: ", item.getIDString(), "has already completed**");
                    }
                    else processWorkItem(item);
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
     * @param item the work item to work on and then complete
     * @throws YQueryException if the data assignments are malformed
     * @throws YEngineStateException if the engine isn't in running state (it will be)
     * @throws YStateException if the state object is out-of-date
     * @throws YDataStateException if the data string is malformed
     */
    private void processWorkItem(YWorkItem item) throws YQueryException,
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
//        String updatedData = JDOMUtil.elementToString(eData);
        String updatedData = "<A><blert>8</blert></A>";


        // tell the engine to complete it
        _engine.completeWorkItem(item, updatedData, null);
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
     * Prints a message with the difference between the start time and now
     * @param msg the message to print
     */
    private void elapsed(String msg) {
        long elapsed = System.currentTimeMillis() - _startTime;
        print(null, msg, String.valueOf(elapsed), "ms");
    }
    
    /**
     * This runs the example above
     * @param args there are none
     */
    public static void main(String[] args) {

//        if (args.length == 0) {
//            System.out.println("USAGE: java YSExample path_to_spec_file");
//            System.exit(0);
//        }
//
//        // absolute path for the spec file
//        String specFile = args[0];

        // a shortcut so I can test inside the IDE
        String specFile = "/Users/adamsmj/Documents/temp/simpleTimerSpec.yawl" ;

        // read file contents into a String
        String specXML = StringUtil.fileToString(specFile);

        // run one instance of the spec
        new YSExample().runOneCase(specXML);

        // run many instances
//        new YSExample().runMany(specXML, 1000);
    }
}
