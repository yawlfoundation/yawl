/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *  The WorkQueue Gateway provides a gateway (or a set of API) between the Resource
 *  Service and the participant workqueue jsps.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@yawlfoundation.org
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
            else if (action.equalsIgnoreCase("getQueuedItems")) {
                String id = req.getParameter("id");
                int queue = Integer.parseInt(req.getParameter("queue"));
                String format = req.getParameter("format");

                result = rm.getQueuedItems(id, queue, format) ;
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



    public List<String> getQueuedItems() {
        ArrayList<String> x = new ArrayList<String>();
        x.add("List Item 1");
        x.add("List Item 2");
        x.add("List Item 77");
        return  x;
    }


    public String login(String userid, String password) {
        return rm.login(userid, password);
    }


    public boolean checkConnection(String sessionHandle) {
        boolean result = false ;
        try {
            result = rm.checkConnection(sessionHandle) ;
        }
        catch (IOException ioe) {
            _log.error("IOException occurred when checking connection to YAWL Engine",
                        ioe);
        }
        return result ;
    }

    public Participant getParticipantFromUserID(String userid) {
        return rm.getParticipantFromUserID(userid) ;
    }
    
    public String getFullNameForUserID(String userID) {
        String result = null ;
        if (userID != null) {
            if (userID.equals("admin"))
                result = "Administrator" ;
            else
               result = rm.getFullNameForUserID(userID);
        }
        return result;                                           
    }

    public boolean successful(String test) {
        return rm.successful(test);
    }

    public Set getQueuedItems(Participant p, int queue) {
        return p.getWorkQueues().getQueuedWorkItems(queue) ;
    }


    public void acceptOffer(Participant p, WorkItemRecord wir) {
        rm.acceptOffer(p, wir) ;
    }

    public void startItem(Participant p, WorkItemRecord wir) {
        rm.start(p, wir) ;
    }

    public String getTestJSON() {
        return "";
    }

    public String getDojoMenuTree(String userID) {
        StringBuilder items = new StringBuilder(
                "{ label: 'name', identifier: 'name', items: [");

        items.append("{ name:'Work Queues', type:'category',")
             .append(" children: [")
             .append("{_reference:'Offered'},")
             .append("{_reference:'Allocated'},")
             .append("{_reference:'Started/Suspended'}]},")
             .append("{ name:'Offered', type:'workqueue'},")
             .append("{ name:'Allocated', type:'workqueue'},")
             .append("{ name:'Started/Suspended', type:'workqueue'},")
             .append("{ name:'Admin Tasks', type: 'category'}");
             
        items.append("]}");

        return items.toString();
    }



}
