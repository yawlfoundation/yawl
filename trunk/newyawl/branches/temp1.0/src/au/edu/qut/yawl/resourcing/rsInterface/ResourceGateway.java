package au.edu.qut.yawl.resourcing.rsInterface;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;

import au.edu.qut.yawl.resourcing.ResourceManager;

import java.io.IOException;
import java.io.PrintWriter;


/**
  *  The Resource Gateway class acts as a gateway between the Resource
 *  Service and the external world. It also initialises the service with values from
 *  'web.xml'.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 13/08/2007
 */

public class ResourceGateway extends HttpServlet {

    private ResourceManager rm = ResourceManager.getInstance() ;

    private static Logger _log =
              Logger.getLogger("au.edu.qut.yawl.resourcing.rsInterface.ResourceGateway");

    public void init() {
        if (! ResourceManager.serviceInitialised) {
            try {
                ServletContext context = getServletContext();

                Docket.setServiceRootDir(context.getRealPath("/")) ;

                String persist = context.getInitParameter("EnablePersistence");
                rm.setPersisting(persist.equalsIgnoreCase("TRUE"));

                String orgDataSource = context.getInitParameter("OrgDataSource");
                int orgDataRefreshRate = Integer.parseInt(
                                  context.getInitParameter("OrgDataRefreshRate"));

                rm.initOrgDataSource(orgDataSource, orgDataRefreshRate);
            }
            catch (Exception e) {
                _log.error("Gateway Initialisation Exception", e);
            }
            finally {
                ResourceManager.setServiceInitialised();
            }
        }
   }



   public void doGet(HttpServletRequest req, HttpServletResponse res)
                               throws IOException, ServletException {

       String result = "";
     //  ResourceManager rm = ResourceManager.getInstance() ;

       try {
           String action = req.getParameter("action");
           if (action == null) {
                  result = "<html><head>" +
               "<title>YAWL Resource Service</title>" +
               "</head><body>" +
               "<H3>Welcome to the YAWL Resource Service \"Gateway\"</H3>" +
               "<p> The Resource Gateway acts as a bridge between the Resource " +
                   "Service and the external world (it isn't meant to be browsed " +
                   " to directly).</p>" +
               "</body></html>";
           }
           else if (action.equalsIgnoreCase("getResourceConstraints")) {
               result = rm.getConstraintsAsXML() ;
           }
           else if (action.equalsIgnoreCase("getResourceFilters")) {
               result = rm.getFiltersAsXML() ;
           }
           else if (action.equalsIgnoreCase("getResourceAllocators")) {
               result = rm.getAllocatorsAsXML() ;
           }
           else if (action.equalsIgnoreCase("getAllSelectors")) {
               result = rm.getAllSelectors() ;
           }
           else if (action.equalsIgnoreCase("getParticipants")) {
               result = rm.getParticipantsAsXML();
           }
           else if (action.equalsIgnoreCase("getRoles")) {
               result = rm.getRolesAsXML();
           }
           else if (action.equalsIgnoreCase("getCapabilities")) {
               result = rm.getCapabilitiesAsXML();
           }
           else if (action.equalsIgnoreCase("getPositions")) {
               result = rm.getPositionsAsXML();
           }
           else if (action.equalsIgnoreCase("getOrgGroups")) {
               result = rm.getOrgGroupsAsXML();
           }
           else if (action.equalsIgnoreCase("getAllParticipantNames")) {
               result = rm.getParticipantNames();
           }
           else if (action.equalsIgnoreCase("getAllRoleNames")) {
               result = rm.getRoleNames();
           }
           else if (action.equalsIgnoreCase("isKnownParticipant")) {
               String id = req.getParameter("id");
               result = String.valueOf(rm.isKnownParticipant(id)) ;
           }
           else if (action.equalsIgnoreCase("isKnownRole")) {
               String id = req.getParameter("id");
               result = String.valueOf(rm.isKnownRole(id)) ;
           }
           else if (action.equalsIgnoreCase("isKnownCapability")) {
               String id = req.getParameter("id");
               result = String.valueOf(rm.isKnownCapability(id)) ;
           }
           else if (action.equalsIgnoreCase("isKnownPosition")) {
               String id = req.getParameter("id");
               result = String.valueOf(rm.isKnownPosition(id)) ;
           }
           else if (action.equalsIgnoreCase("isKnownOrgGroup")) {
               String id = req.getParameter("id");
               result = String.valueOf(rm.isKnownOrgGroup(id)) ;
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


    public void doPost(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {

        ResourceManager rm = ResourceManager.getInstance() ;
        String action = req.getParameter("action");

        if (action.equalsIgnoreCase("refreshOrgDataSet")) {
            rm.loadResources();
        }
        else if (action.equalsIgnoreCase("resetOrgDataRefreshRate")) {
            String rate = req.getParameter("rate");
            rm.startOrgDataRefreshTimer(Long.parseLong(rate));
        }
    }
}
