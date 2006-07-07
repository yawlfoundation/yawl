/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.invoker;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import com.nexusbpm.services.data.NexusServiceData;

/**
 * Environment side controller for NexusWorkflow services.
 * @author Nathan Rose
 */
public class NexusServiceController extends InterfaceBWebsideController {
	
	private static Logger LOG = Logger.getLogger( NexusServiceController.class );
	
	private static JAXBContext context;
	
	private static ValidationEventLogger eventLogger;
	
    private String _sessionHandle = null;
    
    private static final String SERVICENAME_PARAMNAME = "ServiceName";
    private static final String SERVICEDATA_PARAMNAME = "ServiceData";
    
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
                    Element serviceData = inputData.getChild(SERVICEDATA_PARAMNAME);
//                    serviceData.setNamespace(Namespace.getNamespace("http://www.nexusworkflow.com/"));
                    NexusServiceData data = null;
                    
                    System.out.println( "Outputting service data to invoke Nexus Service with:" );
                    new XMLOutputter( Format.getPrettyFormat() ).output( serviceData, System.out );
                    
                    for( int index = 0; index < serviceData.getContentSize(); index++ ) {
                    	Content content = serviceData.getContent( index );
                    	if( content instanceof Element ) {
                    		_logger.debug( "NexusServiceData found" );
                    		Element nexusData = (Element) content;
//                    		System.out.println( "NexusServiceData element:\n" );
//                    		new XMLOutputter( Format.getPrettyFormat() ).output( nexusData, System.out );
                    		if( nexusData.getName().equals( "NexusServiceData" ) ) {
                    			// TODO may want to wrap this in a try/catch
                    			data = unmarshal( nexusData );
                    		}
                    	}
                    	else if( content != null ) {
                    		_logger.debug( content.getClass().toString() + " : " + content.toString() );
                    	}
                    	else {
                    		_logger.debug( "nexus service controller received null data" );
                    	}
                    }
                    
                    NexusServiceData reply =
                            NexusServiceInvoker.invokeService( serviceName, data );
                    
                    System.out.println( "\n\nReply from nexus service '" + serviceName + "':\n" + reply );
                    System.out.println();
//                    System.out.println("\n\nReply from Web service being " +
//                            "invoked is :" + replyFromWebServiceBeingInvoked);
                    
                    Element caseDataBoundForEngine = prepareReplyRootElement(enabledWorkItem, _sessionHandle);
                    
                    // convert reply back to XML
                    if( reply != null ) {
                    	Content replyContent = marshal( reply );
                    	Element outputParam = new Element( "ServiceData" );
//                    	outputParam.removeContent();
                    	outputParam.setContent( replyContent.detach() );
//                    	outputParam.setContent( replyContent );
                    	caseDataBoundForEngine.addContent( outputParam );
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
    
    private static void setNamespace( Element element, Namespace namespace ) {
    	element.setNamespace( namespace );
    	for( Content child : (List<Content>) element.getContent() ) {
    		if( child instanceof Element ) {
    			setNamespace( (Element) child, namespace );
    		}
    	}
    }
    
    public static NexusServiceData unmarshal( Element root ) throws JAXBException, IOException {
		initContext();
		setNamespace( root, Namespace.getNamespace( "http://www.nexusworkflow.com/" ) );
		StringWriter writer = new StringWriter();
		XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
		outputter.output( root, writer );
		
		Unmarshaller unmarshaller = getUnmarshaller();
		
		System.out.println( "Data before unmarshalling:\n" + writer.toString() );
		
		StringReader reader = new StringReader( writer.toString() );
		
		return (NexusServiceData) unmarshaller.unmarshal( reader );
	}
    
	public static Content marshal( NexusServiceData data ) throws JAXBException, IOException, JDOMException {
		initContext();
		Marshaller marshaller = getMarshaller();
		
		StringWriter writer = new StringWriter();
		marshaller.marshal( data, writer );
		
		System.out.println( "Data before marshalling:\n" + writer.toString() );
		
		SAXBuilder builder = new SAXBuilder();
		StringReader reader = new StringReader( writer.toString() );
		Document doc = builder.build( reader );
		
		return doc.getRootElement();
	}
	
    private static void initContext() throws JAXBException {
    	if( context == null ) {
			context = JAXBContext.newInstance(
					"com.nexusbpm.services.data",
					NexusServiceData.class.getClassLoader() );
		}
	}
	
	private static Unmarshaller getUnmarshaller() throws JAXBException {
		initContext();
		
		Unmarshaller unmarshaller = context.createUnmarshaller();
		unmarshaller.setEventHandler(getValidationEventLogger());
		
		return unmarshaller;
	}
	
	private static Marshaller getMarshaller() throws JAXBException {
		initContext();
		
		Marshaller marshaller = context.createMarshaller();
		marshaller.setEventHandler(getValidationEventLogger());
		
		return marshaller;
	}
	
	private static ValidationEventLogger getValidationEventLogger() {
		if( eventLogger == null ) {
			eventLogger = new ValidationEventLogger();
		}
		return eventLogger;
	}
	
	/**
	 * Logs validation messages during JAXB marshalling/unmarshalling and
	 */
	private static class ValidationEventLogger implements ValidationEventHandler {
		public boolean handleEvent( ValidationEvent event ) {
			Throwable exception = event.getLinkedException();
			switch( event.getSeverity() ) {
				case ValidationEvent.WARNING:
					if( exception != null ) {
						LOG.warn( event.getMessage(), exception );
					}
					else {
						LOG.warn( event.getMessage() );
					}
					return true; // continue unmarshalling after a warning
				case ValidationEvent.ERROR:
				case ValidationEvent.FATAL_ERROR:
				default:
					/* TODO since we're returning false and telling it not to continue unmarshalling,
					 * the call to marshaller.marshal() or unmarshaller.unmarshal() will throw an
					 * exception and so we may not need to log it here... */
					if( exception != null ) {
						LOG.error( event.getMessage(), exception );
					}
					else {
						LOG.error( event.getMessage() );
					}
					return false; // don't continue unmarshalling after any errors
			}
		}
	}
	
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
    	// TODO do we need to handle this case?
    }

    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[2];
        YParameter param;
        
        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_NCNAME_TYPE, SERVICENAME_PARAMNAME, XSD_NAMESPACE);
        param.setDocumentation("This is the name of the Nexus Workflow Service");
        params[0] = param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        // TODO change type
        param.setDataTypeAndName("ExtensionType", SERVICEDATA_PARAMNAME, "http://www.citi.qut.edu.au/yawl");
        param.setDocumentation("This is the data for the service.");
        params[1] = param;

        return params;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	// TODO what's this for?

        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/authServlet");

        dispatcher.forward(request, response);
    }
}

