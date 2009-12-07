package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.resource.*;
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
 *  Service and the external world for resource (org data) maintenance. It also
 * initialises the service with values from 'web.xml'.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  13/08/2007
 *
 */

public class ResourceGateway extends HttpServlet {

    private ResourceManager rm = ResourceManager.getInstance() ;
    private ResourceDataSet orgDataSet = rm.getOrgDataSet();
    private static final String SUCCESS = "<success/>";
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

                // for non-default org data sources, check the allow mods value
                if (! orgDataSource.equals("HibernateImpl")) {
                    String allowMods = context.getInitParameter("AllowExternalOrgDataMods");
                    rm.setAllowExternalOrgDataMods(allowMods.equalsIgnoreCase("TRUE"));
                }

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


    public void destroy() {
        ResourceManager.getInstance().shutdown();
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
           if (action.startsWith("get")) {
               result = doGetResourceAction(req, action);
           }
           else if (action.startsWith("set")) {
               result = doSetResourceAction(req, action);
           }
           else if (action.startsWith("isKnown")) {
               result = doIsKnownResourceAction(req, action);
           }
           else if (action.startsWith("add")) {
               result = doAddResourceAction(req, action);
           }
           else if (action.startsWith("update")) {
               result = doUpdateResourceAction(req, action);
           }
           else if (action.startsWith("remove")) {
               result = doRemoveResourceAction(req, action);
           }
           else if (action.equalsIgnoreCase("disconnect")) {
               rm.serviceDisconnect(handle);
           }
           else if (action.equalsIgnoreCase("refreshOrgDataSet")) {
               rm.loadResources();
           }
           else if (action.equalsIgnoreCase("resetOrgDataRefreshRate")) {
               String rate = req.getParameter("rate");
               rm.startOrgDataRefreshTimer(Long.parseLong(rate));
           }
           else {
               result = fail("Unrecognised action: " + action);
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


    public String doAddResourceAction(HttpServletRequest req, String action) {
        String result = "";
        if (action.equalsIgnoreCase("addParticipant")) {
            String userid = req.getParameter("userid");
            if ((userid != null) && (! rm.isKnownUserID(userid))) {
                String lastName = req.getParameter("lastname");
                String firstName = req.getParameter("firstname");
                Participant p = new Participant(lastName, firstName, userid);
                p.setAdministrator(req.getParameter("admin").equalsIgnoreCase("true"));
                String encrypt = req.getParameter("encrypt");
                if (encrypt != null) {
                    p.setPassword(req.getParameter("password"),
                            encrypt.equalsIgnoreCase("true"));
                }
                else {
                    p.setPassword(req.getParameter("password"));
                }
                p.setDescription(req.getParameter("description"));
                p.setNotes(req.getParameter("notes"));
                result = rm.addParticipant(p);
            }
            else result = fail("Add", "Participant", userid);
        }
        else if (action.equalsIgnoreCase("addCapability")) {
            String name = req.getParameter("name");
            if ((name != null) && (! orgDataSet.isKnownCapabilityName(name))) {
                Capability cap = new Capability(name, null);
                updateCommonFields(cap, req);
                result = orgDataSet.addCapability(cap);
            }
            else result = fail("Add", "Capability", name);
        }
        else if (action.equalsIgnoreCase("addRole")) {
            String name = req.getParameter("name");
            if ((name != null) && (! orgDataSet.isKnownRoleName(name))) {
                Role role = new Role(name);
                updateCommonFields(role, req);
                role.setOwnerRole(req.getParameter("containingroleid"));
                result = orgDataSet.addRole(role);
            }
            else result = fail("Add", "Role", name);
        }
        else if (action.equalsIgnoreCase("addPosition")) {
            String name = req.getParameter("name");
            if ((name != null) && (! orgDataSet.isKnownPositionName(name))) {
                Position position = new Position(name);
                updateCommonFields(position, req);
                position.setPositionID(req.getParameter("positionid"));
                position.setReportsTo(req.getParameter("containingpositionid"));
                position.setOrgGroup(req.getParameter("orggroupid"));
                result = orgDataSet.addPosition(position);
            }
            else result = fail("Add", "Position", name);
        }
        else if (action.equalsIgnoreCase("addOrgGroup")) {
            String name = req.getParameter("name");
            if ((name != null) && (! orgDataSet.isKnownOrgGroupName(name))) {
                OrgGroup orgGroup = new OrgGroup();
                orgGroup.setGroupName(name);
                orgGroup.setGroupType(req.getParameter("grouptype"));
                updateCommonFields(orgGroup, req);
                orgGroup.setBelongsTo(req.getParameter("containinggroupid"));
                result = orgDataSet.addOrgGroup(orgGroup);
            }
            else result = fail("Add", "OrgGroup", name);
        }
        else if (action.equalsIgnoreCase("addParticipantToRole")) {
            result = addParticipantToResource(req, "role");
        }
        else if (action.equalsIgnoreCase("addParticipantToCapability")) {
            result = addParticipantToResource(req, "capability");
        }
        else if (action.equalsIgnoreCase("addParticipantToPosition")) {
            result = addParticipantToResource(req, "position");
        }
        return result;
    }
                                         

    public String doUpdateResourceAction(HttpServletRequest req, String action) {
        String result = SUCCESS;
        if (action.equalsIgnoreCase("updateParticipant")) {
            String pid = req.getParameter("participantid");
            if (pid != null) {
                Participant p = orgDataSet.getParticipant(pid);
                if (p != null) {
                    String userid = req.getParameter("userid");
                    if (userid != null) p.setUserID(userid);
                    String lastName = req.getParameter("lastname");
                    if (lastName != null) p.setLastName(lastName);
                    String firstName = req.getParameter("firstname");
                    if (firstName != null) p.setFirstName(firstName);
                    String admin = req.getParameter("admin");
                    if (admin != null) p.setAdministrator(admin.equalsIgnoreCase("true"));
                    String password = req.getParameter("password");
                    if (password != null) {
                        String encrypt = req.getParameter("encrypt");
                        if (encrypt != null) {
                            p.setPassword(password, encrypt.equalsIgnoreCase("true"));
                        }
                        else {
                            p.setPassword(password);
                        }
                    }
                    String desc = req.getParameter("description");
                    if (desc != null) p.setDescription(desc);
                    String notes = req.getParameter("notes");
                    if (notes != null) p.setNotes(notes);
                    p.save();
                }
                else result = fail("participant", pid);
            }
            else result = fail("participant", null);
        }
        else if (action.equalsIgnoreCase("updateCapability")) {
            String cid = req.getParameter("capabilityid");
            if (cid != null) {
                Capability cap = orgDataSet.getCapability(cid);
                if (cap != null) {
                    updateCommonFields(cap, req);
                    String name = req.getParameter("capability");
                    if (name != null) cap.setCapability(name);
                    cap.save();
                }
                else result = fail("capability", cid);
            }
            else result = fail("capability", null);
        }
        else if (action.equalsIgnoreCase("updateRole")) {
            String rid = req.getParameter("roleid");
            if (rid != null) {
                Role role = orgDataSet.getRole(rid);
                if (role != null) {
                    updateCommonFields(role, req);
                    String name = req.getParameter("name");
                    if (name != null) role.setName(name);
                    String ownerID = req.getParameter("containingroleid");
                    if (ownerID != null) role.setOwnerRole(ownerID);
                    role.save();
                }
                else result = fail("role", rid);
            }
            else result = fail("role", null);
        }
        else if (action.equalsIgnoreCase("updatePosition")) {
            String pid = req.getParameter("posid");
            if (pid != null) {
                Position position = orgDataSet.getPosition(pid);
                if (position != null) {
                    updateCommonFields(position, req);
                    String name = req.getParameter("title");
                    if (name != null) position.setTitle(name);
                    String positionID = req.getParameter("positionid") ;
                    if (positionID != null) position.setPositionID(positionID);
                    String reportsTo = req.getParameter("containingpositionid");
                    if (reportsTo != null) position.setReportsTo(reportsTo);
                    String orgGroupID = req.getParameter("orggroupid") ;
                    if (orgGroupID != null) position.setOrgGroup(orgGroupID);
                    position.save();
                }
                else result = fail("position", pid);
            }
            else result = fail("position", null);
        }
        else if (action.equalsIgnoreCase("updateOrgGroup")) {
            String oid = req.getParameter("groupid");
            if (oid != null) {
                OrgGroup orgGroup = orgDataSet.getOrgGroup(oid);
                if (orgGroup != null) {
                    updateCommonFields(orgGroup, req);
                    String name = req.getParameter("name");
                    if (name != null) orgGroup.setGroupName(name);
                    String groupType = req.getParameter("grouptype");
                    if (groupType != null) orgGroup.setGroupType(groupType);
                    String ownerID = req.getParameter("containinggroupid");
                    if (ownerID != null) orgGroup.setBelongsTo(ownerID);
                    orgGroup.save();
                }
                else result = fail("org group", oid);
            }
            else result = fail("org group", null);
        }
        return result;
    }

    
    public String doRemoveResourceAction(HttpServletRequest req, String action) {
        String result = SUCCESS;
        if (action.equalsIgnoreCase("removeParticipant")) {
            if (! rm.removeParticipant(req.getParameter("participantid"))) {
                result = fail("participant", null);
            }
        }
        else if (action.equalsIgnoreCase("removeCapability")) {
            if (! orgDataSet.removeCapability(req.getParameter("capabilityid"))) {
                result = fail("capability", null);
            }
        }
        else if (action.equalsIgnoreCase("removeRole")) {
            if (! orgDataSet.removeRole(req.getParameter("roleid"))) {
                result = fail("role", null);
            }
        }
        else if (action.equalsIgnoreCase("removePosition")) {
            if (! orgDataSet.removePosition(req.getParameter("positionid"))) {
                result = fail("position", null);
            }
        }
        else if (action.equalsIgnoreCase("removeOrgGroup")) {
            if (! orgDataSet.removeOrgGroup(req.getParameter("groupid"))) {
                result = fail("org group", null);
            }
        }
        else if (action.equalsIgnoreCase("removeParticipantFromRole")) {
            result = removeParticipantFromResource(req, "role");
        }
        else if (action.equalsIgnoreCase("removeParticipantFromCapability")) {
            result = removeParticipantFromResource(req, "capability");
        }
        else if (action.equalsIgnoreCase("removeParticipantFromPosition")) {
            result = removeParticipantFromResource(req, "position");
        }
        return result;
    }

    public String doGetResourceAction(HttpServletRequest req, String action) {
        String result = "";
        String id = req.getParameter("id");
        String name = req.getParameter("name");

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
            result = orgDataSet.getParticipantsAsXML();
        }
        else if (action.equalsIgnoreCase("getRoles")) {
            result = orgDataSet.getRolesAsXML();
        }
        else if (action.equalsIgnoreCase("getCapabilities")) {
            result = orgDataSet.getCapabilitiesAsXML();
        }
        else if (action.equalsIgnoreCase("getPositions")) {
            result = orgDataSet.getPositionsAsXML();
        }
        else if (action.equalsIgnoreCase("getOrgGroups")) {
            result = orgDataSet.getOrgGroupsAsXML();
        }
        else if (action.equalsIgnoreCase("getAllParticipantNames")) {
            result = orgDataSet.getParticipantNames();
        }
        else if (action.equalsIgnoreCase("getAllRoleNames")) {
            result = orgDataSet.getRoleNames();
        }
        else if (action.equalsIgnoreCase("getParticipant")) {
            Participant p = orgDataSet.getParticipant(id);
            result = (p != null) ? p.toXML() : fail("Unknown participant id: " + id) ;
        }
        else if (action.equalsIgnoreCase("getParticipantRoles")) {
            result = orgDataSet.getParticipantRolesAsXML(id);
        }
        else if (action.equalsIgnoreCase("getParticipantCapabilities")) {
            result = orgDataSet.getParticipantCapabilitiesAsXML(id);
        }
        else if (action.equalsIgnoreCase("getParticipantPositions")) {
            result = orgDataSet.getParticipantPositionsAsXML(id);
        }
        else if (action.equalsIgnoreCase("getParticipantsWithRole")) {
            result = orgDataSet.getParticpantsWithRoleAsXML(name);
        }
        else if (action.equalsIgnoreCase("getParticipantsWithPosition")) {
            result = orgDataSet.getParticpantsWithPositionAsXML(name);
        }
        else if (action.equalsIgnoreCase("getParticipantsWithCapability")) {
            result = orgDataSet.getParticpantsWithCapabilityAsXML(name);
        }
        else if (action.equalsIgnoreCase("getActiveParticipants")) {
            result = rm.getActiveParticipantsAsXML();
        }
        else if (action.equalsIgnoreCase("getCodelets")) {
            result = rm.getCodeletsAsXML();
        }
        else if (action.equalsIgnoreCase("getParticipantFromUserID")) {
            Participant p = rm.getParticipantFromUserID(id);
            result = (p != null) ? p.toXML() : fail("Unknown userid: " + id) ;
        }
        else if (action.equalsIgnoreCase("getRole")) {
            Role role = orgDataSet.getRole(id);
            result = (role != null) ? role.toXML() : fail("Unknown role id: " + id) ; 
        }
        else if (action.equalsIgnoreCase("getRoleByName")) {
            Role role = orgDataSet.getRoleByName(name);
            result = (role != null) ? role.toXML() : fail("Unknown role name: " + id) ;
        }
        else if (action.equalsIgnoreCase("getCapability")) {
            Capability capability = orgDataSet.getCapability(id);
            result = (capability != null) ? capability.toXML()
                                          : fail("Unknown capability id: " + id) ;
        }
        else if (action.equalsIgnoreCase("getCapabilityByName")) {
            Capability capability = orgDataSet.getCapabilityByLabel(name);
            result = (capability != null) ? capability.toXML()
                                          : fail("Unknown capability name: " + id) ;
        }
        else if (action.equalsIgnoreCase("getPosition")) {
            Position position = orgDataSet.getPosition(id);
            result = (position != null) ? position.toXML()
                                        : fail("Unknown position id: " + id) ;
        }
        else if (action.equalsIgnoreCase("getPositionByName")) {
            Position position = orgDataSet.getPositionByLabel(name);
            result = (position != null) ? position.toXML()
                                        : fail("Unknown position name: " + id) ;
        }
        else if (action.equalsIgnoreCase("getOrgGroup")) {
            OrgGroup group = orgDataSet.getOrgGroup(id);
            result = (group != null) ? group.toXML() : fail("Unknown group id: " + id);
        }
        else if (action.equalsIgnoreCase("getOrgGroupByName")) {
            OrgGroup group = orgDataSet.getOrgGroupByLabel(name);
            result = (group != null) ? group.toXML() : fail("Unknown group name: " + id);
        }
        else if (action.equals("getUserPrivileges")) {
            Participant p = orgDataSet.getParticipant(id);
            if (p != null) {
                UserPrivileges up = p.getUserPrivileges();
                result = (up != null) ? up.toXML() :
                          fail("No privileges available for participant id: " + id);
            }
            else result = fail("Unknown participant id: " + id);
        }
        return result;
    }


    public String doSetResourceAction(HttpServletRequest req, String action) {
        String result = "";
        if (action.equalsIgnoreCase("setContainingRole")) {
            String roleID = req.getParameter("roleid");
            Role role = orgDataSet.getRole(roleID);
            if (role != null) {
                String ownerID = req.getParameter("containingroleid");
                if (role.setOwnerRole(ownerID)) {
                    role.save();
                    result = SUCCESS;
                }
                else result = fail("containing role", ownerID);
            }
            else result = fail("role", roleID);
        }
        else if (action.equalsIgnoreCase("setContainingOrgGroup")) {
            String groupID = req.getParameter("groupid");
            OrgGroup orgGroup = orgDataSet.getOrgGroup(groupID);
            if (orgGroup != null) {
                String ownerID = req.getParameter("containinggroupid");
                if (orgGroup.setBelongsTo(ownerID)) {
                    orgGroup.save();
                    result = SUCCESS;
                }
                else result = fail("containing org group", ownerID);
            }
            else result = fail("org group", groupID);
        }
        else if (action.equalsIgnoreCase("setContainingPosition")) {
            String posID = req.getParameter("positionid");
            Position position = orgDataSet.getPosition(posID);
            if (position != null) {
                String ownerID = req.getParameter("containingpositionid");
                if (position.setReportsTo(ownerID)) {
                    position.save();
                    result = SUCCESS;
                }
                else result = fail("containing position", ownerID);
            }
            else result = fail("position", posID);
        }
        else if (action.equalsIgnoreCase("setPositionOrgGroup")) {
            String posID = req.getParameter("positionid");
            Position position = orgDataSet.getPosition(posID);
            if (position != null) {
                String groupID = req.getParameter("groupid");
                if (position.setOrgGroup(groupID)) {
                    position.save();
                    result = SUCCESS;
                }
                else result = fail("org group", groupID);
            }
            else result = fail("position", posID);
        }
        else if (action.equalsIgnoreCase("setParticipantPrivileges")) {
            String pid = req.getParameter("participantid");
            if (pid != null) {
                Participant p = orgDataSet.getParticipant(pid);
                if (p != null) {
                    String bits = req.getParameter("bitstring");
                    if (bits != null) {
                        UserPrivileges privs = new UserPrivileges(pid);
                        privs.setPrivilegesFromBits(bits);
                        p.setUserPrivileges(privs);
                        result = SUCCESS;
                    }
                    else result = fail("No privileges received");
                }
                else result = fail("No participant found with id: " + pid);
            }
            else result = fail("Null participant id");
        }
        return result;
    }


    public String doIsKnownResourceAction(HttpServletRequest req, String action) {
        String result = "";
        String id = req.getParameter("id");
        if (id != null) {
            if (action.equalsIgnoreCase("isKnownParticipant")) {
                result = String.valueOf(orgDataSet.isKnownParticipant(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownRole")) {
                result = String.valueOf(orgDataSet.isKnownRole(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownCapability")) {
                result = String.valueOf(orgDataSet.isKnownCapability(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownPosition")) {
                result = String.valueOf(orgDataSet.isKnownPosition(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownOrgGroup")) {
                result = String.valueOf(orgDataSet.isKnownOrgGroup(id)) ;
            }
        }
        else {
            result = fail("Invalid ID: null");
        }
        return result;
    }


    /*********************************/
    
    private void updateCommonFields(AbstractResourceAttribute resource,
                                   HttpServletRequest req) {
        String desc = req.getParameter("description");
        if (desc != null) resource.setDescription(desc);
        String notes = req.getParameter("notes");
        if (notes != null) resource.setNotes(notes);
    }


    private String addParticipantToResource(HttpServletRequest req, String attributeType) {
        String result = SUCCESS;
        String pid = req.getParameter("participantid");
        if (pid != null) {
            Participant p = orgDataSet.getParticipant(pid);
            if (p != null) {
                if (attributeType.equals("capability"))
                    p.addCapability(req.getParameter("capabilityid"));
                else if (attributeType.equals("role"))
                    p.addRole(req.getParameter("roleid"));
                else if (attributeType.equals("position"))
                    p.addPosition(req.getParameter("positionid"));

                p.save();
            }
            else result = fail("participant", pid);
        }
        else result = fail("participant", null);

        return result;
    }


    private String removeParticipantFromResource(HttpServletRequest req, String attributeType) {
        String result = SUCCESS;
        String pid = req.getParameter("participantid");
        if (pid != null) {
            Participant p = orgDataSet.getParticipant(pid);
            if (p != null) {
                if (attributeType.equals("capability"))
                    p.removeCapability(req.getParameter("capabilityid"));
                else if (attributeType.equals("role"))
                    p.removeRole(req.getParameter("roleid"));
                else if (attributeType.equals("position"))
                    p.removePosition(req.getParameter("positionid"));

                p.save();
            }
            else result = fail("participant", pid);
        }
        else result = fail("participant", null);

        return result;
    }

    private String fail(String msg) {
        return "<failure>" + msg + "</failure>";
    }


    private String fail(String action, String className, String name) {
        String term = className.equals("Participant") ? "userid" : "name";
        String template = "%s %s unsuccessful: there's already a %s with %s '%s'." ;
        return fail(String.format(template, action, className, className, term, name));
    }

    
    private String fail(String name, String id) {
        String onePart = "Unrecognised or null %s id.";
        String twoPart = "Unrecognised %s id: %s";
        return (id == null) ? fail(String.format(onePart, name))
                            : fail(String.format(twoPart, name, id));
    }

}
