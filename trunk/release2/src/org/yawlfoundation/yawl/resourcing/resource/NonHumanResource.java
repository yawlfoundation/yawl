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

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;

/**
 * @author Michael Adams
 * @date 24/08/2010
 */
public class NonHumanResource extends AbstractResource implements Comparable {

    private String _name;
    private String _category;
    private String _subCategory;

    public NonHumanResource() { super(); }

    public NonHumanResource(String name, String category, String subCategory) {
        super();
        _name = name;
        _category = category;
        _subCategory = subCategory;
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

    public String getCategory() {
        return _category;
    }

    public void setCategory(String category) {
        _category = category;
    }

    public String getSubCategory() {
        return _subCategory;
    }

    public void setSubCategory(String subCategory) {
        _subCategory = subCategory;
    }


    public boolean hasCategory(String category, String subCategory) {
        return _category.equals(category) &&
               ((subCategory == null) || _subCategory.equals(subCategory));
    }


    public int compareTo(Object o) {
        if ((o == null) || (! (o instanceof NonHumanResource))) return 1;
        return this.getName().compareTo(((NonHumanResource) o).getName());
    }


    public String toXML() {
        XNode node = new XNode("nonhumanresource");
        node.addAttribute("id", _resourceID);
        node.addChild("name", _name, true);
        node.addChild("description", _description, true);
        node.addChild("notes", _notes, true);
        node.addChild("category", _category, true);
        node.addChild("subcategory", _subCategory, true);
        return node.toString() ;
    }

    
    public void fromXML(Element e) {
        setID(e.getAttributeValue("id"));
        setName(JDOMUtil.decodeEscapes(e.getChildText("name")));
        setDescription(JDOMUtil.decodeEscapes(e.getChildText("description")));
        setNotes(JDOMUtil.decodeEscapes(e.getChildText("notes")));
        setCategory(JDOMUtil.decodeEscapes(e.getChildText("category")));
        setSubCategory(JDOMUtil.decodeEscapes(e.getChildText("subcategory")));
    }

}
