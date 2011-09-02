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

package org.yawlfoundation.yawl.resourcing.resource.nonhuman;

import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.HashSet;
import java.util.Set;


/**
 * A NHR Category is a grouping class for a set of NHR (much like a Role groups a
 * set of participants)
 *
 * @author Michael Adams
 * @date 25/11/2010
 */
public class NonHumanCategory implements Comparable<NonHumanCategory> {

    private String _id;                                                 // hibernate key
    protected String _categoryName;
    protected String _description;
    protected String _notes;

    // filters the resources in this category via a set of subcategory strings
    protected Set<NonHumanSubCategory> _subcategories;

    public NonHumanCategory() {
        _subcategories = new HashSet<NonHumanSubCategory>();
    }

    public NonHumanCategory(String name) {
        this();
        _categoryName = name;
    }


    public int compareTo(NonHumanCategory other) {
        if (other == null) return 1;
        return this.getName().compareTo(other.getName());
    }



    public String getID() { return _id; }

    public void setID(String id) { _id = id; }


    public String getName() { return _categoryName; }

    public void setName(String name) { _categoryName = name; }


    public String getDescription() { return _description; }

    public void setDescription(String description) { _description = description; }


    public String getNotes() { return _notes; }

    public void setNotes(String notes) { _notes = notes; }


    public Set<NonHumanSubCategory> getSubCategories() { return _subcategories; }

    public Set<String> getSubCategoryNames() {
        Set<String> names = new HashSet<String>();
        for (NonHumanSubCategory subCategory : _subcategories) {
            names.add(subCategory.getName());
        }
        return names;
    }

    public int getSubCategoryCount() { return _subcategories.size(); }

    public boolean addSubCategory(String subCategory) {
        return (! hasSubCategory(subCategory)) &&
                _subcategories.add(new NonHumanSubCategory(subCategory));
    }

    public boolean addSubCategory(NonHumanSubCategory subCategory) {
        return (! hasSubCategory(subCategory.getName())) && _subcategories.add(subCategory);
    }

    public boolean hasSubCategory(String name) {
        return getSubCategory(name) != null;
    }

    public boolean removeSubCategory(String name) {
        if (! ((name == null) || name.equals("None"))) {
            NonHumanSubCategory subCategory = getSubCategory(name);
            if (subCategory != null) {
                if (_subcategories.remove(subCategory)) {
                    for (NonHumanResource resource : subCategory.getResources()) {
                        resource.setSubCategory("None");
                    }
                    return true;
                }
            }    
        }
        return false;
    }

    public NonHumanSubCategory getSubCategory(String name) {
        if (name != null) {
            for (NonHumanSubCategory subCategory : _subcategories) {
                if (subCategory.getName().equals(name)) {
                    return subCategory;
                }
            }
        }
        return null;
    }

    public NonHumanSubCategory getResourceSubCategory(NonHumanResource resource) {
        for (NonHumanSubCategory subCategory : _subcategories) {
            if (subCategory.hasResource(resource)) {
                return subCategory;
            }
        }
        return null;
    }

    public Set<NonHumanResource> getSubCategoryResources(String subCategoryName) {
        if (subCategoryName != null) {
            NonHumanSubCategory subCategory = getSubCategory(subCategoryName);
            if (subCategory != null) {
                return subCategory.getResources();     
            }
            else return null;                               // no subcat of that name
        }
        else return getResources();           // no subcat name supplied means get all
    }

    public boolean moveToSubCategory(NonHumanResource resource, String subCategory) {
        if (subCategory != null) {
            removeFromAll(resource);
            addResource(resource, subCategory);
        }
        return (subCategory != null);
    }


    public Set<NonHumanResource> getResources() {
        Set<NonHumanResource> allResources = new HashSet<NonHumanResource>();
        for (NonHumanSubCategory subCategory : _subcategories) {
            allResources.addAll(subCategory.getResources());
        }
        return allResources;
    }

    public void addResource(NonHumanResource resource, String subCategoryName) {
        if (resource != null) {
            if (subCategoryName == null) subCategoryName = "None";
            NonHumanSubCategory subCategory = getSubCategory(subCategoryName);
            if (subCategory == null) {
                subCategory = new NonHumanSubCategory(subCategoryName);
                _subcategories.add(subCategory);                
            }
            subCategory.addResource(resource);
        }
    }

    public boolean removeResource(NonHumanResource resource, String subCategoryName) {
        if (resource != null) {
            if (subCategoryName != null) {
                Set<NonHumanResource> resources = getSubCategoryResources(subCategoryName);
                if (resources != null) return resources.remove(resource);
            }
            else return removeFromAll(resource);
        }
        return false;
    }


    public boolean removeFromAll(NonHumanResource resource) {
        boolean foundAtLeastOne = false;
        if (resource != null) {
            for (NonHumanSubCategory subCategory : _subcategories) {
                foundAtLeastOne = subCategory.removeResource(resource) || foundAtLeastOne;
            }
        }
        return foundAtLeastOne;
    }


    public boolean hasResource(NonHumanResource resource, String subCategoryName) {
        NonHumanSubCategory subCategory = getSubCategory(subCategoryName);
        return (subCategory != null) && subCategory.hasResource(resource);
    }

    public String toString() {
        return String.format("NonHumanCategory: %s (%s)", getName(), getID());
    }
    

    public XNode toXNode() {
        XNode node = new XNode("nonHumanCategory");
        node.addAttribute("id", _id);
        node.addChild("name", _categoryName);
        node.addChild("description", _description);
        node.addChild("notes", _notes);
        XNode subCatNode = node.addChild("subCategories");
        for (NonHumanSubCategory subCategory : _subcategories) {
            subCatNode.addChild(subCategory.toXNode());
        }
        return node;
    }

    public String toXML() {
        return toXNode().toString();
    }

    public void fromXML(String xml) {
        fromXNode(new XNodeParser().parse(xml));
    }

    public void fromXNode(XNode node) {
        setID(node.getAttributeValue("id"));
        setName(node.getChildText("name"));
        setDescription(node.getChildText("description"));
        setNotes(node.getChildText("notes"));
        XNode subNode = node.getChild("subCategories");
        for (XNode subCatNode : subNode.getChildren()) {
            addSubCategory(new NonHumanSubCategory(subCatNode));
        }
    }
}