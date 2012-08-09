package org.yawlfoundation.yawl.editor.ui.properties;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.propertysheet.DefaultProperty;

/**
 * Loads the properties for a CellProperties backing Bean
 *
 * @author Michael Adams
 * @date 4/07/12
 */
public abstract class YBeanInfo extends BaseBeanInfo {

    public YBeanInfo(Class clazz) {
        super(clazz);
    }


    protected ExtendedPropertyDescriptor addProperty(String id, String category,
                                                   String displayName, String text) {
        ExtendedPropertyDescriptor property = addProperty(id);
        property.setCategory(category != null ? category : "General");
        if (displayName != null) property.setDisplayName(displayName);
        if (text != null) property.setShortDescription(text);
        return property;
    }


    protected DefaultProperty createProperty(String name, String category, Class type) {
        DefaultProperty property = new DefaultProperty();
        property.setName(name);
        if (category != null) property.setCategory(category);
        property.setDisplayName(name);
        property.setType(type);
        return property;
    }

}
