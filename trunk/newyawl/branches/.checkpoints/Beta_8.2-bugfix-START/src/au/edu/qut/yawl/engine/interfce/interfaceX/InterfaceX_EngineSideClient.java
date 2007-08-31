/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.engine.interfce.interfaceX;

import au.edu.qut.yawl.engine.YWorkItem;
import au.edu.qut.yawl.engine.interfce.Interface_Client;
import au.edu.qut.yawl.util.JDOMConversionTools;

import org.apache.log4j.Category;
import org.jdom.Document;

import java.io.IOException;
import java.util.*;

/**
 *  InterfaceX_EngineSideClient passes exception event calls from the engine to the
 *  exception service.
 *
 *  This class is a member class of Interface X, which provides an interface
 *  between the YAWL Engine and a Custom YAWL Service that manages exception
 *  handling at the process level.
 *
 *  InterfaceB_EngineBasedClient was used as a template for this class.
 *
 *  Schematic of Interface X:
 *                                          |
 *                           EXCEPTION      |                              INTERFACE X
 *                            GATEWAY       |                                SERVICE
 *                  (implements) |          |                       (implements) |
 *                               |          |                                    |
 *  +==========+   ----->   ENGINE-SIDE  ---|-->   SERVICE-SIDE  ----->   +=============+
 *  || YAWL   ||              CLIENT        |        SERVER               || EXCEPTION ||
 *  || ENGINE ||                            |                             ||  SERVICE  ||
 *  +==========+   <-----   ENGINE-SIDE  <--|---   SERVICE-SIDE  <-----   +=============+
 *                            SERVER        |         CLIENT
 *                                          |
 *  @author Michael Adams                   |
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  @version 0.8, 04/07/2006
 */

public class InterfaceX_EngineSideClient extends Interface_Client implements ExceptionGateway {

    protected static Category logger = Category.getInstance(InterfaceX_EngineSideClient.class);

    // event types
    protected static final int NOTIFY_CHECK_CASE_CONSTRAINTS = 0;
    protected static final int NOTIFY_CHECK_ITEM_CONSTRAINTS = 1;
    protected static final int NOTIFY_WORKITEM_ABORT = 2;
    protected static final int NOTIFY_TIMEOUT = 3;
    protected static final int NOTIFY_RESOURCE_UNAVAILABLE = 4;
    protected static final int NOTIFY_CONSTRAINT_VIOLATION = 5;
    protected static final int NOTIFY_CANCELLED_CASE = 6;

    private String _observerURI ;

    // the constructor //
    public InterfaceX_EngineSideClient(String observerURI) {
        _observerURI = observerURI ;
    }


    /**
     * Indicates which protocol this shim services
     *
     * @return the scheme
     */
    public String getScheme() {
        return "http";
    }

    public void setURI(String uri) {
        _observerURI = uri ;
    }

    public String getURI() {
        return _observerURI ;
    }

    /*****************************************************************************/

    // ANNOUNCEMENT METHODS - SEE EXCEPTIONGATEWAY FOR COMMENTS //

    public void announceCheckWorkItemConstraints(YWorkItem item, Document data, boolean preCheck) {
        new Handler(_observerURI, item, data, preCheck, NOTIFY_CHECK_ITEM_CONSTRAINTS).start();
    }


    public void announceCheckCaseConstraints(String specID, String caseID,
                                             String data, boolean preCheck) {
        new Handler(_observerURI, specID, caseID, data, preCheck,
                                              NOTIFY_CHECK_CASE_CONSTRAINTS).start();
    }


    public void announceWorkitemAbort(YWorkItem item) {
        new Handler(_observerURI, item, NOTIFY_WORKITEM_ABORT).start();
    }



    public void announceTimeOut(YWorkItem item, List taskList){
       new Handler(_observerURI, item, taskList, NOTIFY_TIMEOUT).start();
    }


    public void announceResourceUnavailable(YWorkItem item){
        new Handler(_observerURI, item, NOTIFY_RESOURCE_UNAVAILABLE).start();
    }


    public void announceConstraintViolation(YWorkItem item){
        new Handler(_observerURI, item, NOTIFY_CONSTRAINT_VIOLATION).start();
    }


    public void announceCaseCancellation(String caseID){
        new Handler(_observerURI, caseID, NOTIFY_CANCELLED_CASE).start();

    }


  ////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handler class called by the above announcement methods - passes the events
     * to the service side.
     */

    static class Handler extends Thread {
        private YWorkItem _workItem;
        private String _observerURI;
        private String _caseID;
        private int _command;
        private boolean _preCheck;
        private Document _dataDoc ;
        private String _dataStr ;
        private String _specID ;
        private List _taskList ;


        /**
         * Different constructors for different event types
         */

        public Handler(String observerURI, YWorkItem workItem, int command) {
            _observerURI = observerURI;
            _workItem = workItem;
            _command = command;
        }

        public Handler(String observerURI, String caseID, int command) {
            _observerURI = observerURI;
            _caseID = caseID;
            _command = command;
        }

        public Handler(String observerURI, YWorkItem workItem, List taskList, int command) {
            _observerURI = observerURI;
            _workItem = workItem;
            _taskList = taskList;
            _command = command;
        }

        public Handler(String observerURI, YWorkItem workItem, Document data,
                       boolean preCheck, int command) {
            _observerURI = observerURI;
            _workItem = workItem;
            _preCheck = preCheck;
            _command = command;
            _dataDoc = data ;
        }

        public Handler(String observerURI, String specID, String caseID, String data,
                       boolean preCheck, int command) {
            _observerURI = observerURI;
            _specID = specID ;
            _caseID = caseID;
            _preCheck = preCheck;
            _command = command;
            _dataStr = data ;
        }

        // POST the event
        public void run() {

           Map paramsMap = new HashMap();
           try {

               // all events have an event type
                paramsMap.put("action", String.valueOf(_command));

                // additional params as required
                switch (_command) {
                    case InterfaceX_EngineSideClient.NOTIFY_CHECK_CASE_CONSTRAINTS:
                        paramsMap.put("specID", _specID);
                        paramsMap.put("caseID", _caseID);
                        paramsMap.put("preCheck", String.valueOf(_preCheck));
                        paramsMap.put("data", _dataStr);
                        break ;
                    case InterfaceX_EngineSideClient.NOTIFY_CHECK_ITEM_CONSTRAINTS:
                        paramsMap.put("workItem", _workItem.toXML());
                        paramsMap.put("preCheck", String.valueOf(_preCheck));
                        paramsMap.put("data", JDOMConversionTools.documentToString(_dataDoc));
                        break ;
                    case InterfaceX_EngineSideClient.NOTIFY_CANCELLED_CASE:
                        paramsMap.put("caseID", _caseID);
                        break ;
                    case InterfaceX_EngineSideClient.NOTIFY_TIMEOUT:
                        paramsMap.put("workItem", _workItem.toXML());
                        paramsMap.put("taskList", _taskList.toString());
                        break ;
                }

                // run the post with the appropriate params
                Interface_Client.executePost(_observerURI, paramsMap);

            } catch (IOException e) {
                logger.error("failed to call YAWL service", e);
                e.printStackTrace();
            }
        }
    }
}
