package org.yawlfoundation.yawl.resourcing.datastore.orgdata;


import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 14/11/2008
 */
public class DataBackupEngine {

    ResourceManager _rm = ResourceManager.getInstance();
    Logger _log = Logger.getLogger(this.getClass());

    public String exportOrgData() {
        StringBuilder result = new StringBuilder("<orgdata>");
        result.append(exportParticipants());
        result.append(exportRoles());
        result.append(exportPositions());
        result.append(exportCapabilities());
        result.append(exportOrgGroups());
        result.append("</orgdata>");
        return result.toString();
    }

    public String importOrgData(String xml) {
        StringBuilder result = new StringBuilder();
        Document doc = JDOMUtil.stringToDocument(xml);
        if (doc != null) {
            Element root = doc.getRootElement();
            if (root != null) {
                result.append(importCapabilities(root.getChild("capabilities"))).append('\n');
                result.append(importRoles(root.getChild("roles"))).append('\n');
                result.append(importOrgGroups(root.getChild("orggroups"))).append('\n');
                result.append(importPositions(root.getChild("positions"))).append('\n');
                result.append(importParticipants(root.getChild("participants"))).append('\n');
            }
        }
        if (result.length() == 0) result.append("Invalid YAWL Org Data Export file.");

        return result.toString();
    }


    private String exportParticipants() {
        HashSet<Participant> pSet = _rm.getParticipants();
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
                xml.append(StringUtil.wrapEscaped(String.valueOf(p.isAvailable()),
                        "isAvailable")) ;

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


    private String exportRoles() { return _rm.getRolesAsXML(); }

    private String exportPositions() { return _rm.getPositionsAsXML(); }

    private String exportCapabilities() { return _rm.getCapabilitiesAsXML(); }

    private String exportOrgGroups() { return _rm.getOrgGroupsAsXML(); }


    private String importCapabilities(Element capElem) {
        String result = "Capabilities: 0 in imported file.";
        int added = 0;
        if (capElem != null) {
            List children = capElem.getChildren();
            for (Object o : children) {
                Element cap = (Element) o;
                String id = cap.getAttributeValue("id");
                Capability c = _rm.getCapability(id);
                if (c == null) {
                    c = new Capability();
                    c.reconstitute(cap);
                    _rm.importCapability(c);
                    added++;
                }
            }
            result = String.format("Capabilities: %d/%d imported.", added, children.size());
        }
        return result;
    }


    private String importRoles(Element roleElem) {
        String result = "Roles: 0 in imported file.";
        Hashtable<String, Role> cyclics = new Hashtable<String, Role>();
        int added = 0;
        if (roleElem != null) {
            List children = roleElem.getChildren();
            for (Object o : children) {
                Element role = (Element) o;
                String id = role.getAttributeValue("id");
                Role r = _rm.getRole(id);
                if (r == null) {
                    r = new Role();

                    // ensure all roles created before cyclic refs are added
                    Element belongsTo = role.getChild("belongsToID");
                    if (belongsTo != null) {
                        cyclics.put(belongsTo.getText(), r);
                    }
                    r.reconstitute(role);
                    _rm.importRole(r);
                    added++;
                }
            }
            for (String id : cyclics.keySet()) {
                Role r = cyclics.get(id);
                r.setOwnerRole(_rm.getRole(id));
                _rm.updateRole(r);
            }
            result = String.format("Roles: %d/%d imported.", added, children.size());
        }
        return result;
    }


    private String importOrgGroups(Element ogElem) {
        String result = "OrgGroup: 0 in imported file.";
        Hashtable<String, OrgGroup> cyclics = new Hashtable<String, OrgGroup>();
        int added = 0;
        if (ogElem != null) {
            List children = ogElem.getChildren();
            for (Object o : children) {
                Element group = (Element) o;
                String id = group.getAttributeValue("id");
                OrgGroup og = _rm.getOrgGroup(id);
                if (og == null) {
                    og = new OrgGroup();

                    // ensure all OrgGroups created before cyclic refs are added
                    Element belongsTo = group.getChild("belongsToID");
                    if (belongsTo != null) {
                        cyclics.put(belongsTo.getText(), og);
                    }
                    og.reconstitute(group);
                    _rm.importOrgGroup(og);
                    added++;
                }
            }
            for (String id : cyclics.keySet()) {
                OrgGroup og = cyclics.get(id);
                og.setBelongsTo(_rm.getOrgGroup(id));
                _rm.updateOrgGroup(og);
            }
            result = String.format("OrgGroups: %d/%d imported.", added, children.size());
        }
        return result;
    }


    private String importPositions(Element posElem) {
        String result = "Positions: 0 in imported file.";
        Hashtable<String, Position> cyclics = new Hashtable<String, Position>();
        int added = 0;
        if (posElem != null) {
            List children = posElem.getChildren();
            for (Object o : children) {
                Element pos = (Element) o;
                String id = pos.getAttributeValue("id");
                Position p = _rm.getPosition(id);
                if (p == null) {
                    p = new Position();

                    // ensure all Positions created before cyclic refs are added
                    Element reportsTo = pos.getChild("reportstoid");
                    if (reportsTo != null) {
                        cyclics.put(reportsTo.getText(), p);
                    }
                    p.reconstitute(pos);

                    Element ogElem = pos.getChild("orggroupid");
                    if (ogElem != null) {
                        p.setOrgGroup(_rm.getOrgGroup(ogElem.getText()));
                    }
                    _rm.importPosition(p);
                    added++;
                }
            }
            for (String id : cyclics.keySet()) {
                Position p = cyclics.get(id);
                p.setReportsTo(_rm.getPosition(id));
                _rm.updatePosition(p);
            }
            result = String.format("Positions: %d/%d imported.", added, children.size());
        }
        return result;
    }


    private String importParticipants(Element pElem) {
        String result = "Participants: 0 in imported file.";
        Hashtable<String, Participant> cyclics = new Hashtable<String, Participant>();
        int added = 0;
        if (pElem != null) {
            List children = pElem.getChildren();
            for (Object o : children) {
                Element part = (Element) o;
                String id = part.getAttributeValue("id");
                Participant p = _rm.getParticipant(id);
                if ((p == null) && (! _rm.isKnownUserID(part.getAttributeValue("userid")))) {
                    p = new Participant();
                    p.reconstitute(part);
                    p.setPassword(part.getChildText("password"));
                    p.setAvailable(part.getChildText("isAvailable").equals("true"));
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
        return result;
    }


    private void addParticipantToResourceGroup(Participant p, Element e, String resGroup) {
        Element groupElem = e.getChild(resGroup);
        if (groupElem != null) {
            List list = groupElem.getChildren();
            for (Object o : list) {
                String id = ((Element) o).getText();
                if (resGroup.equals("roles"))
                    p.addRole(id);
                else if (resGroup.equals("positions"))
                    p.addPosition(id);
                else if (resGroup.equals("capabilities"))
                    p.addCapability(id);
            }
        }
    }

}
