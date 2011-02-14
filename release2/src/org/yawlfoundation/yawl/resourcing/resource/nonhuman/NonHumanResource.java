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

package org.yawlfoundation.yawl.resourcing.resource.nonhuman;

import org.jdom.Element;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;

/**
 * @author Michael Adams
 * @date 24/08/2010
 */
public class NonHumanResource extends AbstractResource implements Comparable {

    private String _name;
    private NonHumanCategory _category;

    public NonHumanResource() { super(); }

    public NonHumanResource(String name, NonHumanCategory category, String subCategoryName) {
        super();
        _name = name;
        _category = category;
        _category.addResource(this, subCategoryName);
    }

    public NonHumanResource(Element e) {
        super();
        fromXML(e);
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public NonHumanCategory getCategory() {
        return _category;
    }

    public void setCategory(NonHumanCategory category) {
        _category = category;
    }

    public NonHumanSubCategory getSubCategory() {
        return _category.getResourceSubCategory(this);
    }

    public String getSubCategoryName() {
        NonHumanSubCategory subCategory = _category.getResourceSubCategory(this);
        return (subCategory != null) ? subCategory.getName() : null;
    }


    public void setSubCategory(String subCategory) {
        _category.moveToSubCategory(this, subCategory);
    }


    public void detachSubCategory() {
        getSubCategory().removeResource(this);
    }


    public boolean hasCategory(String category, String subCategory) {
        return _category.getName().equals(category) && _category.hasResource(this, subCategory);
    }


    public int compareTo(Object o) {
        if ((o == null) || (! (o instanceof NonHumanResource))) return 1;
        return this.getID().compareTo(((NonHumanResource) o).getID());
    }


    public String toXML() {
        XNode node = new XNode("nonhumanresource");
        node.addAttribute("id", _resourceID);
        node.addChild("name", _name, true);
        node.addChild("description", _description, true);
        node.addChild("notes", _notes, true);
        node.addChild("category", _category.getName(), true);
        String subCategoryName = getSubCategoryName();
        if (subCategoryName != null) node.addChild("subcategory", subCategoryName);
        return node.toString() ;
    }

    
    public void fromXML(Element e) {
        setID(e.getAttributeValue("id"));
        setName(JDOMUtil.decodeEscapes(e.getChildText("name")));
        setDescription(JDOMUtil.decodeEscapes(e.getChildText("description")));
        setNotes(JDOMUtil.decodeEscapes(e.getChildText("notes")));
    }

}
