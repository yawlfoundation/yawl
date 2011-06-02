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

package org.yawlfoundation.yawl.schema;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Michael Adams
 * Creation Date: 15/04/2009
 */
public class YDataSchemaCache extends ConcurrentHashMap<String, YDataSchemaCache.SchemaMap> {

    public YDataSchemaCache() {
        super();
    }

    public void add(YSpecificationID specID) {
        String schema = YEngine.getInstance().getSpecificationDataSchema(specID);
        this.put(getKey(specID), assembleMap(schema)) ;        
    }


    public boolean contains(YSpecificationID specID) {
        return this.containsKey(getKey(specID));
    }


    public SchemaMap remove(YSpecificationID specID) {
        return this.remove(getKey(specID));
    }


    public Element getSchemaType(YSpecificationID specID, String typeName) {
        Element result = null;
        SchemaMap map = this.get(getKey(specID));
        if (map != null) {
            result = map.get(typeName);
        }
        return result;
    }


    public Map<String, Element> getSchemaMap(YSpecificationID specID) {
        return this.get(getKey(specID));
    }

    
    public String getSchemaTypeAsString(YSpecificationID specID, String typeName) {
        return JDOMUtil.elementToString(getSchemaType(specID, typeName));
    }


    protected SchemaMap assembleMap(String schema) {
        SchemaMap map = new SchemaMap();
        if (schema != null) {
            Element dataSchema = JDOMUtil.stringToElement(schema);
            List list = dataSchema.getChildren();
            for (Object o : list) {
                Element child = (Element) o;
                String name = child.getAttributeValue("name");
                if (name != null) {
                    map.put(name, child);
                }    
            }
        }
        return map;
    }


    protected String getKey(YSpecificationID specID) {
        return specID.getKey() + specID.getVersionAsString();
    }

    
    class SchemaMap extends Hashtable<String, Element> {
        public SchemaMap() {
            super();
        }
    }
}
