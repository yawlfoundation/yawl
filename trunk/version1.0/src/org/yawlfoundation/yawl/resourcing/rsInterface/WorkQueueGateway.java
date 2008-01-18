/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.jsf.FormParameter;
import org.yawlfoundation.yawl.resourcing.jsf.ApplicationBean;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *  The WorkQueue Gateway provides a gateway (or a set of API) between the Resource
 *  Service and the participant workqueue jsps.
 *
 *  @author Michael Adams
 *  v0.1, 13/08/2007
 *
 *  Last Date: 20/09/2007
 */

public class WorkQueueGateway  {          // extends HttpServlet

    private ResourceManager rm = ResourceManager.getInstance() ;
    private Logger _log = Logger.getLogger(this.getClass());
    private static WorkQueueGateway _me;


    public static WorkQueueGateway getInstance() {
        if (_me == null) _me = new WorkQueueGateway();
        return _me ;
    }

    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {

        String result = "";

        try {
            String action = req.getParameter("action");
            if (action == null) {
                   result = "<html><head>" +
                "<title>YAWL Resource Service WorkQueue Gateway</title>" +
                "</head><body>" +
                "<H3>Welcome to the YAWL Resource Service \"WorkQueue Gateway\"</H3>" +
                "<p>The WorkQueue Gateway acts as a bridge between the Resource " +
                    "Service and a user interface implementation " +
                    "(it isn't meant to be browsed to directly).</p>" +
                "</body></html>";
            }


            // generate the output
            res.setContentType("text/html");
            PrintWriter out = res.getWriter();
            out.write(result);
            out.flush();
            out.close();
         }
         catch (Exception e) {
             _log.error("Exception in doGet()", e);
         }
    }

    public String login(String userid, String password) {
        return rm.login(userid, password);
    }

    public void logout(String handle) {
        rm.logout(handle);
    }

    public boolean isValidSession(String handle) {
        return rm.isValidSession(handle) ;
    }

    public boolean checkConnection(String sessionHandle) throws IOException{
        return rm.checkConnection(sessionHandle) ;
    }

    public Participant getParticipantFromUserID(String userid) {
        return rm.getParticipantFromUserID(userid) ;
    }
    
    public String getFullNameForUserID(String userID) {
        String result = null ;
        if (userID != null) result = rm.getFullNameForUserID(userID);
        return result;                                           
    }

    public boolean successful(String test) {
        return rm.successful(test);
    }

    public Set getQueuedItems(Participant p, int queue) {
        return p.getWorkQueues().getQueuedWorkItems(queue) ;
    }


    public void acceptOffer(Participant p, WorkItemRecord wir, String handle) throws IOException {
        if (checkConnection(handle))
            rm.acceptOffer(p, wir) ;
    }

    public void startItem(Participant p, WorkItemRecord wir, String handle) throws IOException {
        if (checkConnection(handle))
            rm.start(p, wir, handle) ;
    }

    public void deallocateItem(Participant p, WorkItemRecord wir, String handle) throws IOException {
        if (checkConnection(handle))
            rm.deallocateWorkItem(p, wir) ;
    }

    public void delegateItem(Participant pFrom, Participant pTo, WorkItemRecord wir,
                             String handle) throws IOException {
        if (checkConnection(handle))
            rm.delegateWorkItem(pFrom, pTo, wir) ;
    }

    public void skipItem(Participant p, WorkItemRecord wir, String handle) throws IOException {
        if (checkConnection(handle))
            rm.skipWorkItem(p, wir, handle) ;
    }

    public void pileItem(Participant p, WorkItemRecord wir, String handle) throws IOException {
        rm.pileWorkItem(p, wir) ;
    }


    public void suspendItem(Participant p, WorkItemRecord wir, String handle) throws IOException {
        if (checkConnection(handle))
            rm.suspendWorkItem(p, wir) ;
    }

    public void reallocateItem(Participant pFrom, Participant pTo, WorkItemRecord wir,
                               boolean stateful, String handle) throws IOException {
        if (checkConnection(handle))  {
            if (stateful)
               rm.reallocateStatefulWorkItem(pFrom, pTo, wir) ;
            else
               rm.reallocateStatelessWorkItem(pFrom, pTo, wir) ;
        }
    }

    public void completeItem(Participant p,WorkItemRecord wir, String handle) throws IOException {
        if (checkConnection(handle))
            rm.checkinItem(p, wir, handle) ;
    }

    public void unsuspendItem(Participant p, WorkItemRecord wir, String handle) throws IOException {
        if (checkConnection(handle))
            rm.unsuspendWorkItem(p, wir) ;
    }

    public Set<SpecificationData> getLoadedSpecs(String handle) {
        return rm.getLoadedSpecs(handle) ;
    }

    public Set<SpecificationData> getSpecList(String handle) {
        return rm.getSpecList(handle) ;
    }

    public SpecificationData getSpecData(String specID, String handle) {
        return rm.getSpecData(specID, handle) ;
    }

    public List<String> getRunningCases(String specID, String handle) {
        return rm.getRunningCases(specID, handle) ;
    }

    public String uploadSpecification(String fileContents, String fileName, String handle) {
        return rm.uploadSpecification(fileContents, fileName, handle) ;
    }

    public String cancelCase(String caseID, String handle) throws IOException {
        return rm.cancelCase(caseID, handle);
    }

    public String unloadSpecification(String specID, String handle) throws IOException {
        return rm.unloadSpecification(specID, handle);
    }

    public String launchCase(String specID, String caseData, String handle) throws IOException {
        return rm.launchCase(specID, caseData, handle) ;
    }

    public Set<Participant> getReportingToParticipant(String pid) {
        return rm.getParticipantsReportingTo(pid) ;
    }

    public Participant getParticipant(String pid) {
        return rm.getParticipant(pid) ;
    }

    public Set<Participant> getAllParticipants(String handle) throws IOException {
        if (checkConnection(handle)) {
            return rm.getParticipants() ;
        }
        else return null ;        
    }

    public Map<String, FormParameter> getWorkItemParams(WorkItemRecord wir, String handle)
                                                     throws IOException, JDOMException {
        if (checkConnection(handle)) {
            return rm.getWorkItemParamsForPost(wir, handle);
        }
        return null ;
    }

    public void updateWIRCache(WorkItemRecord wir) {
        rm.getWorkItemCache().update(wir) ;
    }

    public String getDecompID(WorkItemRecord wir) {
        return rm.getDecompID(wir) ;
    }

    public void registerJSFApplicationReference(ApplicationBean app) {
        rm.registerJSFApplicationReference(app);
    }

    public String getXFormsURI() { return rm.getXFormsURI() ; }


}
