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

package org.yawlfoundation.yawl.schema;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 13/09/2004
 * Time: 12:40:16
 * 
 */
public class ElementReuseInstruction extends Instruction {
    /**
     * An instruction for creating a schema element.
     * @param elementName the name of the element to create.
     */
    public ElementReuseInstruction(String elementName) {
        if (null == elementName) {
            throw new IllegalArgumentException("you need to specify an element name.");
        }
        _elementName = elementName;
    }
}
