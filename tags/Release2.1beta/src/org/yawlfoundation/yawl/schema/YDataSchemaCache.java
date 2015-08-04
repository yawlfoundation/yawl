package org.yawlfoundation.yawl.schema;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 15/04/2009
 */
public class YDataSchemaCache extends Hashtable<String, YDataSchemaCache.SchemaMap> {

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
