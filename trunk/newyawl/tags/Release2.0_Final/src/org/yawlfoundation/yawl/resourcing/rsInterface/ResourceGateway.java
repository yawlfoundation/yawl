package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.util.Docket;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;


/**
  *  The Resource Gateway class acts as a gateway between the Resource
 *  Service and the external world. It also initialises the service with values from
 *  'web.xml'.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  v0.1, 13/08/2007
 *
 *  Last Date: 03/01/2008
 */

public class ResourceGateway extends HttpServlet {

    private ResourceManager rm = ResourceManager.getInstance() ;
    private static final Logger _log = Logger.getLogger(ResourceGateway.class);


    /** Read settings from web.xml and use them to initialise the service */
    public void init() {
        if (! ResourceManager.serviceInitialised) {
            try {
                ServletContext context = getServletContext();

                // set the actual root file path of the service
                Docket.setServiceRootDir(context.getRealPath("/")) ;

                // set the engine uri and the exception service uri (if enabled)
                rm.initInterfaceClients(context.getInitParameter("InterfaceB_BackEnd"),
                                        context.getInitParameter("InterfaceX_BackEnd"));

                // enable/or disable persistence
                String persist = context.getInitParameter("EnablePersistence");
                rm.setPersisting(persist.equalsIgnoreCase("TRUE"));

                // set the org data source and refresh rate
                String orgDataSource = context.getInitParameter("OrgDataSource");
                String refreshRate = context.getInitParameter("OrgDataRefreshRate") ;
                int orgDataRefreshRate = -1;
                try {
                     orgDataRefreshRate = Integer.parseInt(refreshRate);
                }
                catch (Exception e) {
                    _log.warn("ResourceGateway: Invalid integer value in web.xml" +
                              " for OrgDataRefreshRate; value '" +
                               refreshRate + "' will be ignored.");
                }
                rm.initOrgDataSource(orgDataSource, orgDataRefreshRate);

                // enable/disable logging of all offers
                String logOffers = context.getInitParameter("LogOffers");
                EventLogger.setOfferLogging(logOffers.equalsIgnoreCase("TRUE"));

                // enable/disable the dropping of task piling on logout
                String dropPiling = context.getInitParameter("DropTaskPilingOnLogoff");
                rm.setPersistPiling(dropPiling.equalsIgnoreCase("FALSE")) ;

                // enable the visualiser applet, if necessary
                String enableVisualiser = context.getInitParameter("EnableVisualizer");
                if (enableVisualiser.equalsIgnoreCase("TRUE")) {
                    rm.setVisualiserEnabled(true);
                }

                // now that we have all the settings, complete the init
                rm.finaliseInitialisation() ;

                // and then generate random test data if required
                String randomOrgData = context.getInitParameter("GenerateRandomOrgData");
                int generateOrgDataCount = -1;
                try {
                    generateOrgDataCount = Integer.parseInt(randomOrgData);
                }
                catch (Exception e) {
                    _log.warn("ResourceGateway: Invalid integer value in web.xml" +
                              " for GenerateRandomOrgData; value '" +
                               generateOrgDataCount + "' will be ignored.");
                }
                if (generateOrgDataCount > 0)
                    rm.initRandomOrgDataGeneration(generateOrgDataCount);
            }
            catch (Exception e) {
                _log.error("Gateway Initialisation Exception", e);
            }
            finally {
                ResourceManager.setServiceInitialised();
            }
        }
   }



   public void doPost(HttpServletRequest req, HttpServletResponse res)
                               throws IOException {

       String result = "";

       String action = req.getParameter("action");
       String handle = req.getParameter("sessionHandle");

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
       else if (action.equalsIgnoreCase("connect")) {
           String userid = req.getParameter("userid");
           String password = req.getParameter("password");
           result = rm.serviceConnect(userid, password);
       }
       else if (action.equalsIgnoreCase("checkConnection")) {
            result = String.valueOf(rm.checkServiceConnection(handle)) ;
       }
       else if (rm.checkServiceConnection(handle)) {
           if (action.equalsIgnoreCase("getResourceConstraints")) {
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
           else if (action.equalsIgnoreCase("getParticipant")) {
               String id = req.getParameter("id");
               result = rm.getParticipant(id).toXML();
           }
           else if (action.equalsIgnoreCase("getParticipantRoles")) {
               String id = req.getParameter("id");
               result = rm.getParticipantRolesAsXML(id);
           }
           else if (action.equalsIgnoreCase("getParticipantCapabilities")) {
               String id = req.getParameter("id");
               result = rm.getParticipantCapabilitiesAsXML(id);
           }
           else if (action.equalsIgnoreCase("getParticipantPositions")) {
               String id = req.getParameter("id");
               result = rm.getParticipantPositionsAsXML(id);
           }
           else if (action.equalsIgnoreCase("getParticipantsWithRole")) {
               String name = req.getParameter("name");
               result = rm.getParticpantsWithRoleAsXML(name);
           }
           else if (action.equalsIgnoreCase("getParticipantsWithPosition")) {
               String name = req.getParameter("name");
               result = rm.getParticpantsWithPositionAsXML(name);
           }
           else if (action.equalsIgnoreCase("getParticipantsWithCapability")) {
               String name = req.getParameter("name");
               result = rm.getParticpantsWithCapabilityAsXML(name);
           }
           else if (action.equalsIgnoreCase("getActiveParticipants")) {
               result = rm.getActiveParticipantsAsXML();
           }
           else if (action.equalsIgnoreCase("getCodelets")) {
               result = rm.getCodeletsAsXML();
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
           else if (action.equalsIgnoreCase("refreshOrgDataSet")) {
               rm.loadResources();
           }
           else if (action.equalsIgnoreCase("resetOrgDataRefreshRate")) {
               String rate = req.getParameter("rate");
               rm.startOrgDataRefreshTimer(Long.parseLong(rate));
           }
           else if (action.equalsIgnoreCase("disconnect")) {
               rm.serviceDisconnect(handle);
           }
       }
       else throw new IOException("Invalid or disconnected session handle");

       // generate the output
       OutputStreamWriter outputWriter = ServletUtils.prepareResponse(res);
       ServletUtils.finalizeResponse(outputWriter, result);
    }



    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        doPost(req, res);                                // redirect all GETs to POSTs
    }
}
