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

import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Lachlan Aldred
 * Date: 30/04/2003
 * Time: 10:28:42
 * 
 */
public class YMessagePrinter {
    public static void printMessages(List messages) {
        Iterator iter = messages.iterator();
        while (iter.hasNext()) {
            Object o = null;
            try {
                o = iter.next();
                YVerificationMessage vm = (YVerificationMessage) o;
            } catch (ClassCastException cce) {
                cce.printStackTrace();
            }
        }
    }


    public static String getMessageString(List messages) {
        StringBuilder stringBuffer = new StringBuilder();
        Iterator iter = messages.iterator();
        while (iter.hasNext()) {
            YVerificationMessage message = (YVerificationMessage) iter.next();
            stringBuffer.append("\n" + message.getStatus() + ":" + message.getMessage());
        }
        return stringBuffer.toString();
    }
}
