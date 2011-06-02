/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

    public String getID() {
        return (_id != null) ? _id.getValue() : null;
    }

    public void setID(String s) {
        if (_id == null) _id = new StringWithMessage("Id");
        _id.setValue(s);
    }

    public boolean hasIDOnly() {
        return (getID() != null);
    }


    public String getRole() {
        return (_role != null) ? _role.getValue() : null;
    }

    public void setRole(String s) {
        if (_role == null) _role = new StringWithMessage("Role");
        _role.setValue(s);
    }


    public String getCapability() {
        return (_capability != null) ? _capability.getValue() : null;
    }

    public void setCapability(String s) {
        if (_capability == null) _capability = new StringWithMessage("Capability");
        _capability.setValue(s);
    }


    public String getCategory() {
        return (_category != null) ? _category.getValue() : null;
    }

    public void setCategory(String s) {
        if (_category == null) _category = new StringWithMessage("Category");
        _category.setValue(s);
    }


    public String getSubcategory() {
        return (_subcategory != null) ? _subcategory.getValue() : null;
    }

    public void setSubcategory(String s) {
        if (_subcategory == null) _subcategory = new StringWithMessage("SubCategory");
        _subcategory.setValue(s);
    }

    
    public boolean hasErrors() {
        return StringWithMessage.hasError(_id) ||
               StringWithMessage.hasError(_role) ||
               StringWithMessage.hasError(_capability) ||
               StringWithMessage.hasError(_category) ||
               StringWithMessage.hasError(_subcategory);
    }


    public boolean hasError(StringWithMessage strMsg) {
        return (strMsg != null) && strMsg.hasError();
    }


    public boolean equals(Object o) {
        return (o instanceof UtilisationResource) &&
                toXML().equals(((UtilisationResource) o).toXML());
    }


    public int hashCode() {
        return toXML().hashCode();
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
        if (node.hasChild("Id")) setID(node.getChildText("Id"));
        if (node.hasChild("Role")) setRole(node.getChildText("Role"));
        if (node.hasChild("Capability")) setCapability(node.getChildText("Capability"));
        if (node.hasChild("Category")) setCategory(node.getChildText("Category"));
        if (node.hasChild("SubCategory")) setSubcategory(node.getChildText("SubCategory"));
    }

}
