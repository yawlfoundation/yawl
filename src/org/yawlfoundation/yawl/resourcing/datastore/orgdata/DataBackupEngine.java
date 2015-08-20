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

package org.yawlfoundation.yawl.resourcing.datastore.orgdata;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanSubCategory;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 14/11/2008, 08/11/10 (non-human resources)
 */
public class DataBackupEngine {

    ResourceManager _rm = ResourceManager.getInstance();
    ResourceDataSet orgDataSet = _rm.getOrgDataSet();
    Logger _log = LogManager.getLogger(this.getClass());

    public String exportOrgData() {
        StringBuilder result = new StringBuilder("<orgdata>");
        result.append(exportParticipants());
        result.append(exportRoles());
        result.append(exportPositions());
        result.append(exportCapabilities());
        result.append(exportOrgGroups());
        result.append(exportNonHumanResources());
        result.append(exportNonHumanCategories());
        result.append("</orgdata>");
        XNode node = new XNodeParser().parse(result.toString());
        return node.toPrettyString(true);
    }

    public List<String> importOrgData(String xml) {
        List<String> msgList = new ArrayList<String>();
        Document doc = JDOMUtil.stringToDocument(xml);
        if (doc != null) {
            Element root = doc.getRootElement();
            if (root != null) {
                msgList.add(importCapabilities(root.getChild("capabilities")));
                msgList.add(importRoles(root.getChild("roles")));
                msgList.add(importOrgGroups(root.getChild("orggroups")));
                msgList.add(importPositions(root.getChild("positions")));
                msgList.add(importParticipants(root.getChild("participants")));
                msgList.add(importNonHumanCategories(root.getChild("nonhumancategories")));
                msgList.add(importNonHumanResources(root.getChild("nonhumanresources")));
            }
        }
        if (msgList.isEmpty()) msgList.add("Invalid YAWL Org Data Export file.");

        return msgList;
    }

    /*************************************************************************/


    private String exportParticipants() {
        Set<Participant> pSet = orgDataSet.getParticipants();
        if (pSet != null) {
            StringBuilder xml = new StringBuilder("<participants>");
            for (Participant p : pSet) {
                xml.append(String.format("<participant id=\"%s\">", p.getID())) ;
                xml.append(StringUtil.wrapEscaped(p.getUserID(), "userid"));
                xml.append(StringUtil.wrapEscaped(p.getPassword(), "password")) ;
                xml.append(StringUtil.wrapEscaped(p.getFirstName(), "firstname"));
                xml.append(StringUtil.wrapEscaped(p.getLastName(), "lastname"));
                xml.append(StringUtil.wrapEscaped(p.getDescription(), "description"));
                xml.append(StringUtil.wrapEscaped(p.getNotes(), "notes"));

                xml.append(StringUtil.wrapEscaped(String.valueOf(p.isAdministrator()),
                        "isAdministrator")) ;

                xml.append("<roles>");
                for (Role role : p.getRoles())
                    xml.append(StringUtil.wrap(role.getID(), "role")) ;
                xml.append("</roles>");

                xml.append("<positions>");
                for (Position position : p.getPositions())
                    xml.append(StringUtil.wrap(position.getID(), "position")) ;
                xml.append("</positions>");

                xml.append("<capabilities>");
                for (Capability capability : p.getCapabilities())
                    xml.append(StringUtil.wrap(capability.getID(), "capability")) ;
                xml.append("</capabilities>");

                xml.append("<privileges>")
                   .append(p.getUserPrivileges().getPrivilegesAsBits())
                   .append("</privileges>");

                xml.append("</participant>");
            }
            xml.append("</participants>");
            return xml.toString();
        }
        return "";
    }


    private String exportRoles() { return orgDataSet.getRolesAsXML(); }

    private String exportPositions() { return orgDataSet.getPositionsAsXML(); }

    private String exportCapabilities() { return orgDataSet.getCapabilitiesAsXML(); }

    private String exportOrgGroups() { return orgDataSet.getOrgGroupsAsXML(); }


    private String exportNonHumanResources() {
        XNode top = new XNode("nonhumanresources");
        for (NonHumanResource n : orgDataSet.getNonHumanResources()) {
            XNode resource = top.addChild("nonhumanresource");
            resource.addAttribute("id", n.getID());
            resource.addChild("name", n.getName(), true);
            if (n.getDescription() != null) {
                resource.addChild("description", n.getDescription(), true);
            }
            if (n.getNotes() != null) resource.addChild("notes", n.getNotes(), true);
            XNode category = resource.addChild("category");
            category.addAttribute("id", n.getCategory().getID());
            String subcat = n.getSubCategoryName();
            if (! StringUtil.isNullOrEmpty(subcat)) {
                resource.addChild("subcategory", subcat, true);
            }
        }
        return top.toString();
    }


