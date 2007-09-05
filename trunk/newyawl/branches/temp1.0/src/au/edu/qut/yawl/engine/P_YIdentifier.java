/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import java.util.List;
import java.util.Vector;

/**
 * 
 * This class has control over data structures that allow for
 * storing an identifer and manging a set of children.
 * @author Tore Fjellheim
 * 
 * @deprecated This class looks odd.  Why does it exist?  Can we refactor the YIdentifier class
 * and use it instead? Signed Lach
 */
public class P_YIdentifier {

    private String _idString;
    private List _children = new Vector();

    private List locationNames = new Vector();


    public List getLocationNames() {
        return locationNames;
    }

    public void setLocationNames(List names) {
        this.locationNames = names;
    }

    public String get_idString() {
        return _idString;
    }

    public void set_idString(String id) {
        this._idString = id;
    }

    /**
     * Constructor Identifier.
     * @param idString
     */
    private P_YIdentifier(String idString) {
        _idString = idString;
    }

    public P_YIdentifier() {
    }


    public List get_children() {
        return _children;
    }


    public void set_children(List children) {
        this._children = children;
    }

    public void clearChildren() {
        _children.clear();
    }

    public String toString() {
        return _idString;
    }

}
