package au.edu.qut.yawl.resourcing.rsInterface;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import au.edu.qut.yawl.resourcing.ResourceManager;
import au.edu.qut.yawl.resourcing.allocators.ResourceAllocator;
import au.edu.qut.yawl.resourcing.filters.ResourceFilter;
import au.edu.qut.yawl.resourcing.constraints.ResourceConstraint;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;


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

    private static Logger _log =
              Logger.getLogger("au.edu.qut.yawl.resourcing.rsInterface.ResourceGateway");

    public void init() {
        if (! ResourceManager.serviceInitialised) {
            try {
//                ServletContext context = getServletContext();
//
//                Library.setHomeDir(context.getRealPath("/"));
//                Library.setRepositoryDir(context.getInitParameter("Repository"));
//
//                String persistStr = context.getInitParameter("EnablePersistence");
//                Library.setPersist(persistStr.equalsIgnoreCase("TRUE"));
//
//                WorkletService.getInstance().completeInitialisation();
//                ExceptionService.getInst().completeInitialisation();
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
       ResourceManager rm = ResourceManager.getInstance() ;

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
           else if (action.equalsIgnoreCase("getAllParticipantNames")) {
               result = rm.getParticipantNames();
           }
           else if (action.equalsIgnoreCase("getAllRoleNames")) {
               result = rm.getRoleNames();
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

        String itemid = req.getQueryString() ;
         _log.info("The id passed is: " + itemid);
    }
}