    private String exportNonHumanCategories() {
        XNode top = new XNode("nonhumancategories");
        for (NonHumanCategory category : orgDataSet.getNonHumanCategories()) {
            XNode catNode = top.addChild("nonhumancategory");
            catNode.addAttribute("id", category.getID());
            catNode.addChild("name", category.getName(), true);
            if (category.getDescription() != null) {
                catNode.addChild("description", category.getDescription(), true);
            }
            if (category.getNotes() != null) {
                catNode.addChild("notes", category.getNotes(), true);
            }
            XNode subCatNode = catNode.addChild("subcategories");
            for (NonHumanSubCategory subCategory : category.getSubCategories()) {
                subCatNode.addChild("name", subCategory.getName(), true);
            }
        }
        return top.toString();
    }


    /************************************************************************/

    private String importCapabilities(Element capElem) {
        String result = "Capabilities: 0 in imported file.";
        if (capElem != null) {
            if (orgDataSet.isDataEditable("Capability")) {
                int added = 0;
                List<Element> capList = capElem.getChildren();
                for (Element cap : capList) {
                    String id = cap.getAttributeValue("id");
                    Capability c = orgDataSet.getCapability(id);
                    if ((c == null) && (! orgDataSet.isKnownCapabilityName(cap.getChildText("name")))) {
                        c = new Capability();
                        c.reconstitute(cap);
                        orgDataSet.importCapability(c);
                        added++;
                    }
                }
                result = String.format("Capabilities: %d/%d imported.", added, capList.size());
            }
            else {
                result = "Capabilities: could not import, external dataset is read-only.";
            }
        }
        return result;
    }


    private String importNonHumanCategories(Element nhcElem) {
        String result = "NonHumanCategories: 0 in imported file.";
        if (nhcElem != null) {
            if (orgDataSet.isDataEditable("NonHumanCategory")) {
                int added = 0;
                List<Element> children = nhcElem.getChildren();
                for (Element nhc : children) {
                    String id = nhc.getAttributeValue("id");
                    String name = nhc.getChildText("name");
                    NonHumanCategory c = orgDataSet.getNonHumanCategory(id);
                    if ((c == null) && (! orgDataSet.isKnownNonHumanCategoryName(name))) {
                        c = new NonHumanCategory(name);
                        c.setID(id);
                        c.setDescription(nhc.getChildText("description"));
                        c.setNotes(nhc.getChildText("notes"));
                        for (Element eSubCat : nhc.getChild("subcategories").getChildren()) {
                            String subcat = eSubCat.getText();
                            if (! StringUtil.isNullOrEmpty(subcat)) {
                                c.addSubCategory(subcat);
                            }
                        }
                        orgDataSet.importNonHumanCategory(c);
                        added++;
                    }
                }
                result = String.format("NonHumanCategories: %d/%d imported.", added, children.size());
            }
            else {
                result = "NonHumanCategories: could not import, external dataset is read-only.";
            }
        }
        return result;
    }


    private String importNonHumanResources(Element nhrElem) {
        String result = "NonHumanResources: 0 in imported file.";
        if (nhrElem != null) {
            if (orgDataSet.isDataEditable("NonHumanResource")) {
                int added = 0;
                List<Element> children = nhrElem.getChildren();
                for (Element nhr : children) {
                    String id = nhr.getAttributeValue("id");
                    NonHumanResource r = orgDataSet.getNonHumanResource(id);
                    if ((r == null) && (! orgDataSet.isKnownNonHumanResourceName(
                            nhr.getChildText("name")))) {
                        r = new NonHumanResource(nhr);
                        String catID = nhr.getChild("category").getAttributeValue("id");
                        NonHumanCategory category = orgDataSet.getNonHumanCategory(catID);
                        if (category != null) {
                            r.setCategory(category);
                            String subcat = nhr.getChildText("subcategory");
                            if (! StringUtil.isNullOrEmpty(subcat)) {
                                r.setSubCategory(subcat);
                            }
                        }    
                        orgDataSet.importNonHumanResource(r);
                        added++;
                    }
                }
                result = String.format("NonHumanResources: %d/%d imported.", added, children.size());
            }
            else {
                result = "NonHumanResources: could not import, external dataset is read-only.";
            }
        }
        return result;
    }


    private String importRoles(Element roleElem) {
        String result = "Roles: 0 in imported file.";
        if (roleElem != null) {
            if (orgDataSet.isDataEditable("Role")) {
                Hashtable<String, Role> cyclics = new Hashtable<String, Role>();
                int added = 0;
                List<Element> children = roleElem.getChildren();
                for (Element role : children) {
                    String id = role.getAttributeValue("id");
                    Role r = orgDataSet.getRole(id);
                    if ((r == null) && (! orgDataSet.isKnownRoleName(role.getChildText("name")))) {
                        r = new Role();

                        // ensure all roles created before cyclic refs are added
                        Element belongsTo = role.getChild("belongsToID");
                        if (belongsTo != null) {
                            cyclics.put(belongsTo.getText(), r);
                        }
                        r.reconstitute(role);
                        orgDataSet.importRole(r);
                        added++;
                    }
                }
                for (String id : cyclics.keySet()) {
                    Role r = cyclics.get(id);
                    r.setOwnerRole(orgDataSet.getRole(id));
                    orgDataSet.updateRole(r);
                }
                result = String.format("Roles: %d/%d imported.", added, children.size());
            }
            else {
                result = "Roles: could not import, external dataset is read-only.";
            }
        }    
        return result;
    }


