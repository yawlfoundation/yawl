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

import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.util.TaggedStringList;

import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Michael Adams
 * @date 25/08/2010
 */
public class NonHumanResourceCategories extends Hashtable<Long, TaggedStringList> {

    private Persister _persister;

    public NonHumanResourceCategories() {
        _persister = Persister.getInstance();
    }

    public NonHumanResourceCategories(boolean restore) {
        this();
  //      if (restore) restore();
    }


    public long addCategory(String categoryName) {
        if ((categoryName == null) || getCategoryKey(categoryName) > -1) return -1;
        long key = insert(categoryName, -1);
        put(key, new TaggedStringList(categoryName));
        return key;
    }


    public boolean addSubCategory(String categoryName, String subCategoryName) {
        if ((categoryName == null) || (subCategoryName == null)) return false;
        long key = getCategoryKey(categoryName);
        if (key == -1) {
            key = addCategory(categoryName);
        }
        return addSubCategory(key, subCategoryName);
    }


    public boolean addSubCategory(long key, String subCategoryName) {
        if (subCategoryName == null) return false;
        TaggedStringList subCategoryList = get(key);
        if ((subCategoryList != null) && (! subCategoryList.contains(subCategoryName))) {
            subCategoryList.add(subCategoryName);
            insert(subCategoryName, key);
            return true;
        }
        return false;
    }


    public boolean removeCategory(String category) {
        return (category != null) && removeCategory(getCategoryKey(category));
    }


    public boolean removeCategory(long key) {
        return (key > -1) && (remove(key) != null) && (deleteCategory(key) > 0);
    }


    public boolean removeSubCategory(String category, String subCategory) {
        return removeSubCategory(getCategoryKey(category), subCategory);
    }


    public boolean removeSubCategory(long key, String subCategory) {
        if (subCategory == null) return false;
        TaggedStringList subCategoryList = get(key);
        return (subCategoryList != null) && subCategoryList.remove(subCategory) &&
               (deleteSubCategory(key, subCategory) > 0);
    }


    public long getCategoryKey(String category) {
        if (category != null) {
            for (long key : this.keySet()) {
                if (get(key).getTag().equals(category)) {
                    return key;
                }    
            }
        }
        return -1;
    }


    public Set<String> getCategories() {
        Set<String> categoryNames = new TreeSet<String>();
        for (TaggedStringList list : this.values()) {
            categoryNames.add(list.getTag());
        }
        return categoryNames;
    }


    public Set<String> getSubCategories(String categoryName) {
        long key = getCategoryKey(categoryName);
        return (key > -1) ? new TreeSet<String>(get(key)) : null;
    }


    public boolean isKnownCategory(String categoryName) {
        return getCategoryKey(categoryName) > -1;
    }


    public boolean isKnownSubCategory(String categoryName, String subCategoryName) {
        long key = getCategoryKey(categoryName);
        return (key > -1) && get(key).contains(subCategoryName);
    }


    public String isValidCategoryPair(String categoryName, String subCategoryName) {
        if (categoryName == null) {
            if (subCategoryName == null) {
                return "<success/>";
            }
            else return "<failure>Invalid: null category and non-null subcategory</failure>";           
        }
        else if (isKnownCategory(categoryName)) {
            if ((subCategoryName == null) || isKnownSubCategory(categoryName, subCategoryName)) {
                return "<success/>";
            }
            else return "<failure>Unknown subcategory name '" + subCategoryName +
                       "' for category '" + categoryName + "'</failure>";
        }
        else return "<failure>Unknown category name: " + categoryName + "</failure>";
    }


    /*********************************************************************/

    private long insert(String categoryName, long parentKey) {
        NonHumanCategory category =
                new NonHumanCategory(categoryName);
        _persister.insert(category);
        return -1; // category.getKey();
    }


    private long deleteCategory(long key) {
        String stmt = String.format("delete from NonHumanCategory as nc " +
                                    "where nc._key=%d or nc._parentKey=%d", key, key);
        return _persister.execUpdate(stmt);
    }


    private long deleteSubCategory(long key, String subCategoryName) {
        String stmt = String.format("delete from NonHumanCategory as nc " +
                                    "where nc._category='%s' and nc._parentKey=%d",
                                    subCategoryName, key);
        return _persister.execUpdate(stmt);
    }


//    public void restore() {
//        List rows = _persister.execQuery("from NonHumanCategory");
//        if (rows != null) {
//
//            // two passes categories first
//            for (Object o : rows) {
//                NonHumanCategory row = (NonHumanCategory) o;
//                if (row.getParentKey() == -1) {
//                    put(row.getKey(), new TaggedStringList(row.getName()));
//                }
//            }
//
//            // now subcategories
//            for (Object o : rows) {
//                NonHumanCategory row = (NonHumanCategory) o;
//                if (row.getParentKey() > -1) {
//                    TaggedStringList categoryList = get(row.getParentKey());
//                    if (categoryList != null) {
//                        categoryList.add(row.getName());
//                    }
//                }
//            }
//        }
//        _persister.commit();
//    }

}
