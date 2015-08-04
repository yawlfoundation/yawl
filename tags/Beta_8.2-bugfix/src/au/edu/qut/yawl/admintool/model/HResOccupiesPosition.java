/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool.model;

import java.io.Serializable;

/**
 * 
 * @author Lachlan Aldred
 * Date: 28/09/2005
 * Time: 18:57:44
 * 
 */
public class HResOccupiesPosition implements Serializable {
    private String _hResID;
    private String _positionID;

    public HResOccupiesPosition() {
    }

    public String getHResID() {
        return _hResID;
    }

    public void setHResID(String hResID) {
        this._hResID = hResID;
    }

    public String getPositionID() {
        return _positionID;
    }

    public void setPositionID(String positionID) {
        this._positionID = positionID;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HResOccupiesPosition)) return false;

        final HResOccupiesPosition hResOccupiesPosition = (HResOccupiesPosition) o;

        if (_hResID != null ? !_hResID.equals(hResOccupiesPosition._hResID) : hResOccupiesPosition._hResID != null) return false;
        if (_positionID != null ? !_positionID.equals(hResOccupiesPosition._positionID) : hResOccupiesPosition._positionID != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (_hResID != null ? _hResID.hashCode() : 0);
        result = 29 * result + (_positionID != null ? _positionID.hashCode() : 0);
        return result;
    }
}
