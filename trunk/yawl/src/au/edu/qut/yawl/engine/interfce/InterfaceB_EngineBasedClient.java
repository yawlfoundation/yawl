/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;


import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.ObserverGateway;
import au.edu.qut.yawl.engine.YEngine;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.unmarshal.YDecompositionParser;
import au.edu.qut.yawl.util.JDOMConversionTools;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.apache.log4j.Category;
import au.edu.qut.yawl.engine.EngineFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.*;


/**
 * 
 * @author Lachlan Aldred
 * Date: 22/01/2004
 * Time: 17:19:12
 * 
 */
public class InterfaceB_EngineBasedClient extends Interface_Client implements ObserverGateway {
    protected static Category logger = Category.getInstance(InterfaceB_EngineBasedClient.class);

    protected static final String ADDWORKITEM_CMD =             "announceWorkItem";
    protected static final String CANCELALLWORKITEMS_CMD =      "cancelAllInstancesUnderWorkItem";
    protected static final String CANCELWORKITEM_CMD =          "cancelWorkItem";
    protected static final String ANNOUNCE_COMPLETE_CASE_CMD =  "announceCompletion";

    /**
     * Indicates which protocol this shim services.<P>
     *
     * @return the scheme
     */
    public String getScheme() {
        return "http";
    }

    /**
     * PRE: The work item is enabled.
     * announces a work item to a YAWL Service.
     * @param yawlService the reference to a YAWL service in the environment
     * @param workItem the work item to announce,
     */
    public void announceWorkItem(URI yawlService, YWorkItem workItem) {

        ServiceHandler myHandler = new ServiceHandler(yawlService, workItem, ADDWORKITEM_CMD);
        EngineFactory.getExistingEngine().addServiceNotification(myHandler);
    }

    /**
     * Annonuces work item cancellation to the YAWL Service.
     * @param yawlService the YAWL service reference.
     * @param workItem the work item to cancel.
     */
    static void cancelWorkItem(URI yawlService, YWorkItem workItem) {
        ServiceHandler myHandler = new ServiceHandler(yawlService, workItem, "cancelWorkItem");
        EngineFactory.getExistingEngine().addServiceNotification(myHandler);
    }

    /**
     * Cancels the work item, and all child
     * workitems under the provided work item.
     * @param yawlService the yawl service reference.
     * @param workItem the parent work item to cancel.
     */
    public void cancelAllWorkItemsInGroupOf(URI yawlService, YWorkItem workItem) {
        //System.out.println("Thread::yawlService.getURI() = " + yawlService.getURI());
        //System.out.println("\rworkItem.toXML() = " + workItem.toXML());
        if(workItem.getParent() == null){
            ServiceHandler myHandler = new ServiceHandler(yawlService, workItem, "cancelAllInstancesUnderWorkItem");
            EngineFactory.getExistingEngine().addServiceNotification(myHandler);
        }
        else {
            ServiceHandler myHandler = new ServiceHandler(yawlService, workItem.getParent(), "cancelAllInstancesUnderWorkItem");
            EngineFactory.getExistingEngine().addServiceNotification(myHandler);
        }
    }

    /**
     * Called by engine to announce when a case is complete.
     * @param yawlService the yawl service
     * @param caseID the case that completed
     */
    public void announceCaseCompletion(URI yawlService, 
                                       YIdentifier caseID, Document casedata) {
        ServiceHandler myHandler = new ServiceHandler(yawlService, caseID, casedata, "announceCompletion");
        EngineFactory.getExistingEngine().addServiceNotification(myHandler);
    }

    /**
     * Returns an array of YParameter objects that describe the YAWL service
     * being referenced.
     * @param yawlService the YAWL service reference.
     * @return an array of YParameter objects.
     * @throws IOException if connection problem
     * @throws JDOMException if XML content problem.
     */
    public static YParameter[] getRequiredParamsForService(URI yawlService) throws IOException, JDOMException {
        List paramResults = new ArrayList();

        String urlOfYawlService = yawlService.toString();

        Map params = new HashMap();
        params.put("action", "ParameterInfoRequest");
        String parametersAsString = Interface_Client.executeGet(urlOfYawlService, params);
        //above should have returned a xml doc containing params descriptions
        //of required params to operate custom service.
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(parametersAsString));
        List paramsASXML = doc.getRootElement().getChildren();
        for (int i = 0; i < paramsASXML.size(); i++) {
            Element paramElem = (Element) paramsASXML.get(i);

            YParameter param = new YParameter(null, paramElem.getName());
            YDecompositionParser.parseParameter(
                    paramElem,
                    param,
                    null,
                    false);
            paramResults.add(param);
        }
        return (YParameter[]) paramResults.toArray(new YParameter[paramResults.size()]);
    }

}
