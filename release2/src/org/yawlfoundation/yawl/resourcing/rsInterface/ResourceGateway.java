/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.YHttpServlet;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.util.Docket;
import org.yawlfoundation.yawl.resourcing.util.PluginFactory;
import org.yawlfoundation.yawl.util.XNode;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;


/**
 *  The Resource Gateway class acts as a gateway between the Resource
 *  Service and the external world for resource (org data) maintenance. It also
 *  initialises the service with values from 'web.xml'.
 *
 *  @author Michael Adams
 *  @date 13/08/2007
 *
 */

public class ResourceGateway extends YHttpServlet {

    private ResourceManager _rm = ResourceManager.getInstance();
    private static final String SUCCESS = "<success/>";


    /** Read settings from web.xml and use them to initialise the service */
    public void init() {
        if (! ResourceManager.serviceInitialised) {
            try {
                ServletContext context = getServletContext();

                // set the actual root file path of the service
                Docket.setServiceRootDir(context.getRealPath("/")) ;

                // set the engine uri and the exception service uri (if enabled)
                _rm.getClients().initClients(context.getInitParameter("InterfaceB_BackEnd"),
                        context.getInitParameter("InterfaceX_BackEnd"),
                        context.getInitParameter("InterfaceS_BackEnd"),
                        context.getInitParameter("CostService_BackEnd"),
                        context.getInitParameter("DocStore_BackEnd"));

                // set the path to external plugin classes (if any)
                String pluginPath = context.getInitParameter("ExternalPluginsPath");
                PluginFactory.setExternalPaths(pluginPath);

                // enable/or disable persistence
                String persist = context.getInitParameter("EnablePersistence");
                _rm.setPersisting(getInitBooleanValue(persist, true));
                if (_rm.isPersisting()) {

                    // enable/disable process logging
                    String enableLogging = context.getInitParameter("EnableLogging");
                    EventLogger.setLogging(getInitBooleanValue(enableLogging, true));

                    // enable/disable logging of all offers
                    String logOffers = context.getInitParameter("LogOffers");
                    EventLogger.setOfferLogging(getInitBooleanValue(logOffers, true));
                }

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
                _rm.initOrgDataSource(orgDataSource, orgDataRefreshRate);

                // for non-default org data sources, check the allow mods &
                // user authentication values
                if (! orgDataSource.equals("HibernateImpl")) {
                    String allowMods = context.getInitParameter("AllowExternalOrgDataMods");
                    _rm.setAllowExternalOrgDataMods(getInitBooleanValue(allowMods, false));
                    String externalAuth = context.getInitParameter("ExternalUserAuthentication");
                    _rm.setExternalUserAuthentication(getInitBooleanValue(externalAuth, false));
                }

                // enable/disable blocking process when 2ndary resources unavailable
                String blockOnMissingResources =
                        context.getInitParameter("BlockOnUnavailableSecondaryResources");
                _rm.setBlockOnUnavailableSecondaryResources(
                        getInitBooleanValue(blockOnMissingResources, false)) ;

                // enable/disable the dropping of task piling on logout
                String dropPiling = context.getInitParameter("DropTaskPilingOnLogoff");
                _rm.setPersistPiling(! getInitBooleanValue(dropPiling, false)) ;

                // enable the visualiser applet, if necessary
                String enableVisualiser = context.getInitParameter("EnableVisualizer");
                if (getInitBooleanValue(enableVisualiser, false)) {
                    _rm.setVisualiserEnabled(true);
                    String visualiserSize = context.getInitParameter("VisualizerViewSize");
                    if (visualiserSize != null) {
                        _rm.setVisualiserDimension(visualiserSize);
                    }
                }

                // read the current version properties
                _rm.initBuildProperties(context.getResourceAsStream(
                        "/WEB-INF/classes/version.properties"));

                // now that we have all the settings, complete the init
                _rm.finaliseInitialisation() ;

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
                    _rm.initRandomOrgDataGeneration(generateOrgDataCount);
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
        _rm.shutdown();
        super.destroy();
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
            int interval = req.getSession().getMaxInactiveInterval();
            result = _rm.serviceConnect(userid, password, interval);
        }
        else if (action.equalsIgnoreCase("checkConnection")) {
            result = String.valueOf(_rm.checkServiceConnection(handle)) ;
        }
        else if (_rm.checkServiceConnection(handle)) {
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
                _rm.serviceDisconnect(handle);
            }
            else if (action.equalsIgnoreCase("validateUserCredentials")) {
                String userid = req.getParameter("userid");
                String password = req.getParameter("password");
                String adminStr = req.getParameter("checkForAdmin");
                boolean admin = "true".equalsIgnoreCase(adminStr);
                result = _rm.validateUserCredentials(userid, password, admin);
            }
            else if (action.equalsIgnoreCase("refreshOrgDataSet")) {
                _rm.refreshOrgData();
            }
            else if (action.equalsIgnoreCase("resetOrgDataRefreshRate")) {
                String rate = req.getParameter("rate");
                _rm.startOrgDataRefreshTimer(Long.parseLong(rate));
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
            if ((userid != null) && (! _rm.isKnownUserID(userid))) {
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
                result = _rm.addParticipant(p);
            }
            else result = fail("Add", "Participant", userid);
        }
        else if (action.equalsIgnoreCase("addNonHumanResource")) {
            String name = req.getParameter("name");
            if ((name != null) && (! getOrgDataSet().isKnownNonHumanResourceName(name))) {
                String categoryName = req.getParameter("category");
                String subcategory = req.getParameter("subcategory");
                NonHumanCategory category =
                        getOrgDataSet().getNonHumanCategoryByName(categoryName);
                if (category == null) {
                    category = new NonHumanCategory(categoryName);
                    getOrgDataSet().addNonHumanCategory(category);
                }
                NonHumanResource resource = new NonHumanResource(name, category, subcategory);
                resource.setDescription(req.getParameter("description"));
                resource.setNotes(req.getParameter("notes"));
                result = getOrgDataSet().addNonHumanResource(resource);
            }
            else result = fail("Add", "NonHumanResource", name);
        }
        else if (action.equalsIgnoreCase("addCapability")) {
            String name = req.getParameter("name");
            if ((name != null) && (! getOrgDataSet().isKnownCapabilityName(name))) {
                Capability cap = new Capability(name, null);
                updateCommonFields(cap, req);
                result = getOrgDataSet().addCapability(cap);
            }
            else result = fail("Add", "Capability", name);
        }
        else if (action.equalsIgnoreCase("addRole")) {
            String name = req.getParameter("name");
            if ((name != null) && (! getOrgDataSet().isKnownRoleName(name))) {
                Role role = new Role(name);
                updateCommonFields(role, req);
                role.setOwnerRole(req.getParameter("containingroleid"));
                result = getOrgDataSet().addRole(role);
            }
            else result = fail("Add", "Role", name);
        }
        else if (action.equalsIgnoreCase("addPosition")) {
            String name = req.getParameter("name");
            if ((name != null) && (! getOrgDataSet().isKnownPositionName(name))) {
                Position position = new Position(name);
                updateCommonFields(position, req);
                position.setPositionID(req.getParameter("positionid"));
                position.setReportsTo(req.getParameter("containingpositionid"));
                position.setOrgGroup(req.getParameter("orggroupid"));
                result = getOrgDataSet().addPosition(position);
            }
            else result = fail("Add", "Position", name);
        }
        else if (action.equalsIgnoreCase("addOrgGroup")) {
            String name = req.getParameter("name");
            if ((name != null) && (! getOrgDataSet().isKnownOrgGroupName(name))) {
                OrgGroup orgGroup = new OrgGroup();
                orgGroup.setGroupName(name);
                orgGroup.setGroupType(req.getParameter("grouptype"));
                updateCommonFields(orgGroup, req);
                orgGroup.setBelongsTo(req.getParameter("containinggroupid"));
                result = getOrgDataSet().addOrgGroup(orgGroup);
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
        else if (action.equalsIgnoreCase("addNonHumanCategory")) {
            String categoryName = req.getParameter("category");
            if (categoryName != null) {
                if (getOrgDataSet().getNonHumanCategoryByName(categoryName) == null) {
                    NonHumanCategory category = new NonHumanCategory(categoryName);
                    result = getOrgDataSet().addNonHumanCategory(category);
                }
                else result = fail("Category '" + categoryName + "' already exists");
            }
            else result = fail("Category name is null");
        }
        else if (action.equalsIgnoreCase("addNonHumanSubCategory")) {
            String categoryName = req.getParameter("category");
            String subcategory = req.getParameter("subcategory");
            boolean success = false;
            NonHumanCategory category = (categoryName != null) ?
                    getOrgDataSet().getNonHumanCategoryByName(categoryName) :
                    getOrgDataSet().getNonHumanCategory(req.getParameter("id"));
            if (category != null) {
                success = category.addSubCategory(subcategory);
                if (success) getOrgDataSet().updateNonHumanCategory(category);
            }
            result = success ? "<success/>" : fail("Subcategory '" + subcategory +
                    "' already exists OR category is invalid.");
        }
        return result;
    }


    public String doUpdateResourceAction(HttpServletRequest req, String action) {
        String result = SUCCESS;
        if (action.equalsIgnoreCase("updateParticipant")) {
            String pid = req.getParameter("participantid");
            if (pid != null) {
                Participant p = getOrgDataSet().getParticipant(pid);
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
                    try {
                        p.save();
                    }
                    catch (ResourceGatewayException rge) {
                        result = fail(rge.getMessage());
                    }
                }
                else result = fail("participant", pid);
            }
            else result = fail("participant", null);
        }
        else if (action.equalsIgnoreCase("updateNonHumanResource")) {
            String id = req.getParameter("resourceid");
            if (id != null) {
                NonHumanResource resource = getOrgDataSet().getNonHumanResource(id);
                if (resource != null) {
                    String desc = req.getParameter("description");
                    if (desc != null) resource.setDescription(desc);
                    String notes = req.getParameter("notes");
                    if (notes != null) resource.setNotes(notes);
                    String name = req.getParameter("name");
                    if (name != null) resource.setName(name);
                    String categoryName = req.getParameter("category");
                    if (categoryName != null) {
                        NonHumanCategory category =
                                getOrgDataSet().getNonHumanCategoryByName(categoryName);
                        if (category != null) {
                            resource.setCategory(category);
                            resource.setSubCategory(req.getParameter("subcategory"));
                        }
                    }
                    getOrgDataSet().updateNonHumanResource(resource);
                }
                else result = fail("NonHumanResource", id);
            }
            else result = fail("NonHumanResource", null);
        }
        else if (action.equalsIgnoreCase("updateCapability")) {
            String cid = req.getParameter("capabilityid");
            if (cid != null) {
                Capability cap = getOrgDataSet().getCapability(cid);
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
                Role role = getOrgDataSet().getRole(rid);
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
                Position position = getOrgDataSet().getPosition(pid);
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
                OrgGroup orgGroup = getOrgDataSet().getOrgGroup(oid);
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
        else if (action.equalsIgnoreCase("updateNonHumanCategory")) {
            String cid = req.getParameter("categoryid");
            if (cid != null) {
                NonHumanCategory category = getOrgDataSet().getNonHumanCategory(cid);
                if (category != null) {
                    String name = req.getParameter("name");
                    if (name != null) category.setName(name);
                    String description = req.getParameter("description");
                    if (description != null) category.setDescription(description);
                    String notes = req.getParameter("notes");
                    if (notes != null) category.setNotes(notes);
                    getOrgDataSet().updateNonHumanCategory(category);
                }
                else result = fail("non-human category", cid);
            }
            else result = fail("non-human category", null);
        }
        return result;
    }


    public String doRemoveResourceAction(HttpServletRequest req, String action) {
        String result = SUCCESS;
        if (action.equalsIgnoreCase("removeParticipant")) {
            if (! _rm.removeParticipant(req.getParameter("participantid"))) {
                result = fail("participant", null);
            }
        }
        if (action.equalsIgnoreCase("removeNonHumanResource")) {
            if (! getOrgDataSet().removeNonHumanResource(req.getParameter("resourceid"))) {
                result = fail("NonHumanResource", null);
            }
        }
        else if (action.equalsIgnoreCase("removeCapability")) {
            if (! getOrgDataSet().removeCapability(req.getParameter("capabilityid"))) {
                result = fail("capability", null);
            }
        }
        else if (action.equalsIgnoreCase("removeRole")) {
            if (! getOrgDataSet().removeRole(req.getParameter("roleid"))) {
                result = fail("role", null);
            }
        }
        else if (action.equalsIgnoreCase("removePosition")) {
            if (! getOrgDataSet().removePosition(req.getParameter("positionid"))) {
                result = fail("position", null);
            }
        }
        else if (action.equalsIgnoreCase("removeOrgGroup")) {
            if (! getOrgDataSet().removeOrgGroup(req.getParameter("groupid"))) {
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
        else if (action.equalsIgnoreCase("removeNonHumanCategory")) {
            String categoryID = req.getParameter("id");
            boolean success = false;
            if (categoryID != null) {
                success = getOrgDataSet().removeNonHumanCategory(categoryID);
            }
            result = success ? "<success/>" : fail("category", categoryID);
        }
        else if (action.equalsIgnoreCase("removeNonHumanCategoryByName")) {
            String categoryName = req.getParameter("category");
            boolean success = false;
            if (categoryName != null) {
                NonHumanCategory category =
                        getOrgDataSet().getNonHumanCategoryByName(categoryName);
                if (category != null) {
                    success = getOrgDataSet().removeNonHumanCategory(category.getID());
                }
            }
            result = success ? "<success/>" : fail("Unknown category name: " + categoryName);
        }
        else if (action.equalsIgnoreCase("removeNonHumanSubCategory")) {
            String categoryID = req.getParameter("id");
            String subcategory = req.getParameter("subcategory");
            boolean success = false;
            if (categoryID != null) {
                NonHumanCategory category = getOrgDataSet().getNonHumanCategory(categoryID);
                if (category != null) {
                    success = category.removeSubCategory(subcategory);
                    if (success) getOrgDataSet().updateNonHumanCategory(category);
                }
            }
            result = success ? "<success/>" : fail("Subcategory '" + subcategory +
                    "' not found OR category id is invalid.");
        }
        else if (action.equalsIgnoreCase("removeNonHumanSubCategoryByName")) {
            String categoryName = req.getParameter("category");
            String subcategory = req.getParameter("subcategory");
            boolean success = false;
            if (categoryName != null) {
                NonHumanCategory category =
                        getOrgDataSet().getNonHumanCategoryByName(categoryName);
                if (category != null) {
                    success = category.removeSubCategory(subcategory);
                    if (success) getOrgDataSet().updateNonHumanCategory(category);
                }
            }
            result = success ? "<success/>" : fail("Subcategory '" + subcategory +
                    "' not found OR category name is invalid.");
        }
        return result;
    }

    public String doGetResourceAction(HttpServletRequest req, String action) {
        String result = "";
        String id = req.getParameter("id");
        String name = req.getParameter("name");

        if (action.equalsIgnoreCase("getResourceConstraints")) {
            result = PluginFactory.getConstraintsAsXML() ;
        }
        else if (action.equalsIgnoreCase("getResourceFilters")) {
            result = PluginFactory.getFiltersAsXML() ;
        }
        else if (action.equalsIgnoreCase("getResourceAllocators")) {
            result = PluginFactory.getAllocatorsAsXML() ;
        }
        else if (action.equalsIgnoreCase("getAllSelectors")) {
            result = PluginFactory.getAllSelectors() ;
        }
        else if (action.equalsIgnoreCase("getParticipants")) {
            result = getOrgDataSet().getParticipantsAsXML();
        }
        else if (action.equalsIgnoreCase("getNonHumanResources")) {
            result = getOrgDataSet().getNonHumanResourcesAsXML();
        }
        else if (action.equalsIgnoreCase("getRoles")) {
            result = getOrgDataSet().getRolesAsXML();
        }
        else if (action.equalsIgnoreCase("getCapabilities")) {
            result = getOrgDataSet().getCapabilitiesAsXML();
        }
        else if (action.equalsIgnoreCase("getPositions")) {
            result = getOrgDataSet().getPositionsAsXML();
        }
        else if (action.equalsIgnoreCase("getOrgGroups")) {
            result = getOrgDataSet().getOrgGroupsAsXML();
        }
        else if (action.equalsIgnoreCase("getAllParticipantNames")) {
            result = getOrgDataSet().getParticipantNames();
        }
        else if (action.equalsIgnoreCase("getAllNonHumanResourceNames")) {
            result = getOrgDataSet().getNonHumanResourceNames();
        }
        else if (action.equalsIgnoreCase("getAllRoleNames")) {
            result = getOrgDataSet().getRoleNames();
        }
        else if (action.equalsIgnoreCase("getParticipant")) {
            Participant p = getOrgDataSet().getParticipant(id);
            result = (p != null) ? p.toXML() : fail("Unknown participant id: " + id) ;
        }
        else if (action.equalsIgnoreCase("getNonHumanResource")) {
            NonHumanResource r = getOrgDataSet().getNonHumanResource(id);
            result = (r != null) ? r.toXML() : fail("Unknown NonHumanResource id: " + id) ;
        }
        else if (action.equalsIgnoreCase("getNonHumanResourceByName")) {
            NonHumanResource r = getOrgDataSet().getNonHumanResourceByName(name);
            result = (r != null) ? r.toXML() : fail("Unknown NonHumanResource name: " + name) ;
        }
        else if (action.equalsIgnoreCase("getParticipantRoles")) {
            result = getOrgDataSet().getParticipantRolesAsXML(id);
        }
        else if (action.equalsIgnoreCase("getParticipantCapabilities")) {
            result = getOrgDataSet().getParticipantCapabilitiesAsXML(id);
        }
        else if (action.equalsIgnoreCase("getParticipantPositions")) {
            result = getOrgDataSet().getParticipantPositionsAsXML(id);
        }
        else if (action.equalsIgnoreCase("getParticipantsWithRole")) {
            result = getOrgDataSet().getParticpantsWithRoleAsXML(name);
        }
        else if (action.equalsIgnoreCase("getParticipantsWithPosition")) {
            result = getOrgDataSet().getParticpantsWithPositionAsXML(name);
        }
        else if (action.equalsIgnoreCase("getParticipantsWithCapability")) {
            result = getOrgDataSet().getParticpantsWithCapabilityAsXML(name);
        }
        else if (action.equalsIgnoreCase("getActiveParticipants")) {
            result = _rm.getActiveParticipantsAsXML();
        }
        else if (action.equalsIgnoreCase("getCodelets")) {
            result = PluginFactory.getCodeletsAsXML();
        }
        else if (action.equalsIgnoreCase("getCodeletParameters")) {
            result = PluginFactory.getCodeletParametersAsXML(name);
        }
        else if (action.equalsIgnoreCase("getParticipantFromUserID")) {
            Participant p = _rm.getParticipantFromUserID(id);
            result = (p != null) ? p.toXML() : fail("Unknown userid: " + id) ;
        }
        else if (action.equalsIgnoreCase("getRole")) {
            Role role = getOrgDataSet().getRole(id);
            result = (role != null) ? role.toXML() : fail("Unknown role id: " + id) ;
        }
        else if (action.equalsIgnoreCase("getRoleByName")) {
            Role role = getOrgDataSet().getRoleByName(name);
            result = (role != null) ? role.toXML() : fail("Unknown role name: " + id) ;
        }
        else if (action.equalsIgnoreCase("getCapability")) {
            Capability capability = getOrgDataSet().getCapability(id);
            result = (capability != null) ? capability.toXML()
                    : fail("Unknown capability id: " + id) ;
        }
        else if (action.equalsIgnoreCase("getCapabilityByName")) {
            Capability capability = getOrgDataSet().getCapabilityByLabel(name);
            result = (capability != null) ? capability.toXML()
                    : fail("Unknown capability name: " + id) ;
        }
        else if (action.equalsIgnoreCase("getPosition")) {
            Position position = getOrgDataSet().getPosition(id);
            result = (position != null) ? position.toXML()
                    : fail("Unknown position id: " + id) ;
        }
        else if (action.equalsIgnoreCase("getPositionByName")) {
            Position position = getOrgDataSet().getPositionByLabel(name);
            result = (position != null) ? position.toXML()
                    : fail("Unknown position name: " + id) ;
        }
        else if (action.equalsIgnoreCase("getOrgGroup")) {
            OrgGroup group = getOrgDataSet().getOrgGroup(id);
            result = (group != null) ? group.toXML() : fail("Unknown group id: " + id);
        }
        else if (action.equalsIgnoreCase("getOrgGroupByName")) {
            OrgGroup group = getOrgDataSet().getOrgGroupByLabel(name);
            result = (group != null) ? group.toXML() : fail("Unknown group name: " + id);
        }
        else if (action.equalsIgnoreCase("getNonHumanCategories")) {
            String format = req.getParameter("format");
            if ((format != null) && format.equals("JSON")) {
                String callback = req.getParameter("callback");
                result = stringMapToJSON(
                        getOrgDataSet().getNonHumanCategoryIdentifiers(), callback);
            }
            else result = getOrgDataSet().getNonHumanCategoriesAsXML();
        }
        else if (action.equalsIgnoreCase("getNonHumanSubCategories")) {
            NonHumanCategory category = getOrgDataSet().getNonHumanCategory(id);
            if (category != null) {
                String format = req.getParameter("format");
                if ((format != null) && format.equals("JSON")) {
                    String callback = req.getParameter("callback");
                    result = stringSetToJSON(category.getSubCategoryNames(), callback);
                }
                else result = getOrgDataSet().getNonHumanSubCategoriesAsXML(category.getID());
            }
            else result = fail("Unknown category id: " + id);
        }
        else if (action.equalsIgnoreCase("getNonHumanSubCategoriesByName")) {
            String categoryName = req.getParameter("category");
            NonHumanCategory category = getOrgDataSet().getNonHumanCategoryByName(categoryName);
            if (category != null) {
                String format = req.getParameter("format");
                if ((format != null) && format.equals("JSON")) {
                    String callback = req.getParameter("callback");
                    result = stringSetToJSON(category.getSubCategoryNames(), callback);
                }
                else result = getOrgDataSet().getNonHumanSubCategoriesAsXML(category.getID());
            }
            else result = fail("Unknown category name: " + categoryName);
        }
        else if (action.equalsIgnoreCase("getNonHumanCategoryByName")) {
            NonHumanCategory category = getOrgDataSet().getNonHumanCategoryByName(name);
            result = (category != null) ? category.toXML() : fail("Unknown category name: " + name);
        }
        else if (action.equalsIgnoreCase("getNonHumanCategory")) {
            NonHumanCategory category = getOrgDataSet().getNonHumanCategory(id);
            result = (category != null) ? category.toXML() : fail("Unknown category id: " + id);
        }
        else if (action.equalsIgnoreCase("getNonHumanCategorySet")) {
            result = getOrgDataSet().getNonHumanCategorySet();
        }
        else if (action.equalsIgnoreCase("getReferencedParticipantIDsAsXML")) {
            result = getOrgDataSet().resolveParticipantIdsAsXML(id);
        }
        else if (action.equalsIgnoreCase("getParticipantIdentifiers")) {
            if (id == null) id = "0";
            result = reformatMap(getOrgDataSet().getParticipantIdentifiers(id), req);
        }
        else if (action.equalsIgnoreCase("getNonHumanResourceIdentifiers")) {
            result = reformatMap(getOrgDataSet().getNonHumanResourceIdentifiers(), req);
        }
        else if (action.equalsIgnoreCase("getRoleIdentifiers")) {
            result = reformatMap(getOrgDataSet().getRoleIdentifiers(), req);
        }
        else if (action.equalsIgnoreCase("getPositionIdentifiers")) {
            result = reformatMap(getOrgDataSet().getPositionIdentifiers(), req);
        }
        else if (action.equalsIgnoreCase("getCapabilityIdentifiers")) {
            result = reformatMap(getOrgDataSet().getCapabilityIdentifiers(), req);
        }
        else if (action.equalsIgnoreCase("getOrgGroupIdentifiers")) {
            result = reformatMap(getOrgDataSet().getOrgGroupIdentifiers(), req);
        }
        else if (action.equals("getUserPrivileges")) {
            Participant p = getOrgDataSet().getParticipant(id);
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
            Role role = getOrgDataSet().getRole(roleID);
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
            OrgGroup orgGroup = getOrgDataSet().getOrgGroup(groupID);
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
            Position position = getOrgDataSet().getPosition(posID);
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
            Position position = getOrgDataSet().getPosition(posID);
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
                Participant p = getOrgDataSet().getParticipant(pid);
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
                result = String.valueOf(getOrgDataSet().isKnownParticipant(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownNonHumanResource")) {
                result = String.valueOf(getOrgDataSet().isKnownNonHumanResource(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownRole")) {
                result = String.valueOf(getOrgDataSet().isKnownRole(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownCapability")) {
                result = String.valueOf(getOrgDataSet().isKnownCapability(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownPosition")) {
                result = String.valueOf(getOrgDataSet().isKnownPosition(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownOrgGroup")) {
                result = String.valueOf(getOrgDataSet().isKnownOrgGroup(id)) ;
            }
            else if (action.equalsIgnoreCase("isKnownNonHumanCategory")) {
                result = String.valueOf(getOrgDataSet().isKnownNonHumanCategory(id)) ;
            }
        }
        else result = fail("Invalid ID: null");
        return result;
    }


    /*********************************/


    private ResourceDataSet getOrgDataSet() {
        while (_rm.isOrgDataRefreshing()) {
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException ie) {
                // deliberately do nothing
            }
        }
        return _rm.getOrgDataSet();
    }

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
            Participant p = getOrgDataSet().getParticipant(pid);
            if (p != null) {
                try {
                    if (attributeType.equals("capability"))
                        p.addCapability(req.getParameter("capabilityid"));
                    else if (attributeType.equals("role"))
                        p.addRole(req.getParameter("roleid"));
                    else if (attributeType.equals("position"))
                        p.addPosition(req.getParameter("positionid"));

                    p.save();
                }
                catch (ResourceGatewayException rge) {
                    result = fail(rge.getMessage());
                }
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
            Participant p = getOrgDataSet().getParticipant(pid);
            if (p != null) {
                if (attributeType.equals("capability"))
                    p.removeCapability(req.getParameter("capabilityid"));
                else if (attributeType.equals("role"))
                    p.removeRole(req.getParameter("roleid"));
                else if (attributeType.equals("position"))
                    p.removePosition(req.getParameter("positionid"));
                try {
                    p.save();
                }
                catch (ResourceGatewayException rge) {
                    result = rge.getMessage();
                }
            }
            else result = fail("participant", pid);
        }
        else result = fail("participant", null);

        return result;
    }


    private String fail(String action, String className, String name) {
        String term = className.equals("Participant") ? "userid" : "name";
        String template = "%s %s unsuccessful: there's already a %s with %s '%s'." ;
        return fail(String.format(template, action, className, className, term, name));
    }


    private String fail(String name, String id) {
        return (id == null) ? fail(String.format("Unrecognised or null %s id.", name))
                : fail(String.format("Unrecognised %s id: %s", name, id));
    }


    private String reformatMap(Map<String, String> map, HttpServletRequest req) {
        String format = req.getParameter("format");
        if ((format != null) && format.equals("JSON")) {
            String callback = req.getParameter("callback");
            return stringMapToJSON(map, callback);
        }
        else {
            return stringMapToXML(map);
        }
    }

    private String stringMapToXML(Map<String, String> map) {
        if (map != null) {
            XNode node = new XNode("map");
            for (String key : map.keySet()) {
                XNode child = node.addChild("item", map.get(key));
                child.addAttribute("id", key);
            }
            return node.toString();
        }
        return fail("No values returned.");
    }


    private String stringMapToJSON(Map<String, String> map, String callback) {
        String s = "{";
        if (map != null) {
            for (String key : map.keySet()) {
                if (s.length() > 1) s += ",";
                s += jsonPair(key, map.get(key));
            }
        }
        s += "}";
        return (callback != null) ? String.format("%s(%s)", callback, s) : s ;
    }


    private String stringSetToJSON(Collection<String> set, String callback) {
        String s = "{";
        if (set != null) {
            for (String item : set) {
                if (s.length() > 1) s += ",";
                s += jsonPair(item, item);
            }
        }
        s += "}";
        return (callback != null) ? String.format("%s(%s)", callback, s) : s ;
    }


    private String jsonPair(String key, String value) {
        return String.format("\"%s\":\"%s\"", key, value);
    }


    private long debug(long start, String... msgs) {
        long now = System.currentTimeMillis();
        for (String msg : msgs) {
            System.out.println(msg + "; Elapsed (msecs): " + (now - start));
        }
        return now;
    }

}
