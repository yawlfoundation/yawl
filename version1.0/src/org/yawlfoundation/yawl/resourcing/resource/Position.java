/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;

/**
 * Represents an organisational position that may be held by a participant.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@yawlfoundation.org
 *  v0.1, 09/08/2007
 */

public class Position extends AbstractResourceAttribute {

    private String _positionID ;
    private String _title;
    private OrgGroup _orgGroup;
    private Position _reportsTo;

    // needed for non-hibernate db backends
    private String _orgGroupID ;
    private String _reportsToID ;


    public Position() { super(); }


    public Position(String title) {
        super();
        _title = title;
    }


    public Position(String positionID, String title, String desription,
                    OrgGroup orgGroup, Position reportsTo) {
        this(title) ;
        _positionID = positionID;
        _description = desription;
        _orgGroup = orgGroup ;
        _reportsTo = reportsTo;
    }


    public String getPositionID() {
        return _positionID;
    }

    public void setPositionID(String positionID) {
        _positionID = positionID;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }


    public Position getReportsTo() {
        return _reportsTo;
    }

    public void setReportsTo(Position reportsTo) {
        _reportsTo = reportsTo;
    }


    public OrgGroup getOrgGroup() {
        return _orgGroup;
    }

    public void setOrgGroup(OrgGroup orgGroup) {
        _orgGroup = orgGroup;
    }

    public boolean reportsTo(Position boss) {
        return (_reportsTo != null) && (_reportsTo == boss);
    }

    public boolean ultimatelyReportsTo(Position manager) {
        return reportsTo(manager) ||
               ((_reportsTo != null) && _reportsTo.ultimatelyReportsTo(manager)) ;
    }


    public String getSummaryXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<position id=\"%s\">", _id)) ;
        xml.append(wrap(_positionID, "positionID"));
        xml.append(wrap(_description, "description"));
        xml.append("</position>");
        return xml.toString() ;
    }

    public void reconstitute(Element e) {
        super.reconstitute(e);
        setPositionID(e.getChildText("positionID"));
    }

    // Other-than-hibernate mappings

    public String get_orgGroupID() { return _orgGroupID; }

    public void set_orgGroupID(String orgGroupID) { _orgGroupID = orgGroupID; }

    public String get_reportsToID() { return _reportsToID; }

    public void set_reportsToID(String reportsToID) { _reportsToID = reportsToID; }
}
