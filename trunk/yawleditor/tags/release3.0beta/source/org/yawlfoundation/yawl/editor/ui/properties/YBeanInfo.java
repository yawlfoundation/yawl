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
