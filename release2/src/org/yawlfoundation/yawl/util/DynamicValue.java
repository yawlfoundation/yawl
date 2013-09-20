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

package org.yawlfoundation.yawl.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class represents a value that is evaluated each time #toString is called.
 *
 * @author Mike Fowler (Nov 2, 2005)
 */
public class DynamicValue {

    private String _property;    // a data member name of the object
    private Object _target;      // the object containing the data member

    public DynamicValue(String property, Object target) {
        setProperty(property);
        setTarget(target);
    }


    public String getProperty() { return _property; }

    public void setProperty(String property) {
        if (property != null && property.startsWith("dynamic{")) {
            property = property.substring(8, property.lastIndexOf('}') -1);
        }
        _property = property;
    }

    public Object getTarget() { return _target; }

    public void setTarget(Object target) { _target = target; }


    /**
     * @return a string representation of property's get method invocation against target.
     */
    public String toString() {
        Object result = null;
        Method[] methods = _target.getClass().getMethods();

        //identify the most appropriate "accessor" method
        for (Method method : methods) {
            if (method.getParameterTypes().length == 0) { //no parameter methods only
                String name = method.getName();
                if (name.toLowerCase().equals("get" + _property.toLowerCase()) ||
                        name.toLowerCase().equals("is" + _property.toLowerCase())) {
                    try {
                        result = method.invoke(_target);
                        break;
                    }
                    catch (IllegalAccessException e) {
                        // fall through to empty string
                    }
                    catch (InvocationTargetException e) {
                        // fall through to empty string
                    }
                }
            }
        }
        return result != null ? result.toString() : "";
    }


}
