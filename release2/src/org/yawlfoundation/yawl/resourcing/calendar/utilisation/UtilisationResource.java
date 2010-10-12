/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.calendar.utilisation;

import org.yawlfoundation.yawl.util.XNode;

/**
 * @author Michael Adams
* @date 6/10/2010
*/
public class UtilisationResource {

    private StringWithMessage _id;
    private StringWithMessage _role;
    private StringWithMessage _capability;
    private StringWithMessage _category;
    private StringWithMessage _subcategory;

    public UtilisationResource() { }

    public UtilisationResource(String id, String role, String capability,
                               String category, String subcategory) {
        setID(id);
        setRole(role);
        setCapability(capability);
        setCategory(category);
        setSubcategory(subcategory);
    }

    public UtilisationResource(XNode node) {
        fromXNode(node);
    }


    /**************************************************************************/

    public String getID() { return _id.getValue(); }

    public void setID(String s) {
        if (_id == null) _id = new StringWithMessage("Id");
        _id.setValue(s);
    }

    public String getRole() { return _role.getValue(); }

    public void setRole(String s) {
        if (_role == null) _role = new StringWithMessage("Role");
        _role.setValue(s);
    }

    public String getCapability() { return _capability.getValue(); }

    public void setCapability(String s) {
        if (_capability == null) _capability = new StringWithMessage("Capability");
        _capability.setValue(s);
    }

    public String getCategory() { return _category.getValue(); }

    public void setCategory(String s) {
        if (_category == null) _category = new StringWithMessage("Category");
        _category.setValue(s);
    }

    public String getSubcategory() { return _subcategory.getValue(); }

    public void setSubcategory(String s) {
        if (_subcategory == null) _subcategory = new StringWithMessage("Subcategory");
        _subcategory.setValue(s);
    }

    public String toXML() {
        return toXNode().toString();
    }

    public XNode toXNode() {
        XNode node = new XNode("Resource");
        if (_id != null) node.addChild(_id.toXNode());
        if (_role != null) node.addChild(_role.toXNode());
        if (_capability != null) node.addChild(_capability.toXNode());
        if (_category != null) node.addChild(_category.toXNode());
        if (_subcategory != null) node.addChild(_subcategory.toXNode());
        return node;
    }

    public void fromXNode(XNode node) {
        setID(node.getChildText("Id"));
        setRole(node.getChildText("Role"));
        setCapability(node.getChildText("Capability"));
        setCategory(node.getChildText("Category"));
        setSubcategory(node.getChildText("Subcategory"));
    }

}
