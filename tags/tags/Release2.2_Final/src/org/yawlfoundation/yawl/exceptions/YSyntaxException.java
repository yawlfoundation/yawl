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

package org.yawlfoundation.yawl.exceptions;

import org.yawlfoundation.yawl.elements.YExternalNetElement;

/**
 * 
 * @author Lachlan Aldred
 * 

 */
public class YSyntaxException extends YAWLException {
    public YSyntaxException(String msg) {
        super(msg);
    }


    public YSyntaxException(YExternalNetElement element, String msg) {
        super(element.toString() + " " + msg);
    }
}
