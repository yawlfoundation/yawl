package org.yawlfoundation.yawl.editor.core.util;

import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class DataUtil {

    public static List<String> getBuiltInTypeNames() {
        return XSDType.getInstance().getBuiltInTypeList();
    }


    public static List<String> getInternalTypeNames() {
        List<String> typeList = new ArrayList<String>();
        for (YInternalType internalType : YInternalType.values()) {
            typeList.add(internalType.name());
        }
        return typeList;
    }


    // todo: replace this with a more complete data validation solution
    public static List<String> getUserDefinedTypeNames(String schema) {
        List<String> typeList = new ArrayList<String>();
            if (schema != null) {
                Element dataSchema = JDOMUtil.stringToElement(schema);
                for (Element child : dataSchema.getChildren()) {
                    String name = child.getAttributeValue("name");
                    if (name != null) {
                        typeList.add(name);
                    }
                }
            }
        return typeList;
    }


    public static List<String> getDataTypeNames(String schema) {
        List<String> typeList = getBuiltInTypeNames();
        typeList.addAll(getInternalTypeNames());
        typeList.addAll(getUserDefinedTypeNames(schema));
        Collections.sort(typeList);
        return typeList;
    }



}
