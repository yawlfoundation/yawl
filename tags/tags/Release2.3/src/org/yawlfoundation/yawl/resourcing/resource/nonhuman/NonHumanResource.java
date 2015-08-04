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

package org.yawlfoundation.yawl.resourcing.resource.nonhuman;

import org.jdom.Element;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;

/**
 * @author Michael Adams
 * @date 24/08/2010
 */
public class NonHumanResource extends AbstractResource implements Comparable, Cloneable {

    private String _name;
    private NonHumanCategory _category;

    public NonHumanResource() { super(); }

    public NonHumanResource(String id) {
        super();
        setID(id);
    }

    public NonHumanResource(String name, NonHumanCategory category, String subCategoryName) {
        super();
        _name = name;
        _category = category;
        if (_category != null) _category.addResource(this, subCategoryName);
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

    public void setCategory(NonHumanCategory newCategory) {
        if (newCategory != null) {
            if (_category != null) {
                _category.removeFromAll(this);
            }
            _category = newCategory;
            _category.addResource(this, null);
        }
    }

    public void clearCategory() {
        if (_category != null) {
            _category.removeFromAll(this);
            _category = null;
        }
    }

    public NonHumanSubCategory getSubCategory() {
        return (_category != null) ? _category.getResourceSubCategory(this) : null;
    }

    public String getSubCategoryName() {
        NonHumanSubCategory subCategory = (_category != null) ?
                _category.getResourceSubCategory(this) : null;
        return (subCategory != null) ? subCategory.getName() : null;
    }


    public void setSubCategory(String subCategory) {
        String currentSubCategory = getSubCategoryName();
        if ((_category != null) && (subCategory != null) &&
                (! subCategory.equals(currentSubCategory))) {
            _category.moveToSubCategory(this, subCategory);
        }    
    }


    public void detachSubCategory() {
        NonHumanSubCategory subCategory = getSubCategory();
        if (subCategory != null) subCategory.removeResource(this);
    }


    public boolean hasCategory(String category, String subCategory) {
        return (_category != null) && _category.getName().equals(category) &&
                _category.hasResource(this, subCategory);
    }


    public int compareTo(Object o) {
        if ((o == null) || (! (o instanceof NonHumanResource))) return 1;
        return this.getName().compareTo(((NonHumanResource) o).getName());
    }

    
    public NonHumanResource clone() throws CloneNotSupportedException {
        NonHumanResource cloned = (NonHumanResource) super.clone();
        cloned.setID("_CLONE_" + getID());
        cloned.setSubCategory(getSubCategoryName());   // adds cloned to subcat resources
        return cloned;
    }

    public void merge(NonHumanResource resource) {
        super.merge(resource);
        setName(resource.getName());
        if ((_category == null) || (! _category.equals(resource.getCategory()))) {
            setCategory(resource.getCategory());
        }
        String subCatName = getSubCategoryName();
        if ((subCatName == null) || (! subCatName.equals(resource.getSubCategoryName()))) {
            setSubCategory(resource.getSubCategoryName());
        }
    }


    public String toXML() {
        XNode node = new XNode("nonhumanresource");
        node.addAttribute("id", _resourceID);
        node.addChild("name", _name, true);
        node.addChild("description", _description, true);
        node.addChild("notes", _notes, true);
        if (_category != null) node.addChild(_category.toXNode());
        NonHumanSubCategory subCategory = getSubCategory();
        if (subCategory != null) node.addChild(subCategory.toXNode());
        return node.toString() ;
    }

    
    public void fromXML(Element e) {
        setID(e.getAttributeValue("id"));
        setName(JDOMUtil.decodeEscapes(e.getChildText("name")));
        setDescription(JDOMUtil.decodeEscapes(e.getChildText("description")));
        setNotes(JDOMUtil.decodeEscapes(e.getChildText("notes")));
    }

}
