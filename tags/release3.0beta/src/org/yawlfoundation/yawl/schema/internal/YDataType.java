package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * @author Michael Adams
 * @date 2/08/12
 */
interface YDataType {

    public static final Namespace YAWL_NAMESPACE = Namespace.getNamespace("yawl",
            "http://www.yawlfoundation.org/yawlschema");

    public String getSchemaString();

    public Element getSchema(String name);

}
