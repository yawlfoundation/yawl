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

package org.yawlfoundation.yawl.procletService.persistence;

import org.yawlfoundation.yawl.util.HibernateEngine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DBConnection {

    private static HibernateEngine _db;           // communicates with underlying database

    private DBConnection () {	 }


    public static void init() {

         // setup database connection
         Set<Class> persistedClasses = new HashSet<Class>();
         persistedClasses.add(UniqueID.class);
         persistedClasses.add(StoredBlockRel.class);
         persistedClasses.add(StoredDecisions.class);
         persistedClasses.add(StoredItem.class);
         persistedClasses.add(StoredInteractionArc.class);
         persistedClasses.add(StoredOptions.class);
         persistedClasses.add(StoredPerformative.class);
         persistedClasses.add(StoredPortConnection.class);
         persistedClasses.add(StoredProcletBlock.class);
         persistedClasses.add(StoredProcletPort.class);
         _db = HibernateEngine.getInstance(true, persistedClasses);
    }


    public static boolean insert(Object obj) {
        return _db.exec(obj, HibernateEngine.DB_INSERT, true);
    }


    public static boolean update(Object obj) {
        return _db.exec(obj, HibernateEngine.DB_UPDATE, true);
    }


    public static boolean delete(Object obj) {
        return _db.exec(obj, HibernateEngine.DB_DELETE, true);
    }


    public static List getObjectsForClass(String className) {
        return _db.getObjectsForClass(className);
    }


    public static List getObjectsForClassWhere(String className, String whereClause) {
        return _db.getObjectsForClassWhere(className, whereClause);
    }


    public static void deleteAll(String className) {
        _db.execUpdate("delete from " + className);
    }
    
    public static void deleteAll(Item itemType) {
        _db.execUpdate("delete from StoredItem as s where s.itemType=" + itemType.ordinal());
    }
    

    public static List execQuery(String query) {
        return _db.execQuery(query);
    }

    public static int execUpdate(String query) {
        return _db.execUpdate(query);
    }

    
    public static List getStoredItems(Item itemType) {
        return getObjectsForClassWhere("StoredItem", "itemType=" + itemType.ordinal());
    }
    
    public static StoredItem getStoredItem(String classID, String procletID, 
                                           String blockID, Item itemType) {
        List items = getStoredItems(classID, procletID, blockID, itemType);
        return (! items.isEmpty()) ? (StoredItem) items.get(0) : null;
    }

    public static List getStoredItems(String classID, String procletID,
                                           String blockID, Item itemType) {
        String query = String.format("from StoredItem as s where s.classID='%s' and " +
                           "s.procletID='%s' and s.blockID='%s' and s.itemType=%d",
                classID, procletID, blockID, itemType.ordinal());
        return _db.execQuery(query);
    }

	 
    public static StoredItem getSelectedStoredItem(String classID, String procletID,
                                           String blockID, Item itemType) {
        List items = getSelectedStoredItems(classID, procletID, blockID, itemType);
        return (! items.isEmpty()) ? (StoredItem) items.get(0) : null;
    }

    public static List getSelectedStoredItems(String classID, String procletID,
                                           String blockID, Item itemType) {
        String query = String.format("from StoredItem as s where s.classID='%s' and " +
                           "s.procletID='%s' and s.blockID='%s' and s.itemType=%d and " +
                           "s.selected=%b",
                classID, procletID, blockID, itemType.ordinal(), true);
        return _db.execQuery(query);
    }


    public static void setStoredItemSelected(String classID, String procletID,
                                             String blockID, Item itemType) {
        setStoredItemSelected(getStoredItem(classID, procletID, blockID, itemType));
    }


    public static void setStoredItemsSelected(String classID, String procletID,
                                             String blockID, Item itemType) {
        setStoredItemsSelected(getStoredItems(classID, procletID, blockID, itemType));
    }


    public static void setStoredItemSelected(StoredItem item) {
        if (item != null) {
            item.setSelected(true);
            update(item);
        }
    }
    
    public static void setStoredItemsSelected(List items) {
        for (Object o :items) {
            ((StoredItem) o).setSelected(true);
            update(o);
        }
    }
 
}