    private String importOrgGroups(Element ogElem) {
        String result = "OrgGroup: 0 in imported file.";
        if (ogElem != null) {
            if (orgDataSet.isDataEditable("OrgGroup")) {
                Hashtable<String, OrgGroup> cyclics = new Hashtable<String, OrgGroup>();
                int added = 0;
                List<Element> children = ogElem.getChildren();
                for (Element group : children) {
                    String id = group.getAttributeValue("id");
                    OrgGroup og = orgDataSet.getOrgGroup(id);
                    if ((og == null) && (! orgDataSet.isKnownOrgGroupName(group.getChildText("groupName")))) {
                        og = new OrgGroup();

                        // ensure all OrgGroups created before cyclic refs are added
                        Element belongsTo = group.getChild("belongsToID");
                        if (belongsTo != null) {
                            cyclics.put(belongsTo.getText(), og);
                        }
                        og.reconstitute(group);
                        orgDataSet.importOrgGroup(og);
                        added++;
                    }
                }
                for (String id : cyclics.keySet()) {
                    OrgGroup og = cyclics.get(id);
                    og.setBelongsTo(orgDataSet.getOrgGroup(id));
                    orgDataSet.updateOrgGroup(og);
                }
                result = String.format("OrgGroups: %d/%d imported.", added, children.size());
            }
            else {
                result = "OrgGroups: could not import, external dataset is read-only.";
            }
        }
        return result;
    }


    private String importPositions(Element posElem) {
        String result = "Positions: 0 in imported file.";
        if (posElem != null) {
            if (orgDataSet.isDataEditable("OrgGroup")) {
                Hashtable<String, Position> cyclics = new Hashtable<String, Position>();
                int added = 0;
                List<Element> children = posElem.getChildren();
                for (Element pos : children) {
                    String id = pos.getAttributeValue("id");
                    Position p = orgDataSet.getPosition(id);
                    if ((p == null) && (! orgDataSet.isKnownPositionName(pos.getChildText("title")))) {
                        p = new Position();

                        // ensure all Positions created before cyclic refs are added
                        Element reportsTo = pos.getChild("reportstoid");
                        if (reportsTo != null) {
                            cyclics.put(reportsTo.getText(), p);
                        }
                        p.reconstitute(pos);

                        Element ogElem = pos.getChild("orggroupid");
                        if (ogElem != null) {
                            p.setOrgGroup(orgDataSet.getOrgGroup(ogElem.getText()));
                        }
                        orgDataSet.importPosition(p);
                        added++;
                    }
                }
                for (String id : cyclics.keySet()) {
                    Position p = cyclics.get(id);
                    p.setReportsTo(orgDataSet.getPosition(id));
                    orgDataSet.updatePosition(p);
                }
                result = String.format("Positions: %d/%d imported.", added, children.size());
            }
            else {
                result = "Positions: could not import, external dataset is read-only.";
            }
        }
        return result;
    }


    private String importParticipants(Element pElem) {
        String result = "Participants: 0 in imported file.";
        int added = 0;
        if (pElem != null) {
            if (orgDataSet.isDataEditable("Participant")) {
                List<Element> children = pElem.getChildren();
                for (Element part : children) {
                    String id = part.getAttributeValue("id");
                    Participant p = orgDataSet.getParticipant(id);
                    if ((p == null) && (! _rm.isKnownUserID(part.getChildText("userid")))) {
                        p = new Participant();
                        p.reconstitute(part);
                        p.setPassword(part.getChildText("password"));
                        p.setDescription(part.getChildText("description"));
                        p.setNotes(part.getChildText("notes"));
                        addParticipantToResourceGroup(p, part, "roles");
                        addParticipantToResourceGroup(p, part, "positions");
                        addParticipantToResourceGroup(p, part, "capabilities");
                        p.getUserPrivileges().setPrivilegesFromBits(part.getChildText("privileges"));
                        _rm.importParticipant(p);
                        added++;
                    }
                }
                result = String.format("Participants: %d/%d imported.", added, children.size());
            }
            else {
                result = "Positions: could not import, external dataset is read-only.";
            }
        }
        return result;
    }


    private void addParticipantToResourceGroup(Participant p, Element e, String resGroup) {
        Element groupElem = e.getChild(resGroup);
        if (groupElem != null) {
            for (Element eID : groupElem.getChildren()) {
                String id = eID.getText();
                try {
                    if (resGroup.equals("roles"))
                        p.addRole(id);
                    else if (resGroup.equals("positions"))
                        p.addPosition(id);
                    else if (resGroup.equals("capabilities"))
                        p.addCapability(id);
                }
                catch (ResourceGatewayException rge) {
                    _log.warn("Unable to add {} for participant '{}': {}",
                            resGroup, p.getFullName(), rge.getMessage());
                }
            }
        }
    }

}
