/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.invoker;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Content;
import org.jdom.Element;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import com.nexusbpm.services.data.NexusServiceData;

/**
 * Environment side controller for NexusWorkflow services.
 * @author Nathan Rose
 */
public class NexusServiceController extends InterfaceBWebsideController {
    private String _sessionHandle = null;
    
    public static final String SERVICENAME_PARAMNAME = "ServiceName";
    
    /**
     * Implements InterfaceBWebsideController.  It recieves messages from the engine
     * notifying an enabled task and acts accordingly.  In this case it takes the message,
     * tries to check out the work item, and if successful it begins to start up a web service
     * invokation.
     * @param enabledWorkItem
     */
    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
        try {
            if (!checkConnection(_sessionHandle)) {
                _sessionHandle = connect(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
            }
            if (successful(_sessionHandle)) {
                List executingChildren = checkOutAllInstancesOfThisTask(enabledWorkItem, _sessionHandle);
                
                for (int i = 0; i < executingChildren.size(); i++) {
                    WorkItemRecord itemRecord = (WorkItemRecord) executingChildren.get(i);
                    Element inputData = itemRecord.getWorkItemData();
                    String serviceName = inputData.getChildText(SERVICENAME_PARAMNAME);
                    List<Content> serviceData = inputData.cloneContent();
                    NexusServiceData data = null;
                    
                    for( Iterator<Content> iter = serviceData.iterator(); iter.hasNext(); ) {
                        Content c = iter.next();
                        if( c instanceof Element && ((Element)c).getName().equals( SERVICENAME_PARAMNAME ) ) {
                            iter.remove();
                        }
                    }
                    
//                    System.out.println( "Outputting service data to invoke Nexus Service with:" );
//                    new XMLOutputter( Format.getRawFormat() ).output( inputData, System.out );
                    
                    data = NexusServiceData.unmarshal( serviceData );
                    
                    NexusServiceData reply =
                            NexusServiceInvoker.invokeService( serviceName, data );
                    
                    System.out.println( "\n\nReply from nexus service '" + serviceName + "':\n" + reply );
                    System.out.println();
                    
                    Element caseDataBoundForEngine = prepareReplyRootElement(enabledWorkItem, _sessionHandle);
                    
                    // convert reply back to XML
                    if( reply != null ) {
                        caseDataBoundForEngine.addContent( NexusServiceData.marshal( reply ) );
                    }
                    else {
                    	_logger.error( "Nexus Service '" + serviceName + "' returned no resulting data!" );
                    }
                    
                    String checkInResult = checkInWorkItem(
                            itemRecord.getID(),
                            inputData,
                            caseDataBoundForEngine,
                            _sessionHandle);
                    
                    _logger.debug(
//                    System.out.println(
                    		"\nResult of item [" + itemRecord.getID() +
                    		"] checkin is : " + checkInResult );
                }
            }
        } catch( Exception e ) {
        	e.printStackTrace( System.out );
            _logger.error(e.getMessage(), e);
        }
    }
	
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
    	// TODO do we need to handle this case?
    }
    
    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[1];
        YParameter param;
        
        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_NCNAME_TYPE, SERVICENAME_PARAMNAME, XSD_NAMESPACE);
        param.setDocumentation("This is the name of the Nexus Workflow Service");
        params[0] = param;
        
        return params;
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	// TODO do we need this?

        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/authServlet");

        dispatcher.forward(request, response);
    }
}

