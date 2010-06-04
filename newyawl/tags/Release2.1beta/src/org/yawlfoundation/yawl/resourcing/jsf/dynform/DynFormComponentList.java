/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import javax.faces.component.UIComponent;
import java.util.ArrayList;

/**
 * Maintains a list of components and their combined height
 *
 * Author: Michael Adams
 * Creation Date: 25/02/2008
 */

public class DynFormComponentList extends ArrayList<UIComponent> {

    private int _height;


    public DynFormComponentList() {
        super();
    }
    

    public int getHeight() { return _height; }

    public void setHeight(int height) { _height = height; }

}
