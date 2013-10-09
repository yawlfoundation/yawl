/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;

import java.util.ArrayList;
import java.util.List;

public class GenericNonHumanCategory implements Comparable<GenericNonHumanCategory> {

    private String _id;
    private String _name;
    protected String _subCatName;

    public GenericNonHumanCategory(String id, String name) {
        setId(id);
        setName(name);
    }

    public GenericNonHumanCategory(String id, String name, String subcat) {
        this(id, name);
        setSubcategory(subcat);
    }

    public void setId(String id) { _id = id; }

    public String getID() { return _id; }


    public void setName(String name) { _name = name; }

    public String getName() { return _name; }


    public void setSubcategory(String subcat) { _subCatName = subcat; }

    public String getSubcategory() { return _subCatName; }


    public String getListLabel() {
        String label = getName();
        String subcat = getSubcategory();
        if (subcat != null) {
            if (subcat.equals("None")) subcat = "No category";
            label += " -> " + subcat;
        }
        return label;
    }


    public String getKey() {
        String key = getID();
        String subcat = getSubcategory();
        if (subcat != null) {
            key += "<>" + subcat;
        }
        return key;
    }


    public boolean equals(Object o) {
        if (o instanceof GenericNonHumanCategory) {
            GenericNonHumanCategory other = (GenericNonHumanCategory) o;
            return getID().equals(other.getID()) &&
                    getListLabel().equals(other.getListLabel());
        }
        return false;
    }


    public int hashCode() {
        return 17 * getListLabel().hashCode();
    }


    public int compareTo(GenericNonHumanCategory other) {
        return other == null ? 1 : getListLabel().compareTo(other.getListLabel());
    }

    public static List<GenericNonHumanCategory> convertCategories(
            List<NonHumanCategory> yCategories) {

        List<GenericNonHumanCategory> categories = new ArrayList<GenericNonHumanCategory>();
        for (NonHumanCategory category : yCategories) {
            String catName = category.getName();
            String catID = category.getID();
            categories.add(new GenericNonHumanCategory(catID, catName));
            for (String subcat : category.getSubCategoryNames()) {
                categories.add(new GenericNonHumanCategory(catID, catName, subcat));
            }
        }
        //               Collections.sort(categories);

        return categories;
    }


}