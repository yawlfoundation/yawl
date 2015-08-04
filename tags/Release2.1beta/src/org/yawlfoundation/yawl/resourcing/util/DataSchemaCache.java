package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.schema.YDataSchemaCache;

/**
 * Author: Michael Adams
 * Creation Date: 5/04/2010
 */
public class DataSchemaCache extends YDataSchemaCache {

    public DataSchemaCache() {
        super();
    }

    public void add(YSpecificationID specID, String schema) {
        this.put(getKey(specID), assembleMap(schema)) ;
    }

}
