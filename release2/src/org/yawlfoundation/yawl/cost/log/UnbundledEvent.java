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

package org.yawlfoundation.yawl.cost.log;

import org.yawlfoundation.yawl.util.XNode;

import java.util.Hashtable;
import java.util.Map;

/**
 * Unbundles the information in a log event node into its composite parts
 */
class UnbundledEvent {
    private XNode xnode;                                  // the orig. event node
    private String timestamp;
    private String name;                                  // task name
    private String transition;                            // lifecycle change
    private String instance;                              // work item's case id
    private String resource;                              // resource id

    // we're only interested in the data in 'complete' events, so to save time
    // we won't unravel the data elements when they are unneeded
    private boolean hasData = false;
    private Map<String, String> dataMap;


    /**
     * Constructs a new UnbundledEvent
     * @param eventNode the node this object will represent
     */
    public UnbundledEvent(XNode eventNode) {
        xnode = eventNode;                               // store orig. node

        // read values from event node elements
        for (XNode child : xnode.getChildren()) {
            String key = child.getAttributeValue("key");
            if (key.equals("time:timestamp")) {
                timestamp = child.getAttributeValue("value");
            }
            else if (key.equals("concept:name")) {
                name = child.getAttributeValue("value");
            }
            else if (key.equals("lifecycle:transition")) {
                transition = child.getAttributeValue("value");
            }
            else if (key.equals("lifecycle:instance")) {
                instance = child.getAttributeValue("value");
            }
            else if (key.equals("org:resource")) {
                resource = child.getAttributeValue("value");
            }
            else hasData = true;                    // no match means a data element
        }
    }


    /**
     * Checks if there is a matching data variable AND value in this event
     * @param key the name of the variable
     * @param value its value
     * @return true iff a data variable of the same name as 'key' exists in this
     * event, and its 'value' matches the value logged for it
     */
    public boolean hasDataMatch(String key, String value) {
        return getDataMap().containsKey(key) && value.equals(getDataMap().get(key));
    }


    /**
     * Checks if this event's transition come before the 'start' event
     * @return true if this event precedes the starting of the work item
     */
    public boolean isPreStart() {
        return transition.equals("schedule") || transition.equals("reassign") ||
                transition.equals("allocate");
    }


    /**
     * Checks if this event's transition is a completion
     * @return true if this event represents the completion of the work item
     */
    public boolean isCompletedTransition() {
        return transition.equals("complete") || transition.endsWith("abort");
    }


    /**
     * Gets the map of data variables and values for this event, if any. (Note:
     * only 'start' and 'complete' events contain data values)
     * @return the data map (which will be empty if there are no data values
     * associated with this event)
     */
    public Map<String, String> getDataMap() {
        if (dataMap == null) {
            dataMap = new Hashtable<String, String>();
            for (XNode child : xnode.getChildren()) {
                if (! child.getAttributeValue("key").contains(":")) {
                    dataMap.put(child.getAttributeValue("key"),
                            child.getAttributeValue("value"));
                }
            }
        }
        return dataMap;
    }


    /**
     * Gets the key used to index this event in higher level maps
     * @return the appropriate key
     */
    public String getKey() {
        String id = instance;

        // if this event is of a child work item, we need the key of its parent
        if (! isPreStart()) {
            int pos = instance.lastIndexOf('.');
            if (pos > -1) id = instance.substring(0, pos);
        }
        return id + ":" + name;
    }
    
    
    public boolean hasTransition(String t) {
        return (transition != null) && transition.equals(t);
    }

    public boolean hasData() { return hasData; }

    public String getName() { return name; }

    public String getResource() { return resource; }
    
    public XNode getNode() { return xnode; }
    
    public String getInstance() { return instance; }

    public String getTimestamp() { return timestamp; }

    public String getTransition() { return transition; }

}
