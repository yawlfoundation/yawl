/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.procletService.connect;

import java.io.*;


/** Class of static methods to convert objects to ByteStream compatible
 * representations and back again
 * @author Guy Gallasch
 * @version 0.6
 */

public class EncodeDecode
{
/** Method to convert a string to a ByteArrayInputStream
 * @param toConvert The string to convert
 * @return A ByteArrayInputStream representing the string
 */
    public static ByteArrayInputStream encodeString(String toConvert)
    {
        return new ByteArrayInputStream(toConvert.getBytes());
    }
    
/** Method to convert a ByteArrayOutputStream to a string
 * @param toConvert A ByteArrayOutputStream to convert to string
 * @return String decoded from the ByteArrayOutputStream
 */
    public static String decodeString(ByteArrayOutputStream toConvert)
    {
        return toConvert.toString();
    }
    
}


