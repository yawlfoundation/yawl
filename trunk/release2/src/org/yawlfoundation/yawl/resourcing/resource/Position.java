/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Represents an organisational position that may be held by a participant.
 *
 *  @author Michael Adams
 *  v0.1, 09/08/2007
 */

public class Position extends AbstractResourceAttribute implements Comparable {

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
        updateThis();
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
        updateThis();
    }


    public Position getReportsTo() {
        return _reportsTo;
    }

    public void setReportsTo(Position reportsTo) {
        _reportsTo = reportsTo;
        updateThis();
    }


    public OrgGroup getOrgGroup() {
        return _orgGroup;
    }

    public void setOrgGroup(OrgGroup orgGroup) {
        _orgGroup = orgGroup;
        updateThis();
    }

    public boolean reportsTo(Position boss) {
        boolean result = false ;
        if (_reportsTo != null) result = _reportsTo.equals(boss);
        return result;
    }


    public boolean ultimatelyReportsTo(Position manager) {
        boolean result = reportsTo(manager);
        if (! result)
             result = ((_reportsTo != null) && _reportsTo.ultimatelyReportsTo(manager)) ;
        return result ;
    }

    // two Position objects are equal if they have the same id
    public boolean equals(Object o) {
        return (o instanceof Position) && ((Position) o).getID().equals(_id);
    }

    public int compareTo(Object o) {
        if ((o == null) || (! (o instanceof Position))) return 1;
        return this.getTitle().compareTo(((Position) o).getTitle());
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<position id=\"%s\">", _id)) ;
        xml.append(StringUtil.wrap(StringUtil.xmlEncode(_positionID), "positionid"));
        xml.append(StringUtil.wrap(StringUtil.xmlEncode(_description), "description"));
        xml.append(StringUtil.wrap(StringUtil.xmlEncode(_notes), "notes"));
        if (_orgGroup != null)
            xml.append(StringUtil.wrap(_orgGroup.getID(), "orggroupid"));
        if (_reportsTo != null)
            xml.append(StringUtil.wrap(StringUtil.xmlEncode(_reportsTo.getID()),
                                                            "reportstoid"));
        xml.append("</position>");
        return xml.toString() ;
    }

    

    public void reconstitute(Element e) {
        super.reconstitute(e);
        setPositionID(StringUtil.xmlDecode(e.getChildText("positionid")));
        set_reportsToID(StringUtil.xmlDecode(e.getChildText("reportstoid")));
        set_orgGroupID(e.getChildText("orggroupid"));
    }

    // Other-than-hibernate mappings

    public String get_orgGroupID() { return _orgGroupID; }

    public void set_orgGroupID(String orgGroupID) { _orgGroupID = orgGroupID; }

    public String get_reportsToID() { return _reportsToID; }

    public void set_reportsToID(String reportsToID) { _reportsToID = reportsToID; }
}
